package com.example.carpool_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import android.util.Log;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;


import android.widget.Button;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    //private Button getRideButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.main_btnGetRide).setOnClickListener(this);
        findViewById(R.id.main_btnOfferRide).setOnClickListener(this);


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
        /*
        // Go to log in... for testing purposes
        Intent i = new Intent(MainActivity.this, LogInActivity.class);
        startActivity(i);
         */
        // Go to profile test
        ActivitySwitcher.GoToProfileActivity(getApplicationContext(), FirebaseHelper.getUid());

     

  
    }

    /*
    private void initMainButtons(){
        getRideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent GetRideIntent = new Intent(MainActivity.this, GetRideActivity.class);
                startActivity(GetRideIntent);
            }
        });
    }
*/
    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.main_btnGetRide)
        {
            Intent GetRideIntent = new Intent(MainActivity.this, GetRideActivity.class);
            startActivity(GetRideIntent);
        }
        else if(v.getId() == R.id.main_btnOfferRide)
        {
            Intent SetRideIntent = new Intent(MainActivity.this, SetRideActivity.class);
            startActivity(SetRideIntent);
        }

    }
}
