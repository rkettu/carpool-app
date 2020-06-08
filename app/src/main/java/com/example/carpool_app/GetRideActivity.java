package com.example.carpool_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TimePicker;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
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
    private ArrayList<RideUser> rideUserArrayList = new ArrayList<>();
    private GetRideAdapter getRideAdapter;
    private CollectionReference rideReference = FirebaseFirestore.getInstance().collection("rides");
    private CollectionReference userReference = FirebaseFirestore.getInstance().collection("users");
    private ProgressDialog progressDialog;
    private int mYear, mMonth, mDay, newHour, newMinute;
    private int startDateDay, startDateMonth, startDateYear, startTimeHour, startTimeMinute;
    private int endDateDay, endDateMonth, endDateYear, endTimeHour, endTimeMinute;
    private String startDateString;
    private Calendar calendar;
    private List<HashMap<String, String>> points;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_ride);

        initGetRideLayoutElements();
        initGetRideButtons();
    }

    //Initializing all xml, TextViews, EditText and ListView + sets adapter to ListView + add current time as placeholder in start time/date
    private void initGetRideLayoutElements() {
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

        //sets arrayLists to adapter
        getRideAdapter = new GetRideAdapter(this, rideUserArrayList);
        rideListView.setAdapter(getRideAdapter);

        //setting placeholder time in edit texts
        startDateEditText.setText(CalendarHelper.getDateTimeString(System.currentTimeMillis()));
        estStartTimeEditText.setText(CalendarHelper.getHHMMString(System.currentTimeMillis()));
    }

    //Initialising two buttons, Search Rides and Back Arrow
    private void initGetRideButtons()
    {
        //if you press Search Button
        searchRideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //clearing array lists, if you press button twice
                rideUserArrayList.clear();
                try
                {
                    //takes start point and destination to String, so we can use them on search
                    String startPoint = startPointEditText.getText().toString();
                    String destination = destinationEditText.getText().toString();

                    //takes start and end date, change them to millis so we can search rides between those times
                    calendar.set(startDateYear, startDateMonth, startDateDay, startTimeHour, startTimeMinute);
                    float date1 = calendar.getTimeInMillis();
                    calendar.set(endDateYear, endDateMonth, endDateDay, endTimeHour, endTimeMinute);
                    float date2 = calendar.getTimeInMillis();

                    //show progress dialog when doing search and hide keyboard when pressing search button
                    showProgressDialog(GetRideActivity.this);
                    hideKeyboard(GetRideActivity.this);

                    //show 0 data in list
                    getRideAdapter.notifyDataSetChanged();

                    //calling findRides function where is db search with algorithm
                    findRides(startPoint, destination, date1, date2);
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

        calendar = Calendar.getInstance();
        mYear = calendar.get(Calendar.YEAR);
        mMonth = calendar.get(Calendar.MONTH);
        mDay = calendar.get(Calendar.DAY_OF_MONTH);

        //when you press startDateEditText, it will open datePickerDialog where you can select date in dd/MM/yyyy
        startDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(GetRideActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        calendar.set(year, month, dayOfMonth);

                        //format the output of date
                        startDateString = new SimpleDateFormat("dd.MM.yyyy").format(calendar.getTime());
                        startDateEditText.setText(startDateString);

                        //place picked date into variables, so we can change the date into millis for algorithm
                        startDateYear = calendar.get(Calendar.YEAR);
                        startDateMonth = calendar.get(Calendar.MONTH);
                        startDateDay = calendar.get(Calendar.DAY_OF_MONTH);
                    }
                }, mYear, mMonth, mDay);
                //datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
                datePickerDialog.show();
            }
        });

        //when you press endDateEditText, it will open datePickerDialog where you can select date in dd/MM/yyyy
        endDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(GetRideActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        calendar.set(year, month, dayOfMonth);

                        //format the output of date
                        String dateString = new SimpleDateFormat("dd.MM.yyyy").format(calendar.getTime());
                        endDateEditText.setText(dateString);

                        //place picked date into variables, so we can change the date into millis for algorithm
                        endDateYear = calendar.get(Calendar.YEAR);
                        endDateMonth = calendar.get(Calendar.MONTH);
                        endDateDay = calendar.get(Calendar.DAY_OF_MONTH);
                    }
                }, mYear, mMonth, mDay);
                datePickerDialog.show();
            }
        });

        newHour = calendar.get(Calendar.HOUR_OF_DAY);
        newMinute = calendar.get(Calendar.MINUTE);

        //when you click estStartTimeText, it will popup timePickerDialog, where you set hours and minutes
        estStartTimeEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(GetRideActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        //place picked time into variables, so we can change the date into millis for algorithm
                        startTimeHour = hourOfDay;
                        startTimeMinute = minute;

                        //format the output of time and print the time
                        String format = "%1$02d";
                        String estHour = String.format(format, hourOfDay);
                        String estMinute = String.format(format, minute);
                        String estTime = estHour + ":" + estMinute;
                        estStartTimeEditText.setText(estTime);
                    }
                }, newHour, newMinute, true);
                timePickerDialog.show();
            }
        });

        //when you click estEndTimeText, it will popup timePickerDialog, where you set hours and minutes
        estEndTimeEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(GetRideActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        //place picked time into variables, so we can change the date into millis for algorithm
                        endTimeHour = hourOfDay;
                        endTimeMinute = minute;

                        //format the output of time and print the time
                        String format = "%1$02d";
                        String estHour = String.format(format, hourOfDay);
                        String estMinute = String.format(format, minute);
                        String estTime = estHour + ":" + estMinute;
                        estEndTimeEditText.setText(estTime);
                    }
                }, newHour, newMinute, true);
                timePickerDialog.show();
            }
        });
    }

    //progress dialog. shows when called. used when app is finding matches
    private void showProgressDialog(Context context)
    {
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Finding matching routes");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    //algorithm and db search
    private void findRides(final String startPoint, final String destination, float date1, float date2)
    {
        //ThreadPoolExecutor with 2 threads for each processor on the device and a 5 second keep-alive time.
        int numOfCores = Runtime.getRuntime().availableProcessors();
        final ThreadPoolExecutor executor = new ThreadPoolExecutor(numOfCores * 2, numOfCores * 2, 5L, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>());
        //Log.d(TAG, "findRides: " + executor.toString());
        final List<Task<DocumentSnapshot>> myList = new ArrayList<>();

        Log.d(TAG, "findRides: ennen query");

        //making query, where ride time is between date1 and date2
        Query query = rideReference.whereGreaterThanOrEqualTo("leaveTime", date1).whereLessThanOrEqualTo("leaveTime", date2);
        query.orderBy("leaveTime");

        //counter to check when all tasks are done
        final int[] counter = {0};

        //TODO when task is completed, exceptions and testing
        Log.d(TAG, "findRides: queryn jälkeen");
        query.get().addOnCompleteListener(executor, new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task)
            {
                if(task.isSuccessful())
                {
                    for(QueryDocumentSnapshot rideDoc : task.getResult())
                    {
                        try
                        {
                            //takes pickUpDistance and points from rides so we can use our algorithm to filter matching routes
                            float pickUpDistance = (long) rideDoc.get("pickUpDistance");
                            points = (List) rideDoc.get("points");
                            AppMath appMath = new AppMath();

                            //Start point and destination coordinate points for algorithm
                            float startLat = GeoCoderHelper.getCoordinates(startPoint, GetRideActivity.this).get(0);
                            float startLng = GeoCoderHelper.getCoordinates(startPoint, GetRideActivity.this).get(1);
                            float destinationLat = GeoCoderHelper.getCoordinates(destination, GetRideActivity.this).get(0);
                            float destinationLng = GeoCoderHelper.getCoordinates(destination, GetRideActivity.this).get(1);

                            Log.d(TAG, "onComplete: ollaan ennen appmath if lausetta");

                            //algorithm (in appMath class)
                            if(appMath.isRouteInRange(pickUpDistance, startLat, startLng, destinationLat, destinationLng, points))
                            {
                                //Adds data to ride class from database
                                final Ride ride = rideDoc.toObject(Ride.class);
                                Log.d(TAG, "onComplete: ollaan ride objectia ");

                                if (ride.getUid() != null)
                                {
                                    counter[0]++;
                                    Log.d(TAG, "onComplete: ride.getUid() != null");
                                    Task<DocumentSnapshot> taskItem = userReference.document(ride.getUid()).get();
                                    //uses uid to get correct provider data from database
                                    userReference.document(ride.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful())
                                            {
                                                DocumentSnapshot userDoc = task.getResult();
                                                try
                                                {
                                                    final User user = userDoc.toObject(User.class);
                                                    for(int i = 0; i < 10; i++)
                                                    {
                                                        rideUserArrayList.add(new RideUser(ride, user));
                                                    }
                                                    Log.d(TAG, "onComplete: laitettu listoihin objectit");
                                                    counter[0]--;
                                                    Log.d(TAG, "onComplete: " + counter[0]);
                                                }
                                                catch (Exception e)
                                                {
                                                    e.printStackTrace();
                                                }
                                                /*if (counter[0] == 0) {
                                                    getRideAdapter.notifyDataSetChanged();
                                                    progressDialog.dismiss();
                                                }*/
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
                                else
                                {
                                    //rideUid is null
                                    //we have to skip task, if rideUid is null
                                    counter[0]--;
                                }

                                Log.d(TAG, "getUserData: route rangella funktion loppu" );
                            }
                            else
                            {
                                //appMath failed
                                Log.d(TAG, "appMath failed");
                            }
                        }
                        catch (Exception e)
                        {
                            //missing points/pickUpDistance or GeoCoderHelper failed
                            e.printStackTrace();
                            Log.d(TAG, "rideReference" + e.toString());
                        }
                        Log.d(TAG, "onComplete: for lauseen lopussa");
                    }
                    Log.d(TAG, "onComplete: task is successful lopussa");
                }
                else
                {
                    Exception exception = task.getException();
                    Log.d(TAG, "onComplete: task failed because: " + exception);
                    //Task is not successful
                    //TODO if task if not successful
                }
                Log.d(TAG, "onCompleten lopussa ollaan nyt");

                //when all tasks are done, dismiss the progress dialog and refresh the adapter data
                //both of them are running on ui thread, what why runOnUiThread()
                Tasks.whenAll(myList).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.d(TAG, "run: " + myList);
                                progressDialog.dismiss();
                                getRideAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                });
            }
        });
        Log.d(TAG, "findRides: ollaan täällä");
    }

    public static void hideKeyboard(Activity activity) {
        /*InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View v = activity.getCurrentFocus();
        if (v == null)
        {
            v = new View(activity);
        }
        inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
         */
    }
}
