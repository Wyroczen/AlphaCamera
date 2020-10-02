package com.wyroczen.alphacamera;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Size;
import android.view.View;

import java.util.Arrays;
import java.util.Collections;

public class SettingsUtils {

    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener;

    //public static final String PREF_ENABLE_RAW_KEY = "pref_enableRaw_key";
    public static final String PREF_IMAGE_FORMAT_KEY = "pref_imageFormat_key";
    public static final String PREF_RESOLUTION_BACK_KEY = "pref_resolution_backLens";
    public static final String PREF_MAX_BRIGHTNESS_KEY = "pref_maxBrightness_key";
    public static final String PREF_TAP_TO_CAPTURE_KEY = "pref_tapToCapture_key";
    public static final String PREF_FRONT_FLIP_KEY = "pref_frontFlip_key";
    public static final String PREF_SHUTTER_SOUND_KEY = "pref_shutterSound_key";
    public static final String PREF_ANTIBANDING_MODE_KEY = "pref_antibanding_mode_key";
    public static final String PREF_STREAM_CONFIG_KEY = "pref_streamConfig_key";

    public Boolean readBooleanSettings(Context context, String key) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        Boolean result = sharedPreferences.getBoolean(key, false);
        return result;
    }

    public int readIntSettings(Context context, String key) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        int result = sharedPreferences.getInt(key, 0);
        return result;
    }

    public String readStringSettings(Context context, String key) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String result = sharedPreferences.getString(key, "0");
        return result;
    }

    public Size readSizeSettings(Context context, String key) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String result = sharedPreferences.getString(key, "4640x3472");
        if (result.equals("")) {
            return new Size(0, 0);
        }
        Log.i("AlphaCamera", " Split: " + result + " " + result.split("x")[0]);
        Integer width = Integer.valueOf(result.split("x")[0]);
        Integer height = Integer.valueOf(result.split("x")[1]);
        return new Size(width, height);
    }

}
