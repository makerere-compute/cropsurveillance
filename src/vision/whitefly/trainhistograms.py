'''
Learn histograms corresponding to leaves and whiteflies.
'''

import cv
import numpy 
import glob

def hsvhistograms(trainfiles):
        # Load example histogram, and split into H, S and V images
        numbins = 32
        h_ranges = [0, 180]
        s_ranges = [0, 255]
        v_ranges = [0, 255]
        
        hist_h = cv.CreateHist([numbins], cv.CV_HIST_ARRAY, [h_ranges], 1)
        hist_s = cv.CreateHist([numbins], cv.CV_HIST_ARRAY, [s_ranges], 1)
        hist_v = cv.CreateHist([numbins], cv.CV_HIST_ARRAY, [v_ranges], 1)
        
        for i in range(numbins):
            hist_h.bins[i] = 0
            hist_s.bins[i] = 0
            hist_v.bins[i] = 0
           
        for trainfile in trainfiles:
            image = cv.LoadImageM(trainfile)
            imexamplehsv = cv.CreateImage(cv.GetSize(image),8,3)
            cv.CvtColor(image,imexamplehsv,cv.CV_BGR2HSV)
            example_h = cv.CreateMat(image.height, image.width, cv.CV_8UC1)
            example_s = cv.CreateMat(image.height, image.width, cv.CV_8UC1)
            example_v = cv.CreateMat(image.height, image.width, cv.CV_8UC1)
            cv.Split(imexamplehsv,example_h,example_s,example_v, None)
            
            cv.CalcHist([cv.GetImage(example_h)], hist_h,True)
            cv.CalcHist([cv.GetImage(example_s)], hist_s,True)
            cv.CalcHist([cv.GetImage(example_v)], hist_v,True)
            
        cv.NormalizeHist(hist_h,1000)  
        cv.NormalizeHist(hist_s,1000) 
        cv.NormalizeHist(hist_v,1000) 
        
        return hist_h, hist_s, hist_v
        
        
def printhistogram(hist,name):
    ''' Print the contents of a histogram as array. Assume 32 bins. '''
    histstr = name + ' = ['
    for i in range(32):
        histstr = histstr + '%.3f' % (max(0,hist.bins[i]))
        if i<31:
            histstr = histstr + ', '
    histstr = histstr + ']'
    print histstr

def trainhistograms(train_again=True):
    train_data_dir = '../../../data/whitefly/train/'
        
    if train_again:
        # Train histograms from H, S and V images
        leaftrainfiles = glob.glob(train_data_dir + 'leaf*.jpg')
        leaf_hist_h, leaf_hist_s, leaf_hist_v = hsvhistograms(leaftrainfiles)  
    
        flytrainfiles = glob.glob(train_data_dir + 'whitefly*.jpg')
        fly_hist_h, fly_hist_s, fly_hist_v = hsvhistograms(flytrainfiles)  

    else:
        print('Using precomputed parameters')
        leaf_hist_h_array = [0.000, 0.000, 0.000, 0.000, 0.003, 0.017, 0.051, 0.044, 0.258, 0.533, 0.075, 0.017, 0.002, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000]
        leaf_hist_s_array = [0.000, 0.000, 0.003, 0.006, 0.008, 0.021, 0.029, 0.040, 0.076, 0.125, 0.165, 0.165, 0.130, 0.079, 0.051, 0.032, 0.019, 0.012, 0.009, 0.006, 0.005, 0.004, 0.003, 0.002, 0.002, 0.001, 0.001, 0.001, 0.001, 0.001, 0.001, 0.000]
        leaf_hist_v_array = [0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.002, 0.004, 0.004, 0.006, 0.008, 0.024, 0.062, 0.062, 0.094, 0.111, 0.113, 0.101, 0.074, 0.072, 0.088, 0.080, 0.048, 0.019, 0.010, 0.007, 0.007, 0.004, 0.002, 0.000]
        fly_hist_h_array = [0.000, 0.000, 0.000, 0.000, 0.012, 0.140, 0.195, 0.087, 0.079, 0.069, 0.029, 0.068, 0.093, 0.071, 0.073, 0.044, 0.036, 0.003, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000]
        fly_hist_s_array = [0.057, 0.211, 0.250, 0.143, 0.126, 0.072, 0.046, 0.019, 0.015, 0.018, 0.010, 0.007, 0.010, 0.003, 0.007, 0.004, 0.001, 0.001, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000]
        fly_hist_v_array = [0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.001, 0.001, 0.001, 0.001, 0.001, 0.000, 0.002, 0.003, 0.001, 0.006, 0.009, 0.018, 0.023, 0.029, 0.040, 0.043, 0.093, 0.125, 0.212, 0.394]        
            
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

    histograms = (leaf_hist_h,leaf_hist_s,leaf_hist_v, fly_hist_h,fly_hist_s,fly_hist_v)
    
    return histograms      

if __name__ == '__main__':
    
    train_data_dir = '../../../data/whitefly/train/'

    # Train histograms from H, S and V images
    leaftrainfiles = glob.glob(train_data_dir + 'leaf*.jpg')
    leaf_hist_h, leaf_hist_s, leaf_hist_v = hsvhistograms(leaftrainfiles)  

    flytrainfiles = glob.glob(train_data_dir + 'whitefly*.jpg')
    fly_hist_h, fly_hist_s, fly_hist_v = hsvhistograms(flytrainfiles)  
                
    # print the histogram values for use by other scripts
    printhistogram(leaf_hist_h,'leaf_hist_h_array')
    printhistogram(leaf_hist_s,'leaf_hist_s_array')
    printhistogram(leaf_hist_v,'leaf_hist_v_array')
    printhistogram(fly_hist_h,'fly_hist_h_array')
    printhistogram(fly_hist_s,'fly_hist_s_array')
    printhistogram(fly_hist_v,'fly_hist_v_array')        
            
      
