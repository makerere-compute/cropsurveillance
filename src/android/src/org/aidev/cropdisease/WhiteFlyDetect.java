package org.aidev.cropdisease;

import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;
import static com.googlecode.javacv.cpp.opencv_core.cvClearMemStorage;
import static com.googlecode.javacv.cpp.opencv_core.cvGetSeqElem;
import static com.googlecode.javacv.cpp.opencv_core.cvLoad;
import static com.googlecode.javacv.cpp.opencv_objdetect.CV_HAAR_DO_CANNY_PRUNING;
import static com.googlecode.javacv.cpp.opencv_objdetect.cvHaarDetectObjects;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;


import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.cpp.opencv_objdetect;
import com.googlecode.javacv.cpp.opencv_core.CvMemStorage;
import com.googlecode.javacv.cpp.opencv_core.CvRect;
import com.googlecode.javacv.cpp.opencv_core.CvSeq;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_objdetect.CvHaarClassifierCascade;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

public class WhiteFlyDetect extends Activity {
	   private FrameLayout layout;
	    private WhiteFlyDetectView whiteflyView;
	    private Preview mPreview;
    
	    @Override
	    protected void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);

	        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

	        // Hide the window title.
	        requestWindowFeature(Window.FEATURE_NO_TITLE);

	        // Create our Preview view and set it as the content of our activity.
	        try {
	            layout = new FrameLayout(this);
	            whiteflyView = new WhiteFlyDetectView(this);
	            mPreview = new Preview(this, whiteflyView);
	            layout.addView(mPreview);
	            layout.addView(whiteflyView);
	            setContentView(layout);
	        } catch (IOException e) {
	            e.printStackTrace();
	            new AlertDialog.Builder(this).setMessage(e.getMessage()).create().show();
	        }
	    }
	    @Override
		public boolean onCreateOptionsMenu(Menu menu) {

			// Inflate our menu which can gather user input for switching camera
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.whiteflymenu, menu);
			return true;
		}

		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
			// Handle all of the possible menu actions.
			switch (item.getItemId()) {
			case R.id.cassavamosaic:
				startActivity(new Intent("MosaicDetection"));
				break;
			
			}
			return super.onOptionsItemSelected(item);

		}
	}

	// ----------------------------------------------------------------------

	class WhiteFlyDetectView extends View implements Camera.PreviewCallback {
	    public static final int SUBSAMPLING_FACTOR = 4;

	    private IplImage grayImage;
	    private CvHaarClassifierCascade classifier;
	    private CvMemStorage storage;
	    private CvSeq whiteflies;
        int tt=0;
	    public WhiteFlyDetectView(WhiteFlyDetect context) throws IOException {
	        super(context);

	        // Load the classifier file from Java resources.
	        File classifierFile = Loader.extractResource(getClass(),
	            "/org/aidev/cropdisease/haarcascade191.xml",
	            context.getCacheDir(), "classifier", ".xml");
	        if (classifierFile == null || classifierFile.length() <= 0) {
	            throw new IOException("Could not extract the classifier file.");
	        }

	        // Preload the opencv_objdetect module to work around a known bug.
	        Loader.load(opencv_objdetect.class);
	        classifier = new CvHaarClassifierCascade(cvLoad(classifierFile.getAbsolutePath()));
	        classifierFile.delete();
	        if (classifier.isNull()) {
	            throw new IOException("Could not load the classifier file.");
	        }
	        storage = CvMemStorage.create();
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

	        whiteflies = cvHaarDetectObjects(grayImage, classifier, storage, 3, 1, CV_HAAR_DO_CANNY_PRUNING);
	        postInvalidate();
	        cvClearMemStorage(storage);
	    }

	    @Override
	    protected void onDraw(Canvas canvas) {
	        Paint paint = new Paint();
	        paint.setColor(Color.RED);
	        paint.setTextSize(20);

	        String s= tt + "White Flies Detected";;
	        float textWidth = paint.measureText(s);
	        canvas.drawText(s, (getWidth()-textWidth)/2, 20, paint);

	        if (whiteflies != null) {
	            paint.setStrokeWidth(2);
	            paint.setStyle(Paint.Style.STROKE);
	            float scaleX = (float)getWidth()/grayImage.width();
	            float scaleY = (float)getHeight()/grayImage.height();
	            int total = whiteflies.total();
	            tt=total;
	            for (int i = 0; i < total; i++) {
	                CvRect r = new CvRect(cvGetSeqElem(whiteflies, i));
	                int x = r.x(), y = r.y(), w = r.width(), h = r.height();
	                canvas.drawRect(x*scaleX, y*scaleY, (x+w)*scaleX, (y+h)*scaleY, paint);
	            }
	        }
	    }
	}

	// ----------------------------------------------------------------------

	class WhiteflyPreview extends SurfaceView implements SurfaceHolder.Callback {
	    SurfaceHolder mHolder;
	    Camera mCamera;
	    Camera.PreviewCallback previewCallback;

	    WhiteflyPreview(Context context, Camera.PreviewCallback previewCallback) {
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