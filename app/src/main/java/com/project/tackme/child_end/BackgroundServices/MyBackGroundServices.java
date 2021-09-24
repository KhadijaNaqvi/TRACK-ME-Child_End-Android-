package com.project.tackme.child_end.BackgroundServices;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.project.tackme.child_end.MainActivity;
import com.project.tackme.child_end.Model.Child;
import com.project.tackme.child_end.Model.Date;
import com.project.tackme.child_end.R;

import org.greenrobot.eventbus.EventBus;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MyBackGroundServices extends Service {
    private static final String Channel_ID = "my channel";
    private static final String EXTRA_STARTED_FROM_NOTIFICATION = "com.project.tackme.child_end.BackgroundServices" + ".started_from_notification";
    private final IBinder iBinder = new LocalBinder();
    private Timer mTimer = null,mTimerLiveLocation=null;
    private static final long UPDATE_INTERVAL_IN_MIL = 10000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MUL = UPDATE_INTERVAL_IN_MIL / 2;
    private static final int NOTI_ID = 1223;
    private boolean mChangingConfiguration = false;
    private NotificationManager mNotificationManager;
    private FirebaseFirestore mFirestore,getmFirestore;
    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private Handler mServiceHandler,mlocationhandler;
    private Location mLocation;
    private String mtime,mdate;
    private Date date;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mChangingConfiguration=true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        boolean startedFromNotification=intent.getBooleanExtra(EXTRA_STARTED_FROM_NOTIFICATION,false);
        if (startedFromNotification){
            removeLocationUpdate();
            stopSelf();
        }
        return START_NOT_STICKY;
    }

    public void removeLocationUpdate() {
        try{
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
            Common.setRequestingLocationUpdate(this,false);
            stopSelf();
           // mTimer.cancel();

            Log.e("removeLocationUpdate: ","removed" );
        }catch (Exception ex){
            Common.setRequestingLocationUpdate(this,true);
           // Log.e("Location", "Lost location permission could not remove updates: "+ex);

        }
    }

    @Override
    public void onCreate() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                onNewLocation(locationResult.getLastLocation());
            }
        };
        createLocationRequest();
        getLastLocation();

        HandlerThread handlerThread=new HandlerThread("MyThread");
        handlerThread.start();
        mServiceHandler=new Handler(handlerThread.getLooper());

        HandlerThread handlerThread2=new HandlerThread("locationThread");
        handlerThread2.start();
        mlocationhandler=new Handler(handlerThread2.getLooper());







        mNotificationManager=(NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){

            NotificationChannel notificationChannel=new NotificationChannel(Channel_ID,getString(R.string.app_name),NotificationManager.IMPORTANCE_DEFAULT);
            mNotificationManager.createNotificationChannel(notificationChannel);
        }

    }

    private void getLastLocation() {
        Log.e("loop", "looptest: " );
        try{
            fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    if(task.isSuccessful() && task.getResult() != null){
                        mLocation=task.getResult();
                    }else Log.e("Failed To get Location", "onComplete: " );
                }
            });

        }catch (SecurityException es){
            Log.e("Location", "Lost last location "+es);
        }
    }

    private void createLocationRequest() {
        mLocationRequest=new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MIL);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MUL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    }

    private void onNewLocation(Location lastLocation) {
        mLocation=lastLocation;
        EventBus.getDefault().postSticky(new SendLocationToActivity(mLocation));
        //Update Notification Content if running as a foreground services
        if (ServiceIsRunningInForeground(this)){
            mNotificationManager.notify(NOTI_ID,getNotification());
        }

    }

    private Notification getNotification() {
        Intent intent =new Intent(this,MyBackGroundServices.class);
        String txt= Common.getLocationText(mLocation);
        intent.putExtra(EXTRA_STARTED_FROM_NOTIFICATION,true);
        PendingIntent servicePendingIntent= PendingIntent.getService(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent activityPendingIntent =PendingIntent.getActivity(this,0,new Intent(this, MainActivity.class),0);
        NotificationCompat.Builder builder=new NotificationCompat.Builder(this)
                .addAction(R.drawable.ic_baseline_place_24,"Lanch",activityPendingIntent)
                .setContentTitle("Application is Running")
                .setContentInfo(Common.getLocationTitle(this))
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .setSmallIcon(R.drawable.ic_baseline_cancel_24)
                .setWhen(System.currentTimeMillis());

        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O){

            builder.setChannelId(Channel_ID);
        }
        return builder.build();
    }

    private boolean ServiceIsRunningInForeground(Context context) {
        ActivityManager activityManager=(ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for(ActivityManager.RunningServiceInfo service:activityManager.getRunningServices(Integer.MAX_VALUE)){
           if(getClass().getName().equals(service.service.getClassName()))
               if(service.foreground) return true;
         return false;
        }
        return true;
    }
    private class TimerTaskToGetLocation extends TimerTask {
        @Override
        public void run() {

            mServiceHandler.post(new Runnable() {
                @Override
                public void run() {
                    databaseLocationSaved();
                    Log.e("Thread", "Check Thread "+Common.getLocationText(mLocation).toString() );
                }
            });

        }
    }
    private class TimerTaskToGetLiveLocation extends TimerTask {
        @Override
        public void run() {

            mlocationhandler.post(new Runnable() {
                @Override
                public void run() {
                    checkLiveLocation();



                }
            });

        }
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        stopForeground(true);
        mChangingConfiguration=false;
        return iBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        stopForeground(true);
        mChangingConfiguration=false;
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if(!mChangingConfiguration && Common.RequestingLocationUpdates(this));
        startForeground(NOTI_ID,getNotification());
        return true;
    }

    @Override
    public void onDestroy() {
        mServiceHandler.removeCallbacks(null);
        super.onDestroy();
    }

    public void requestLocationUpdates() {

        Common.setRequestingLocationUpdate(this,true);
        startService(new Intent(getApplicationContext(),MyBackGroundServices.class));
        try{
            fusedLocationProviderClient.requestLocationUpdates(mLocationRequest,locationCallback, Looper.myLooper());

        }catch (SecurityException es){

            Log.e("LOcation", "Lost location Permission: "+es );
        }

        //mTimerLiveLocation=new Timer();
       // mTimerLiveLocation.schedule(new TimerTaskToGetLiveLocation(),1000,1000);
        mTimer = new Timer();
        mTimer.schedule(new TimerTaskToGetLocation(), 3000, 300000);

    }



    public class LocalBinder extends Binder {
       public MyBackGroundServices getService() {return MyBackGroundServices.this;}
    }
    public void databaseLocationSaved( ){
        SharedPreferences dh=getSharedPreferences("MySharedPref",MODE_PRIVATE);
        getDateTime();
        mFirestore= FirebaseFirestore.getInstance();
        @SuppressLint("WrongViewCast")
        Map<String, Object> user = new HashMap<>();
        user.put("latitude", mLocation.getLatitude()+"");
        user.put("longitude", mLocation.getLongitude()+"");
        user.put("date", date.getDate()+"");
        user.put("time", date.getTime()+"");
        user.put("id", dh.getString("referenceParent","").toString());

        mFirestore.collection(dh.getString("referenceChild","")).add(user)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.e( "onSuccess: ", "DataSaved");

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
                Log.e( "onFailure: ", "DataNotSaves");
            }
        });

    }
    public void checkLiveLocation(){


        getmFirestore=FirebaseFirestore.getInstance();
        getmFirestore.collection("cheklivelocation").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){

                    for(QueryDocumentSnapshot snapshot :task.getResult()){
                       if(snapshot.getBoolean("check")){
                           saveLiveLocation();
                           Log.e("onComplete: ", "value true");

                       }


                    }

                    

                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
            }
        });

    }
    private void saveLiveLocation(){
        getDateTime();
        SharedPreferences dh=getSharedPreferences("MySharedPref",MODE_PRIVATE);
        Map<String, Object> user = new HashMap<>();
        user.put("latitude", mLocation.getLatitude()+"");
        user.put("longitude", mLocation.getLongitude()+"");
        user.put("date", date.getDate()+"");
        user.put("time", date.getTime()+"");
        user.put("parent_id", dh.getString("referenceParent","").toString());
        user.put("child_id", dh.getString("referenceChild","").toString());

        getmFirestore.collection("LiveLocation").add(user)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.e( "onSuccess: ", "DataSaved");

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
                Log.e( "onFailure: ", "DataNotSaves");
            }
        });


    }
    private void getDateTime( ){

        mdate= new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new java.util.Date());
        mtime= new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new java.util.Date());
        date=new Date(mdate,mtime);

    }

}
