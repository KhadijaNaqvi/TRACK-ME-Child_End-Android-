package com.project.tackme.child_end.BackgroundServices;

import android.content.Context;
import android.location.Location;
import android.preference.Preference;
import android.preference.PreferenceManager;

import java.text.DateFormat;
import java.util.Date;

public class Common {

    public static final String KEY_REQUESTING_LOCATION_UPDATE ="LocationUpdateEnable";

    public static String getLocationText(Location mLocation) {
        return mLocation==null ? "Unknown Location" : new StringBuilder()
                .append(mLocation.getLatitude())
                .append(" / ")
                .append(mLocation.getAltitude()).toString();
    }


    public static CharSequence getLocationTitle(MyBackGroundServices myBackGroundServices) {
        return String.format("Location Update: %1$s", DateFormat.getDateInstance().format(new Date()));
    }

    public static void setRequestingLocationUpdate(Context context, boolean value) {
        androidx.preference.PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(KEY_REQUESTING_LOCATION_UPDATE,value)
                .apply();
    }

    public static boolean RequestingLocationUpdates(Context context) {
        return androidx.preference.PreferenceManager.getDefaultSharedPreferences(context).getBoolean(KEY_REQUESTING_LOCATION_UPDATE,false);

    }
}
