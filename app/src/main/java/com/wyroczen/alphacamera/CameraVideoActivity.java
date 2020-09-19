package com.wyroczen.alphacamera;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.wyroczen.alphacamera.location.LocationService;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GestureDetectorCompat;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class CameraVideoActivity extends AppCompatActivity {

    private final String TAG = "AlphaCamera-CamVidAvtivity";
    private GestureDetectorCompat gestureDetectorCompat = null;
    private BroadcastReceiver receiver;
    public float latitude;
    public float longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_video);

        //Location //TODO REWORK REDUNDANT CODE
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
                    latitude = Float.parseFloat(values[0]);
                    longitude = Float.parseFloat(values[1]);
                }
            }
        };
        registerReceiver(receiver, filter);

        startService(new Intent(this, LocationService.class));
        ///////////////////////////////////////////////////////////////////////////////

        DetectSwipeGestureListener gestureListener = new DetectSwipeGestureListener();
        gestureListener.setActivityCameraVideo(this);
        gestureDetectorCompat = new GestureDetectorCompat(this, gestureListener);

        if (null == savedInstanceState) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, CameraVideoFragment.newInstance())
                    .commit();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        gestureDetectorCompat.onTouchEvent(event);
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
}