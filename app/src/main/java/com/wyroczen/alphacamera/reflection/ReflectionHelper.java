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
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

public class ReflectionHelper {

    public static CaptureResult.Key<int[]> SUPPERNIGHT_BLACKLEVEL_KEY = null;
    public static CaptureResult.Key<byte[]> DISTORTION_FPC_DATA = null;

    //Capture Request Keys
    public static CaptureRequest.Key<Boolean> HINT_FOR_RAW_REPROCESS_KEY = null;
    public static CaptureRequest.Key<int[]> MTK_REMOSAIC_ENABLE_KEY = null;
    public static final int[] CONTROL_REMOSAIC_HINT_OFF = new int[]{0};
    public static final int[] CONTROL_REMOSAIC_HINT_ON = new int[]{1};
    public static CaptureRequest.Key<Integer> XIAOMI_REMOSAIC_ENABLE_KEY = null;
    public static CaptureRequest.Key<Rect> POST_PROCESS_CROP_REGION = null;
    public static CaptureRequest.Key<byte[]> CONTROL_DISTORTION_FPC_DATA = null;
    public static CaptureRequest.Key<Byte> ULTRA_WIDE_LENS_DISTORTION_CORRECTION_LEVEL = null;
    public static CaptureRequest.Key<Byte> NORMAL_WIDE_LENS_DISTORTION_CORRECTION_LEVEL = null;
    public static CaptureRequest.Key<Byte> DEPURPLE = null;
    public static CaptureRequest.Key<Boolean> FRONT_MIRROR = null;
    public static CaptureRequest.Key<Integer> SANPSHOT_FLIP_MODE = null;
    public static final int VALUE_SANPSHOT_FLIP_MODE_OFF = 0;
    public static final int VALUE_SANPSHOT_FLIP_MODE_ON = 1;

//    public static CaptureRequest.Key<Boolean> SUPER_NIGHT_SCENE_ENABLED = null;
//    public static CaptureRequest.Key<Integer> CONTROL_CAPTURE_HIGH_QUALITY_REPROCESS = null;
//    public static final int CONTROL_CAPTURE_HIGH_QUALITY_YUV_OFF = 0;
//    public static final int CONTROL_CAPTURE_HIGH_QUALITY_YUV_ON = 1;
//    public static CaptureRequest.Key<Integer> CONTROL_CAPTURE_HINT_FOR_ISP_TUNING = null;
//    public static final int CONTROL_CAPTURE_HINT_FOR_ISP_TUNING_HDR = 5005;
//    public static final int CONTROL_CAPTURE_HINT_FOR_ISP_TUNING_LLHDR = 5007;
//    public static final int CONTROL_CAPTURE_HINT_FOR_ISP_TUNING_MFSR = 5006;
//    public static CaptureRequest.Key<Byte> CONTROL_CAPTURE_ISP_META_ENABLE = null;
//    public static CaptureRequest.Key<Byte> CONTROL_CAPTURE_ISP_META_REQUEST = null;
//    public static final byte CONTROL_CAPTURE_ISP_TUNING_DATA_BUFFER = 2;
//    public static final byte CONTROL_CAPTURE_ISP_TUNING_DATA_IN_METADATA = 1;
//    public static final byte CONTROL_CAPTURE_ISP_TUNING_DATA_NONE = 0;
//    public static final byte CONTROL_CAPTURE_ISP_TUNING_REQ_RAW = 1;
//    public static final byte CONTROL_CAPTURE_ISP_TUNING_REQ_YUV = 2;

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
            FRONT_MIRROR =
                    captureRequestKeyConstructor.newInstance("xiaomi.flip.enabled", Boolean.class);
            SANPSHOT_FLIP_MODE =
                    captureRequestKeyConstructor.newInstance("com.mediatek.control.capture.flipmode", Integer.class);
            //SUPER_NIGHT_SCENE_ENABLED =
            //        captureRequestKeyConstructor.newInstance("xiaomi.supernight.enabled", Boolean.class);
//            CONTROL_CAPTURE_HIGH_QUALITY_REPROCESS =
//                    captureRequestKeyConstructor.newInstance("com.mediatek.control.capture.highQualityYuv", Integer.class);
//            CONTROL_CAPTURE_HINT_FOR_ISP_TUNING =
//                    captureRequestKeyConstructor.newInstance("com.mediatek.control.capture.hintForIspTuning", Integer.class);
//            CONTROL_CAPTURE_ISP_META_ENABLE =
//                    captureRequestKeyConstructor.newInstance("com.mediatek.control.capture.ispMetaEnable", Byte.class);
//            CONTROL_CAPTURE_ISP_META_REQUEST =
//                    captureRequestKeyConstructor.newInstance("com.mediatek.control.capture.ispTuningRequest", Byte.class);


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

        //test
        ignoreInputConfiguraionCheck();
    }

    public void ignoreInputConfiguraionCheck() {

        Class  cameraDeviceImpl = null;

        try {
            cameraDeviceImpl = Class.forName("android.hardware.camera2.impl.CameraDeviceImpl");

            if(cameraDeviceImpl != null){
                Log.i("AlphaCamera", "I have a class");

                Field[] fields = cameraDeviceImpl.getDeclaredFields();
                Log.i("AlphaCamera", " Number of fields: " + String.valueOf(fields.length));
                for(Field field : fields){
                    Log.i("AlphaCamera", field.getName());
                }

                Method[] methods = cameraDeviceImpl.getDeclaredMethods();
                Log.i("AlphaCamera", " Number of methods: " + String.valueOf(methods.length));
                for(Method method : methods){
                    Log.i("AlphaCamera", method.getName());
                }

                Field[] fields2 = cameraDeviceImpl.getFields();
                Log.i("AlphaCamera", " Number of fields: " + String.valueOf(fields2.length));
                for(Field field : fields2){
                    Log.i("AlphaCamera", field.getName());
                }

                Method checkInputConfiguration = null;
                try {
                    checkInputConfiguration = cameraDeviceImpl.getDeclaredMethod("checkInputConfiguration", InputConfiguration.class);
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
                if(checkInputConfiguration != null){
                    Log.i("AlphaCamera", "Method is not null!");}
                else {
                    Log.i("AlphaCamera", "Method is null!");
                }
            }


        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }


//        try {
//            Field field = cameraDeviceImpl.getField("mAppNames");
//
//            if(field != null){
//
//                Log.i("AlphaCamera", "I have field");
//
//                field.setAccessible(true);
//
//                String[] myStringArray = {"com.android.camera", "com.wyroczen.alphacamera"};
//                String[] objectInstance = new String[1];
//
//                String[] value = (String[]) field.get(objectInstance);
//
//                Log.i("AlphaCamera", " Value: " + value[0]);
//
//                //field.set(objetInstance, value);
//            }
//        } catch (NoSuchFieldException | IllegalAccessException e) {
//            e.printStackTrace();
//        }
    }


}
