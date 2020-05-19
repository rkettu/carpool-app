package com.example.carpool_app;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class GetRideActivity extends AppCompatActivity {

    private Button searchRideButton;
    private ImageView backButton;
    private EditText startPointEditText, destinationEditText, startDateEditText, endDateEditText, estStartDateEditText, estEndDateEditText;
    private ListView rideListView;
    private final static String TAG = "GetRideActivity";
    private ArrayList<User> userArrayList = new ArrayList<>();
    private ArrayList<Ride> rideArrayList = new ArrayList<>();
    private GetRideAdapter getRideAdapter;

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
        rideListView = findViewById(R.id.getRide_rideListView);
    }

    //Initialising two buttons, Search Rides and Back Arrow
    private void initGetRideButtons()
    {
        getRideAdapter = new GetRideAdapter(this, userArrayList, rideArrayList);
        rideListView.setAdapter(getRideAdapter);
        //TODO find trip in asynctask
        //if you press Search Button
        searchRideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String startPoint = startPointEditText.getText().toString();
                String destination = destinationEditText.getText().toString();

                FindRideASync findRideASync = new FindRideASync();
                findRideASync.FindRideASync(new FindRideInterface() {

                    //Interface to communicate with FindRideASync class
                    @Override
                    public void getRideData(Ride ride) {
                        rideArrayList.add(ride);
                        getRideAdapter.notifyDataSetChanged();
                        Log.d(TAG, "getRideData: taalla ollaan rajapinnassa" + ride.getEndCity());
                    }

                    @Override
                    public void getUserData(User user){
                        userArrayList.add(user);
                        getRideAdapter.notifyDataSetChanged();
                        Log.d(TAG, "getUserData: taalla ollaan rajapinnassa" + user.getFname());
                    }
                }, getApplicationContext());

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
