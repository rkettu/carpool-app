package com.example.carpool_app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import android.widget.Button;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private Button getRideButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        findViewById(R.id.main_SetRide).setOnClickListener(this);
   
        getRideButton = findViewById(R.id.main_btnGetRide);
        initMainButtons();
    }

    private void initMainButtons(){
        getRideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent GetRideIntent = new Intent(MainActivity.this, GetRideActivity.class);
                startActivity(GetRideIntent);
            }
        });
    }
}
