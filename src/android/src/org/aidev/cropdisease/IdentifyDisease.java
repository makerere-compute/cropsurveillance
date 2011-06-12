package org.aidev.cropdisease;

import android.app.Activity;
import android.util.Log;
import android.os.Bundle;
import android.widget.TextView;
import android.app.Activity;
import android.os.Bundle;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.hardware.Camera;
import android.hardware.Camera.Size;
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

public class IdentifyDisease extends Activity {
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
}	

 


class FaceView extends View implements Camera.PreviewCallback {
    public static final int SUBSAMPLING_FACTOR = 1;

    private IplImage grayImage;
    private CvSeq keypoints;
    private CvSeq descriptors;
    private String diagnosis;
    private int SURF_EXTENDED = 0;
    private double SURF_HESSIAN_THRESHOLD = 500;
    private int SURF_NOCTAVES = 1;
    private int SURF_NOCTAVELAYERS = 3; 

    
    //magic numbers from surftraining.py
    private double mu_healthy[] = {-3.118052e-04, -4.726703e-04, 5.366992e-03, 5.392777e-03,
	  3.760885e-03, -4.631664e-03, 4.393208e-02, 3.601589e-02,
	  1.108613e-02, -7.479655e-04, 4.313403e-02, 3.241004e-02,
	  -6.244350e-04, -3.842427e-05, 5.234052e-03, 4.273326e-03,
	  -6.941322e-03, -4.789381e-04, 3.948714e-02, 3.657523e-02,
	  4.311266e-02, -2.768694e-02, 3.483021e-01, 2.418292e-01,
	  1.052872e-01, 4.564949e-03, 3.540763e-01, 1.927371e-01,
	  -5.241965e-03, 1.124981e-03, 3.848016e-02, 2.936660e-02,
	  -7.368022e-03, -5.431388e-04, 3.879141e-02, 3.466704e-02,
	  5.399801e-02, 2.390639e-02, 3.484577e-01, 2.383851e-01,
	  9.697981e-02, -4.233985e-03, 3.319180e-01, 1.973090e-01,
	  -3.302007e-03, -1.126303e-03, 3.897240e-02, 3.033000e-02,
	  -7.239500e-04, 3.381195e-04, 5.391312e-03, 5.518351e-03,
	  6.201418e-03, 6.024834e-03, 4.303622e-02, 3.573428e-02,
	  8.996616e-03, -9.458708e-04, 4.093724e-02, 3.039013e-02,
	  -5.312954e-04, -1.923020e-04, 5.361548e-03, 4.609910e-03};
	
	  private double Sinv_healthy[] = {6.297128e+04, 6.295184e+04, 7.062340e+04, 6.841371e+04,
	  1.676808e+03, 1.818870e+03, 1.737049e+03, 2.325288e+03,
	  1.774238e+03, 1.884356e+03, 1.999462e+03, 1.727194e+03,
	  7.130400e+04, 8.826979e+04, 8.470452e+04, 8.190018e+04,
	  1.344469e+03, 2.131436e+03, 1.352816e+03, 2.087601e+03,
	  3.085336e+01, 1.132704e+02, 5.463504e+01, 9.408888e+01,
	  3.345226e+01, 1.303181e+02, 5.587426e+01, 1.086733e+02,
	  1.382468e+03, 3.254301e+03, 1.630458e+03, 2.037920e+03,
	  1.591678e+03, 2.530738e+03, 1.612432e+03, 2.065459e+03,
	  3.232907e+01, 1.130705e+02, 5.640818e+01, 9.050178e+01,
	  3.602481e+01, 1.544826e+02, 6.372651e+01, 9.785590e+01,
	  1.259830e+03, 2.974648e+03, 1.756735e+03, 2.408653e+03,
	  6.005039e+04, 5.538997e+04, 7.801517e+04, 6.608585e+04,
	  1.675375e+03, 1.860922e+03, 1.961730e+03, 2.417131e+03,
	  2.009256e+03, 2.219367e+03, 2.219631e+03, 2.402469e+03,
	  6.150136e+04, 7.942436e+04, 7.106193e+04, 7.970127e+04};
	
	  private double mu_mosaic[] = {-2.270207e-04, -3.556670e-04, 5.267857e-03, 5.000850e-03,
	  7.482945e-03, -3.253581e-03, 4.102412e-02, 3.179165e-02,
	  1.145973e-02, 2.254550e-03, 4.729395e-02, 3.316699e-02,
	  -1.951490e-04, 4.685989e-04, 5.975331e-03, 5.009572e-03,
	  -2.300480e-03, -1.639703e-03, 3.469958e-02, 3.283808e-02,
	  7.987677e-02, -1.290196e-02, 3.036886e-01, 2.066718e-01,
	  1.162997e-01, 1.698569e-02, 3.746482e-01, 2.173399e-01,
	  -7.011408e-03, 3.411227e-03, 4.628498e-02, 3.344982e-02,
	  -2.201749e-03, 1.657483e-03, 3.296774e-02, 3.140261e-02,
	  7.712477e-02, 1.825041e-02, 2.946999e-01, 2.059174e-01,
	  1.231726e-01, -1.523990e-02, 3.714480e-01, 2.210355e-01,
	  -5.295806e-03, -1.491449e-03, 4.714707e-02, 3.565206e-02,
	  2.801800e-04, 4.039594e-04, 4.740387e-03, 4.745470e-03,
	  6.376045e-03, 2.516032e-03, 3.830774e-02, 3.368368e-02,
	  1.112291e-02, -1.859360e-03, 4.625768e-02, 3.417885e-02,
	  -3.009075e-05, -4.185366e-04, 6.156252e-03, 5.119974e-03};
	
	  private double Sinv_mosaic[] = {5.907496e+04, 6.566787e+04, 7.704055e+04, 8.222220e+04,
	  1.548400e+03, 1.864463e+03, 1.992526e+03, 2.695041e+03,
	  1.316822e+03, 1.724580e+03, 1.604291e+03, 2.453041e+03,
	  4.223293e+04, 5.626059e+04, 6.562688e+04, 6.216869e+04,
	  1.529511e+03, 2.674136e+03, 2.010963e+03, 3.051148e+03,
	  3.344226e+01, 1.301831e+02, 6.062590e+01, 1.354838e+02,
	  2.564809e+01, 1.122913e+02, 5.458969e+01, 9.163731e+01,
	  7.883700e+02, 2.533225e+03, 1.096940e+03, 2.763472e+03,
	  1.575574e+03, 2.680087e+03, 2.126122e+03, 2.942548e+03,
	  3.228644e+01, 1.185273e+02, 5.746028e+01, 1.071286e+02,
	  2.957614e+01, 1.166933e+02, 5.386052e+01, 9.044136e+01,
	  7.440719e+02, 2.083761e+03, 9.648930e+02, 1.806654e+03,
	  6.521806e+04, 7.228192e+04, 8.197407e+04, 8.010508e+04,
	  2.024365e+03, 1.852515e+03, 2.250772e+03, 1.810990e+03,
	  1.584236e+03, 1.975902e+03, 1.932737e+03, 2.230274e+03,
	  3.746144e+04, 6.213529e+04, 4.993680e+04, 6.898721e+04};

    
    public FaceView(IdentifyDisease context) throws IOException {
        super(context);
        diagnosis = "Initialising...";
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
    
    /* Kullback-Leibler divergence between two Gaussian distributions, with
     * diagonal covariance. */
    public double kldivergence(double mu1[],double Sinv1[], double mu2[], double Sinv2[]){
    	double dist = 0;
    	int dim = mu1.length;
    	double inner1;
    	double inner2;
    	double inner3;
    	double inner4a;
    	double inner4b;
    	double inner4;
        for (int i=0; i<dim; i++) {
            inner1 = Sinv1[i]*(1/Sinv2[i]);
            inner2 = Sinv2[i]*(1/Sinv1[i]);
            inner3 = -2.0;
            inner4a = Sinv1[i]+Sinv2[i];
            inner4b = Math.pow((mu1[i]-mu2[i]),2);
            inner4 = inner4a * inner4b;
            dist += inner1 + inner2 + inner3 + inner4;
        }
    	return dist;
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
           
        // Need to load in any function from objdetect for some reason...
        CvHaarFeature feat = new CvHaarFeature();
        
        //FloatBuffer objectDescriptors;
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
        
        // calculate summary statistics of the descriptors in this frame
        int elem_size = tmpdescriptors.elem_size();
        int descriptorlength = 64;
        double mu[] = new double[descriptorlength];
        double Sinv[] = new double[descriptorlength];
        for (int i=0; i<descriptorlength; i++) {
        	mu[i] = 0;
        	Sinv[i] = 0;
        }
                   
        
        if (tmpdescriptors.total() > 0) {
            int total = tmpdescriptors.total();
            FloatBuffer[] objectDescriptors = new FloatBuffer[total];
            
            // Mean
            for (int i = 0; i < total; i++ ) {
                objectDescriptors[i] = cvGetSeqElem(tmpdescriptors, i).asByteBuffer(elem_size).asFloatBuffer();
                for(int j=0;j<descriptorlength;j++) {
                	mu[j] += objectDescriptors[i].get(j);
                }
            }

            for(int j=0;j<descriptorlength;j++) {
            	mu[j] = mu[j]/total;
            }
            
            // Variance
            for (int i = 0; i < total; i++ ) {
                for(int j=0;j<descriptorlength;j++) {
                	Sinv[j] += Math.pow(mu[j] - objectDescriptors[i].get(j),2);
                }
            }
            
            for(int j=0;j<descriptorlength;j++) {
            	Sinv[j] = Sinv[j]/total;
            	Sinv[j] = 1/Sinv[j];
            }
            
            // Find the distance of the descriptor distribution from the healthy
            // and mosaic disease cases.
            double dist1 = kldivergence(mu,Sinv,mu_healthy,Sinv_healthy);
            double dist2 = kldivergence(mu,Sinv,mu_mosaic,Sinv_mosaic);
            
            
            if ((dist1>40) && (dist2>40)) {
                diagnosis = "Scanning for leaf..."; // (" + String.format("%.1f", dist1) + ", " + String.format("%.1f", dist2) + ")";
            }
            else if (dist1<.8*dist2) {
                diagnosis = "Healthy";
            }
            else {
                diagnosis = "Cassava Mosaic Disease";
            }    
        }
        
        cvClearMemStorage(tmpstorage);
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        
        if (diagnosis != null) {
        	paint.setTextSize(20);
	        float textWidth = paint.measureText(diagnosis);
	        canvas.drawText(diagnosis, (getWidth()-textWidth)/2, 20, paint);
        }
        
        if (keypoints != null) {
        	paint.setTextSize(10);
	        String s = "image keypoints: " + keypoints.total();
	        float textWidth = paint.measureText(s);
	        canvas.drawText(s, (getWidth()-textWidth)/2, 40, paint);
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
