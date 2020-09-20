package com.wyroczen.alphacamera;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.ThumbnailUtils;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.wyroczen.alphacamera.metadata.ExifHelper;

import java.io.File;
import java.io.IOException;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private final String TAG = "AlphaCamera-Maps";
    private File[] pictureFiles;
    private File[] videoFiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Load photos from dir
        //String path = Environment.getExternalStorageDirectory().toString()+"/Pictures";
        String picturesPath = getExternalFilesDir(null).toString() + "/Pictures";
        String videosPath = getExternalFilesDir(null).toString() + "/video";
        Log.i(TAG, "Pictures Path: " + picturesPath);
        Log.i(TAG, "Videos Path: " + videosPath);
        File directoryPics = new File(picturesPath);
        pictureFiles = directoryPics.listFiles();
        File directoryVids = new File(videosPath);
        videoFiles = directoryVids.listFiles();
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

        if (pictureFiles != null) {
            Log.i(TAG, "Files (Pic) Size: " + pictureFiles.length);
            for (int i = 0; i < pictureFiles.length; i++) {
                Log.i(TAG, "Files FileName:" + pictureFiles[i].getName());
                try {
                    //Thumbnail
                    int height = 70;
                    int width = 70;
                    Bitmap bitmap = BitmapFactory.decodeFile(pictureFiles[i].getCanonicalPath());
                    Bitmap smallMarker = Bitmap.createScaledBitmap(bitmap, width, height, false);
                    //
                    String[] gpsData = ExifHelper.getMetadata(pictureFiles[i]);
                    Log.i(TAG, "PICTURE GPS DATA: " + gpsData[0] + " " + gpsData[1]);
                    LatLng location = new LatLng(Double.parseDouble(gpsData[0]), Double.parseDouble(gpsData[1]));
                    mMap.addMarker(new MarkerOptions()
                            .position(location)
                            .title(gpsData[2]) //Does that make any sense? TODO video code is better probably
                            //.icon(BitmapDescriptorFactory.fromPath(files[i].getCanonicalPath())));
                            .icon(BitmapDescriptorFactory.fromBitmap(smallMarker)));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(location));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        if (videoFiles != null) {
            Log.i(TAG, "Files (Vid) Size: " + videoFiles.length);
            for (int i = 0; i < videoFiles.length; i++) {
                Log.i(TAG, "Files FileName:" + videoFiles[i].getName());
                try {
                    //Thumbnail
                    int height = 70;
                    int width = 70;
                    Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(videoFiles[i].getCanonicalPath(),
                            MediaStore.Images.Thumbnails.MINI_KIND);
                    Bitmap smallMarker = Bitmap.createScaledBitmap(bitmap, width, height, false);
                    //
                    String[] gpsData = ExifHelper.getVideoMetadata(videoFiles[i]);
                    Log.i(TAG, "VIDEO GPS DATA: " + gpsData[0] + " " + gpsData[1]);
                    LatLng location = new LatLng(Double.parseDouble(gpsData[0]), Double.parseDouble(gpsData[1]));
                    mMap.addMarker(new MarkerOptions()
                            .position(location)
                            .title(videoFiles[i].getName())
                            .icon(BitmapDescriptorFactory.fromBitmap(smallMarker)));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(location));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
