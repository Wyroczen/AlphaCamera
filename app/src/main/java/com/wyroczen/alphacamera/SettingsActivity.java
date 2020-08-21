package com.wyroczen.alphacamera;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toolbar;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        if(findViewById(R.id.settings_fragment_container) != null)
        {
            if(savedInstanceState != null)
                return;

            getFragmentManager().beginTransaction().add(R.id.settings_fragment_container,
                    new SettingsFragment()).commit();

        }
    }

    @Override
    public void onBackPressed() {
        //todo
    }
}