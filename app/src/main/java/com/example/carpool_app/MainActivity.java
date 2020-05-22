package com.example.carpool_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setting auth state listener
        // onAuthStateChanged is called when user logs in / out
        // onAuthStateChanged is also called when starting app
        FirebaseAuth.AuthStateListener als = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(FirebaseAuth.getInstance().getCurrentUser() != null)
                {
                    // If user has logged in
                    Log.d("TAG", "onAuthStateChanged: true");
                    // Checking that user has created a profile
                    // Sends user to profile edit if not
                    FirebaseHelper.checkProfileCreated(getApplicationContext());
                }
                else
                {
                    // If user has logged out
                    Log.d("TAG", "onAuthStateChanged: false");
                    FirebaseHelper.loggedIn = false;
                }
            }
        };
        FirebaseAuth.getInstance().addAuthStateListener(als);
    }

    public void SelectOfferARide(View v)
    {
        // Go to log in... for testing purposes
        Intent i = new Intent(MainActivity.this, LogInActivity.class);
        startActivity(i);
    }
}
