package org.aidev.mcrop.whitefly;

import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;
import static com.googlecode.javacv.cpp.opencv_core.cvCircle;
import static com.googlecode.javacv.cpp.opencv_core.cvClearMemStorage;
import static com.googlecode.javacv.cpp.opencv_core.cvGetSeqElem;
import static com.googlecode.javacv.cpp.opencv_core.cvReleaseImage;
import static com.googlecode.javacv.cpp.opencv_core.cvScalar;

import java.io.File;
import java.nio.FloatBuffer;
import java.util.ArrayList;


import android.graphics.Color;
import android.net.Uri;
import android.widget.ImageView;
import android.widget.TextView;

import com.googlecode.javacv.cpp.opencv_core.CvArr;
import com.googlecode.javacv.cpp.opencv_core.CvContour;
import com.googlecode.javacv.cpp.opencv_core.CvMat;
import com.googlecode.javacv.cpp.opencv_core.CvPoint;
import com.googlecode.javacv.cpp.opencv_core.CvScalar;
import com.googlecode.javacv.cpp.opencv_core.CvMemStorage;
import com.googlecode.javacv.cpp.opencv_core.CvSeq;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_features2d.CvSURFParams;
import com.googlecode.javacv.cpp.opencv_features2d.CvSURFPoint;
import com.googlecode.javacv.cpp.opencv_imgproc.CvHistogram;
import com.googlecode.javacv.cpp.opencv_objdetect.CvHaarFeature;
import com.googlecode.javacv.*;

import static com.googlecode.javacv.cpp.opencv_features2d.cvExtractSURF;
import static com.googlecode.javacv.cpp.opencv_features2d.cvSURFParams;
import static com.googlecode.javacv.cpp.opencv_highgui.cvLoadImage;
import static com.googlecode.javacv.cpp.opencv_highgui.cvSaveImage;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_BGR2GRAY;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_THRESH_BINARY;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvBoundingRect;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvCvtColor;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvResize;

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
	
	private float averagefield(IplImage image,int[] range_x,int[] range_y)
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

	

	 private CvArr backprojectimage(IplImage im,CvHistogram hist_h, CvHistogram hist_s, CvHistogram  hist_v){
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
	
	
  private ArrayList findmatches(IplImage image,CvArr imbackproject_fly,CvArr imbackproject_leaf,CvArr imbackproject_leafarea) {
	// Find the postions of  in an image
	   int imagewidth = image.width();
	    
	    double MAX_PROPORTIONAL__SIZE = 0.03;
	    int FLY_THRESHOLD = 20,
	    LEAF_THRESHOLD = 30;
	    
	    ArrayList matches = null;

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
               
                if(w<imagewidth*MAX_PROPORTIONAL__SIZE){ 
                    
                   // test that the ratio of major and minor axes is within range
                    if((1.0*w)/h<MAX_ASPECT_RATIO && (1.0*height)/width<MAX_ASPECT_RATIO){                
                        
                        int[] range_x = null ;//=xleft,xleft+width);
                        int[] range_y = null ;//= (ytop,ytop+height)
                        
                        // convert to use cv.Avg()
                        
                        float  fly = averagefield((IplImage) imbackproject_fly,range_x,range_y);
                        float leaf = averagefield((IplImage) imbackproject_leaf,range_x,range_y);
                        float leafarea = averagefield((IplImage) imbackproject_leafarea,range_x,range_y);
        
                        // test that the contour contains a fly and the surrounding is leaf
                        if(fly>FLY_THRESHOLD){ 
                        if ((leafarea-leaf)>LEAF_THRESHOLD){
                        	int matchlocation[] = {xleft+width/2,ytop+height/2, width, height};
                            matches.add(matchlocation);
                        	
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
  
public ArrayList detect(IplImage im){
      double leaf_hist_h_array[] = {0.000, 0.000, 0.000, 0.000, 0.003, 0.017, 0.051, 0.044, 0.258, 0.533, 0.075, 0.017, 0.002, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000};
      double leaf_hist_s_array[] = {0.000, 0.000, 0.003, 0.006, 0.008, 0.021, 0.029, 0.040, 0.076, 0.125, 0.165, 0.165, 0.130, 0.079, 0.051, 0.032, 0.019, 0.012, 0.009, 0.006, 0.005, 0.004, 0.003, 0.002, 0.002, 0.001, 0.001, 0.001, 0.001, 0.001, 0.001, 0.000};
      double leaf_hist_v_array[] = {0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.002, 0.004, 0.004, 0.006, 0.008, 0.024, 0.062, 0.062, 0.094, 0.111, 0.113, 0.101, 0.074, 0.072, 0.088, 0.080, 0.048, 0.019, 0.010, 0.007, 0.007, 0.004, 0.002, 0.000};
      double fly_hist_h_array[] = {0.000, 0.000, 0.000, 0.000, 0.012, 0.140, 0.195, 0.087, 0.079, 0.069, 0.029, 0.068, 0.093, 0.071, 0.073, 0.044, 0.036, 0.003, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000};
      double fly_hist_s_array[] = {0.057, 0.211, 0.250, 0.143, 0.126, 0.072, 0.046, 0.019, 0.015, 0.018, 0.010, 0.007, 0.010, 0.003, 0.007, 0.004, 0.001, 0.001, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000};
      double fly_hist_v_array[] = {0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.000, 0.001, 0.001, 0.001, 0.001, 0.001, 0.000, 0.002, 0.003, 0.001, 0.006, 0.009, 0.018, 0.023, 0.029, 0.040, 0.043, 0.093, 0.125, 0.212, 0.394};    
 
      int h_bins[] = {32};
      int s_bins[] = {32};
      int v_bins[] = {32};
      float h_ranges[][] = {{0, 180}};
      float s_ranges[][] = {{0, 255}};
      float v_ranges[][] = {{0, 255}};
      
      CvHistogram leaf_hist_h = cvCreateHist(1, h_bins, CV_HIST_ARRAY, h_ranges, 1);
      CvHistogram leaf_hist_s = cvCreateHist(1, s_bins, CV_HIST_ARRAY, s_ranges, 1);
      CvHistogram leaf_hist_v = cvCreateHist(1, v_bins, CV_HIST_ARRAY, v_ranges, 1);
      CvHistogram fly_hist_h = cvCreateHist(1, h_bins, CV_HIST_ARRAY, h_ranges, 1);
      CvHistogram fly_hist_s = cvCreateHist(1, s_bins, CV_HIST_ARRAY, s_ranges, 1);
      CvHistogram fly_hist_v = cvCreateHist(1, v_bins, CV_HIST_ARRAY, v_ranges, 1) ;
      
      for (int i=0; i<h_bins.length;i++) {
    	  //leaf_hist_h.bin
    	  cvSet1D(leaf_hist_h.bins(), i, cvScalar(leaf_hist_h_array[i],0.0,0.0,0.0));
    	  cvSet1D(leaf_hist_s.bins(), i, cvScalar(leaf_hist_s_array[i],0.0,0.0,0.0));
    	  cvSet1D(leaf_hist_v.bins(), i, cvScalar(leaf_hist_v_array[i],0.0,0.0,0.0));
    	  cvSet1D(fly_hist_h.bins(), i, cvScalar(fly_hist_h_array[i],0.0,0.0,0.0));
    	  cvSet1D(fly_hist_s.bins(), i, cvScalar(fly_hist_s_array[i],0.0,0.0,0.0));
    	  cvSet1D(fly_hist_v.bins(), i, cvScalar(fly_hist_v_array[i],0.0,0.0,0.0));

    	  /*
          leaf_hist_h.bins() = leaf_hist_h_array[i];
          leaf_hist_s.bins()[i] = leaf_hist_s_array[i];
          leaf_hist_v.bins()[i] = leaf_hist_v_array[i];
          fly_hist_h.bins()[i] = fly_hist_h_array[i]; 
          fly_hist_s.bins[i] = fly_hist_s_array[i]; 
          fly_hist_v.bins[i] = fly_hist_v_array[i]; */
      }
                                                
      /*
	  CvHistogram leaf_hist_h = histograms[0];
	  CvHistogram leaf_hist_s = histograms[1];
	  CvHistogram leaf_hist_v = histograms[2];
	  CvHistogram fly_hist_h = histograms[3];
	  CvHistogram fly_hist_s = histograms[4];
	  CvHistogram fly_hist_v = histograms[5];*/
	    
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
	    //TODO cvCanny throws error
	    cvCanny(imgray, imthreshold, 190, 255,255);
	  
	    
	    //# Find the matching points and highlight them
	    ArrayList matchingcoords = findmatches(imthreshold,imbackproject_fly,imbackproject_leaf,imbackproject_leafarea);
	    return matchingcoords;   

  }
  

}
