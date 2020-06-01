package com.example.carpool_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class GetRideActivity extends AppCompatActivity {

    private Button searchRideButton;
    private ImageView backButton;
    private EditText startPointEditText, destinationEditText, startDateEditText, endDateEditText, estStartTimeEditText, estEndTimeEditText;
    private ListView rideListView;
    private final static String TAG = "GetRideActivity";
    private ArrayList<User> userArrayList = new ArrayList<>();
    private ArrayList<Ride> rideArrayList = new ArrayList<>();
    private GetRideAdapter getRideAdapter;
    private CollectionReference rideReference = FirebaseFirestore.getInstance().collection("rides");
    private CollectionReference userReference = FirebaseFirestore.getInstance().collection("users");
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_ride);

        initGetRideLayoutElements();
        initGetRideButtons();
    }

    //Initializing all xml, TextViews, EditText and ListView + sets adapter to ListView + add current time as placeholder in start time/date
    private void initGetRideLayoutElements(){
        searchRideButton = findViewById(R.id.getRide_btnSearch);
        backButton = findViewById(R.id.getRide_btnBack);
        startPointEditText = findViewById(R.id.getRide_startPointEditText);
        destinationEditText = findViewById(R.id.getRide_destinationEditText);
        startDateEditText = findViewById(R.id.getRide_startDate);
        endDateEditText = findViewById(R.id.getRide_endDate);
        estStartTimeEditText = findViewById(R.id.getRide_estimateStartTimeEditText);
        estEndTimeEditText = findViewById(R.id.getRide_estimateEndTimeEditText);
        rideListView = findViewById(R.id.getRide_rideListView);
        startPointEditText.setText("Oulu");
        destinationEditText.setText("Helsinki");

        getRideAdapter = new GetRideAdapter(this, userArrayList, rideArrayList);
        rideListView.setAdapter(getRideAdapter);

        startDateEditText.setText(CalendarHelper.getDateTimeString(System.currentTimeMillis()));
        estStartTimeEditText.setText(CalendarHelper.getHHMMString(System.currentTimeMillis()));
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

                try
                {
                    //takes start point and destination to String, so we can use them on search
                    String startPoint = startPointEditText.getText().toString();
                    String destination = destinationEditText.getText().toString();

                    //takes start date/time and end date/time to string
                    final String startDate = startDateEditText.getText().toString();
                    final String estStartTime = estStartTimeEditText.getText().toString();
                    final String endDate = endDateEditText.getText().toString();
                    final String estEndTime = estEndTimeEditText.getText().toString();

                    showProgressDialog(GetRideActivity.this);
                    findRides(startPoint, destination);
                }
                catch (Exception e)
                {
                    //missing important data from editTexts
                }
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

    private void showProgressDialog(Context context)
    {
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Finding matching routes");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void findRides(String startPoint, String destination)
    {
        //ThreadPoolExecutor with 2 threads for each processor on the device and a 5 second keep-alive time.
        int numOfCores = Runtime.getRuntime().availableProcessors();
        final ThreadPoolExecutor executor = new ThreadPoolExecutor(numOfCores * 2, numOfCores * 2, 5L, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>());
        //Log.d(TAG, "findRides: " + executor.toString());
        final List<Task<DocumentSnapshot>> myList = new ArrayList<>();

        //TODO algorithm, exceptions and testing
        rideReference.get().addOnCompleteListener(executor, new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task)
            {
                if(task.isSuccessful())
                {
                    for(QueryDocumentSnapshot rideDoc : task.getResult()){
                        try
                        {
                            //Adds data to ride class from database
                            final Ride ride = rideDoc.toObject(Ride.class);

                            if(ride.getUid() != null)
                            {
                                Task<DocumentSnapshot> taskItem = userReference.document(ride.getUid()).get();
                                //uses uid to get correct provider data from database
                                userReference.document(ride.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if(task.isSuccessful())
                                        {
                                            DocumentSnapshot userDoc = task.getResult();
                                            try
                                            {
                                                final User user = userDoc.toObject(User.class);
                                                rideArrayList.add(ride);
                                                userArrayList.add(user);
                                                Log.d(TAG, "onComplete: laitettu listoihin objectit");
                                            }
                                            catch (Exception e)
                                            {
                                                e.printStackTrace();
                                            }
                                        }
                                        else
                                        {
                                            Exception exception = task.getException();
                                            Log.d(TAG, "onComplete: " + exception);
                                        }
                                    }
                                });
                                myList.add(taskItem);
                            }
                            Log.d(TAG, "getUserData: ousdia" );
                        }
                        catch (Exception e)
                        {
                            //Cannot Add data to ride class
                            e.printStackTrace();
                            Log.d(TAG, "rideReference" + e.toString());
                        }
                    }
                    Tasks.whenAll(myList).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            progressDialog.dismiss();
                            Log.d(TAG, "onComplete: Tasks.whenall ");
                            getRideAdapter.notifyDataSetChanged();
                            for(int i = 0; i < rideArrayList.size(); i++){
                                Log.d(TAG, "onComplete: " + rideArrayList.get(i).getUid() + " " + userArrayList.get(i).getFname());
                            }
                        }
                    });
                }
                else
                {
                    Exception exception = task.getException();
                    Log.d(TAG, "onComplete: task failed because: " + exception);
                    //Task is not successful
                    //TODO if task if not successful
                }
            }
        });
    }
}
