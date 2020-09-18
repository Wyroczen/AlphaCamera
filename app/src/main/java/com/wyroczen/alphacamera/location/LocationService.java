package com.wyroczen.alphacamera.location;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class LocationService extends Service {

    private GoogleLocationService googleLocationService;

    private final String TAG = "AlphaCamera-Service";

    private double longitude;
    private double latitude;

    @Override
    public void onCreate() {
        super.onCreate();
        //start the handler for getting locations
        //create component
        updateLocation(getApplicationContext());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    //get current location os user
    private void updateLocation(Context context) {
        googleLocationService = new GoogleLocationService(context, new LocationUpdateListener() {
            @Override
            public void canReceiveLocationUpdates() {
            }

            @Override
            public void cannotReceiveLocationUpdates() {
            }

            //update location to our servers for tracking purpose
            @Override
            public void updateLocation(Location location) {
                if (location != null ) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    Log.i(TAG, "updated location " +  latitude + " " + longitude);

                    Intent retIntent = new Intent("GEO_DATA");
                    String geoDataValue = latitude + " " + longitude;
                    retIntent.putExtra("geoDataValues", geoDataValue);
                    sendBroadcast(retIntent);
                }
            }

            @Override
            public void updateLocationName(String localityName, Location location) {

                googleLocationService.stopLocationUpdates();
            }
        });
        googleLocationService.startUpdates();
    }


    IBinder mBinder = new LocalBinder();


    public class LocalBinder extends Binder {
        public LocationService getServerInstance() {
            return LocationService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    //stop location updates on stopping the service
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (googleLocationService != null) {
            googleLocationService.stopLocationUpdates();
        }
    }
}