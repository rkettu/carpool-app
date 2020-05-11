package com.example.carpool_app;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class GetRideActivity extends AppCompatActivity {

    private Button searchRideButton;
    private ImageView backButton;
    private EditText startPointEditText, destinationEditText, startDateEditText, endDateEditText, estStartDateEditText, estEndDateEditText;
    private final static String TAG = "GetRideActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_ride);

        initGetRideLayoutElements();
        initGetRideButtons();
    }

    private void initGetRideLayoutElements(){
        searchRideButton = findViewById(R.id.getRide_btnSearch);
        backButton = findViewById(R.id.getRide_btnBack);
        startPointEditText = findViewById(R.id.getRide_startPointEditText);
        destinationEditText = findViewById(R.id.getRide_destinationEditText);
        startDateEditText = findViewById(R.id.getRide_startDate);
        endDateEditText = findViewById(R.id.getRide_endDate);
        estStartDateEditText = findViewById(R.id.getRide_estimateStartTimeEditText);
        estEndDateEditText = findViewById(R.id.getRide_estimateEndTimeEditText);
    }

    //Initialising two buttons, Search Rides and Back Arrow
    private void initGetRideButtons()
    {
        //if you press Search Button
        searchRideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String startPoint = startPointEditText.getText().toString();
                String destination = startPointEditText.getText().toString();

                FindRideASync findRideASync = new FindRideASync();
                findRideASync.execute(startPoint, destination);
            }
        });

        //if you press Back Arrow
        backButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                onBackPressed();
            }
        });
    }
}
