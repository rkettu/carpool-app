package com.example.carpool_app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

// Class for holding various useful constant variables to be used throughout the app
public class Constant {
    public final static long MinuteInMillis = 60000;
    public final static long HourInMillis = 3600000;
    public final static long DayInMillis = 86400000;

    public final static String defaultProfileImageAddress = "https://firebasestorage.googleapis.com/v0/b/carpool-app-2020.appspot.com/o/profpics%2Fdefault_pic.jpg?alt=media&token=f84336d2-c628-475a-82de-0941586fd0f7";

    //hide keyboard when called this
    public static void hideKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View v = activity.getCurrentFocus();
        if (v == null)
        {
            v = new View(activity);
        }
        inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    //checks if device is connected to internet
    public static boolean isNetworkConnected(Context context){
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if(networkInfo != null && networkInfo.isConnected())
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    private AlertDialog loadingDialog;

    public void startLoadingDialog(Context context)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        LayoutInflater inflater = LayoutInflater.from(context);
        builder.setView(inflater.inflate(R.layout.progressbar_loading, null));
        builder.setCancelable(false);

        loadingDialog = builder.create();
        loadingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        loadingDialog.show();
    }

    public void dismissLoadingDialog()
    {
        loadingDialog.dismiss();
    }
}
