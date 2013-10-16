package garin.artemiy.simplescanner.library.views;

import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.view.*;

/**
 * Author: Artemiy Garin
 * Date: 16.10.13
 */
public class SimpleCameraView extends SurfaceView implements SurfaceHolder.Callback {

    private static final int DEGREES_0 = 0;
    private static final int DEGREES_90 = 90;
    private static final int DEGREES_180 = 180;
    private static final int DEGREES_270 = 270;

    private SurfaceHolder surfaceHolder;
    private Camera camera;
    private Camera.AutoFocusCallback autoFocusCallback;
    private Camera.PreviewCallback previewCallback;
    private Context context;

    @SuppressWarnings("deprecation")
    public SimpleCameraView(Context context,
                            Camera.AutoFocusCallback autoFocusCallback,
                            Camera.PreviewCallback previewCallback) {
        super(context);
        this.autoFocusCallback = autoFocusCallback;
        this.previewCallback = previewCallback;
        this.context = context;

        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        setKeepScreenOn(true);

        getCamera();
    }

    public void surfaceCreated(SurfaceHolder holder) {
        try {
            camera.setPreviewDisplay(holder);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        stopCamera();
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        startCamera();
    }

    public Camera getCamera() {
        try {
            if (camera == null) {
                camera = Camera.open();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return camera;
    }

    public void configureCamera(Configuration configuration) {
        if (camera != null) {
            Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

            int displayOrientationDegrees = DEGREES_0;
            switch (display.getRotation()) {
                case Surface.ROTATION_0:
                    displayOrientationDegrees = DEGREES_90;
                    break;
                case Surface.ROTATION_90:
                    displayOrientationDegrees = DEGREES_0;
                    break;
                case Surface.ROTATION_180:
                    displayOrientationDegrees = DEGREES_270;
                    break;
                case Surface.ROTATION_270:
                    displayOrientationDegrees = DEGREES_180;
                    break;
            }

            camera.setDisplayOrientation(displayOrientationDegrees);

            Camera.Size previewSize = camera.getParameters().getPreviewSize();
            float aspect = (float) previewSize.width / previewSize.height;

            ViewGroup.LayoutParams cameraHolderParams = getLayoutParams();
            if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                cameraHolderParams.height = display.getHeight();
                cameraHolderParams.width = (int) (display.getHeight() / aspect);
            } else {
                cameraHolderParams.width = display.getWidth();
                cameraHolderParams.height = (int) (display.getWidth() / aspect);
            }

            setLayoutParams(cameraHolderParams);
        }
    }

    public void stopCamera() {
        try {

            camera.stopPreview();
            camera.setPreviewCallback(null);
            camera.release();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startCamera() {
        try {

            if (surfaceHolder.getSurface() == null) {
                return;
            }

            camera.reconnect();
            camera.setPreviewDisplay(surfaceHolder);
            camera.setPreviewCallback(previewCallback);
            camera.startPreview();
            camera.autoFocus(autoFocusCallback);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}