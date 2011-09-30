package org.aidev.mcrop;

import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;
import static com.googlecode.javacv.cpp.opencv_features2d.cvExtractSURF;
import static com.googlecode.javacv.cpp.opencv_features2d.cvSURFParams;
import static com.googlecode.javacv.cpp.opencv_highgui.*;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import org.aidev.mcrop.whitefly.Operations;


import com.googlecode.javacv.cpp.opencv_core.CvMemStorage;
import com.googlecode.javacv.cpp.opencv_core.CvSeq;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_features2d.CvSURFParams;
import com.googlecode.javacv.cpp.opencv_features2d.CvSURFPoint;
import com.googlecode.javacv.cpp.opencv_objdetect.CvHaarFeature;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputFilter.LengthFilter;
import android.widget.ImageView;
import android.widget.TextView;

public class PestDetected extends Activity {

	TextView message;
	TextView time;
	ImageView img;
	ImageView orig;

	public static final int SUBSAMPLING_FACTOR = 1;

	private IplImage originalImage;
	private CvMemStorage storage;
	private CvSeq keypoints;
	private CvSeq descriptors;
	private ArrayList matches;
	private int SURF_EXTENDED = 0;
	private double SURF_HESSIAN_THRESHOLD = 500;
	private int SURF_NOCTAVES = 1;
	private int SURF_NOCTAVELAYERS = 3;
	private float MATCH_THRESHOLD = 20;

	// magic numbers from surftraining.py
	private double mu[] = { 5.819627e-04, -1.362842e-03, 2.391441e-03,
			2.645388e-03, 1.736164e-02, -1.230714e-02, 2.497016e-02,
			1.932175e-02, 1.357752e-02, -5.328481e-03, 1.746906e-02,
			1.109308e-02, 4.588704e-04, 1.048637e-04, 9.707647e-04,
			8.380755e-04, 1.770340e-03, -4.429551e-03, 1.501682e-02,
			1.513996e-02, -4.374340e-04, -1.287441e-01, 2.782514e-01,
			1.948743e-01, 2.738553e-01, -1.216200e-01, 2.884202e-01,
			1.637557e-01, 3.944878e-03, 1.265369e-03, 6.518393e-03,
			5.406772e-03, 3.621008e-03, 3.041889e-03, 1.612071e-02,
			1.579410e-02, -1.054817e-02, 1.283805e-01, 2.812133e-01,
			2.028567e-01, 2.837351e-01, 1.170555e-01, 2.924680e-01,
			1.694813e-01, 3.927908e-03, -9.551742e-04, 6.768408e-03,
			6.973732e-03, 6.549640e-04, 2.033027e-03, 2.435575e-03,
			2.853759e-03, 1.458403e-02, 1.152258e-02, 2.540248e-02,
			2.062079e-02, 1.357735e-02, 1.539471e-03, 1.763335e-02,
			1.248309e-02, 7.774467e-04, -6.445289e-04, 1.358011e-03,
			1.293342e-03 };

	private double inverse_sigma[] = { 6.801185e+04, 6.588168e+04,
			9.711188e+04, 8.665568e+04, 1.158875e+03, 2.371370e+03,
			1.567239e+03, 3.626427e+03, 2.683483e+03, 5.881650e+03,
			3.250738e+03, 8.754201e+03, 5.403449e+05, 8.249664e+05,
			6.542625e+05, 8.664666e+05, 2.302975e+03, 2.368799e+03,
			3.773819e+03, 3.477313e+03, 1.244203e+01, 6.123593e+01,
			8.211791e+01, 1.743040e+02, 7.084477e+01, 8.174691e+01,
			1.041477e+02, 1.523873e+02, 1.958434e+04, 1.445799e+04,
			2.428065e+04, 1.636511e+04, 1.742460e+03, 1.978514e+03,
			2.543752e+03, 2.813858e+03, 1.284125e+01, 5.745215e+01,
			8.913852e+01, 1.617787e+02, 1.000989e+02, 7.500543e+01,
			1.201700e+02, 1.758948e+02, 1.844502e+04, 6.310191e+03,
			2.515136e+04, 7.853815e+03, 7.065411e+04, 8.797910e+04,
			9.633089e+04, 1.050358e+05, 1.138238e+03, 2.344646e+03,
			1.575057e+03, 3.568681e+03, 2.623678e+03, 4.360050e+03,
			3.405330e+03, 7.129815e+03, 1.734295e+05, 1.515146e+05,
			1.806751e+05, 1.696722e+05 };
	ProgressDialog dialog;
	Thread t;

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case 0: {
			dialog = new ProgressDialog(this);
			dialog.setMessage("Please wait while Processing...");
			dialog.setIndeterminate(true);
			dialog.setCancelable(true);
			return dialog;
		}
		}
		return null;
	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			String processmsg = (String) msg.obj;
			if (processmsg.equals("SUCCESS")) {
				removeDialog(0);

			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pestdetected);

		
				processImage();


	}

	protected void processImage() {
		try {
			
			long start = System.currentTimeMillis();

			// do machine vision
			storage = CvMemStorage.create();
			matches = new ArrayList();
			
			// Load the original image.
			IplImage originalImage = cvLoadImage(new File(
					"/sdcard/mcrop/rawimage.jpg").getAbsolutePath());
			//new File(new File("/sdcard/mcrop/rawimage.jpg").getAbsolutePath())
			//		.delete();

			// create a new image of the same size as the original one.
			//originalImage = IplImage.create(originalImage.width(),
			//		originalImage.height(), IPL_DEPTH_8U, 3);
			
			
			// We convert the original image to grayscale.
			//cvCvtColor(originalImage, originalImage, CV_BGR2GRAY);
			// process
		
			// the image needs to be a big bigger than video standard to catch
			// surf features

			IplImage ImageResized = IplImage.create(640, 480, IPL_DEPTH_8U,
					3);
			cvResize(originalImage, ImageResized);
			originalImage = ImageResized;
			
			

		/*	// Need to load in any function from objdetect for some reason...
			CvHaarFeature feat = new CvHaarFeature();

			CvMemStorage tmpstorage = CvMemStorage.create();
			CvSeq tmpkeypoints = new CvSeq(null);
			CvSeq tmpdescriptors = new CvSeq(null);
			CvSURFParams tmpparams = cvSURFParams(SURF_HESSIAN_THRESHOLD,
					SURF_EXTENDED);
			tmpparams.nOctaves(SURF_NOCTAVES);
			tmpparams.nOctaveLayers(SURF_NOCTAVELAYERS);

			// This function doesn't like variables with more than local scope,
			// so make
			// some copies to keep it happy
			IplImage tmpgrayImage = grayImage;
			cvExtractSURF(tmpgrayImage, null, tmpkeypoints, tmpdescriptors,
					tmpstorage, tmpparams, 0);

			keypoints = tmpkeypoints;

			// now find which of these descriptors matches the training data
			matches.clear();
			int elem_size = tmpdescriptors.elem_size();

			if (tmpdescriptors != null) {
				int total = tmpdescriptors.total();
				FloatBuffer[] objectDescriptors = new FloatBuffer[total];

				for (int i = 0; i < total; i++) {
					objectDescriptors[i] = cvGetSeqElem(tmpdescriptors, i)
							.asByteBuffer(elem_size).asFloatBuffer();

					// squared Mahalanobis distance, assuming diagonal
					// covariance
					float dist = 0;
					for (int j = 0; j < 64; j++) {
						dist += ((Math.pow(
								(objectDescriptors[i].get(j) - mu[j]), 2)) * inverse_sigma[j]);
					}

					if (dist < MATCH_THRESHOLD) {
						matches.add(new Integer(i));
						// add to matches
					}
				}
			}
			
			  if (matches != null && keypoints != null) {
		         //int total = keypoints.total();
		            int total = matches.size();
		            
		            for (int i = 0; i < total; i++) {
		            	int j = (Integer) matches.get(i);
		                CvSURFPoint r = new CvSURFPoint(cvGetSeqElem(keypoints, j));
		                CvPoint center =  new CvPoint();
		                center.set((int) r.pt().x(), (int) r.pt().y());
		               
		                
		                cvCircle(grayImage, center, 15,cvScalar(255,0,0,255), 2, 8, 0 );
		                
		            }
		        }
*/
		new Operations().detect(ImageResized);
		
		 
			
			// matches.add(new Integer(1));

			cvClearMemStorage(storage);

			long end = System.currentTimeMillis();
			long elapse = end - start;
			// save

			//cvReleaseImage(originalImage);
			// read and display
			img = (ImageView) findViewById(R.id.pest);
			orig= (ImageView) findViewById(R.id.original);
			message = (TextView) findViewById(R.id.message);
			time = (TextView) findViewById(R.id.time);

			time.setText("It took " + elapse/1000 + "secs");
			if (matches.size() > 0) {
				message.setText("White Fly Detected: " + matches.size()
						+ " out of" + keypoints.total());
				message.setBackgroundColor(Color.RED);
			} else {
				message.setText("No White Fly Detected");
				message.setBackgroundColor(Color.GREEN);
			}

			img.setImageURI(Uri.parse(new File("/sdcard/mcrop/pest.jpg")
					.getAbsolutePath()));
			
			orig.setImageURI(Uri.parse(new File("/sdcard/mcrop/rawimage.jpg")
			.getAbsolutePath()));

			//new File(new File("/sdcard/mcrop/pest.jpg").getAbsolutePath())
				//	.delete();
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
