'''
Count whiteflies in a leaf image.
'''

import cv
import numpy

def averagefield(image,range_x,range_y):
    ''' Average pixel value in a rectangular region of an image '''
    total = 0
    for x in range(range_x[0],range_x[1]):
        for y in range(range_y[0],range_y[1]):
            total += image[y,x]
    numpixels = (range_y[1]-range_y[0])*(range_x[1]-range_x[0])    
    return total/numpixels
    
def findmatches(image,imbackproject_fly,imbackproject_leaf,imbackproject_leafarea):
    ''' Find the postions of whitefly in an image '''
    imagewidth = image.width
    
    MAX_PROPORTIONAL_WHITEFLY_SIZE = 0.03
    FLY_THRESHOLD = 20
    LEAF_THRESHOLD = 30
    
    matches= []

    # Get the contours from the binary image
    threshold_image = cv.CreateImage(cv.GetSize(image),8,1)
    cv.Threshold(image, threshold_image, 220, 255, cv.CV_THRESH_BINARY)
    storage = cv.CreateMemStorage()
    current_contour = cv.FindContours(threshold_image, storage, mode=cv.CV_RETR_LIST); 
    
    while current_contour:            
        
        # test that the contour is not empty
        if len(current_contour) >= 3:
               
            # test that each contour is the right kind of size relative to the image
            (xleft, ytop, width, height) = cv.BoundingRect(current_contour)
            if width<imagewidth*MAX_PROPORTIONAL_WHITEFLY_SIZE:

                # test that the ratio of major and minor axes is within range
                if (1.0*width)/height<3 and (1.0*height)/width<3:
                    range_x = (xleft,xleft+width)
                    range_y = (ytop,ytop+height)
                    fly = averagefield(imbackproject_fly,range_x,range_y)
                    leaf = averagefield(imbackproject_leaf,range_x,range_y)
                    leafarea = averagefield(imbackproject_leafarea,range_x,range_y)
                    
                    # test that the contour contains a fly and the surrounding is leaf
                    if fly>FLY_THRESHOLD and (leafarea-leaf)>LEAF_THRESHOLD:
                        matches.append((xleft+width/2,ytop+height/2, width, height))
                    
        current_contour =current_contour.h_next()
            
    return matches       
        
def backprojectimage(im,hist_h, hist_s, hist_v):
        ''' Histogram back-projection '''
        
        imhsv = cv.CreateImage(cv.GetSize(im),8,3)
        cv.CvtColor(im,imhsv,cv.CV_BGR2HSV)  
        image_h = cv.CreateMat(im.height, im.width, cv.CV_8UC1)
        image_s = cv.CreateMat(im.height, im.width, cv.CV_8UC1)
        image_v = cv.CreateMat(im.height, im.width, cv.CV_8UC1)
        cv.CvtPixToPlane(imhsv,image_h,image_s,image_v, None)      
        
        imbackproject_h = cv.CreateImage((im.width,im.height),8,1)
        imbackproject_s = cv.CreateImage((im.width,im.height),8,1)
        imbackproject_v = cv.CreateImage((im.width,im.height),8,1)

        cv.CalcBackProject([cv.GetImage(image_h)], imbackproject_h, hist_h)
        cv.CalcBackProject([cv.GetImage(image_s)], imbackproject_s, hist_s)
        cv.CalcBackProject([cv.GetImage(image_v)], imbackproject_v, hist_v)
        
        imbackproject = cv.CreateImage((im.width,im.height),8,1)
        cv.AddWeighted(imbackproject_h,.3333,imbackproject_s,.3333,0,imbackproject)
        cv.AddWeighted(imbackproject_v,.3333,imbackproject,1,0,imbackproject) 
        
        return imbackproject
        
def printhistogram(hist,name):
    ''' Print the contents of a histogram as array. Assume 32 bins. '''
    histstr = name + ' = ['
    for i in range(30):
        histstr = histstr + '%.1f' % (hist.bins[i])
        if i<29:
            histstr = histstr + ', '
    histstr = histstr + ']\n'
    print histstr
        

if __name__ == '__main__':
    
    test_data_dir = '../../../data/whitefly/'
    
    leaf_hist_h_array = [0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 2.0, 151.0, 4706.0, 5748.0, 2055.0, 10.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0]
    leaf_hist_s_array = [0.0, 0.0, 0.0, 7.0, 210.0, 152.0, 106.0, 110.0, 230.0, 608.0, 1942.0, 2227.0, 1600.0, 1335.0, 1323.0, 933.0, 493.0, 342.0, 545.0, 329.0, 116.0, 16.0, 1.0, 3.0, 4.0, 13.0, 9.0, 11.0, 7.0, 0.0, 0.0, 0.0]
    leaf_hist_v_array = [0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 14.0, 20.0, 7.0, 6.0, 3.0, 6.0, 17.0, 15.0, 15.0, 188.0, 577.0, 952.0, 2124.0, 2546.0, 3568.0, 1739.0, 282.0, 114.0, 73.0, 120.0, 256.0, 30.0, 0.0, 0.0]
    fly_hist_h_array = [0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 8.0, 14.0, 11.0, 14.0, 31.0, 46.0, 60.0, 43.0, 49.0, 4.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0]
    fly_hist_s_array = [0.0, 38.0, 53.0, 53.0, 90.0, 32.0, 11.0, 2.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0]
    fly_hist_v_array = [0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 1.0, 1.0, 1.0, 9.0, 9.0, 20.0, 19.0, 35.0, 35.0, 66.0, 83.0] 

    try:
        cv.NamedWindow("result", 1)
        im = cv.LoadImageM(test_data_dir + 'test_leaf1.jpg')
        
        # Histograms of fly and leaf
        h_bins = 32
        s_bins = 32
        v_bins = 32
        h_ranges = [0, 180]
        s_ranges = [0, 255]
        v_ranges = [0, 255]
        
        leaf_hist_h = cv.CreateHist([h_bins], cv.CV_HIST_ARRAY, [h_ranges], 1)
        leaf_hist_s = cv.CreateHist([s_bins], cv.CV_HIST_ARRAY, [s_ranges], 1)
        leaf_hist_v = cv.CreateHist([v_bins], cv.CV_HIST_ARRAY, [v_ranges], 1)
        fly_hist_h = cv.CreateHist([h_bins], cv.CV_HIST_ARRAY, [h_ranges], 1)
        fly_hist_s = cv.CreateHist([s_bins], cv.CV_HIST_ARRAY, [s_ranges], 1)
        fly_hist_v = cv.CreateHist([v_bins], cv.CV_HIST_ARRAY, [v_ranges], 1) 
        
        for i in range(h_bins):
            leaf_hist_h.bins[i] = leaf_hist_h_array[i]
            leaf_hist_s.bins[i] = leaf_hist_s_array[i]
            leaf_hist_v.bins[i] = leaf_hist_v_array[i] 
            fly_hist_h.bins[i] = fly_hist_h_array[i] 
            fly_hist_s.bins[i] = fly_hist_s_array[i] 
            fly_hist_v.bins[i] = fly_hist_v_array[i] 
            
        # Load the test image, and split into H, S and V images
        imbackproject_fly = backprojectimage(im,fly_hist_h, fly_hist_s, fly_hist_v)
        imbackproject_leaf = backprojectimage(im,leaf_hist_h, leaf_hist_s, leaf_hist_v)
        
        # Example of convolution with a custom template
        imsmoothed = cv.CreateImage((im.width,im.height),cv.IPL_DEPTH_8U,3)
        filtersize = 2*(im.width/120) + 1
        cv.Smooth(im,imsmoothed,smoothtype = cv.CV_MEDIAN, param1=filtersize)
        imbackproject_leafarea = backprojectimage(imsmoothed,leaf_hist_h, leaf_hist_s, leaf_hist_v)
               
        # Now do the same thing for edge detection image
        imthreshold = cv.CreateImage((im.width,im.height),8,1)
        imgray = cv.CreateImage((im.width,im.height),cv.IPL_DEPTH_8U,1)
        cv.CvtColor(im,imgray,cv.CV_RGB2GRAY)
        cv.Canny(imgray, imthreshold, 128, 150)
        
        # Find the matching points and highlight them
        matchingcoords = findmatches(imthreshold,imbackproject_fly,imbackproject_leaf,imbackproject_leafarea)
        for match in matchingcoords:
            xcentre = match[0]
            ycentre = match[1]
            cv.Circle( im, (xcentre,ycentre), 10, [255,0,0] )
           
        if len(matchingcoords)>0:
            cv.PutText(im, 'whitefly count: %d' % (len(matchingcoords)), (11,21), cv.InitFont(cv.CV_FONT_HERSHEY_SIMPLEX,0.5,0.5), (0,0,0))
            cv.PutText(im, 'whitefly count: %d' % (len(matchingcoords)), (10,20), cv.InitFont(cv.CV_FONT_HERSHEY_SIMPLEX,0.5,0.5), (255,255,255))
        
        cv.ShowImage("result", im)
        
        cv.WaitKey(0)

    finally:
        cv.DestroyWindow("result")
      