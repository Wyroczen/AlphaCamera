package com.wyroczen.alphacamera;

import androidx.appcompat.widget.AppCompatImageButton;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.DngCreator;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.BlackLevelPattern;
import android.hardware.camera2.params.OutputConfiguration;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
//import android.support.annotation.NonNull;
//import android.support.v4.app.ActivityCompat;
//import android.support.v4.app.DialogFragment;
//import android.support.v4.app.Fragment;
//import android.support.v4.content.ContextCompat;
import android.provider.Settings;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.wyroczen.alphacamera.metadata.ExifHelper;
import com.wyroczen.alphacamera.reflection.ReflectionHelper;
import com.wyroczen.alphacamera.stock.ReflectUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import android.provider.Settings.System;

public class CameraFragment extends Fragment
        implements View.OnClickListener, ActivityCompat.OnRequestPermissionsResultCallback {

    /**
     * Conversion from screen rotation to JPEG orientation.
     */
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private static final int REQUEST_FINE_LOCATION_PERMISSION = 2;
    private static final String FRAGMENT_DIALOG = "dialog";
    private int chosenImageFormat;
    private Size choosenBackResolution;
    private Boolean mFrontMirror;
    private Boolean mShutterSoundEnabled;
    private CaptureResult mCaptureResult;
    private CameraCharacteristics mCameraCharacteristics;
    private SettingsUtils mSettingsUtils;
    //private CaptureResult mPreviewCaptureResult;

    private MediaPlayer shutterMediaPlayer = null;

    private ReflectionHelper reflectionHelper = null;

    public static final String CAMERA_BACK_MAIN = "0";
    public static final String CAMERA_FRONT = "1";
    public static final String CAMERA_BACK_WIDE = "21";
    public static final String CAMERA_BACK_MACRO = "22";
    public Boolean SEMI_MANUAL_MODE = false;
    public Boolean SEMI_MANUAL_MODE_ISO = false;
    public Boolean SEMI_MANUAL_MODE_EXPOSURE = false;
    public Boolean FULL_MANUAL_MODE = false;
    public Integer MANUAL_ISO_VALUE = 100;
    public Long MANUAL_EXP_VALUE = 100000L;
    //Antibanding
    public int mAntibandingMode = 3;
    //StreamConfig/Opmode
    public int mStreamConfigOpmode = 0;

    private final String[] exposureEntries = new String[]{"1/1000","1/500","1/250","1/125","1/60","1/30","1/15","1/8","1/4","1/2","1","2","4","8","16","32"};
    private final Long[] exposureValues = new Long[]{1000000L,2000000L,4000000L,8000000L,16666666L,32333333L,64666666L,125000000L,250000000L,500000000L,1000000000L,2000000000L,4000000000L,8000000000L,16000000000L,32000000000L};

    private TextView isoValueTextView;
    private TextView exposureValueTextView;

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    /**
     * Tag for the {@link Log}.
     */
    private static final String TAG = "AlphaCamera - CameraFragment";

    /**
     * Camera state: Showing camera preview.
     */
    private static final int STATE_PREVIEW = 0;

    /**
     * Camera state: Waiting for the focus to be locked.
     */
    private static final int STATE_WAITING_LOCK = 1;

    /**
     * Camera state: Waiting for the exposure to be precapture state.
     */
    private static final int STATE_WAITING_PRECAPTURE = 2;

    /**
     * Camera state: Waiting for the exposure state to be something other than precapture.
     */
    private static final int STATE_WAITING_NON_PRECAPTURE = 3;

    /**
     * Camera state: Picture was taken.
     */
    private static final int STATE_PICTURE_TAKEN = 4;

    /**
     * Max preview width that is guaranteed by Camera2 API
     */
    private static final int MAX_PREVIEW_WIDTH = 1920;

    /**
     * Max preview height that is guaranteed by Camera2 API
     */
    private static final int MAX_PREVIEW_HEIGHT = 1080;

    /**
     * {@link TextureView.SurfaceTextureListener} handles several lifecycle events on a
     * {@link TextureView}.
     */
    private final TextureView.SurfaceTextureListener mSurfaceTextureListener
            = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
            openCamera(width, height);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
            configureTransform(width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture texture) {
        }

    };

    /**
     * ID of the current {@link CameraDevice}.
     */
    private String mCameraId = CAMERA_BACK_MAIN;

    /**
     * An {@link AutoFitTextureView} for camera preview.
     */
    private AutoFitTextureView mTextureView;

    /**
     * A {@link CameraCaptureSession } for camera preview.
     */
    private CameraCaptureSession mCaptureSession;

    /**
     * A reference to the opened {@link CameraDevice}.
     */
    private CameraDevice mCameraDevice;

    /**
     * The {@link android.util.Size} of camera preview.
     */
    private Size mPreviewSize;

    /**
     * {@link CameraDevice.StateCallback} is called when {@link CameraDevice} changes its state.
     */
    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            // This method is called when the camera is opened.  We start camera preview here.
            mCameraOpenCloseLock.release();
            mCameraDevice = cameraDevice;
            createCameraPreviewSession();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
            Activity activity = getActivity();
            if (null != activity) {
                activity.finish();
            }
        }

    };

    /**
     * An additional thread for running tasks that shouldn't block the UI.
     */
    private HandlerThread mBackgroundThread;

    /**
     * A {@link Handler} for running tasks in the background.
     */
    private Handler mBackgroundHandler;

    /**
     * An {@link ImageReader} that handles still image capture.
     */
    private ImageReader mImageReader;

    /**
     * This is the output file for our picture.
     */
    private File mFile;

    /**
     * This a callback object for the {@link ImageReader}. "onImageAvailable" will be called when a
     * still image is ready to be saved.
     */
    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener
            = new ImageReader.OnImageAvailableListener() {

        @Override
        public void onImageAvailable(ImageReader reader) {
            //TODO EXIF
//            try {
//                //saveImageMetadata(mFile);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }

//            String fileName = new SimpleDateFormat("yyyMMddHHhh").format(new Date()) + "_AlphaCamera";
//            if (chosenImageFormat == ImageFormat.JPEG) {
//                mFile = new File(getActivity().getExternalFilesDir(null), fileName + ".jpg");
//            } else if (chosenImageFormat == ImageFormat.RAW_SENSOR) {
//                mFile = new File(getActivity().getExternalFilesDir(null), fileName + ".dng");
//            }

            //Location
            //getLocation();
            mBackgroundHandler.post(new ImageSaver(reader.acquireNextImage(), mFile, chosenImageFormat, mCameraCharacteristics, mCaptureResult, ((CameraActivity) getActivity()).longitude, ((CameraActivity) getActivity()).latitude));

//            //TODO EXIF TAGS LOCATION
//            ExifInterface exif = null;
//            try {
//                exif = new ExifInterface(mFile.getAbsolutePath());
//                exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, "10");
//                exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, "10");
//                exif.saveAttributes();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        }

    };

    private static final long LOCK_FOCUS_DELAY_ON_FOCUSED = 5000;
    private static final long LOCK_FOCUS_DELAY_ON_UNFOCUSED = 1000;

    private Integer mLastAfState = null;
    private Handler mUiHandler = new Handler(); // UI handler
    private Runnable mLockAutoFocusRunnable = new Runnable() {

        @Override
        public void run() {
            lockAutoFocus();
        }
    };

    public void lockAutoFocus() {
        try {
            // This is how to tell the camera to lock focus.
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);

            //TEST AF FIX
            //mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CameraMetadata.CONTROL_AF_MODE_AUTO);
            //

            CaptureRequest captureRequest = mPreviewRequestBuilder.build();
            //mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, null); // prevent CONTROL_AF_TRIGGER_START from calling over and over again
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_IDLE);

            mCaptureSession.capture(captureRequest, mCaptureCallback, mBackgroundHandler);
            //Test:
            //mCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(), mCaptureCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private float getMinimumFocusDistance() {
        if (mCameraId == null)
            return 0;

        Float minimumLens = null;
        try {
            CameraManager manager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
            CameraCharacteristics c = manager.getCameraCharacteristics(mCameraId);
            minimumLens = c.get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE);
        } catch (Exception e) {
            Log.e(TAG, "isHardwareLevelSupported Error", e);
        }
        if (minimumLens != null)
            return minimumLens;
        return 0;
    }

    private boolean isAutoFocusSupported() {
        Log.i(TAG, " Is autofocus supported: " +  getMinimumFocusDistance());
        return getMinimumFocusDistance() > 0;
    }



    /**
     * {@link CaptureRequest.Builder} for the camera preview
     */
    private CaptureRequest.Builder mPreviewRequestBuilder;

    /**
     * {@link CaptureRequest} generated by {@link #mPreviewRequestBuilder}
     */
    private CaptureRequest mPreviewRequest;

    /**
     * The current state of camera state for taking pictures.
     *
     * @see #mCaptureCallback
     */
    private int mState = STATE_PREVIEW;

    /**
     * A {@link Semaphore} to prevent the app from exiting before closing the camera.
     */
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);

    /**
     * Whether the current camera device supports Flash or not.
     */
    private boolean mFlashSupported;

    /**
     * Orientation of the camera sensor
     */
    private int mSensorOrientation;

    /**
     * A {@link CameraCaptureSession.CaptureCallback} that handles events related to JPEG capture.
     */
    private CameraCaptureSession.CaptureCallback mCaptureCallback
            = new CameraCaptureSession.CaptureCallback() {

        private void process(CaptureResult result) {
            switch (mState) {
                case STATE_PREVIEW: {
                    // We have nothing to do when the camera preview is working normally.
                    // TODO: handle auto focus
//                    Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
//                    if (afState != null && !afState.equals(mLastAfState)) {
//                        switch (afState) {
//                            case CaptureResult.CONTROL_AF_STATE_INACTIVE:
//                                Log.d(TAG, "CaptureResult.CONTROL_AF_STATE_INACTIVE");
//                                lockAutoFocus();
//                                break;
//                            case CaptureResult.CONTROL_AF_STATE_ACTIVE_SCAN:
//                                Log.d(TAG, "CaptureResult.CONTROL_AF_STATE_ACTIVE_SCAN");
//                                break;
//                            case CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED:
//                                Log.d(TAG, "CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED");
//                                mUiHandler.removeCallbacks(mLockAutoFocusRunnable);
//                                mUiHandler.postDelayed(mLockAutoFocusRunnable, LOCK_FOCUS_DELAY_ON_FOCUSED);
//                                break;
//                            case CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED:
//                                mUiHandler.removeCallbacks(mLockAutoFocusRunnable);
//                                mUiHandler.postDelayed(mLockAutoFocusRunnable, LOCK_FOCUS_DELAY_ON_UNFOCUSED);
//                                Log.d(TAG, "CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED");
//                                break;
//                            case CaptureResult.CONTROL_AF_STATE_PASSIVE_UNFOCUSED:
//                                mUiHandler.removeCallbacks(mLockAutoFocusRunnable);
//                                //mUiHandler.postDelayed(mLockAutoFocusRunnable, LOCK_FOCUS_DELAY_ON_UNFOCUSED);
//                                Log.d(TAG, "CaptureResult.CONTROL_AF_STATE_PASSIVE_UNFOCUSED");
//                                break;
//                            case CaptureResult.CONTROL_AF_STATE_PASSIVE_SCAN:
//                                Log.d(TAG, "CaptureResult.CONTROL_AF_STATE_PASSIVE_SCAN");
//                                break;
//                            case CaptureResult.CONTROL_AF_STATE_PASSIVE_FOCUSED:
//                                mUiHandler.removeCallbacks(mLockAutoFocusRunnable);
//                                //mUiHandler.postDelayed(mLockAutoFocusRunnable, LOCK_FOCUS_DELAY_ON_FOCUSED);
//                                Log.d(TAG, "CaptureResult.CONTROL_AF_STATE_PASSIVE_FOCUSED");
//                                break;
//                        }
//                    }
//                    mLastAfState = afState;
                    break;
                }
                case STATE_WAITING_LOCK: {
                    Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
                    if (afState == null) {
                        captureStillPicture();
                    } else if (CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED == afState ||
                            CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED == afState) {
                        // CONTROL_AE_STATE can be null on some devices
                        Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                        if (aeState == null ||
                                aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
                            mState = STATE_PICTURE_TAKEN;
                            captureStillPicture();
                        } else {
                            runPrecaptureSequence();
                        }
                    }
                    break;
                }
                case STATE_WAITING_PRECAPTURE: {
                    // CONTROL_AE_STATE can be null on some devices
                    Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                    if (aeState == null ||
                            aeState == CaptureResult.CONTROL_AE_STATE_PRECAPTURE ||
                            aeState == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED) {
                        mState = STATE_WAITING_NON_PRECAPTURE;
                    }
                    break;
                }
                case STATE_WAITING_NON_PRECAPTURE: {
                    // CONTROL_AE_STATE can be null on some devices
                    Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                    if (aeState == null || aeState != CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
                        mState = STATE_PICTURE_TAKEN;
                        captureStillPicture();
                    }
                    break;
                }
            }
        }

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session,
                                        @NonNull CaptureRequest request,
                                        @NonNull CaptureResult partialResult) {
            //Preview capture result TODO
            //mPreviewCaptureResult = partialResult;
            //
            process(partialResult);
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                       @NonNull CaptureRequest request,
                                       @NonNull TotalCaptureResult result) {
            mCaptureResult = result;
            process(result);
        }

    };

    /**
     * Shows a {@link Toast} on the UI thread.
     *
     * @param text The message to show
     */
    private void showToast(final String text) {
        final Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(activity, text, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /**
     * Given {@code choices} of {@code Size}s supported by a camera, choose the smallest one that
     * is at least as large as the respective texture view size, and that is at most as large as the
     * respective max size, and whose aspect ratio matches with the specified value. If such size
     * doesn't exist, choose the largest one that is at most as large as the respective max size,
     * and whose aspect ratio matches with the specified value.
     *
     * @param choices           The list of sizes that the camera supports for the intended output
     *                          class
     * @param textureViewWidth  The width of the texture view relative to sensor coordinate
     * @param textureViewHeight The height of the texture view relative to sensor coordinate
     * @param maxWidth          The maximum width that can be chosen
     * @param maxHeight         The maximum height that can be chosen
     * @param aspectRatio       The aspect ratio
     * @return The optimal {@code Size}, or an arbitrary one if none were big enough
     */
    private static Size chooseOptimalSize(Size[] choices, int textureViewWidth,
                                          int textureViewHeight, int maxWidth, int maxHeight, Size aspectRatio) {

        // Collect the supported resolutions that are at least as big as the preview Surface
        List<Size> bigEnough = new ArrayList<>();
        // Collect the supported resolutions that are smaller than the preview Surface
        List<Size> notBigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            if (option.getWidth() <= maxWidth && option.getHeight() <= maxHeight &&
                    option.getHeight() == option.getWidth() * h / w) {
                if (option.getWidth() >= textureViewWidth &&
                        option.getHeight() >= textureViewHeight) {
                    bigEnough.add(option);
                } else {
                    notBigEnough.add(option);
                }
            }
        }

        // Pick the smallest of those big enough. If there is no one big enough, pick the
        // largest of those not big enough.
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else if (notBigEnough.size() > 0) {
            return Collections.max(notBigEnough, new CompareSizesByArea());
        } else {
            Log.e(TAG, "Couldn't find any suitable preview size");
            return choices[0];
        }
    }

    public static CameraFragment newInstance() {
        return new CameraFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_camera, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        view.findViewById(R.id.capture_button).setOnClickListener(this);
        view.findViewById(R.id.settings).setOnClickListener(this);
        mTextureView = (AutoFitTextureView) view.findViewById(R.id.view_finder);

        //Set on click listener for camera switch button:
        AppCompatImageButton cameraSwitchButton = view.findViewById(R.id.camera_switch);
        cameraSwitchButton.setOnClickListener((v) -> {
            switchBackCamera();
        });
        cameraSwitchButton.setOnLongClickListener((v) -> {
            switchFrontCamera();
            return true;
        });

        //ISO and Exposure TextViews
        isoValueTextView = (TextView) view.findViewById(R.id.iso_textView);
        exposureValueTextView = (TextView) view.findViewById(R.id.exposure_textView);

        //Set change listener for iso and exposure seekbars
        SeekBar isoSeekBar = view.findViewById(R.id.iso_seekBar);
        isoSeekBar.setOnSeekBarChangeListener(isoSeekbarChangeListener);
        isoSeekBar.incrementProgressBy(100);
        isoSeekBar.setProgress(0);
        isoSeekBar.setMax(3200);
        SeekBar exposureSeekBar = view.findViewById(R.id.exposure_seekBar);
        exposureSeekBar.setOnSeekBarChangeListener(exposureSeekbarChangeListener);
        exposureSeekBar.incrementProgressBy(1);
        exposureSeekBar.setProgress(0);
        exposureSeekBar.setMax(16);
    }

    SeekBar.OnSeekBarChangeListener isoSeekbarChangeListener = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            // updated continuously as the user slides the thumb
            //Update ISO
            //Log.i("AlphaCamera","ISO Progress: " + progress);
            if (progress == 0 && !SEMI_MANUAL_MODE_EXPOSURE) {
                SEMI_MANUAL_MODE = false;
                SEMI_MANUAL_MODE_ISO = false;
                isoValueTextView.setText("ISO: AUTO");
            } else if (progress == 0) {
                SEMI_MANUAL_MODE = true;
                SEMI_MANUAL_MODE_ISO = false;
                isoValueTextView.setText("ISO: AUTO EM");
            } else {
                SEMI_MANUAL_MODE = true;
                SEMI_MANUAL_MODE_ISO = true;
                progress = progress / 100;
                progress = progress * 100;
                MANUAL_ISO_VALUE = progress;
                isoValueTextView.setText("ISO: " + progress);
            }

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            // called when the user first touches the SeekBar
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            // called after the user finishes moving the SeekBar
        }
    };

    SeekBar.OnSeekBarChangeListener exposureSeekbarChangeListener = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            //Log.i("AlphaCamera","EXPOSURE Progress: " + progress);
            // updated continuously as the user slides the thumb
            //Update Exposure
            if (progress == 0 && !SEMI_MANUAL_MODE_ISO) {
                SEMI_MANUAL_MODE = false;
                SEMI_MANUAL_MODE_EXPOSURE = false;
                exposureValueTextView.setText("EXP: AUTO");
            } else if (progress == 0) {
                SEMI_MANUAL_MODE = true;
                SEMI_MANUAL_MODE_EXPOSURE = false;
                exposureValueTextView.setText("EXP: AUTO IM");
            } else {
                SEMI_MANUAL_MODE = true;
                SEMI_MANUAL_MODE_EXPOSURE = true;
                //Long expTime = Long.valueOf(String.valueOf(progress));
                //expTime *= 100000;
                Long expTime = exposureValues[progress-1];
                MANUAL_EXP_VALUE = expTime;
                exposureValueTextView.setText("EXP: " + exposureEntries[progress-1]);
            }

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            // called when the user first touches the SeekBar
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            // called after the user finishes moving the SeekBar
        }
    };

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mSettingsUtils = new SettingsUtils();

//        Boolean rawEnabled = mSettingsUtils.readBooleanSettings(getContext(), SettingsUtils.PREF_ENABLE_RAW_KEY);
//        chosenImageFormat = rawEnabled ? ImageFormat.RAW_SENSOR : ImageFormat.JPEG;
//
//        //mFile = new File(getActivity().getExternalFilesDir(null), "pic.jpg");
//        String fileName = new SimpleDateFormat("yyyMMddHHhh").format(new Date()) + "_AlphaCamera";
//        if (chosenImageFormat == ImageFormat.JPEG) {
//            mFile = new File(getActivity().getExternalFilesDir(null), fileName + ".jpg");
//        } else if (chosenImageFormat == ImageFormat.RAW_SENSOR) {
//            mFile = new File(getActivity().getExternalFilesDir(null), fileName + ".dng");
//        }
    }

    @Override
    public void onResume() {
        super.onResume();
        startBackgroundThread();

        // When the screen is turned off and turned back on, the SurfaceTexture is already
        // available, and "onSurfaceTextureAvailable" will not be called. In that case, we can open
        // a camera and start preview from here (otherwise, we wait until the surface is ready in
        // the SurfaceTextureListener).
        if (mTextureView.isAvailable()) {
            openCamera(mTextureView.getWidth(), mTextureView.getHeight());
        } else {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
    }

    @Override
    public void onPause() {
        closeCamera();
        stopBackgroundThread();
        super.onPause();
    }

    //@Override
    //public void onStop() {
    //    super.onStop();
    //}

    @Override
    public void onStart() {

        //Camera shutter sound
        shutterMediaPlayer = MediaPlayer.create(getActivity(), R.raw.shutter_sound);

        //Settings:
        Boolean rawEnabled = mSettingsUtils.readBooleanSettings(getContext(), SettingsUtils.PREF_ENABLE_RAW_KEY);
        chosenImageFormat = rawEnabled ? ImageFormat.RAW_SENSOR : ImageFormat.JPEG;
        choosenBackResolution = mSettingsUtils.readSizeSettings(getContext(), SettingsUtils.PREF_RESOLUTION_BACK_KEY);
        mFrontMirror = mSettingsUtils.readBooleanSettings(getContext(), SettingsUtils.PREF_FRONT_FLIP_KEY);
        mShutterSoundEnabled = mSettingsUtils.readBooleanSettings(getContext(), SettingsUtils.PREF_SHUTTER_SOUND_KEY);
        //Antibanding
        mAntibandingMode = Integer.parseInt(mSettingsUtils.readStringSettings(getContext(), SettingsUtils.PREF_ANTIBANDING_MODE_KEY));
        //Opmode/stream config
        mStreamConfigOpmode = Integer.parseInt(mSettingsUtils.readStringSettings(getContext(),SettingsUtils.PREF_STREAM_CONFIG_KEY));
        if(mStreamConfigOpmode == 32770) //Vendor mode 2// dualcam
            forceSwitchBackCamera("61");
        //Brightness:
        Boolean maxBrightness = mSettingsUtils.readBooleanSettings(getContext(), SettingsUtils.PREF_MAX_BRIGHTNESS_KEY);
        setAppBrightness(maxBrightness);

        //Reflection Helper:
        reflectionHelper = new ReflectionHelper();

        //mFile = new File(getActivity().getExternalFilesDir(null), "pic.jpg");
        String fileName = new SimpleDateFormat("yyyMMddHHhh").format(new Date()) + UUID.randomUUID().toString().substring(0,5) + "_AlphaCamera";
        if (chosenImageFormat == ImageFormat.JPEG) {
            mFile = new File(getActivity().getExternalFilesDir("Pictures"), fileName + ".jpg");
        } else if (chosenImageFormat == ImageFormat.RAW_SENSOR) {
            mFile = new File(getActivity().getExternalFilesDir("RAW"), fileName + ".dng");
        }
        super.onStart();

    }

    private void setAppBrightness(Boolean shouldUseMaxBrightness) {
        if (shouldUseMaxBrightness) {
            //Set max brightness
            float brightness = 100 / (float) 255;
            WindowManager.LayoutParams lp = getActivity().getWindow().getAttributes();
            lp.screenBrightness = brightness;
            getActivity().getWindow().setAttributes(lp);
        } else {
            //Set brightness to system brightness
            ContentResolver contentResolver = getActivity().getContentResolver();
            float brightness = 0;
            try {
                brightness = System.getInt(contentResolver, System.SCREEN_BRIGHTNESS);
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }
            WindowManager.LayoutParams lp = getActivity().getWindow().getAttributes();
            lp.screenBrightness = brightness;
            getActivity().getWindow().setAttributes(lp);
        }
    }


    private void requestLocationPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
            new ConfirmationDialog().show(getChildFragmentManager(), FRAGMENT_DIALOG);
        } else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION_PERMISSION);
        }
    }


    private void requestCameraPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            new ConfirmationDialog().show(getChildFragmentManager(), FRAGMENT_DIALOG);
        } else {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                ErrorDialog.newInstance(getString(R.string.request_permission))
                        .show(getChildFragmentManager(), FRAGMENT_DIALOG);
            }
        } else if (requestCode == REQUEST_FINE_LOCATION_PERMISSION) {
            if (grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                ErrorDialog.newInstance(getString(R.string.request_fine_location_permission))
                        .show(getChildFragmentManager(), FRAGMENT_DIALOG);
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public String getmCameraId() {
        return mCameraId;
    }

    /**
     * Change camera
     */
    private void switchBackCamera() {
        if (mCameraId.equals(CAMERA_BACK_MAIN)) {
            mCameraId = CAMERA_BACK_WIDE;
            closeCamera();
            onResume();
        } else if (mCameraId.equals(CAMERA_BACK_WIDE)) {
            mCameraId = CAMERA_BACK_MACRO;
            closeCamera();
            onResume();
        } else if (mCameraId.equals(CAMERA_BACK_MACRO)) {
            mCameraId = CAMERA_BACK_MAIN;
            closeCamera();
            onResume();
        }
    }

    private void forceSwitchBackCamera(String newCameraId) {
        mCameraId = newCameraId;
        closeCamera();
        onResume();
    }

    private void switchFrontCamera() {
        if (mCameraId.equals(CAMERA_BACK_MAIN)) {
            mCameraId = CAMERA_FRONT;
            closeCamera();
            onResume();
        } else if (mCameraId.equals(CAMERA_FRONT)) {
            mCameraId = CAMERA_BACK_MAIN;
            closeCamera();
            onResume();
        }
    }


    /**
     * Sets up member variables related to camera.
     *
     * @param width  The width of available size for camera preview
     * @param height The height of available size for camera preview
     */
    @SuppressWarnings("SuspiciousNameCombination")
    private void setUpCameraOutputs(int width, int height) {
        Activity activity = getActivity();
        CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        try {
            for (String cameraId : manager.getCameraIdList()) {
                CameraCharacteristics characteristics
                        = manager.getCameraCharacteristics(mCameraId);

                mCameraCharacteristics = characteristics;

                // We don't use a front facing camera in this sample.
                //Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                //if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                //    continue;
                //}

                StreamConfigurationMap map = characteristics.get(
                        CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                if (map == null) {
                    continue;
                }

                // For still image captures, we use the largest available size.
                Size largest = Collections.max(
                        Arrays.asList(map.getOutputSizes(chosenImageFormat)),
                        new CompareSizesByArea());

                Log.i("AlphaCamera: ", "Image reader creator: ID: " + mCameraId + " Width: " + choosenBackResolution.getWidth() + " Height: " + choosenBackResolution.getHeight());
                if (choosenBackResolution.getHeight() == 0 || choosenBackResolution.getWidth() == 0) {
                    choosenBackResolution = largest;
                }

                if (chosenImageFormat == ImageFormat.JPEG) {
                    mImageReader = ImageReader.newInstance(choosenBackResolution.getWidth(), choosenBackResolution.getHeight(),
                            chosenImageFormat, /*maxImages*/1); //było 2
                } else {
                    mImageReader = ImageReader.newInstance(largest.getWidth(), largest.getHeight(),
                            chosenImageFormat, /*maxImages*/1); //było 2
                }

                mImageReader.setOnImageAvailableListener(
                        mOnImageAvailableListener, mBackgroundHandler);


                // Find out if we need to swap dimension to get the preview size relative to sensor
                // coordinate.
                int displayRotation = activity.getWindowManager().getDefaultDisplay().getRotation();
                //noinspection ConstantConditions
                mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
                boolean swappedDimensions = false;
                switch (displayRotation) {
                    case Surface.ROTATION_0:
                    case Surface.ROTATION_180:
                        if (mSensorOrientation == 90 || mSensorOrientation == 270) {
                            swappedDimensions = true;
                        }
                        break;
                    case Surface.ROTATION_90:
                    case Surface.ROTATION_270:
                        if (mSensorOrientation == 0 || mSensorOrientation == 180) {
                            swappedDimensions = true;
                        }
                        break;
                    default:
                        Log.e(TAG, "Display rotation is invalid: " + displayRotation);
                }

                Point displaySize = new Point();
                activity.getWindowManager().getDefaultDisplay().getSize(displaySize);
                int rotatedPreviewWidth = width;
                int rotatedPreviewHeight = height;
                int maxPreviewWidth = displaySize.x;
                int maxPreviewHeight = displaySize.y;

                if (swappedDimensions) {
                    rotatedPreviewWidth = height;
                    rotatedPreviewHeight = width;
                    maxPreviewWidth = displaySize.y;
                    maxPreviewHeight = displaySize.x;
                }

                if (maxPreviewWidth > MAX_PREVIEW_WIDTH) {
                    maxPreviewWidth = MAX_PREVIEW_WIDTH;
                }

                if (maxPreviewHeight > MAX_PREVIEW_HEIGHT) {
                    maxPreviewHeight = MAX_PREVIEW_HEIGHT;
                }

                // Danger, W.R.! Attempting to use too large a preview size could  exceed the camera
                // bus' bandwidth limitation, resulting in gorgeous previews but the storage of
                // garbage capture data.
                mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),
                        rotatedPreviewWidth, rotatedPreviewHeight, maxPreviewWidth,
                        maxPreviewHeight, largest);

                // We fit the aspect ratio of TextureView to the size of preview we picked.
                int orientation = getResources().getConfiguration().orientation;
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    mTextureView.setAspectRatio(
                            mPreviewSize.getWidth(), mPreviewSize.getHeight());
                } else {
                    mTextureView.setAspectRatio(
                            mPreviewSize.getHeight(), mPreviewSize.getWidth());
                }

                // Check if the flash is supported.
                Boolean available = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                mFlashSupported = available == null ? false : available;

                //mCameraId = cameraId;
                return;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            // Currently an NPE is thrown when the Camera2API is used but not supported on the
            // device this code runs.
            ErrorDialog.newInstance(getString(R.string.camera_error))
                    .show(getChildFragmentManager(), FRAGMENT_DIALOG);
        }
    }

    /**
     * Opens the camera specified by {@link CameraFragment#mCameraId}.
     */
    private void openCamera(int width, int height) {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission();
            return;
        }
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission();
            return;
        }
        setUpCameraOutputs(width, height);
        configureTransform(width, height);
        Activity activity = getActivity();
        CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        try {
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }
            manager.openCamera(mCameraId, mStateCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera opening.", e);
        }
    }

    /**
     * Closes the current {@link CameraDevice}.
     */
    private void closeCamera() {
        try {
            mCameraOpenCloseLock.acquire();
            if (null != mCaptureSession) {
                mCaptureSession.close();
                mCaptureSession = null;
            }
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
            if (null != mImageReader) {
                mImageReader.close();
                mImageReader = null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
        } finally {
            mCameraOpenCloseLock.release();
        }
    }

    /**
     * Starts a background thread and its {@link Handler}.
     */
    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    /**
     * Stops the background thread and its {@link Handler}.
     */
    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a new {@link CameraCaptureSession} for camera preview.
     */
    private void createCameraPreviewSession() {
        try {
            SurfaceTexture texture = mTextureView.getSurfaceTexture();
            assert texture != null;

            // We configure the size of default buffer to be the size of camera preview we want.
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());

            // This is the output Surface we need to start preview.
            Surface surface = new Surface(texture);

            // We set up a CaptureRequest.Builder with the output Surface.
            mPreviewRequestBuilder
                    = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewRequestBuilder.addTarget(surface);

            // Here, we create a CameraCaptureSession for camera preview.
            List<OutputConfiguration> outputConfigurationList = new ArrayList<OutputConfiguration>();
            List<Surface> surfaces = Arrays.asList(surface, mImageReader.getSurface());
            for (Surface outputSurface : surfaces) {
                OutputConfiguration outputConfiguration = new OutputConfiguration(outputSurface);
                outputConfigurationList.add(outputConfiguration);
            }
            ReflectionHelper.createCustomCaptureSession.invoke(mCameraDevice, null, outputConfigurationList, mStreamConfigOpmode, new CameraCaptureSession.StateCallback() { //32778 TODO night mode
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                            // The camera is already closed
                            if (null == mCameraDevice) {
                                return;
                            }

                            // When the session is ready, we start displaying the preview.
                            mCaptureSession = cameraCaptureSession;
                            try {
                                // Auto focus should be continuous for camera preview.
                                //mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                                //        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

                                //CONTROL PREVIEW /////////////////////////////////////////////////////////////////
                                //Antibanding
                                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_ANTIBANDING_MODE, mAntibandingMode);
                                //TEST DISTORTION CORRECTION
                                if (mCameraId.equals(CAMERA_BACK_MAIN)) {
                                    mPreviewRequestBuilder.set(ReflectionHelper.NORMAL_WIDE_LENS_DISTORTION_CORRECTION_LEVEL, Byte.valueOf("1"));
                                } else if (mCameraId.equals(CAMERA_BACK_WIDE)) {
                                    mPreviewRequestBuilder.set(ReflectionHelper.ULTRA_WIDE_LENS_DISTORTION_CORRECTION_LEVEL, Byte.valueOf("1"));
                                } else if (mCameraId.equals(CAMERA_FRONT)) {
                                    //mPreviewRequestBuilder.set(ReflectionHelper.FRONT_MIRROR, true);
                                    //mPreviewRequestBuilder.set(ReflectionHelper.SANPSHOT_FLIP_MODE, ReflectionHelper.VALUE_SANPSHOT_FLIP_MODE_OFF);
                                }
                                //
                                //mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                                //        CaptureRequest.CONTROL_AF_MODE_AUTO);
                                //if (isAutoFocusSupported())
                                //    mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                                //            CaptureRequest.CONTROL_AF_MODE_AUTO);
                                //else
                                    mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                                            CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

                                // Flash is automatically enabled when necessary.
                                setAutoFlash(mPreviewRequestBuilder);

                                // Finally, we start displaying the camera preview.
                                mPreviewRequest = mPreviewRequestBuilder.build();
                                mCaptureSession.setRepeatingRequest(mPreviewRequest,
                                        mCaptureCallback, mBackgroundHandler);
                                //try {
                                //    mCaptureSession.capture(mPreviewRequestBuilder.build(), mPreCaptureCallback, mBackgroundHandler);
                                //} catch (Exception e) {e.printStackTrace();}
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(
                                @NonNull CameraCaptureSession cameraCaptureSession) {
                            showToast("Failed");
                        }
                    }, null
            );


//            mCameraDevice.createCaptureSession(Arrays.asList(surface, mImageReader.getSurface()),
//                    new CameraCaptureSession.StateCallback() {
//
//                        @Override
//                        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
//                            // The camera is already closed
//                            if (null == mCameraDevice) {
//                                return;
//                            }
//
//                            // When the session is ready, we start displaying the preview.
//                            mCaptureSession = cameraCaptureSession;
//                            try {
//                                // Auto focus should be continuous for camera preview.
//                                //mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
//                                //        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
//
//                                //CONTROL PREVIEW /////////////////////////////////////////////////////////////////
//                                //TEST DISTORTION CORRECTION
//                                if (mCameraId.equals("0")) {
//                                    mPreviewRequestBuilder.set(ReflectionHelper.NORMAL_WIDE_LENS_DISTORTION_CORRECTION_LEVEL, Byte.valueOf("1"));
//                                }
//                                if (mCameraId.equals("21")) {
//                                    mPreviewRequestBuilder.set(ReflectionHelper.ULTRA_WIDE_LENS_DISTORTION_CORRECTION_LEVEL, Byte.valueOf("1"));
//                                }
//                                //
//                                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
//                                        CaptureRequest.CONTROL_AF_MODE_AUTO);
//
//                                // Flash is automatically enabled when necessary.
//                                setAutoFlash(mPreviewRequestBuilder);
//
//                                // Finally, we start displaying the camera preview.
//                                mPreviewRequest = mPreviewRequestBuilder.build();
//                                mCaptureSession.setRepeatingRequest(mPreviewRequest,
//                                        mCaptureCallback, mBackgroundHandler);
//                            } catch (CameraAccessException e) {
//                                e.printStackTrace();
//                            }
//                        }
//
//                        @Override
//                        public void onConfigureFailed(
//                                @NonNull CameraCaptureSession cameraCaptureSession) {
//                            showToast("Failed");
//                        }
//                    }, null
//            );
        } catch (CameraAccessException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /**
     * Configures the necessary {@link android.graphics.Matrix} transformation to `mTextureView`.
     * This method should be called after the camera preview size is determined in
     * setUpCameraOutputs and also the size of `mTextureView` is fixed.
     *
     * @param viewWidth  The width of `mTextureView`
     * @param viewHeight The height of `mTextureView`
     */
    private void configureTransform(int viewWidth, int viewHeight) {
        Activity activity = getActivity();
        if (null == mTextureView || null == mPreviewSize || null == activity) {
            return;
        }
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / mPreviewSize.getHeight(),
                    (float) viewWidth / mPreviewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180, centerX, centerY);
        }
        mTextureView.setTransform(matrix);
    }

    /**
     * Initiate a still image capture.
     */
    //private void takePicture() {
    public void takePicture() {
        if (mCameraId.equals(CAMERA_BACK_WIDE) || mCameraId.equals(CAMERA_FRONT)) { //TODO it should detect whether camera has autofocus feature or not
            captureStillPicture();
        } else {
            lockFocus();
        }
    }

    /**
     * Lock the focus as the first step for a still image capture.
     */
    private void lockFocus() {
        try {
            Log.i(TAG, "Locking focus");
            // This is how to tell the camera to lock focus.
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
                    CameraMetadata.CONTROL_AF_TRIGGER_START);
            // Tell #mCaptureCallback to wait for the lock.
            mState = STATE_WAITING_LOCK;
            mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback,
                    mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Run the precapture sequence for capturing a still image. This method should be called when
     * we get a response in {@link #mCaptureCallback} from {@link #lockFocus()}.
     */
    private void runPrecaptureSequence() {
        try {
            // This is how to tell the camera to trigger.
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,
                    CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START);
            // Tell #mCaptureCallback to wait for the precapture sequence to be set.
            mState = STATE_WAITING_PRECAPTURE;
            mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback,
                    mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Capture a still picture. This method should be called when we get a response in
     * {@link #mCaptureCallback} from both {@link #lockFocus()}.
     */
    private void captureStillPicture() {
        Log.i(TAG, " capturing Still Picture - start");
        try {
            final Activity activity = getActivity();
            if (null == activity || null == mCameraDevice) {
                return;
            }

            final CaptureRequest.Builder captureBuilder;
            if (SEMI_MANUAL_MODE) {
                Log.i(TAG, "SEMI_MANUAL_MODE enabled" + " Max frame dur: " + mCameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_MAX_FRAME_DURATION).toString()
                        + " Max analog sensitivity: " + mCameraCharacteristics.get(CameraCharacteristics.SENSOR_MAX_ANALOG_SENSITIVITY)
                        + " MANUAL ISO VALUE: " + MANUAL_ISO_VALUE.toString() + " Manual EXP VALUE: " + MANUAL_EXP_VALUE.toString());
                captureBuilder =
                        mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_MANUAL);
                captureBuilder.addTarget(mImageReader.getSurface());

                // Use the same AE and AF modes as the preview.
                //captureBuilder.set(CaptureRequest.CONTROL_AE_MODE,
                //        CaptureRequest.CONTROL_AE_MODE_OFF);
                //setAutoFlash(captureBuilder);

                captureBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_MODE_OFF);
                captureBuilder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, MANUAL_EXP_VALUE); //1/2S 500000000L
                captureBuilder.set(CaptureRequest.SENSOR_SENSITIVITY, MANUAL_ISO_VALUE);

                //captureBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                //        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                //if (isAutoFocusSupported())
                //    mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                //            CaptureRequest.CONTROL_AF_MODE_AUTO);
                //else
                    mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                            CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

                if (mCameraId.equals(CAMERA_BACK_MAIN) && MANUAL_ISO_VALUE < 400 && mImageReader.getHeight() == 6936) {
                    Log.i(TAG, "Get height: " + mImageReader.getHeight());
                    captureBuilder.set(ReflectionHelper.MTK_REMOSAIC_ENABLE_KEY, ReflectionHelper.CONTROL_REMOSAIC_HINT_ON);
                    captureBuilder.set(ReflectionHelper.XIAOMI_REMOSAIC_ENABLE_KEY, 1);
                }

            } else {
                Log.i(TAG, "AUTO_DEFAULT_MODE enabled");
                // This is the CaptureRequest.Builder that we use to take a picture.
                captureBuilder =
                        mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
                captureBuilder.addTarget(mImageReader.getSurface());

                //Blacklevel
                //BlackLevelPattern blevel = mCameraCharacteristics.get(CameraCharacteristics.SENSOR_BLACK_LEVEL_PATTERN);

                //Antibanding
                captureBuilder.set(CaptureRequest.CONTROL_AE_ANTIBANDING_MODE, mAntibandingMode);
                //TEST MTK
                if (mCameraId.equals(CAMERA_BACK_MAIN) && mImageReader.getHeight() == 6936) {
                    //force remosaic
                    captureBuilder.set(ReflectionHelper.MTK_REMOSAIC_ENABLE_KEY, ReflectionHelper.CONTROL_REMOSAIC_HINT_ON);
                    captureBuilder.set(ReflectionHelper.XIAOMI_REMOSAIC_ENABLE_KEY, 1);
                    captureBuilder.set(ReflectionHelper.NORMAL_WIDE_LENS_DISTORTION_CORRECTION_LEVEL, Byte.valueOf("1"));
                    captureBuilder.set(ReflectionHelper.DEPURPLE, Byte.valueOf("1"));
                } else if (mCameraId.equals(CAMERA_BACK_WIDE)) {
                    captureBuilder.set(ReflectionHelper.ULTRA_WIDE_LENS_DISTORTION_CORRECTION_LEVEL, Byte.valueOf("1"));
//                    byte [] byteArray = new byte[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,-16,63,0,0,0,0,0,0,0,0,-128,-49,60,106,44,73,
//                            -95,63,64,91,115,-8,-88,77,-63,63,0,64,56,119,-51,127,-45,63,48,
//                            -25,-67,-109,-78,95,-31,63,88,115,-113,-10,-48,58,-21,63,-44,28,121,
//                            -111,-39,-83,-13,63,20,85,113,45,-86,-25,-6,63,70,-57,120,73,87,-87,1,64,-66,120,-61,
//                            -91,19,124,6,64,-28,70,-94,-98,109,-15,11,64,-82,79,127,-80,-51,7,17,64,6,96,-24,26,-36,110,
//                            20,64,-31,-55,26,18,-19,49,24,64,84,-10,-42,-87,-107,85,28,64,-50,-27,-86,-14,-127,111,32,64,-94,-10,-18
//                            ,17,8,-22,34,64,94,122,114,-57,-88,-99,37,64,-41,33,22,12,27,-114,40,64,119,-111,-122,91,-113,-65,43,64,40,-120,-76,94,-64,54,47,64};
//                    captureBuilder.set(ReflectionHelper.CONTROL_DISTORTION_FPC_DATA, byteArray);
                    captureBuilder.set(ReflectionHelper.DEPURPLE, Byte.valueOf("1"));
                } else if (mCameraId.equals("1")) {
                    if (mFrontMirror) {
                        captureBuilder.set(ReflectionHelper.FRONT_MIRROR, true);
                        captureBuilder.set(ReflectionHelper.SANPSHOT_FLIP_MODE, ReflectionHelper.VALUE_SANPSHOT_FLIP_MODE_ON);
                    } else {
                        captureBuilder.set(ReflectionHelper.FRONT_MIRROR, true);
                        captureBuilder.set(ReflectionHelper.SANPSHOT_FLIP_MODE, ReflectionHelper.VALUE_SANPSHOT_FLIP_MODE_OFF);
                    }
                }


                // Use the same AE and AF modes as the preview.
                //captureBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                //        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                //if (isAutoFocusSupported())
                //    mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                //            CaptureRequest.CONTROL_AF_MODE_AUTO);
                //else
                    mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                            CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                //captureBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                //        CaptureRequest.CONTROL_AF_MODE_AUTO);
                setAutoFlash(captureBuilder);
            }

            // Orientation
            int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
            if (chosenImageFormat == ImageFormat.JPEG) {
                captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, getOrientation(rotation));
                //captureBuilder.set(CaptureRequest.JPEG_GPS_LOCATION, new Location(""));
            }

            CameraCaptureSession.CaptureCallback CaptureCallback
                    = new CameraCaptureSession.CaptureCallback() {

                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                               @NonNull CaptureRequest request,
                                               @NonNull TotalCaptureResult result) {
                    //mCaptureResult = result;
                    showToast("Saved: " + mFile);
                    Log.d(TAG, mFile.toString());
                    unlockFocus();
                }
            };

            mCaptureSession.stopRepeating();
            mCaptureSession.abortCaptures();
            mCaptureSession.capture(captureBuilder.build(), CaptureCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves the JPEG orientation from the specified screen rotation.
     *
     * @param rotation The screen rotation.
     * @return The JPEG orientation (one of 0, 90, 270, and 360)
     */
    private int getOrientation(int rotation) {
        // Sensor orientation is 90 for most devices, or 270 for some devices (eg. Nexus 5X)
        // We have to take that into account and rotate JPEG properly.
        // For devices with orientation of 90, we simply return our mapping from ORIENTATIONS.
        // For devices with orientation of 270, we need to rotate the JPEG 180 degrees.
        return (ORIENTATIONS.get(rotation) + mSensorOrientation + 270) % 360;
    }

    /**
     * Unlock the focus. This method should be called when still image capture sequence is
     * finished.
     */
    private void unlockFocus() {
        try {
            // Reset the auto-focus trigger
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
                    CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
            setAutoFlash(mPreviewRequestBuilder);
            mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback,
                    mBackgroundHandler);
            // After this, the camera will go back to the normal state of preview.
            mState = STATE_PREVIEW;
            mCaptureSession.setRepeatingRequest(mPreviewRequest, mCaptureCallback,
                    mBackgroundHandler);
            //try {
            //    mCaptureSession.capture(mPreviewRequestBuilder.build(), mPreCaptureCallback, mBackgroundHandler);
            //} catch (Exception e) {e.printStackTrace();}
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.capture_button: {
                if (shutterMediaPlayer != null && mShutterSoundEnabled)
                    shutterMediaPlayer.start();
                takePicture();
                break;
            }
            case R.id.settings: {
                Activity activity = getActivity();
                //if (null != activity) {
                //    new AlertDialog.Builder(activity)
                //            .setMessage(R.string.intro_message)
                //            .setPositiveButton(android.R.string.ok, null)
                //            .show();
                //}
                Intent intent = new Intent(activity, SettingsActivity.class);
                intent.putExtra("mCameraId", mCameraId);
                startActivity(intent);

                break;
            }
        }
    }

    private void setAutoFlash(CaptureRequest.Builder requestBuilder) {
        if (mFlashSupported) { //TODO FLASH NOT ALWAYS ON AUTO
            //requestBuilder.set(CaptureRequest.CONTROL_AE_MODE,
            //        CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
        }
    }

    /**
     * Saves a JPEG or RAW {@link Image} into the specified {@link File}.
     */
    private static class ImageSaver implements Runnable {

        /**
         * The JPEG image
         */
        private final Image mImage;
        /**
         * The file we save the image into.
         */
        private final File mFile;

        private final int mImageFormat;
        private final CameraCharacteristics mCameraCharacteristics;
        private final CaptureResult mCaptureResult;
        private double mLongitude;
        private double mLatutude;

        ImageSaver(Image image, File file, int format, CameraCharacteristics cameraCharacteristics, CaptureResult captureResult, double longitude, double latitude) {

            mImage = image;
            mFile = file;
            mImageFormat = format;
//            if(cameraCharacteristics == null)
//                Log.i("AlphaCamera", "cameraCharacteristics null!");
//            if(captureResult == null)
//                Log.i("AlphaCamera", "captureResult null!");
            mCameraCharacteristics = cameraCharacteristics;

            mLongitude = longitude;
            mLatutude = latitude;

            //DISTORTION DATA
//            Log.i("AlphaCamera", "Distortion data: ");
//            byte[] byteArray = (byte[]) captureResult.get(ReflectionHelper.DISTORTION_FPC_DATA); //VendorTagHelper.getValueQuietly(captureResult, CaptureResultVendorTags.DISTORTION_FPC_DATA);
//            String s = "";
//            for(byte b : byteArray){
//                s += String.valueOf(b) + ",";
//            }
//            Log.i("AlphaCamera", s);

            mCaptureResult = captureResult;
        }

        @Override
        public void run() {
            if (mImageFormat == ImageFormat.JPEG) {
                ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
                byte[] bytes = new byte[buffer.remaining()];
                buffer.get(bytes);
                FileOutputStream output = null;
                try {
                    output = new FileOutputStream(mFile);
                    output.write(bytes);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    mImage.close();

                    if (null != output) {
                        try {
                            output.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                //Save metadata (GPS mainly) to exif
                try {

                    ExifHelper.saveMetaData(mFile, mLatutude, mLongitude);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else if (mImageFormat == ImageFormat.RAW_SENSOR) {
                DngCreator dngCreator = new DngCreator(mCameraCharacteristics, mCaptureResult);
                FileOutputStream rawFileOutputStream = null;
                try {
                    rawFileOutputStream = new FileOutputStream(mFile);
                    dngCreator.writeImage(rawFileOutputStream, mImage);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    mImage.close();
                    if (rawFileOutputStream != null) {
                        try {
                            rawFileOutputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    /**
     * Compares two {@code Size}s based on their areas.
     */
    static class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }

    }

    /**
     * Shows an error message dialog.
     */
    public static class ErrorDialog extends DialogFragment {

        private static final String ARG_MESSAGE = "message";

        public static ErrorDialog newInstance(String message) {
            ErrorDialog dialog = new ErrorDialog();
            Bundle args = new Bundle();
            args.putString(ARG_MESSAGE, message);
            dialog.setArguments(args);
            return dialog;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Activity activity = getActivity();
            return new AlertDialog.Builder(activity)
                    .setMessage(getArguments().getString(ARG_MESSAGE))
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            activity.finish();
                        }
                    })
                    .create();
        }

    }

    /**
     * Shows OK/Cancel confirmation dialog about camera permission.
     */
    public static class ConfirmationDialog extends DialogFragment {

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Fragment parent = getParentFragment();
            return new AlertDialog.Builder(getActivity())
                    .setMessage(R.string.request_permission)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            parent.requestPermissions(new String[]{Manifest.permission.CAMERA},
                                    REQUEST_CAMERA_PERMISSION);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Activity activity = parent.getActivity();
                                    if (activity != null) {
                                        activity.finish();
                                    }
                                }
                            })
                    .create();
        }
    }

}