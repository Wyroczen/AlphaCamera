package com.wyroczen.alphacamera;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.wyroczen.alphacamera.metadata.ExifHelper;

import java.io.File;
import java.io.IOException;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private final String TAG = "AlphaCamera-Maps";
    private File[] files;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Load photos from dir
        //String path = Environment.getExternalStorageDirectory().toString()+"/Pictures"; //TODO change default photos saving dir
        String path = getExternalFilesDir(null).toString() + "/Pictures";
        Log.i(TAG, "Files Path: " + path);
        File directory = new File(path);
        files = directory.listFiles();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * <p>
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions()
//                .position(sydney)
//                .title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        if (files != null) {
            Log.i(TAG, "Files Size: " + files.length);
            for (int i = 0; i < files.length; i++) {
                Log.i(TAG, "Files FileName:" + files[i].getName());
                try {
                    String[] gpsData = ExifHelper.getMetadata(files[i]);
                    Log.i(TAG, "GPS DATA: " + gpsData[0] + " " + gpsData[1]);
                    LatLng location = new LatLng(Double.parseDouble(gpsData[0]), Double.parseDouble(gpsData[1]));
                    mMap.addMarker(new MarkerOptions()
                            .position(location)
                            .title("Marker"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(location));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
