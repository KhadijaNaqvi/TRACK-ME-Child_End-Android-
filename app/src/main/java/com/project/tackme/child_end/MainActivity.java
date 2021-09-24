package com.project.tackme.child_end;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.project.tackme.child_end.BackgroundServices.Common;
import com.project.tackme.child_end.BackgroundServices.MyBackGroundServices;
import com.project.tackme.child_end.BackgroundServices.SendLocationToActivity;
import com.project.tackme.child_end.Model.Child;
import com.project.tackme.child_end.Model.Date;
import com.project.tackme.child_end.Model.Location;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;
import java.util.Timer;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private Button mbtnRequestLocation,mbtnRemoveLocation;
    private boolean isGPSAvailable=false;
    private MyBackGroundServices mService=null;
    boolean mBound=false;
    private long pressedTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        Dexter.withContext(this).withPermissions(
                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.LOCATION_HARDWARE

        ).withListener(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {

                mbtnRemoveLocation=findViewById(R.id.btn_data);
                mbtnRequestLocation=findViewById(R.id.btnCheck);
                mbtnRequestLocation.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(isGpsAvailable()){
                            mService.requestLocationUpdates();
                        }else {

                            buildAlertMessageNoGps("Gps Service required to start tracking ");
                        }


                    }
                });


                mbtnRemoveLocation.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mService.removeLocationUpdate();
                    }
                });
                setButtonState(Common.RequestingLocationUpdates(MainActivity.this));
                bindService(new Intent(MainActivity.this,MyBackGroundServices.class),mServiceConnection,Context.BIND_AUTO_CREATE);
            }


            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {

            }
        }).check();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.manu_top_bar, menu);


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Log.e("onOptionsItemSelected: ",""+item.getTitle().toString());
        switch (item.getItemId()){
            case R.id.logout_Child:
            {
                mService.removeLocationUpdate();

                SharedPreferences sharedPreferences=getSharedPreferences("MySharedPref",MODE_PRIVATE);
                sharedPreferences.edit().clear().commit();
                Intent intent=new Intent(MainActivity.this,LoginActivity.class);
                startActivity(intent);
                 finish();

                return true;
            }



        }
        return super.onOptionsItemSelected(item);

    }

    private final ServiceConnection mServiceConnection=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MyBackGroundServices.LocalBinder localBinder=(MyBackGroundServices.LocalBinder)service;
            mService=localBinder.getService();
            mBound=true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService=null;
            mBound=false;
        }
    };





    @Override
    protected void onStart() {
        super.onStart();
        androidx.preference.PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {

        if(mBound) {
            unbindService(mServiceConnection);
            mBound=false;
        }

        androidx.preference.PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
        EventBus.getDefault().unregister(this);
        super.onStop();

    }


    public boolean isInternetAvailable(){

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }
    public boolean isGpsAvailable(){

        LocationManager locationManager=(LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        isGPSAvailable=locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return isGPSAvailable;

    }
    private void buildAlertMessageNoGps(String condi) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(condi)
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));


                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
      if(key.equals(Common.KEY_REQUESTING_LOCATION_UPDATE)){
          setButtonState(sharedPreferences.getBoolean(Common.KEY_REQUESTING_LOCATION_UPDATE,false));

      }
    }

    private void setButtonState(boolean isRequestEnable) {
        if(isRequestEnable){
           mbtnRequestLocation.setEnabled(false);
           mbtnRemoveLocation.setEnabled(true);

        }else {
            mbtnRequestLocation.setEnabled(true);
            mbtnRemoveLocation.setEnabled(false);
        }
        }


    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
     public void onListenLocation(SendLocationToActivity event) {
    if(event!=null){
   String data=new StringBuilder()
           .append(event.getLocation().getAltitude())
           .append("/")
           .append(event.getLocation().getLatitude()).toString();
       // Toast.makeText(getApplicationContext(), ""+data, Toast.LENGTH_SHORT).show();

    }

}

    @Override
    public void onBackPressed() {
        if (pressedTime + 2000 > System.currentTimeMillis()) {
            super.onBackPressed();
            finish();
        } else {
            Toast.makeText(getBaseContext(), "Press back again to exit", Toast.LENGTH_SHORT).show();
        }
        pressedTime = System.currentTimeMillis();
    }
}
