package org.aidev.mcrop;

import android.app.Activity;
import android.os.Bundle;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.ArrayList;
import java.nio.FloatBuffer;



import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_features2d.*;
import static com.googlecode.javacv.cpp.opencv_objdetect.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;
import static com.googlecode.javacv.cpp.opencv_highgui.*;


public class VisualDetection extends Activity {
    private FrameLayout layout;
    private FaceView faceView;
    private Preview mPreview;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Hide the window title.
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        // Create our Preview view and set it as the content of our activity.
        try {
            layout = new FrameLayout(this);
            faceView = new FaceView(this);
            mPreview = new Preview(this, faceView);
            layout.addView(mPreview);
            layout.addView(faceView);
            setContentView(layout);
        } catch (Exception e) {
            e.printStackTrace();
            new AlertDialog.Builder(this).setMessage(e.getMessage()).create().show();
        }
    }
    

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate our menu which can gather user input for switching camera
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.list_visual_options_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle all of the possible menu actions.
		switch (item.getItemId()) {
		case R.id.menu_camera_close:
			setResult(1);
			finish();
			break;
		case R.id.menu_camera_snap_diagnosis:
						
			Intent i = new Intent("SnapDetection");			
			startActivity(i);

			break;

		}
		return super.onOptionsItemSelected(item);

	}
}

class FaceView extends View implements Camera.PreviewCallback {
    public static final int SUBSAMPLING_FACTOR = 1;

    private IplImage grayImage;
    private CvMemStorage storage;
    private CvSeq keypoints;
    private CvSeq descriptors;
    private ArrayList matches;
    private int SURF_EXTENDED = 0;
    private double SURF_HESSIAN_THRESHOLD = 500;
    private int SURF_NOCTAVES = 1;
    private int SURF_NOCTAVELAYERS = 3; 
    private float MATCH_THRESHOLD = 20; 
    
    //magic numbers from surftraining.py
    private double mu[] = {5.819627e-04, -1.362842e-03, 2.391441e-03, 2.645388e-03,
          1.736164e-02, -1.230714e-02, 2.497016e-02, 1.932175e-02,
          1.357752e-02, -5.328481e-03, 1.746906e-02, 1.109308e-02,
          4.588704e-04, 1.048637e-04, 9.707647e-04, 8.380755e-04,
          1.770340e-03, -4.429551e-03, 1.501682e-02, 1.513996e-02,
          -4.374340e-04, -1.287441e-01, 2.782514e-01, 1.948743e-01,
          2.738553e-01, -1.216200e-01, 2.884202e-01, 1.637557e-01,
          3.944878e-03, 1.265369e-03, 6.518393e-03, 5.406772e-03,
          3.621008e-03, 3.041889e-03, 1.612071e-02, 1.579410e-02,
          -1.054817e-02, 1.283805e-01, 2.812133e-01, 2.028567e-01,
          2.837351e-01, 1.170555e-01, 2.924680e-01, 1.694813e-01,
          3.927908e-03, -9.551742e-04, 6.768408e-03, 6.973732e-03,
          6.549640e-04, 2.033027e-03, 2.435575e-03, 2.853759e-03,
          1.458403e-02, 1.152258e-02, 2.540248e-02, 2.062079e-02,
          1.357735e-02, 1.539471e-03, 1.763335e-02, 1.248309e-02,
          7.774467e-04, -6.445289e-04, 1.358011e-03, 1.293342e-03};
    
    private double inverse_sigma[] = {6.801185e+04, 6.588168e+04, 9.711188e+04, 8.665568e+04,
          1.158875e+03, 2.371370e+03, 1.567239e+03, 3.626427e+03,
          2.683483e+03, 5.881650e+03, 3.250738e+03, 8.754201e+03,
          5.403449e+05, 8.249664e+05, 6.542625e+05, 8.664666e+05,
          2.302975e+03, 2.368799e+03, 3.773819e+03, 3.477313e+03,
          1.244203e+01, 6.123593e+01, 8.211791e+01, 1.743040e+02,
          7.084477e+01, 8.174691e+01, 1.041477e+02, 1.523873e+02,
          1.958434e+04, 1.445799e+04, 2.428065e+04, 1.636511e+04,
          1.742460e+03, 1.978514e+03, 2.543752e+03, 2.813858e+03,
          1.284125e+01, 5.745215e+01, 8.913852e+01, 1.617787e+02,
          1.000989e+02, 7.500543e+01, 1.201700e+02, 1.758948e+02,
          1.844502e+04, 6.310191e+03, 2.515136e+04, 7.853815e+03,
          7.065411e+04, 8.797910e+04, 9.633089e+04, 1.050358e+05,
          1.138238e+03, 2.344646e+03, 1.575057e+03, 3.568681e+03,
          2.623678e+03, 4.360050e+03, 3.405330e+03, 7.129815e+03,
          1.734295e+05, 1.515146e+05, 1.806751e+05, 1.696722e+05};
    
    public FaceView(VisualDetection context) throws IOException {
        super(context);
        storage = CvMemStorage.create();
        matches = new ArrayList();
        //keypoints = new CvSeq(null);
    }

    public void onPreviewFrame(final byte[] data, final Camera camera) {
        try {
            Camera.Size size = camera.getParameters().getPreviewSize();
            processImage(data, size.width, size.height);
            camera.addCallbackBuffer(data);
        } catch (RuntimeException e) {
            // The camera has probably just been released, ignore.
        }
    }
    
    protected void processImage(byte[] data, int width, int height) {
        // First, downsample our image and convert it into a grayscale IplImage
        int f = SUBSAMPLING_FACTOR;
        if (grayImage == null || grayImage.width() != width/f || grayImage.height() != height/f) {
            grayImage = IplImage.create(width/f, height/f, IPL_DEPTH_8U, 1);
        }
        
        int imageWidth  = grayImage.width();
        int imageHeight = grayImage.height();
        int dataStride = f*width;
        int imageStride = grayImage.widthStep();
        ByteBuffer imageBuffer = grayImage.getByteBuffer();
        for (int y = 0; y < imageHeight; y++) {
            int dataLine = y*dataStride;
            int imageLine = y*imageStride;
            for (int x = 0; x < imageWidth; x++) {
                imageBuffer.put(imageLine + x, data[dataLine + f*x]);
            }
        }
        
        // the image needs to be a big bigger than video standard to catch surf features
        
        IplImage grayImageResized = IplImage.create(640,480, IPL_DEPTH_8U, 1);
        cvResize(grayImage,grayImageResized);
        grayImage = grayImageResized;
       
        
        // Need to load in any function from objdetect for some reason...
        CvHaarFeature feat = new CvHaarFeature();
        
    	CvMemStorage tmpstorage = CvMemStorage.create();
        CvSeq tmpkeypoints = new CvSeq(null);
        CvSeq tmpdescriptors = new CvSeq(null); 
        CvSURFParams tmpparams = cvSURFParams(SURF_HESSIAN_THRESHOLD, SURF_EXTENDED);
        tmpparams.nOctaves(SURF_NOCTAVES);
        tmpparams.nOctaveLayers(SURF_NOCTAVELAYERS);
        
        // This function doesn't like variables with more than local scope, so make
        // some copies to keep it happy
        IplImage tmpgrayImage = grayImage;
        cvExtractSURF(tmpgrayImage,null,tmpkeypoints,tmpdescriptors,tmpstorage,tmpparams,0);  

        keypoints = tmpkeypoints;
        
        // now find which of these descriptors matches the training data
    	matches.clear();
    	int elem_size = tmpdescriptors.elem_size();
    	
        if (tmpdescriptors != null) {
            int total = tmpdescriptors.total();
            FloatBuffer[] objectDescriptors = new FloatBuffer[total];
            
            for (int i = 0; i < total; i++ ) {
                objectDescriptors[i] = cvGetSeqElem(tmpdescriptors, i).asByteBuffer(elem_size).asFloatBuffer();

                // squared Mahalanobis distance, assuming diagonal covariance
                float dist = 0;     
                for(int j=0;j<64;j++) {
                    dist += ((Math.pow((objectDescriptors[i].get(j)-mu[j]),2))*inverse_sigma[j]);
                }

                if (dist<MATCH_THRESHOLD){
                	matches.add(new Integer(i));
                    // add to matches
                }
            }
        }
        
        //cvSaveImage("/sdcard/capture.png",grayImage);
        
        
        //matches.add(new Integer(1));
        
        cvClearMemStorage(storage);
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setTextSize(20);
       
        
        if (matches != null) {
	        String s = "plasmodium: " + matches.size() + " matches.";
	        float textWidth = paint.measureText(s);
	        canvas.drawText(s, (getWidth()-textWidth)/2, 20, paint);
        }
        
        
        if (keypoints != null) {
	        String s = "keypoints: " + keypoints.total();
	        float textWidth = paint.measureText(s);
	        canvas.drawText(s, (getWidth()-textWidth)/2, 40, paint);
        }
        
        if (matches != null && keypoints != null) {
        	// find the scaling factor -- how many pixels wide is the canvas, and grayImage?
            float xscale = getWidth() / ((float) grayImage.width());
            float yscale = getHeight() / ((float) grayImage.height());
            paint.setStrokeWidth(2);
            paint.setStyle(Paint.Style.STROKE);
            float scaleX = (float)getWidth()/grayImage.width();
            float scaleY = (float)getHeight()/grayImage.height();
            //int total = keypoints.total();
            int total = matches.size();
            
            for (int i = 0; i < total; i++) {
            	int j = (Integer) matches.get(i);
                CvSURFPoint r = new CvSURFPoint(cvGetSeqElem(keypoints, j));
                float x = r.pt().x() * xscale;
                float y = r.pt().y() * yscale;
                canvas.drawCircle(x, y, 10, paint);
            }
        }
    }
}

// ----------------------------------------------------------------------
class Preview extends SurfaceView implements SurfaceHolder.Callback {
    SurfaceHolder mHolder;
    Camera mCamera;
    Camera.PreviewCallback previewCallback;

    Preview(Context context, Camera.PreviewCallback previewCallback) {
        super(context);
        this.previewCallback = previewCallback;

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, acquire the camera and tell it where
        // to draw.
        mCamera = Camera.open();
        try {
           mCamera.setPreviewDisplay(holder);
        } catch (IOException exception) {
            mCamera.release();
            mCamera = null;
            // TODO: add more exception handling logic here
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // Surface will be destroyed when we return, so stop the preview.
        // Because the CameraDevice object is not a shared resource, it's very
        // important to release it when the activity is paused.
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }


    private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.05;
        double targetRatio = (double) w / h;
        if (sizes == null) return null;

        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        // Try to find an size match aspect ratio and size
        for (Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // Now that the size is known, set up the camera parameters and begin
        // the preview.
        Camera.Parameters parameters = mCamera.getParameters();

        List<Size> sizes = parameters.getSupportedPreviewSizes();
        Size optimalSize = getOptimalPreviewSize(sizes, w, h);
        parameters.setPreviewSize(optimalSize.width, optimalSize.height);

        mCamera.setParameters(parameters);
        if (previewCallback != null) {
            mCamera.setPreviewCallbackWithBuffer(previewCallback);
            Camera.Size size = parameters.getPreviewSize();
            byte[] data = new byte[size.width*size.height*
                    ImageFormat.getBitsPerPixel(parameters.getPreviewFormat())/8];
            mCamera.addCallbackBuffer(data);
        }
        mCamera.startPreview();
    }
}

