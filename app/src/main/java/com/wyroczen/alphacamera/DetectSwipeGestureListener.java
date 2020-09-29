package com.wyroczen.alphacamera;

import android.content.Intent;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.wyroczen.alphacamera.gallery.GalleryActivity;
import com.wyroczen.alphacamera.gallery.GalleryAdapter;

import java.util.Map;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

public class DetectSwipeGestureListener extends GestureDetector.SimpleOnGestureListener {
    private static int MIN_SWIPE_DISTANCE_X = 100;
    private static int MIN_SWIPE_DISTANCE_Y = 100;

    private static int MAX_SWIPE_DISTANCE_X = 1000;
    private static int MAX_SWIPE_DISTANCE_Y = 1000;

    private CameraActivity activityCamera = null;
    private CameraVideoActivity activityCameraVideo = null;
    private MapsActivity activityMaps = null;

    public void setActivityCamera(CameraActivity cameraActivity) {
        this.activityCamera = cameraActivity;
    }

    public void setActivityCameraVideo(CameraVideoActivity cameraVideoActivity) {
        this.activityCameraVideo = cameraVideoActivity;
    }

    public void setActivityMaps(MapsActivity mapsActivity) {
        this.activityMaps = mapsActivity;
    }

    public CameraActivity getActivityCamera() {
        return this.activityCamera;
    }

    public CameraVideoActivity getActivityCameraVideo() {
        return this.activityCameraVideo;
    }

    public MapsActivity getActivityMaps() {
        return this.activityMaps;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        float deltaX = e1.getX() - e2.getX();
        float deltaY = e1.getY() - e2.getY();

        float deltaXAbs = Math.abs(deltaX);
        float deltaYAbs = Math.abs(deltaY);

        if (deltaXAbs >= MIN_SWIPE_DISTANCE_X && deltaXAbs <= MAX_SWIPE_DISTANCE_X) {
            if (deltaX > 0) {
                Log.i("AlphaCamera", "Swipe to left");
                //Start video activity
                if (activityCamera != null) {

                    //Get info about currently used lens to pass it through to video activity
                    CameraFragment cameraFragment = (CameraFragment) activityCamera.getSupportFragmentManager().findFragmentById(R.id.container);
                    String cameraId = cameraFragment.getmCameraId();

                    Intent i = new Intent(activityCamera, CameraVideoActivity.class);
                    i.putExtra("CAMERA_ID", cameraId);
                    i.putExtra("LATITUDE", String.valueOf(activityCamera.latitude));
                    i.putExtra("LONGITUDE", String.valueOf(activityCamera.longitude));
                    activityCamera.startActivity(i);
                }
            } else {
                Log.i("AlphaCamera", "Swipe to right");

                if (activityCamera != null) {
                    Intent i = new Intent(activityCamera, MapsActivity.class);
                    activityCamera.startActivity(i);
                }

                if (activityCameraVideo != null) {
                    Intent i = new Intent(activityCameraVideo, CameraActivity.class);
                    activityCameraVideo.startActivity(i);
                }
            }
        }

        if (deltaYAbs >= MIN_SWIPE_DISTANCE_Y && deltaYAbs <= MAX_SWIPE_DISTANCE_Y) {
            if (deltaY > 0) {
                Log.i("AlphaCamera", "Swipe to top");
            } else {
                Log.i("AlphaCamera", "Swipe to down");
            }
        }

        return true;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        //Tap to capture photo
        SettingsUtils settingsUtils = new SettingsUtils();
        if (activityCamera != null) {
            CameraFragment cameraFragment = (CameraFragment) activityCamera.getSupportFragmentManager().findFragmentById(R.id.container);
            Boolean tapToCapture = settingsUtils.readBooleanSettings(cameraFragment.getContext(), SettingsUtils.PREF_TAP_TO_CAPTURE_KEY);
            if (tapToCapture)
                cameraFragment.takePicture();
        }
        Log.i("AlphaCamera", "Single tap");
        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        Log.i("AlphaCamera", "Double tap");
        if (activityCamera != null) {
            Intent i = new Intent(activityCamera, GalleryActivity.class);
            activityCamera.startActivity(i);
        }
        return true;
    }

}
