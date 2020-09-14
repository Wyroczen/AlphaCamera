package com.wyroczen.alphacamera.reflection;

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
    public static CaptureRequest.Key<Boolean> HINT_FOR_RAW_REPROCESS_KEY = null;
    public static CaptureRequest.Key<Integer> MTK_REMOSAIC_ENABLE_KEY = null;
    public static CaptureRequest.Key<Integer> XIAOMI_REMOSAIC_ENABLE_KEY = null;

    public static Method createCustomCaptureSession = null;

    public ReflectionHelper() {
        //Vendor keys
        Constructor<CaptureResult.Key> cameraCharacteristicsConstructor;
        try {
            cameraCharacteristicsConstructor = CaptureResult.Key.class.getDeclaredConstructor(String.class, Class.class);
            cameraCharacteristicsConstructor.setAccessible(true);
            SUPPERNIGHT_BLACKLEVEL_KEY =
                    cameraCharacteristicsConstructor.newInstance("com.mediatek.suppernightfeature.blacklevel", int[].class);

        } catch (Exception e) {
            e.printStackTrace();
        }

        Constructor<CaptureRequest.Key> cameraCharacteristicsConstructor2;
        try {
            cameraCharacteristicsConstructor2 = CaptureRequest.Key.class.getDeclaredConstructor(String.class, Class.class);
            cameraCharacteristicsConstructor2.setAccessible(true);
            HINT_FOR_RAW_REPROCESS_KEY =
                    cameraCharacteristicsConstructor2.newInstance("com.mediatek.control.capture.hintForRawReprocess", Boolean.class);
            MTK_REMOSAIC_ENABLE_KEY =
                    cameraCharacteristicsConstructor2.newInstance("com.mediatek.control.capture.remosaicenable", Integer.class);
            XIAOMI_REMOSAIC_ENABLE_KEY =
                    cameraCharacteristicsConstructor2.newInstance("xiaomi.remosaic.enabled", Integer.class);
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
