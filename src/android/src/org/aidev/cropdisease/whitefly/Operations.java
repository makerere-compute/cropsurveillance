package org.aidev.cropdisease.whitefly;

import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.*;

public class Operations {
	public float averagefield(int[][] image,int[] range_x,int[] range_y)
	{
		// Average pixel value in a rectangular region of an image 
	    int total = 0;
	    for(int x =0;x<range_x.length;x++){
	        for (int y=0; y<range_y.length;y++){
	            total += image[y][x];
	        }
	    }
	    int numpixels = (range_y[1]-range_y[0])*(range_x[1]-range_x[0]);    
	    return total/numpixels;
	}/**
  public int[] findmatches(IplImage image) {
	// Find the postions of whitefly in an image
	   int imagewidth = image.width();
	    
	    double MAX_PROPORTIONAL_WHITEFLY_SIZE = 0.03;
	    int FLY_THRESHOLD = 20,
	    LEAF_THRESHOLD = 30;
	    
	    int[]matches;

	   //Get the contours from the binary image
	   
	   IplImage threshold_image = CV cv.CreateImage(cv.GetSize(image),8,1);
	    cv.Threshold(image, threshold_image, 220, 255, cv.CV_THRESH_BINARY);
	    storage = cv.CreateMemStorage();
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
	
}*/
	    
}
