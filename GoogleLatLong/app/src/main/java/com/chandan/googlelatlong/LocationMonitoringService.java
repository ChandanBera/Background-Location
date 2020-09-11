package com.chandan.googlelatlong;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.text.SimpleDateFormat;
import java.util.Date;

public class LocationMonitoringService extends Service implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener {


    private static final String TAG = "SRMB SRMB SERVICE";
    private static final String TAG2 = "vvvvvvvvvvvvvvvvvv";
    GoogleApiClient mLocationClient;
    LocationRequest mLocationRequest = new LocationRequest();


    public static final String ACTION_LOCATION_BROADCAST = LocationMonitoringService.class.getName() + "LocationBroadcast";
    public static final String EXTRA_LATITUDE = "extra_latitude";
    public static final String EXTRA_LONGITUDE = "extra_longitude";
    public static final String EXTRA_TIME = "extra_time";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            mLocationClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();


            mLocationRequest.setInterval(Constants.LOCATION_INTERVAL);
            mLocationRequest.setFastestInterval(Constants.FASTEST_LOCATION_INTERVAL);


            int priority = LocationRequest.PRIORITY_HIGH_ACCURACY; //by default
            //PRIORITY_BALANCED_POWER_ACCURACY, PRIORITY_LOW_POWER, PRIORITY_NO_POWER are the other priority modes


            mLocationRequest.setPriority(priority);
            mLocationClient.connect();

            //Make it stick to the notification panel so it is less prone to get cancelled by the Operating System.
            return START_STICKY;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return START_STICKY_COMPATIBILITY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /*
     * LOCATION CALLBACKS
     */
    @Override
    public void onConnected(Bundle dataBundle) {
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.

                Log.d(TAG, "== Error On onConnected() Permission not granted");
                //Permission not granted by user so cancel the further execution.

                return;
            }
            LocationServices.FusedLocationApi.requestLocationUpdates(mLocationClient, mLocationRequest, this);

            Log.d(TAG, "Connected to Google API");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * Called by Location Services if the connection to the
     * location client drops because of an error.
     */
    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "Connection suspended");
    }


    //to get the location change
    @Override
    public void onLocationChanged(Location location) {
        try {
            Log.d(TAG, "Location changed");

            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss z");
            String currentDateAndTime = sdf.format(new Date());
            Log.d(TAG,currentDateAndTime+"---");

            if (location != null) {
                Log.d(TAG, "Latitude : "+location.getLatitude()+"  "+"Longitude : "+location.getLongitude());
                Toast.makeText(getApplicationContext(),"Latitude : "+location.getLatitude()+"  "+"Longitude : "+location.getLongitude(),Toast.LENGTH_LONG).show();
                //Send result to activities
                sendMessageToUI(String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()),currentDateAndTime);
            }else {
                Log.d(TAG, "location == null");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void sendMessageToUI(String lat, String lng, String currentTime) {

        try {
            Log.d(TAG, "Sending info...");

            Intent intent = new Intent(ACTION_LOCATION_BROADCAST);
            intent.putExtra(EXTRA_LATITUDE, lat);
            intent.putExtra(EXTRA_LONGITUDE, lng);
            intent.putExtra(EXTRA_TIME, currentTime);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "Failed to connect to Google API");

    }


}
