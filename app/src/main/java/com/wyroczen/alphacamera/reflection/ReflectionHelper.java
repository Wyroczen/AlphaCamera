package com.wyroczen.alphacamera.reflection;

import android.graphics.Rect;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.DngCreator;
import android.hardware.camera2.params.BlackLevelPattern;
import android.hardware.camera2.params.InputConfiguration;
import android.media.ImageReader;
import android.os.Handler;
import android.util.Log;
import android.util.Size;

import com.wyroczen.alphacamera.CameraFragment;
import com.wyroczen.alphacamera.stock.ReflectUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.nio.ByteBuffer;
import java.sql.Ref;
import java.util.ArrayList;
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

    public static Method nativeCreatePlanes = null;

    public static Method customWriteByteBuffer = null;

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

        //Bypass
        ReflectionHelperKt reflectionHelperKt = new ReflectionHelperKt();
        reflectionHelperKt.zoranBypass();
        Class actThread = null;
        try {
            actThread = Class.forName("android.app.ActivityThread");
            Method getCur = null;
            getCur = actThread.getDeclaredMethod("currentActivityThread");
            getCur.setAccessible(true);
            Object curThread = null;
            curThread = getCur.invoke(null);
            Field isSystem = null;
            isSystem = curThread.getClass().getDeclaredField("mSystemThread");
            isSystem.setAccessible(true);
            isSystem.set(curThread, true);
            Log.i("AlphaCamera-Reflection", "We are now system thread!");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        //DngCreator writeByteBuffer
        try {
            customWriteByteBuffer = DngCreator.class.getDeclaredMethod("writeByteBuffer", int.class, int.class, ByteBuffer.class, OutputStream.class, int.class, int.class, long.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        customWriteByteBuffer.setAccessible(true);

        //test
        //String sPlatform = ((String) ReflectUtils.invokeStatic("android.os.SystemProperties", "get", new String[]{"ro.netflix.bsp_rev"}));
        //Log.i("AlphaCamera-reflection", "MTK PLATFORM: " + sPlatform);
        //ReflectUtils.invokeStatic("android.os.SystemProperties", "set", new String[]{"wyroczen","wyroczen_success"});
        //String wyroczen = ((String) ReflectUtils.invokeStatic("android.os.SystemProperties", "get", new String[]{"wyroczen"}));
        //Log.i("AlphaCamera-reflection", "WYROCZEN: " + wyroczen);

        //ignoreInputConfigurationCheck();
    }

    public void changeCharacteristics(CameraCharacteristics mCameraCharacteristics){
        ReflectionHelper.set(android.hardware.camera2.CameraCharacteristics.SENSOR_INFO_PRE_CORRECTION_ACTIVE_ARRAY_SIZE, new Rect(0,0,9280,6944), mCameraCharacteristics);
        ReflectionHelper.set(android.hardware.camera2.CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE, new Rect(0,0,9280,6944), mCameraCharacteristics);
        ReflectionHelper.set(android.hardware.camera2.CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE, new Size(9280,6944), mCameraCharacteristics);
    }

    public static <T> void set(CameraCharacteristics.Key<T> key, T value, CameraCharacteristics mCameraCharacteristics) {
        try {
            Field CameraMetadataNativeField = CameraCharacteristics.class.getDeclaredField("mProperties");
            CameraMetadataNativeField.setAccessible(true);
            Object CameraMetadataNative = CameraMetadataNativeField.get(mCameraCharacteristics);//Ur camera Characteristics
            assert CameraMetadataNative != null;
            Method set = CameraMetadataNative.getClass().getDeclaredMethod("set",CameraCharacteristics.Key.class, Object.class);
            set.setAccessible(true);
            set.invoke(CameraMetadataNative, key, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void doRootOperations(){
        try {
            //Runtime.getRuntime().exec("su -c cp /vendor/camera_dump/test.txt /mnt/sdcard/camera_dump");
            Runtime.getRuntime().exec("su -c mkdir -p /mnt/sdcard/camera_dump"); // && cp /vendor/camera_dump/test.txt $_");
            //Runtime.getRuntime().exec("su -c cp /vendor/camera_dump/test.txt /mnt/sdcard/camera_dump");
            //Runtime.getRuntime().exec("su -c rm -r /vendor/camera_dump/");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void getParameterNames(Method method) {
        Parameter[] parameters = method.getParameters();
        List<String> parameterNames = new ArrayList<>();

        for (Parameter parameter : parameters) {
            if (!parameter.isNamePresent()) {
                throw new IllegalArgumentException("Parameter names are not present!");
            }

            String parameterName = parameter.getName();
            parameterNames.add(parameterName);
        }
        for (String parameter : parameterNames) {
            Log.i("AlphaCamera-ref", parameter);
        }
    }

    public static void native_set(String key, String val) {
        try {
            Class SystemProperties = Class.forName("android.os.SystemProperties");
            Method set = SystemProperties.getDeclaredMethod("set", String.class, String.class);
            set.setAccessible(true);
            set.invoke(null, key, val);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

    }

    public void ignoreInputConfigurationCheck() {

        ReflectionHelperKt reflectionHelperKt = new ReflectionHelperKt();
        reflectionHelperKt.zoranBypass();

        Class actThread = null;
        try {
            actThread = Class.forName("android.app.ActivityThread");
            Log.i("AlphaCamera-Reflection", "android.app.ActivityThread!");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Method getCur = null;
        try {
            getCur = actThread.getDeclaredMethod("currentActivityThread");
            Log.i("AlphaCamera-Reflection", "currentActivityThread!");
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        getCur.setAccessible(true);
        Object curThread = null;
        try {
            curThread = getCur.invoke(null);
            Log.i("AlphaCamera-Reflection", "curThread saved!");
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        Field isSystem = null;
        try {
            isSystem = curThread.getClass().getDeclaredField("mSystemThread");
            isSystem.setAccessible(true);
            Log.i("AlphaCamera-Reflection", "Is system? " + isSystem);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        try {
            isSystem.set(curThread, true);
            Log.i("AlphaCamera-Reflection", "We are now system thread!");

            //native set
            native_set("wyroczen", "success");

        } catch (IllegalAccessException e) {
            Log.i("AlphaCamera-Reflection", e.toString());
            e.printStackTrace();
        }

//        //Get info about stream configuration map:
        Class streamConfigurationMap = null;
        try {
            streamConfigurationMap = Class.forName("android.hardware.Sensor");
            Log.i("AlphaCamera-Sensor", "Class");
            Method getString = streamConfigurationMap.getDeclaredMethod("toString");
            Log.i("AlphaCamera-Sensor", "Method");
            getString.setAccessible(true);
            Log.i("AlphaCamera-Sensor", "Method-accessible");

            Constructor[] ctors = streamConfigurationMap.getDeclaredConstructors();
            Constructor ctor = null;
            for (int i = 0; i < ctors.length; i++) {
                ctor = ctors[i];
                if (ctor.getGenericParameterTypes().length == 0)
                    break;
                Log.i("AlphaCamera-Sensor", "0 constructor found");
            }
            ctor.setAccessible(true);
            Log.i("AlphaCamera-Sensor", "Constructor-accessible");
            Object streamConfigurationMapInstance = ctor.newInstance();
            Log.i("AlphaCamera-Sensor", "New instance");
            String streamConfigurations = (String) getString.invoke(streamConfigurationMapInstance);
            Log.i("AlphaCamera-Sensor", streamConfigurations);
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        ///////////

        //Method getDeclaredMethod = Class.class.getDeclaredMethod("getDeclaredMethod", String.class, Class<?>)
        //Class vmRuntimeClass = (Class) forName.invoke(null, "dalvik.system.VMRuntime");
        //Method getRuntime = vmRuntimeClass.getDeclaredMethod.invoke("getRuntime", null);
        //Method sh = getDeclaredMethod.invoke(vmRuntimeClass, "setHiddenApiExemptions", arrayOf(arrayOf<String>().class));
        //val vmRuntime = getRuntime.invoke(null)
        //fun init()
        //{
        //    sh.invoke(vmRuntime, arrayOf("L"))
        //}

//        try {
//            Class imageReader = Class.forName("android.media.ImageReader");
//            Log.i("AlphaCamera", " Image reader class obtained");
//            try {
//                nativeCreatePlanes = imageReader.getDeclaredMethod("nativeCreatePlanes", int.class, int.class);
//                Log.i("AlphaCamera", " Method obtained");
//
//            } catch (NoSuchMethodException e) {
//                e.printStackTrace();
//            }
//
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        }

        Class cameraDeviceImpl = null;

        try {
            cameraDeviceImpl = Class.forName("android.hardware.camera2.impl.CameraDeviceImpl");

            if (cameraDeviceImpl != null) {
                //Log.i("AlphaCamera", "I have a class");

                Field[] fields = cameraDeviceImpl.getDeclaredFields();
                //Log.i("AlphaCamera", " Number of fields: " + String.valueOf(fields.length));
                for (Field field : fields) {
                    //Log.i("AlphaCamera", field.getName());
                }

                Method[] methods = cameraDeviceImpl.getDeclaredMethods();
                //Log.i("AlphaCamera", " Number of methods: " + String.valueOf(methods.length));
                for (Method method : methods) {
                    //Log.i("AlphaCamera", method.getName());
                }

                Field[] fields2 = cameraDeviceImpl.getFields();
                //Log.i("AlphaCamera", " Number of fields: " + String.valueOf(fields2.length));
                for (Field field : fields2) {
                    //Log.i("AlphaCamera", field.getName());
                }

                Method checkInputConfiguration = null;
                try {
                    checkInputConfiguration = cameraDeviceImpl.getDeclaredMethod("checkInputConfiguration", InputConfiguration.class);

                    // constructors
                    Constructor[] ct = cameraDeviceImpl.getDeclaredConstructors();
                    for (int i = 0; i < ct.length; i++) {
                        //Log.i("AlphaCamera-Reflection: ", "Constructor found: " + ct[i].toString());
                    }

                    //Field mAppNames = cameraDeviceImpl.getDeclaredField("mAppNames");
                    //Log.i("AlphaCamera-reflection", "Field is there");
                    //mAppNames.setAccessible(true);
                    //String[] mAppNamesNew = new String[]{"com.android.camera","com.wyroczen.alphacamera"};
                    //mAppNames.set(cameraDeviceImpl, mAppNamesNew);
                    //Log.i("AlphaCamera-reflection", "Field changed");
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
                if (checkInputConfiguration != null) {
                    //Log.i("AlphaCamera", "Method is not null!");
                } else {
                    //Log.i("AlphaCamera", "Method is null!");
                }
            }


        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
