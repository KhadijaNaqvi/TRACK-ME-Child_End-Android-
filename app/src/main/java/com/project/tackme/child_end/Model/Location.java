package com.project.tackme.child_end.Model;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.project.tackme.child_end.MainActivity;

import java.util.Timer;
import java.util.TimerTask;

public class Location extends Service implements LocationListener {
    private String latitude;
    private String longitude;
    private LocationManager locationManager;
    private android.location.Location location;
    private Intent intent;
    private Timer mTimer = null;
    private Handler mHandler;
    long notify_interval = 10;
    private Context context;
    private LocationListener locationListener;


    public Location(Context context,LocationListener locationListener) {
     this.context=context;
     this.locationListener=locationListener;




    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(@NonNull android.location.Location location) {


    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {

    }

    public String getLatitude() {
        fn_getlocation();
      return  latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mTimer = new Timer();
        mTimer.schedule(new TimerTaskToGetLocation(), 0, notify_interval);
    }
    private class TimerTaskToGetLocation extends TimerTask {
        @Override
        public void run() {

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    fn_getlocation();
                    Log.e( "loop: ",""+latitude );
                }
            });

        }
    }

    public void fn_getlocation() {
                try {
                    locationManager = (LocationManager) context.getApplicationContext().getSystemService(LOCATION_SERVICE);
                    location = null;
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 0, locationListener);
                    if (locationManager != null) {
                       // location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        location=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        longitude= String.valueOf(location.getLongitude());
                        latitude=String.valueOf(location.getLatitude());
                        Log.e( "fn_getlocation: ",""+latitude );}
                    //Toast.makeText(context, "TEST", Toast.LENGTH_SHORT).show();
                }catch (Exception e){
                    e.printStackTrace();
                }



            }

    }


