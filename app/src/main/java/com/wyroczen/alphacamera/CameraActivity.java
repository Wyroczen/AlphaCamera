package com.wyroczen.alphacamera;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.wyroczen.alphacamera.location.LocationService;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GestureDetectorCompat;

import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

public class CameraActivity extends AppCompatActivity {

    private final String TAG = "AlphaCamera-CamAvtivity";
    private GestureDetectorCompat gestureDetectorCompat = null;
    private BroadcastReceiver receiver;
    public double latitude;
    public double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        //Location
        IntentFilter filter = new IntentFilter();
        filter.addAction("GEO_DATA");

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if(action.equals("GEO_DATA")){
                    String valueFromService = intent.getStringExtra("geoDataValues");
                    Log.i(TAG, "Recieved data: " + valueFromService);
                    String[] values = valueFromService.split("\\s+");
                    latitude = Double.parseDouble(values[0]);
                    longitude = Double.parseDouble(values[1]);
                }
            }
        };
        registerReceiver(receiver, filter);

        startService(new Intent(this, LocationService.class));

        DetectSwipeGestureListener gestureListener = new DetectSwipeGestureListener();
        gestureListener.setActivityCamera(this);
        gestureDetectorCompat = new GestureDetectorCompat(this, gestureListener);

        //Toolbar toolbar = findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        //Default settings saved into sharedPreferences file
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);


//        FloatingActionButton fab = findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        //Fragment
        //setContentView(R.layout.activity_camera);In
        if (null == savedInstanceState) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, CameraFragment.newInstance())
                    .commit();
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        gestureDetectorCompat.onTouchEvent(event);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onPause(){
        super.onPause();
        stopService(new Intent(this, LocationService.class));
    }

    @Override
    protected void onDestroy() {
        stopService(new Intent(this, LocationService.class));
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }
        super.onDestroy();
    }


//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            //return true;
//            startActivity(new Intent(this, SettingsActivity.class));
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
}