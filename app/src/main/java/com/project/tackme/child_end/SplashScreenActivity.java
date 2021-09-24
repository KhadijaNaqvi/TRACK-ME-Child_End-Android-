package com.project.tackme.child_end;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class SplashScreenActivity extends AppCompatActivity {
    private Handler mHandler;


    private SharedPreferences mSharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        mSharedPreferences=getSharedPreferences("MySharedPref",MODE_PRIVATE);

        mHandler=new Handler();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                    if(mSharedPreferences.getBoolean("login_state",false)){
                        Intent intent=new Intent(SplashScreenActivity.this,MainActivity.class);
                        startActivity(intent);
                        finish();


                    }else{

                        Intent intent=new Intent(SplashScreenActivity.this,LoginActivity.class);
                        startActivity(intent);
                        finish();

                    }




            }
        },3000);
    }


}
