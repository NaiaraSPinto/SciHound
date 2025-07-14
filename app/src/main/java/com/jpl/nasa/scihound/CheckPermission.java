package com.jpl.nasa.scihound;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import androidx.core.content.ContextCompat;

/**
 * A static class that will be used throughout the application to check if the application has the necessary permissions.
 */
public class CheckPermission {
    /**
     * General method that is used throughout application to check the permissions that
     * are given to the application.
     * @param perm : The permission that we're checking
     * @param context : The context that we're calling the function from
     * @return boolean : Does the application have the permission or not.
     */
    public static boolean checkPermission(String perm, Context context) {
        return ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Simple method that checks if the phone has internet access.
     * @param context : The context that we're calling the function from
     * @return boolean : Does the application have connection to the internet?
     */
    public static boolean isNetworkAvailable(Context context){
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
