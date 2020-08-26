package com.wyroczen.alphacamera;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Size;
import android.view.View;

public class SettingsUtils {

    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener;

    public static final String PREF_ENABLE_RAW_KEY = "pref_enableRaw_key";
    public static final String PREF_RESOLUTION_BACK_KEY = "pref_resolution_backLens";

    public Boolean readBooleanSettings(Context context, String key)
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        Boolean result = sharedPreferences.getBoolean(key, false);
        return result;
    }

    public Size readSizeSettings(Context context, String key)
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String result = sharedPreferences.getString(key, "4640x3472");
        if(result.equals("")) result = "4640x3472";
        Log.i("AlphaCamera"," Split: " + result + " " + result.split("x")[0]);
        Integer width = Integer.valueOf(result.split("x")[0]);
        Integer height = Integer.valueOf(result.split("x")[1]);
        return new Size(width,height);
    }

}
