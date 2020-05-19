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

    //Initializing all xml, TextViews, EditText and ListView + sets adapter to ListView
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
        startPointEditText.setText("Oulu");
        destinationEditText.setText("Helsinki");

        getRideAdapter = new GetRideAdapter(this, userArrayList, rideArrayList);
        rideListView.setAdapter(getRideAdapter);
    }

    //Initialising two buttons, Search Rides and Back Arrow
    private void initGetRideButtons()
    {
        //TODO timestamps from editTexts to ASyncTask in millis
        //if you press Search Button
        searchRideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //clearing array lists, if you press button twice
                userArrayList.clear();
                rideArrayList.clear();

                //takes start point and destination to String, so we can use them on search
                String startPoint = startPointEditText.getText().toString();
                String destination = destinationEditText.getText().toString();

                //ASyncTask, where we find matching rides for our start point and destination
                FindRideASync findRideASync = new FindRideASync();
                findRideASync.FindRideASync(new FindRideInterface() {

                    @Override
                    public void getErrorData(String errorMessage){
                        //TODO if error occurs
                    }

                    //Interface to communicate with FindRideASync class, gets data for ride details
                    @Override
                    public void getRideData(final Ride ride) {
                        rideArrayList.add(ride);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                getRideAdapter.notifyDataSetChanged();
                            }
                        });
                    }

                    //Interface to communicate with FindRideASync class, gets data for user details
                    @Override
                    public void getUserData(final User user){
                        userArrayList.add(user);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                getRideAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                }, GetRideActivity.this);
                findRideASync.execute(startPoint, destination);
            }
        });

        //if you press Back Arrow on top of activity
        backButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                onBackPressed();
            }
        });
    }
}
