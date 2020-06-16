package com.example.carpool_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

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
    private Spinner sortListSpinner;
    private ListView rideListView;
    private final static String TAG = "GetRideActivity";
    private ArrayList<RideUser> rideUserArrayList = new ArrayList<>();
    private GetRideAdapter getRideAdapter;
    private ArrayAdapter<CharSequence> spinnerAdapter;
    private CollectionReference rideReference = FirebaseFirestore.getInstance().collection("rides");
    private CollectionReference userReference = FirebaseFirestore.getInstance().collection("users");
    private ProgressDialog progressDialog;
    private int mYear, mMonth, mDay, newHour, newMinute;
    private int startDateDay, startDateMonth, startDateYear, startTimeHour, startTimeMinute;
    private int endDateDay, endDateMonth, endDateYear, endTimeHour, endTimeMinute;
    private long date1, date2;
    private Calendar calendar;
    private List<HashMap<String, String>> points;
    private int spinnerCase = 0;
    private float startLat, startLng, destinationLat, destinationLng;
    private long systemTime = System.currentTimeMillis();

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
        sortListSpinner = findViewById(R.id.getRide_sortListSpinner);

        //simply spinner which is used to sort rides
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(GetRideActivity.this, R.array.getRideSpinnerItems,
                android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortListSpinner.setAdapter(new GetRideSpinner(spinnerAdapter, R.layout.spinner_title_get_ride, GetRideActivity.this));

        //pre filled for testing
        startPointEditText.setText("Oulu");
        destinationEditText.setText("Helsinki");

        //sets arrayLists to adapter
        getRideAdapter = new GetRideAdapter(this, rideUserArrayList);
        rideListView.setAdapter(getRideAdapter);

        //setting placeholder time in edit texts
        startDateEditText.setText(CalendarHelper.getDateTimeString(systemTime));
        estStartTimeEditText.setText("00:00");
        endDateEditText.setText(CalendarHelper.getDateTimeString(systemTime + 604800000));
        estEndTimeEditText.setText(CalendarHelper.getHHMMString(systemTime));
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


                    //show progress dialog when doing search and hide keyboard when pressing search button
                    showProgressDialog(GetRideActivity.this);
                    hideKeyboard(GetRideActivity.this);

                    //show 0 data in list
                    getRideAdapter.notifyDataSetChanged();

                    //takes start and end date, change them to millis so we can search rides between those times
                    calendar.set(startDateYear, startDateMonth-1, startDateDay, startTimeHour, startTimeMinute);
                    date1 = calendar.getTimeInMillis();
                    calendar.set(endDateYear, endDateMonth-1, endDateDay, endTimeHour, endTimeMinute);
                    date2 = calendar.getTimeInMillis();

                    Log.d(TAG, "onClick: " + startDateMonth);
                    Log.d(TAG, "onClick: " + endDateMonth);
                    Log.d(TAG, "onClick: " + date1);
                    Log.d(TAG, "onClick: " + date2);

                    //if end time is bigger or equal to start time
                    if(date2 >= date1)
                    {
                        //Getting start and destination coordinates in async task
                        GetCoordinatesASync getCoordinatesASync = new GetCoordinatesASync(new GetCoordinatesInterface() {
                            @Override
                            public void getCoordinates(GetCoordinatesUtility getRideUtility) {
                                startLat = getRideUtility.getStartLat();
                                startLng = getRideUtility.getStartLng();
                                destinationLat = getRideUtility.getDestinationLat();
                                destinationLng = getRideUtility.getDestinationLng();

                                //calling findRides function where is db search with algorithm
                                findRides(startLat, startLng, destinationLat, destinationLng, date1, date2, spinnerCase);
                            }
                        }, GetRideActivity.this);
                        getCoordinatesASync.execute(startPoint, destination);
                    }
                    //if start time is bigger than end time which is not possible when searching rides
                    else
                    {
                        progressDialog.dismiss();
                        startDateEditText.setTextColor(Color.parseColor("#B75272"));
                        endDateEditText.setTextColor(Color.parseColor("#B75272"));
                        estStartTimeEditText.setTextColor(Color.parseColor("#B75272"));
                        estEndTimeEditText.setTextColor(Color.parseColor("#B75272"));
                        Toast.makeText(GetRideActivity.this, "Tarkista päivämäärät ja kellonajat", Toast.LENGTH_LONG).show();
                    }

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

        startDateDay = CalendarHelper.getDayString(systemTime);
        startDateMonth = CalendarHelper.getMonthString(systemTime);
        startDateYear = CalendarHelper.getYearString(systemTime);
        endDateDay = CalendarHelper.getDayString(systemTime + 604800000);
        endDateMonth = CalendarHelper.getMonthString(systemTime + 604800000);
        endDateYear = CalendarHelper.getYearString(systemTime + 604800000);
        endTimeHour = CalendarHelper.getHourString(systemTime);
        endTimeMinute = CalendarHelper.getMinuteString(systemTime);

        Log.d(TAG, "initGetRideButtons: " + mYear);
        Log.d(TAG, "initGetRideButtons: " + mMonth);
        Log.d(TAG, "initGetRideButtons: " + startDateYear);
        Log.d(TAG, "initGetRideButtons: " + startDateMonth);
        Log.d(TAG, "initGetRideButtons: " + startDateDay);
        Log.d(TAG, "initGetRideButtons: " + endDateYear);
        Log.d(TAG, "initGetRideButtons: " + endDateMonth);
        Log.d(TAG, "initGetRideButtons: " + endDateDay);


        //when you press startDateEditText, it will open datePickerDialog where you can select date in dd/MM/yyyy
        startDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(GetRideActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        calendar.set(year, month, dayOfMonth);
                        final String startDateString = new SimpleDateFormat("dd.MM.yyyy").format(calendar.getTime());
                        startDateEditText.setText(startDateString);

                        //place picked date into variables, so we can change the date into millis for algorithm
                        startDateYear = calendar.get(Calendar.YEAR);
                        startDateMonth = calendar.get(Calendar.MONTH);
                        startDateDay = calendar.get(Calendar.DAY_OF_MONTH);
                    }
                }, mYear, mMonth, mDay);

                calendar.set(endDateYear, endDateMonth, endDateDay);
                long maximumDate = calendar.getTimeInMillis();
                datePickerDialog.getDatePicker().setMaxDate(maximumDate);
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
                        String endDateString = new SimpleDateFormat("dd.MM.yyyy").format(calendar.getTime());
                        endDateEditText.setText(endDateString);

                        //place picked date into variables, so we can change the date into millis for algorithm
                        endDateYear = calendar.get(Calendar.YEAR);
                        endDateMonth = calendar.get(Calendar.MONTH);
                        endDateDay = calendar.get(Calendar.DAY_OF_MONTH);
                    }
                }, mYear, mMonth, mDay);

                calendar.set(startDateYear, startDateMonth, startDateDay);
                long minimumDate = calendar.getTimeInMillis();
                datePickerDialog.getDatePicker().setMinDate(minimumDate);
                datePickerDialog.show();
            }
        });

        newHour = calendar.get(Calendar.HOUR_OF_DAY);
        newMinute = calendar.get(Calendar.MINUTE);

        //when you click estStartTimeText, it will popup timePickerDialog, where you set hours and minutes
        estStartTimeEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(GetRideActivity.this, AlertDialog.THEME_HOLO_LIGHT, new TimePickerDialog.OnTimeSetListener() {
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
                TimePickerDialog timePickerDialog = new TimePickerDialog(GetRideActivity.this, AlertDialog.THEME_HOLO_LIGHT, new TimePickerDialog.OnTimeSetListener() {
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

        //spinner, where you can sort data from arraylist
        sortListSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position)
                {
                    case 0:
                        //default, when user haven't selected any sorting method
                        spinnerCase = 0;
                        getRideAdapter.notifyDataSetChanged();
                        break;

                    case 1:
                        //case 1 is when user select "Aika"
                        //prints time from lowest to highest
                        spinnerCase = 1;
                        Collections.sort(rideUserArrayList, new Comparator<RideUser>() {
                            @Override
                            public int compare(RideUser o1, RideUser o2) {
                                String first = String.valueOf(o1.getRide().getLeaveTime());
                                String second = String.valueOf(o2.getRide().getLeaveTime());
                                return first.compareTo(second);
                            }
                        });
                        getRideAdapter.notifyDataSetChanged();
                        break;

                    case 2:
                        //case 2 is when user select "hinta"
                        //prints price from lowers to highest
                        spinnerCase = 2;
                        Collections.sort(rideUserArrayList, new Comparator<RideUser>() {
                            @Override
                            public int compare(RideUser o1, RideUser o2) {
                                String first = String.valueOf(o1.getRide().getPrice());
                                String second = String.valueOf(o2.getRide().getPrice());
                                return first.compareTo(second);
                            }
                        });
                        getRideAdapter.notifyDataSetChanged();
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //case 0 is happening of nothing is selected
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
    private void findRides(final float startLat, final float startLng, final float destinationLat, final float destinationLng, float date1, float date2, final int spinnerCase)
    {
        //ThreadPoolExecutor with 2 threads for each available processor on the device and a 5 second keep-alive time.
        int numOfCores = Runtime.getRuntime().availableProcessors();
        final ThreadPoolExecutor executor = new ThreadPoolExecutor(numOfCores * 2, numOfCores * 2, 5L, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>());
        //Log.d(TAG, "findRides: " + executor.toString());
        final List<Task<DocumentSnapshot>> myList = new ArrayList<>();

        Log.d(TAG, "findRides: ennen query");

        //making query, where ride time is between date1 and date2
        Query query = rideReference.whereGreaterThanOrEqualTo("leaveTime", date1).whereLessThanOrEqualTo("leaveTime", date2);

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
                            points = (List<HashMap<String, String>>) rideDoc.get("points");
                            Log.d(TAG, "onComplete: ollaan ennen appmath if lausetta");

                            //algorithm (in appMath class)
                            AppMath appMath = new AppMath();
                            if(appMath.isRouteInRange(pickUpDistance, startLat, startLng, destinationLat, destinationLng, points))
                            {
                                //Adds data to ride class from database
                                final Ride ride = rideDoc.toObject(Ride.class);
                                Log.d(TAG, "onComplete: ollaan ride objectia ");

                                if (ride.getUid() != null)
                                {
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
                                                    rideUserArrayList.add(new RideUser(ride, user));
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
                                else
                                {
                                    //rideUid is null
                                    //we have to skip task, if rideUid is null
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
                //TODO Last task doesn't count because it is still running in async
                //both of them are running on ui thread, what why runOnUiThread()
                Tasks.whenAll(myList).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        GetRideSorting sorting = new GetRideSorting(new GetRideSortingInterface() {
                            @Override
                            public void GetRideSorting(final ArrayList<RideUser> rideUserArrayList) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Log.d(TAG, "run: ollaan getridesortinging rajapinnassa");
                                        getRideAdapter.notifyDataSetChanged();
                                        progressDialog.dismiss();
                                        //if there is no rides, use toast to indicate that
                                        Log.d(TAG, "run: rajapinnan ui threadissa");
                                        if(rideUserArrayList.size() == 0)
                                        {
                                            Toast.makeText(GetRideActivity.this, "Sopivia kyytejä ei löytynyt.", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        }, GetRideActivity.this);
                        sorting.execute(rideUserArrayList, spinnerCase);
                    }
                });
            }
        });
        Log.d(TAG, "findRides: ollaan täällä");
    }

    //method, where the sorting happens after the array list is filled from database
    //data will be sorted already (depends on spinners value) when displaying first time

    /*
    private void sortingAfterDbSearch(int spinnerCase)
    {
        //when "järjestä" or nothing is selected in spinner
        if(spinnerCase == 0)
        {
            getRideAdapter.notifyDataSetChanged();
        }

        //when "aika" is selected in spinner
        else if(spinnerCase == 1)
        {
            Collections.sort(rideUserArrayList, new Comparator<RideUser>() {
                @Override
                public int compare(RideUser o1, RideUser o2) {
                    String first = String.valueOf(o1.getRide().getLeaveTime());
                    String second = String.valueOf(o2.getRide().getLeaveTime());
                    return first.compareTo(second);
                }
            });
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getRideAdapter.notifyDataSetChanged();
                }
            });
        }

        //when "hinta" is selected in spinner
        else if(spinnerCase == 2)
        {
            Collections.sort(rideUserArrayList, new Comparator<RideUser>() {
                @Override
                public int compare(RideUser o1, RideUser o2) {
                    String first = String.valueOf(o1.getRide().getPrice());
                    String second = String.valueOf(o2.getRide().getPrice());
                    return first.compareTo(second);
                }
            });
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getRideAdapter.notifyDataSetChanged();
                }
            });
        }
    }*/

    public static void hideKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View v = activity.getCurrentFocus();
        if (v == null)
        {
            v = new View(activity);
        }
        inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }
}
