package com.example.carpool_app;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class FirebaseHelper {

    // Set to true when user is logged in and profile exists for that user
    // Else false
    public static boolean loggedIn = false;
    public static boolean userJustCreated = false;

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

    // Function for checking that user is valid - and thus has a complete profile...
    // On success :: sets loggedIn to true
    // On failure :: sends user to profile edit Activity
    public static void checkProfileCreated(final Context context)
    {
        String uid = getUid();

        if(uid != null)
        {

            // Fetching profile data from database
            FirebaseFirestore.getInstance().collection("users").document(uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    boolean profileCreated = false;
                    User user = task.getResult().toObject(User.class);
                    if(user != null)
                    {
                        // Data fetched successfully, getting "profile created"-variable
                        profileCreated = task.getResult().toObject(User.class).getProfileCreated();
                        if(profileCreated)
                            loggedIn = true;
                    }
                    else
                    {
                        // User has no set profile => sending user to profile edit Activity
                        // If document exists, passing it as an intent extra
                        // HOWEVER, if user has just been created, they will be automatically send there anyway
                        // And therefore not needed to go there twice...
                        if(!userJustCreated)
                        {
                            Intent intent = new Intent(context, EditProfileActivity.class);
                            intent.putExtra("USERINFO", task.getResult().toObject(User.class));
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(intent);
                        }
                    }
                }
            });
        }
    }
}
