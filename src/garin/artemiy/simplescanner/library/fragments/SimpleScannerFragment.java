package garin.artemiy.simplescanner.library.fragments;

import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import garin.artemiy.simplescanner.library.views.SimpleCameraView;
import net.sourceforge.zbar.*;

/**
 * Author: Artemiy Garin
 * Date: 16.10.13
 */
public class SimpleScannerFragment extends Fragment {

    private static final String Z_BAR_LIBRARY = "iconv";
    private static final String GREY_COLOR_SPACE = "Y800";
    private static final long REFRESH_TIME = 2000;

    private ImageScanner scanner;
    private SimpleCameraView cameraView;
    private PackageManager packageManager;
    private Handler autoFocusHandler = new Handler();
    private Camera.PreviewCallback previewCallback = new CustomPreviewCallback();

    static {
        System.loadLibrary(Z_BAR_LIBRARY);
    }

    private Camera.AutoFocusCallback autoFocusCallback = new Camera.AutoFocusCallback() {
        public void onAutoFocus(boolean success, Camera camera) {
            autoFocusHandler.postDelayed(runAutoFocus, REFRESH_TIME);
        }
    };

    private Runnable runAutoFocus = new Runnable() {
        public void run() {
            if (cameraView != null && cameraView.getCamera() != null && isHaveAutoFocus())
                cameraView.getCamera().autoFocus(autoFocusCallback);
        }
    };

    private boolean isHaveAutoFocus() {
        if (packageManager == null) {
            packageManager = getActivity().getPackageManager();
        }
        return packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_AUTOFOCUS);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        cameraView.configureCamera(getResources().getConfiguration());

        scanner = new ImageScanner();
        scanner.setConfig(0, Config.X_DENSITY, 3);
        scanner.setConfig(0, Config.Y_DENSITY, 3);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (isHaveAutoFocus())
            cameraView.getCamera().cancelAutoFocus();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isHaveAutoFocus())
            cameraView.getCamera().autoFocus(autoFocusCallback);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        cameraView.configureCamera(newConfig);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        cameraView = new SimpleCameraView(inflater.getContext(), autoFocusCallback, previewCallback);
        return cameraView;
    }

    private class CustomPreviewCallback implements Camera.PreviewCallback {

        public void onPreviewFrame(byte[] data, Camera incomingCamera) {
            Camera.Parameters cameraParameters = incomingCamera.getParameters();
            Camera.Size previewSize = cameraParameters.getPreviewSize();

            Image barcode = new Image(previewSize.width, previewSize.height, GREY_COLOR_SPACE);
            barcode.setData(data);

            int result = scanner.scanImage(barcode);

            if (result != 0) {

                SymbolSet scannerResults = scanner.getResults();
                for (Symbol symbol : scannerResults) {
                    Toast.makeText(getActivity(), symbol.getData(), Toast.LENGTH_LONG).show();
                }
            }
        }

    }

}