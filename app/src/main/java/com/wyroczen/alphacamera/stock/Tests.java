package com.wyroczen.alphacamera.stock;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.params.StreamConfiguration;
import android.hardware.camera2.params.StreamConfigurationDuration;
import android.util.Log;
import android.util.Size;
import android.view.Surface;

import com.wyroczen.alphacamera.reflection.ReflectionHelper;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

public class Tests {

    private String mCameraId;
    private Integer mLegacyCameraId;

    public static void clearSurfaces(Set<Surface> set) {
        for (Surface s : set) {
            s.release();
        }
    }

    public static String[] getCameraIdList() {
        return new String[]{"0", "1", "20", "21", "22", "61"};
    }

    public static int getNumberOfCameras() {
        return 6;
    }

    public static Application getApplicationUsingReflection() throws Exception {
        return (Application) Class.forName("android.app.ActivityThread")
                .getMethod("currentApplication").invoke(null, (Object[]) null);
    }

    public static void logCameraId(String id) {
        Log.i("Wyroczen", "Camera ID: " + id);
    }



    public static void switchBackCamera() {

        try {
            Application app = getApplicationUsingReflection();

            Log.i("Wyroczen", "App obtained!");

            SharedPreferences prefs = app.getApplicationContext().getSharedPreferences(
                    "com.example.app", Context.MODE_PRIVATE);

            Log.i("Wyroczen", "Prefs obtained!");

            String mCameraId = prefs.getString("camId", "0");

            Log.i("Wyroczen", "camId obtained!");

            if (mCameraId.equals("0")) {
                mCameraId = "21";
                prefs.edit().putString("camId", mCameraId).apply();
                Log.i("Wyroczen", "CamId: 21");
            } else if (mCameraId.equals("21")) {
                mCameraId = "22";
                prefs.edit().putString("camId", mCameraId).apply();
                Log.i("Wyroczen", "CamId: 22");
            } else if (mCameraId.equals("22")) {
                mCameraId = "0";
                prefs.edit().putString("camId", mCameraId).apply();
                Log.i("Wyroczen", "CamId: 0");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getCameraIdFromPrefs() {
        try {
            Application app = getApplicationUsingReflection();

            SharedPreferences prefs = app.getApplicationContext().getSharedPreferences(
                    "com.example.app", Context.MODE_PRIVATE);

            String mCameraId = prefs.getString("camId", "0");
            Log.i("Wyroczen", "From prefs camId: " + mCameraId);

            if (mCameraId == null) {
                mCameraId = "0";
            }

            return mCameraId;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "0";
    }

    public static void doRestart() {
        Application app = null;
        try {
            app = getApplicationUsingReflection();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Intent i = app.getBaseContext().getPackageManager().
                getLaunchIntentForPackage(app.getBaseContext().getPackageName());
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        app.startActivity(i);
    }

    public static void doRestartOld() {

        try {
            Application app = getApplicationUsingReflection();
            Context c = app.getApplicationContext();

            //check if the context is given
            if (c != null) {
                //fetch the packagemanager so we can get the default launch activity
                // (you can replace this intent with any other activity if you want
                PackageManager pm = c.getPackageManager();
                //check if we got the PackageManager
                if (pm != null) {
                    //create the intent with the default start activity for your application
                    Intent mStartActivity = pm.getLaunchIntentForPackage(
                            c.getPackageName()
                    );
                    if (mStartActivity != null) {
                        mStartActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        //create a pending intent so the application is restarted after System.exit(0) was called.
                        // We use an AlarmManager to call this intent in 100ms
                        int mPendingIntentId = 223344;
                        PendingIntent mPendingIntent = PendingIntent
                                .getActivity(c, mPendingIntentId, mStartActivity,
                                        PendingIntent.FLAG_CANCEL_CURRENT);
                        AlarmManager mgr = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
                        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
                        //kill the application
                        System.exit(0);
                    } else {
                        Log.e("Wyroczen", "Was not able to restart application, mStartActivity null");
                    }
                } else {
                    Log.e("Wyroczen", "Was not able to restart application, PM null");
                }
            } else {
                Log.e("Wyroczen", "Was not able to restart application, Context null");
            }
        } catch (Exception ex) {
            Log.e("Wyroczen", "Was not able to restart application");
        }
    }

    public static String getCameraIdIfNotFront(String id) {
        if (id.equals("1")) {
            return "1";
        } else {
            String nid = getCameraIdFromPrefs();

            return nid;
        }
    }

    private void CameraId() {
        String str = getCameraIdFromPrefs();
        this.mCameraId = str;
        this.mLegacyCameraId = Integer.valueOf(str);
    }

    public static void changeCharacteristics(CameraCharacteristics mCameraCharacteristics){
        Tests.set(android.hardware.camera2.CameraCharacteristics.SENSOR_INFO_PRE_CORRECTION_ACTIVE_ARRAY_SIZE, new Rect(0,0,4640,3472), mCameraCharacteristics);
        Tests.set(android.hardware.camera2.CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE, new Rect(0,0,4640,3472), mCameraCharacteristics);
        Tests.set(android.hardware.camera2.CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE, new Size(4640,3472), mCameraCharacteristics);

        CameraCharacteristics.Key<StreamConfigurationDuration[]> SCALER_AVAILABLE_MIN_FRAME_DURATIONS =
                getCameraCharacteristicsKey("android.scaler.availableMinFrameDurations",
                        StreamConfigurationDuration[].class);
        CameraCharacteristics.Key<StreamConfiguration[]> SCALER_AVAILABLE_STREAM_CONFIGURATIONS =
                getCameraCharacteristicsKey("android.scaler.availableStreamConfigurations", StreamConfiguration[].class);

        StreamConfiguration[] mapp = mCameraCharacteristics.get(SCALER_AVAILABLE_STREAM_CONFIGURATIONS);
        StreamConfigurationDuration[] durmap = mCameraCharacteristics.get(SCALER_AVAILABLE_MIN_FRAME_DURATIONS);

        int cnt2 = 0;
        int nformat;
        boolean patched = false;
        for(int i =0; i<mapp.length;i++){
            if(mapp[i].getHeight()*mapp[i].getWidth() <= 30000){
                switch (cnt2){
                    case 0:
                        nformat = 35;
                        mapp[i] = new StreamConfiguration(nformat,9248,6936,false);
                        cnt2++;
                        break;
                    case 1:
                        nformat = 33;
                        mapp[i] = new StreamConfiguration(nformat,9248,6936,false);
                        cnt2++;
                    case 2:
                        patched = true;
                        break;
                }
            }
            if(patched) {
                patched = false;
                cnt2 = 0;
                break;
            }
        }
        for(int i =0; i<durmap.length;i++){
            if(durmap[i].getHeight()*durmap[i].getWidth() <= 30000){
                switch (cnt2){
                    case 0:
                        nformat = 35;
                        durmap[i] = new StreamConfigurationDuration(nformat,9248,6936,50000000);
                        cnt2++;
                        break;
                    case 1:
                        nformat = 33;
                        durmap[i] = new StreamConfigurationDuration(nformat,9248,6936,50000000);
                        cnt2++;
                        break;
                    case 2:
                        patched = true;
                        break;
                }
            }
            if(patched) {
                patched = false;
                cnt2 = 0;
                break;
            }
        }
        ReflectionHelper.set(SCALER_AVAILABLE_MIN_FRAME_DURATIONS, durmap, mCameraCharacteristics);
        ReflectionHelper.set(SCALER_AVAILABLE_STREAM_CONFIGURATIONS, mapp, mCameraCharacteristics);
    }

    public static CameraCharacteristics.Key getCameraCharacteristicsKey(final String s, final Class clazz) {
        try {

            final Constructor constructor = CameraCharacteristics.Key.class.getDeclaredConstructors()[2];
            constructor.setAccessible(true);
            return (CameraCharacteristics.Key) constructor.newInstance(new Object[]{s, clazz});

        } catch (InstantiationException e) {
            e.printStackTrace();
            return null;
        } catch (IllegalAccessException e2) {
            e2.printStackTrace();
            return null;
        } catch (InvocationTargetException e3) {
            e3.printStackTrace();
            return null;
        }
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

    public static void checkSizes(int a, int b, int c, int d){
        if((a == 6000 || b == 6000) && c == 35){
            a = 3000;
            b = 4000;
        }
    }


}
