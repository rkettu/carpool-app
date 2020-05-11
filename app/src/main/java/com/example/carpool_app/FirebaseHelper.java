package com.example.carpool_app;

import android.content.Context;
import android.content.Intent;

public class FirebaseHelper {

    // Set to true when user is logged in and profile exists for that user
    // Else false
    public static boolean loggedIn = false;


    // Used at login checks to send user to login screen if not logged in
    public static void GoToLogin(Context context)
    {
        // TODO: add Loginactivity and uncomment code
        //Intent intent = new Intent(context, LogInActivity.class);
        //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //context.startActivity(intent);
    }

}
