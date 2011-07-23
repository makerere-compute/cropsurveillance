package org.aidev.cropdisease.whitefly;

import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;

import com.googlecode.javacv.cpp.opencv_core.CvArr;
import com.googlecode.javacv.cpp.opencv_core.CvContour;
import com.googlecode.javacv.cpp.opencv_core.CvMat;
import com.googlecode.javacv.cpp.opencv_core.CvMemStorage;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_THRESH_BINARY;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvBoundingRect;
import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.*;
import com.googlecode.javacv.cpp.*;
import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;
import static com.googlecode.javacv.cpp.opencv_calib3d.*;
import static com.googlecode.javacv.cpp.opencv_objdetect.*;
public class Operations {
	private static final double MAX_ASPECT_RATIO = 0;
	private CvMemStorage storage;
	
	public float averagefield(IplImage image,int[] range_x,int[] range_y)
	{
		// Average pixel value in a rectangular region of an image 
	    int total = 0;
	    for(int x =0;x<range_x.length;x++){
	        for (int y=0; y<range_y.length;y++){
	            //total += image [y][x];
	        }
	    }
	    int numpixels = (range_y[1]-range_y[0])*(range_x[1]-range_x[0]);    
	    return total/numpixels;
	}

	

	 public CvArr backprojectimage(IplImage im,CvHistogram hist_h, CvHistogram hist_s, CvHistogram  hist_v){
	     // ''' Histogram back-projection '''
		 IplImage imhsv  = cvCreateImage(cvGetSize(im),8,3);
	      cvCvtColor(im, imhsv, CV_BGR2HSV);
	     
	      
	      CvMat image_h = cvCreateMat(im.height(), im.width(), CV_8UC1);
	      CvMat image_s = cvCreateMat(im.height(), im.width(), CV_8UC1);
	      CvMat image_v = cvCreateMat(im.height(), im.width(), CV_8UC1);
	      
	      //cvCvtPixToPlane(imhsv,image_h,image_s,image_v);    
	      
	      CvArr imbackproject_h =IplImage.create(im.width(),im.height(), 8, 1);
	      CvArr imbackproject_s =IplImage.create(im.width(),im.height(), 8, 1);
	      CvArr imbackproject_v =IplImage.create(im.width(),im.height(), 8, 1);     
	   
	    
	      //cvCalcBackProject(cvGetImage(image_h,im), imbackproject_h, hist_h);
	     // cvCalcBackProject(cvGetImage(image_s,im), imbackproject_s, hist_s);
	      //cvCalcBackProject(cvGetImage(image_v,im), imbackproject_s, hist_s);
	     	      
	     // imbackproject = cv.CreateImage((im.width,im.height),8,1)
	      CvArr imbackproject =IplImage.create(im.width(),im.height(), 8, 1);     
	      cvAddWeighted(imbackproject_h,.3333,imbackproject_s,.3333,0,imbackproject);
	      cvAddWeighted(imbackproject_v,.3333,imbackproject,1.0,0,imbackproject); 

	      return imbackproject;
	}
	
	
  public int[] findmatches(IplImage image,CvArr imbackproject_fly,CvArr imbackproject_leaf,CvArr imbackproject_leafarea) {
	// Find the postions of whitefly in an image
	   int imagewidth = image.width();
	    
	    double MAX_PROPORTIONAL_WHITEFLY_SIZE = 0.03;
	    int FLY_THRESHOLD = 20,
	    LEAF_THRESHOLD = 30;
	    
	    int[]matches = null;

	   //Get the contours from the binary image
	    int width = image.width();
	    int height = image.height();
	    
	  // IplImage threshold_image = IplImage.create(image.width(),image.height(), IPL_DEPTH_8U, 1);
	   IplImage threshold_image = cvCreateImage(cvGetSize(image),8,1);
	   
	    
	    storage = CvMemStorage.create();
	    cvThreshold(threshold_image, threshold_image, 220, 255, CV_THRESH_BINARY);

        // To check if an output argument is null we may call either isNull() or equals(null).
        CvSeq contour = new CvSeq(null);
        cvFindContours(threshold_image, storage, contour, Loader.sizeof(CvContour.class),
                CV_RETR_LIST, CV_CHAIN_APPROX_SIMPLE);
	    
        
        while (contour != null && !contour.isNull()) {
        	int counter =0;
            if (contour.elem_size() >=3) {
                CvSeq points = cvApproxPoly(contour, Loader.sizeof(CvContour.class),
                        storage, CV_POLY_APPROX_DP, cvContourPerimeter(contour)*0.02, 0);
                //cvDrawContours(threshold_image, points, CvScalar.BLUE, CvScalar.BLUE, -1, 1, CV_AA);
                
                CvRect rect = cvBoundingRect(contour, 0);
               int xleft=rect.x();
               int ytop=rect.y();
               int w=rect.width();
               int h=rect.height();           
               
                if(w<imagewidth*MAX_PROPORTIONAL_WHITEFLY_SIZE){ 
                    
                   // test that the ratio of major and minor axes is within range
                    if((1.0*w)/h<MAX_ASPECT_RATIO && (1.0*height)/width<MAX_ASPECT_RATIO){                
                        
                        int[] range_x = null ;//=xleft,xleft+width);
                        int[] range_y = null ;//= (ytop,ytop+height)
                        float  fly = averagefield((IplImage) imbackproject_fly,range_x,range_y);
                        float leaf = averagefield((IplImage) imbackproject_leaf,range_x,range_y);
                        float leafarea = averagefield((IplImage) imbackproject_leafarea,range_x,range_y);
        
                        // test that the contour contains a fly and the surrounding is leaf
                        if(fly>FLY_THRESHOLD){ 
                        if ((leafarea-leaf)>LEAF_THRESHOLD){
                            //matches[counter++]=((xleft+width/2,ytop+height/2, width, height));
                        	
                        } 
                        }
                    }
            //current_contour =current_contour.h_next();
                }
            }
            contour = contour.h_next();
        }
		return matches;
        
        
	  
}
  
  int[] detect(IplImage im,CvHistogram []histograms){
	  CvHistogram leaf_hist_h = histograms[0];
	  CvHistogram leaf_hist_s = histograms[1];
	  CvHistogram leaf_hist_v = histograms[2];
	  CvHistogram fly_hist_h = histograms[3];
	  CvHistogram fly_hist_s = histograms[4];
	  CvHistogram fly_hist_v = histograms[5];
	    
	   // # Load the test image, and split into H, S and V images
	  CvArr imbackproject_fly = backprojectimage(im,fly_hist_h, fly_hist_s, fly_hist_v);
	  CvArr   imbackproject_leaf = backprojectimage(im,leaf_hist_h, leaf_hist_s, leaf_hist_v);
		

	   // # Example of convolution with a custom template
	      
	   
	    
	    IplImage imsmoothed = cvCreateImage(cvGetSize(im),IPL_DEPTH_8U,3);
	    int filtersize = 2*(im.width()/120) + 1;
	    
	    cvSmooth(im,imsmoothed,CV_MEDIAN, filtersize);
	    CvArr imbackproject_leafarea = backprojectimage(imsmoothed,leaf_hist_h, leaf_hist_s, leaf_hist_v);
	         
	   // # Now do the same thing for edge detection image
	    IplImage imthreshold = cvCreateImage(cvGetSize(im),8,1);
	    IplImage imgray = cvCreateImage(cvGetSize(im),IPL_DEPTH_8U,1);
	    cvCvtColor(im,imgray,CV_RGB2GRAY);
	    cvCanny(imgray, imthreshold, 190, 255,255);
	  
	    
	    //# Find the matching points and highlight them
	    int[] matchingcoords = findmatches(imthreshold,imbackproject_fly,imbackproject_leaf,imbackproject_leafarea);
	    return matchingcoords;
	   

  }
}

