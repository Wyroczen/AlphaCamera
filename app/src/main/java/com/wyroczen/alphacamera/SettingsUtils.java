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

    public static final String PREF_ENABLE_RAW_KEY = "pref_enableRaw_key";
    public static final String PREF_RESOLUTION_BACK_KEY = "pref_resolution_backLens";

    public Boolean readBooleanSettings(Context context, String key) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        Boolean result = sharedPreferences.getBoolean(key, false);
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
