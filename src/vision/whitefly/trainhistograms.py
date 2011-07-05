'''
Learn histograms corresponding to leaves and whiteflies.
'''

import cv
import numpy 

def hsvhistograms(image, hist_h, hist_s, hist_v):
        # Load example histogram, and split into H, S and V images
        
        imexamplehsv = cv.CreateImage(cv.GetSize(image),8,3)
        cv.CvtColor(image,imexamplehsv,cv.CV_BGR2HSV)
        example_h = cv.CreateMat(image.height, image.width, cv.CV_8UC1)
        example_s = cv.CreateMat(image.height, image.width, cv.CV_8UC1)
        example_v = cv.CreateMat(image.height, image.width, cv.CV_8UC1)
        cv.Split(imexamplehsv,example_h,example_s,example_v, None)
        
        if hist_h==None:
            #if True:
                
            # Learn example histogram
            h_bins = 32
            s_bins = 32
            v_bins = 32
    
            h_ranges = [0, 180]
            s_ranges = [0, 255]
            v_ranges = [0, 255]
            
            hist_h = cv.CreateHist([h_bins], cv.CV_HIST_ARRAY, [h_ranges], 1)
            hist_s = cv.CreateHist([s_bins], cv.CV_HIST_ARRAY, [s_ranges], 1)
            hist_v = cv.CreateHist([v_bins], cv.CV_HIST_ARRAY, [v_ranges], 1)
            
            cv.CalcHist([cv.GetImage(example_h)], hist_h)
            cv.CalcHist([cv.GetImage(example_s)], hist_s)
            cv.CalcHist([cv.GetImage(example_v)], hist_v)   
            
        else:
        
            cv.CalcHist([cv.GetImage(example_h)], hist_h,True)
            cv.CalcHist([cv.GetImage(example_s)], hist_s,True)
            cv.CalcHist([cv.GetImage(example_v)], hist_v,True)
            
        return hist_h, hist_s, hist_v
        
        
def printhistogram(hist,name):
    ''' Print the contents of a histogram as array. Assume 32 bins. '''
    histstr = name + ' = ['
    for i in range(32):
        histstr = histstr + '%.1f' % (hist.bins[i])
        if i<31:
            histstr = histstr + ', '
    histstr = histstr + ']'
    print histstr
        

if __name__ == '__main__':
    
    test_data_dir = '../../../data/whitefly/'

    # Load example histogram, and split into H, S and V images
    imexample = cv.LoadImage(test_data_dir + 'whitefly_example.jpg')
    fly_hist_h = None
    fly_hist_s = None
    fly_hist_v = None       
    fly_hist_h, fly_hist_s, fly_hist_v = hsvhistograms(imexample, fly_hist_h, fly_hist_s, fly_hist_v)        

    leaf_hist_h = None
    leaf_hist_s = None
    leaf_hist_v = None

    # cycle through training images, accumulating
    imexample = cv.LoadImageM(test_data_dir + 'leaf_example.jpg')
    leaf_hist_h, leaf_hist_s, leaf_hist_v = hsvhistograms(imexample, leaf_hist_h, leaf_hist_s, leaf_hist_v)  
    
    # print the histogram values for use by other scripts
    printhistogram(leaf_hist_h,'leaf_hist_h_array')
    printhistogram(leaf_hist_s,'leaf_hist_s_array')
    printhistogram(leaf_hist_v,'leaf_hist_v_array')
    printhistogram(fly_hist_h,'fly_hist_h_array')
    printhistogram(fly_hist_s,'fly_hist_s_array')
    printhistogram(fly_hist_v,'fly_hist_v_array')        
            
      