package com.wyroczen.alphacamera;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;

public class SettingsUtils {

    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener;

    public static final String PREF_ENABLE_RAW_KEY = "pref_enableRaw_key";

    public Boolean readBooleanSettings(Context context, String key)
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        Boolean result = sharedPreferences.getBoolean(key, false);
        return result;
    }

}
