package com.example.carpool_app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button getRideButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
