'''
Count whiteflies in a leaf image.
'''

import cv
import numpy
import glob
import trainhistograms

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
    MAX_ASPECT_RATIO = 3
    FLY_THRESHOLD = 30
    LEAF_THRESHOLD = 10
    
    matches= []

    # Get the contours from the binary image
    threshold_image = cv.CreateImage(cv.GetSize(image),8,1)
    cv.Threshold(image, threshold_image, 200, 255, cv.CV_THRESH_BINARY)
    storage = cv.CreateMemStorage()
    current_contour = cv.FindContours(threshold_image, storage, mode=cv.CV_RETR_LIST); 
    
    while current_contour: 
           
        

        # test that the contour is not empty
        if len(current_contour) >= 3:
            # test that each contour is the right kind of size relative to the image
            (xleft, ytop, width, height) = cv.BoundingRect(current_contour)
            if width<imagewidth*MAX_PROPORTIONAL_WHITEFLY_SIZE: 
                
                # test that the ratio of major and minor axes is within range
                if (1.0*width)/height<MAX_ASPECT_RATIO and (1.0*height)/width<MAX_ASPECT_RATIO:                
                    
                    range_x = (xleft,xleft+width)
                    range_y = (ytop,ytop+height)
                    fly = averagefield(imbackproject_fly,range_x,range_y)
                    leaf = averagefield(imbackproject_leaf,range_x,range_y)
                    leafarea = averagefield(imbackproject_leafarea,range_x,range_y)
    
                    # test that the contour contains a fly and the surrounding is leaf
                    #if fly>FLY_THRESHOLD: # and 
                    if (leafarea-leaf)>LEAF_THRESHOLD:
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
        cv.AddWeighted(imbackproject_v,.3333,imbackproject,1.0,0,imbackproject) 

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
        
def detect(im,histograms):
    leaf_hist_h = histograms[0]
    leaf_hist_s = histograms[1]
    leaf_hist_v = histograms[2]
    fly_hist_h = histograms[3]
    fly_hist_s = histograms[4]
    fly_hist_v = histograms[5]
    
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
    cv.Canny(imgray, imthreshold, 190, 255)
    
    # Find the matching points and highlight them
    matchingcoords = findmatches(imthreshold,imbackproject_fly,imbackproject_leaf,imbackproject_leafarea)
    return matchingcoords

if __name__ == '__main__':
    histograms = trainhistograms.trainhistograms(True)    

    test_data_dir = '../../../data/whitefly/test/good/'
    testfiles = glob.glob(test_data_dir + '*.jpg')   
    
    cv.NamedWindow("result", 1)      
    for testfile in testfiles:
        print testfile

        im = cv.LoadImageM(testfile)          
        matchingcoords = detect(im,histograms)
        
        for match in matchingcoords:
            xcentre = match[0]
            ycentre = match[1]
            cv.Circle( im, (xcentre,ycentre), 10, [255,0,0] )
           
        if len(matchingcoords)>0:
            cv.PutText(im, 'whitefly count: %d' % (len(matchingcoords)), (11,21), cv.InitFont(cv.CV_FONT_HERSHEY_SIMPLEX,0.5,0.5), (0,0,0))
            cv.PutText(im, 'whitefly count: %d' % (len(matchingcoords)), (10,20), cv.InitFont(cv.CV_FONT_HERSHEY_SIMPLEX,0.5,0.5), (255,255,255))
        
        cv.ShowImage("result", im)
        
        if cv.WaitKey(0) == 27:
            break

    cv.DestroyWindow("result")
      