package com.wyroczen.alphacamera.reflection;

import android.graphics.Rect;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.params.InputConfiguration;
import android.os.Handler;
import android.util.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

public class ReflectionHelper {

    public static CaptureResult.Key<int[]> SUPPERNIGHT_BLACKLEVEL_KEY = null;
    public static CaptureResult.Key<byte[]> DISTORTION_FPC_DATA = null;

    //Capture Request Keys
    public static CaptureRequest.Key<Boolean> HINT_FOR_RAW_REPROCESS_KEY = null;
    public static CaptureRequest.Key<int[]> MTK_REMOSAIC_ENABLE_KEY = null;
    public static CaptureRequest.Key<Integer> XIAOMI_REMOSAIC_ENABLE_KEY = null;
    public static CaptureRequest.Key<Rect> POST_PROCESS_CROP_REGION = null;
    public static CaptureRequest.Key<byte[]> CONTROL_DISTORTION_FPC_DATA = null;
    public static CaptureRequest.Key<Byte> ULTRA_WIDE_LENS_DISTORTION_CORRECTION_LEVEL = null;
    public static CaptureRequest.Key<Byte> NORMAL_WIDE_LENS_DISTORTION_CORRECTION_LEVEL = null;
    public static CaptureRequest.Key<Byte> DEPURPLE = null;


    public static final int[] CONTROL_REMOSAIC_HINT_OFF = new int[]{0};
    public static final int[] CONTROL_REMOSAIC_HINT_ON = new int[]{1};

    //Methods
    public static Method createCustomCaptureSession = null;

    public ReflectionHelper() {
        //Vendor keys
        Constructor<CaptureResult.Key> captureResultKeyConstructor;
        try {
            captureResultKeyConstructor = CaptureResult.Key.class.getDeclaredConstructor(String.class, Class.class);
            captureResultKeyConstructor.setAccessible(true);
            SUPPERNIGHT_BLACKLEVEL_KEY =
                    captureResultKeyConstructor.newInstance("com.mediatek.suppernightfeature.blacklevel", int[].class);
            DISTORTION_FPC_DATA =
                    captureResultKeyConstructor.newInstance("xiaomi.distortion.distortioFpcData", byte[].class);

        } catch (Exception e) {
            e.printStackTrace();
        }

        Constructor<CaptureRequest.Key> captureRequestKeyConstructor;
        try {
            captureRequestKeyConstructor = CaptureRequest.Key.class.getDeclaredConstructor(String.class, Class.class);
            captureRequestKeyConstructor.setAccessible(true);
            HINT_FOR_RAW_REPROCESS_KEY =
                    captureRequestKeyConstructor.newInstance("com.mediatek.control.capture.hintForRawReprocess", Boolean.class);
            MTK_REMOSAIC_ENABLE_KEY =
                    captureRequestKeyConstructor.newInstance("com.mediatek.control.capture.remosaicenable", int[].class);
            XIAOMI_REMOSAIC_ENABLE_KEY =
                    captureRequestKeyConstructor.newInstance("xiaomi.remosaic.enabled", Integer.class);
            POST_PROCESS_CROP_REGION =
                    captureRequestKeyConstructor.newInstance("xiaomi.superResolution.cropRegionMtk", Rect.class);
            CONTROL_DISTORTION_FPC_DATA =
                    captureRequestKeyConstructor.newInstance("xiaomi.distortion.distortioFpcData", byte[].class);
            ULTRA_WIDE_LENS_DISTORTION_CORRECTION_LEVEL =
                    captureRequestKeyConstructor.newInstance("xiaomi.distortion.ultraWideDistortionLevel", Byte.class);
            NORMAL_WIDE_LENS_DISTORTION_CORRECTION_LEVEL =
                    captureRequestKeyConstructor.newInstance("xiaomi.distortion.distortionLevelApplied", Byte.class);
            DEPURPLE =
                    captureRequestKeyConstructor.newInstance("xiaomi.depurple.enabled", Byte.class);


        } catch (Exception e) {
            e.printStackTrace();
        }

        //Custom Capture Session
        try {
            createCustomCaptureSession = CameraDevice.class.getDeclaredMethod("createCustomCaptureSession", InputConfiguration.class, List.class, int.class, CameraCaptureSession.StateCallback.class, Handler.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        createCustomCaptureSession.setAccessible(true);
    }
}
