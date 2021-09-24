package com.project.tackme.child_end.Model;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.IBinder;

import androidx.annotation.Nullable;


public class Child extends Service {

    private String name;
    private String email;

    private Location location;
    private Date date;
    private Context context;



    public Child(String name, String email, Location location, Date date) {

        this.name = name;
        this.email = email;
        this.location = location;
        this.date = date;
    }

    public Child(Location location, Date date, Context context) {
        this.location = location;
        this.date = date;
        this.context = context;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Location getLocation() {
        return location;
    }

    public Date getDate() {
        return date;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
