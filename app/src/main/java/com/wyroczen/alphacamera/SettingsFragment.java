package com.wyroczen.alphacamera;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.util.Size;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.view.menu.ListMenuPresenter;

public class SettingsFragment extends PreferenceFragment {

    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        try {
            final ListPreference resulutionBackListPreference = (ListPreference) findPreference("pref_resolution_backLens");
            setResolutionListPreferenceData(resulutionBackListPreference);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        preferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(preferenceChangeListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(preferenceChangeListener);
    }

    public void setResolutionListPreferenceData(ListPreference resulutionBackListPreference) throws CameraAccessException {



        Activity activity = getActivity();

        Bundle extras = activity.getIntent().getExtras();
        String mCameraId = "0";
        if (extras != null) {
            mCameraId = extras.getString("mCameraId");
        }

        CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        CameraCharacteristics characteristics
                = manager.getCameraCharacteristics(mCameraId);
        StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

        Size[] outputSizes = map.getOutputSizes(ImageFormat.JPEG);
        CharSequence[] entries = new CharSequence[outputSizes.length];
        CharSequence[] entryValues = new CharSequence[outputSizes.length];

        int i = 0;
        for(Size size : outputSizes){
            Log.i("AlphaCamera", "Size: " + size.toString());
            entries[i] = size.toString();
            entryValues[i] = size.toString();
            i++;
        }
        resulutionBackListPreference.setEntries(entries);
        resulutionBackListPreference.setEntryValues(entryValues);
    }
}
