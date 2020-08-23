package com.wyroczen.alphacamera;

import android.content.Intent;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

public class DetectSwipeGestureListener extends GestureDetector.SimpleOnGestureListener {
    private static int MIN_SWIPE_DISTANCE_X = 100;
    private static int MIN_SWIPE_DISTANCE_Y = 100;

    private static int MAX_SWIPE_DISTANCE_X = 1000;
    private static int MAX_SWIPE_DISTANCE_Y = 1000;

    private CameraActivity activityCamera = null;
    private CameraVideoActivity activityCameraVideo = null;

    public void setActivityCamera(CameraActivity cameraActivity){
        this.activityCamera = cameraActivity;
    }

    public void setActivityCameraVideo(CameraVideoActivity cameraVideoActivity){
        this.activityCameraVideo = cameraVideoActivity;
    }

    public CameraActivity getActivityCamera(){
        return this.activityCamera;
    }

    public CameraVideoActivity getActivityCameraVideo(){
        return this.activityCameraVideo;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY){
        float deltaX = e1.getX() - e2.getX();
        float deltaY = e1.getY() - e2.getY();

        float deltaXAbs = Math.abs(deltaX);
        float deltaYAbs = Math.abs(deltaY);

        if(deltaXAbs >= MIN_SWIPE_DISTANCE_X && deltaXAbs <= MAX_SWIPE_DISTANCE_X) {
            if(deltaX > 0){
                Log.i("AlphaCamera", "Swipe to left");
                //Start video activity
                Intent i = new Intent(activityCamera, CameraVideoActivity.class);
                activityCamera.startActivity(i);
            } else {
                Log.i("AlphaCamera", "Swipe to right");
                Intent i = new Intent(activityCameraVideo, CameraActivity.class);
                activityCameraVideo.startActivity(i);
            }
        }

        if(deltaYAbs >= MIN_SWIPE_DISTANCE_Y && deltaYAbs <= MAX_SWIPE_DISTANCE_Y) {
            if(deltaY > 0){
                Log.i("AlphaCamera", "Swipe to top");
            } else {
                Log.i("AlphaCamera", "Swipe to down");
            }
        }

        return true;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e){
        Log.i("AlphaCamera", "Single tap");
        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e){
        Log.i("AlphaCamera", "Double tap");
        return true;
    }

}
