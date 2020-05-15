package com.example.carpool_app;

import android.content.Context;
import android.content.Intent;

import com.google.firebase.auth.FirebaseAuth;

public class FirebaseHelper {

    // Set to true when user is logged in and profile exists for that user
    // Else false
    public static boolean loggedIn = false;

    // Used at login checks to send user to login screen if not logged in
    public static void GoToLogin(Context context)
    {
        Intent intent = new Intent(context, LogInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    // Simpler way to get current user's uid
    public static String getUid()
    {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }
}
