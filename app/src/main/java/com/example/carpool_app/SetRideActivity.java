package com.example.carpool_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class SetRideActivity extends AppCompatActivity implements Serializable, OnMapReadyCallback, SetRideTaskLoadedCallback, View.OnClickListener, GoogleMap.OnPolylineClickListener {

    private GoogleMap mMap;

    //PlaceAutoComplete preferences
    private static final String TAG = SetRideActivity.class.getSimpleName();
    private ListView AutoCompleteStartpointListView, AutoCompleteDestinationListView;
    private ArrayList<String> autoCompleteValues;
    private ArrayAdapter<String> autoCompleteListAdapter;

    //Address editors preferences
    private EditText startEditor, destinationEditor, waypointEditor1, waypointEditor2;
    private String strStart, strDestination, strWaypoint1, strWaypoint2;

    //Layout preferences
    Animation ttbAnim, bttAnim;
    private LinearLayout linearContainer;
    private ConstraintLayout routeDetails;
    private Button drawerButton, nextBtn;
    private ImageButton locationButton, waypointRemoveBtn1, waypointRemoveBtn2;
    private boolean drawer_expand = true;

    List<Polyline> allPolylines = new ArrayList<>(); // Contains all currently drawn polyline data, REMEMBER TO CLEAR

    //Location preferences
    GeoCoderHelper geoCoderHelper = new GeoCoderHelper();
    int LOCATION_REQUEST_CODE = 10001;

    //Route and map preferences
    private MarkerOptions place1, place2, wayPoint1, wayPoint2;
    TextView distance, duration;
    private int locker = 0;
    private double startLat, startLng, stopLat, stopLng;
    private String fastestRoute;
    private HashMap<String, Route> polylineHashMap = new HashMap<>(); // Contains polyline id and matching route info
    private Route currentRoute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_ride);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);


        //PlaceAutocomplete settings
        Places.initialize(getApplicationContext(), getString(R.string.api_key));
        AutoCompleteStartpointListView = (ListView)findViewById(R.id.set_ride_autoComplete_startpointListView);
        AutoCompleteDestinationListView = (ListView)findViewById(R.id.set_ride_autoComplete_destinationListView);
        autoCompleteValues = new ArrayList<>();
        autoCompleteListAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, android.R.id.text1, autoCompleteValues);

        //Buttons
        locationButton = findViewById(R.id.set_ride_sijaintiButton);
        locationButton.setOnClickListener(this);
        findViewById(R.id.set_ride_haeButton).setOnClickListener(this);
        findViewById(R.id.set_ride_etappiBtn).setOnClickListener(this);
        waypointRemoveBtn1 = (ImageButton)findViewById(R.id.set_ride_etappiRemoveBtn);
        waypointRemoveBtn2 = (ImageButton)findViewById(R.id.set_ride_etappiRemoveBtn2);
        waypointRemoveBtn1.setOnClickListener(this);
        waypointRemoveBtn2.setOnClickListener(this);
        nextBtn = (Button) findViewById(R.id.set_ride_nextBtn);
        nextBtn.setOnClickListener(this);
        nextBtn.setEnabled(false);

        //Editors
        startEditor = (EditText) findViewById(R.id.set_ride_lahtoEdit);
        destinationEditor = (EditText) findViewById(R.id.set_ride_maaranpaaEdit);
        waypointEditor1 = (EditText) findViewById(R.id.set_ride_etappiEdit);
        waypointEditor2 = (EditText) findViewById(R.id.set_ride_etappiEdit2);

        //Add google map to set_ride_mapView fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.set_ride_mapView);
        mapFragment.getMapAsync(this);

        //Layout preferences
        ttbAnim = new AnimationUtils().loadAnimation(this, R.anim.toptobottomanimation);
        bttAnim = new AnimationUtils().loadAnimation(this, R.anim.bottomtotopanimation);
        linearContainer = (LinearLayout) findViewById(R.id.set_ride_linearLayout);
        drawerButton = (Button) findViewById(R.id.set_ride_drawer_bottom);
        routeDetails = (ConstraintLayout) findViewById(R.id.set_ride_routeDetails);
        distance = (TextView) findViewById(R.id.set_ride_infoTxt);
        duration = (TextView) findViewById(R.id.set_ride_infoTxt2);

        bttAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                linearContainer.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, 0));
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        //AutoComplete request update after added every character. And addresses save to "autoCompleteValues" arraylist.
        startEditor.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                autoCompleteValues.clear();
                AutoCompleteStartpointListView.setVisibility(View.VISIBLE);
                
                PlaceAutoComplete(startEditor.getText().toString(), 0);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        //AutoComplete request update after added every character. And addresses save to "autoCompleteValues" arraylist.
        destinationEditor.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                autoCompleteValues.clear();
                AutoCompleteDestinationListView.setVisibility(View.VISIBLE);

                PlaceAutoComplete(destinationEditor.getText().toString(), 1);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        //itemClickListener to startpointListView. Add clicked address to startEditor field
        AutoCompleteStartpointListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startEditor.setText(((TextView) view).getText().toString());
                autoCompleteValues.clear();
                AutoCompleteStartpointListView.setVisibility(View.GONE);
            }
        });

        //itemClickListener to destinationPointListView. Add clicked address to endEditor field
        AutoCompleteDestinationListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                destinationEditor.setText(((TextView) view).getText().toString());
                autoCompleteValues.clear();
                AutoCompleteDestinationListView.setVisibility(View.GONE);
            }
        });
    }


    //Layout menu animation functionality
    public void expandableDrawer(View view) { animationHandler(); }
    private void animationHandler(){
        if (!drawer_expand){
            linearContainer.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            doAnimation(ttbAnim);
            drawer_expand = true;
        }else{
            doAnimation(bttAnim);
            drawer_expand = false;
        }
    }
    private void doAnimation(Animation anim){
        linearContainer.startAnimation(anim);
        drawerButton.startAnimation(anim);
       // locationButton.startAnimation(anim);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        //Zoom a map up to Finland when open that acticity
        mMap = googleMap;
        LatLng suomi = new LatLng(65.55, 25.55);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(suomi));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(suomi, 5));
        mMap.setOnPolylineClickListener(this);
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.set_ride_etappiBtn & locker == 0)
        {
            waypointEditor1.setVisibility(View.VISIBLE);
            waypointRemoveBtn1.setVisibility(View.VISIBLE);
            locker = 1;

        }
        else if (v.getId() == R.id.set_ride_etappiBtn & locker == 1)
        {
            waypointEditor2.setVisibility(View.VISIBLE);
            waypointRemoveBtn2.setVisibility(View.VISIBLE);
        }
        else if (v.getId() == R.id.set_ride_etappiRemoveBtn)
        {
            waypointEditor1.setVisibility(View.GONE);
            waypointRemoveBtn1.setVisibility(View.GONE);
            strWaypoint1 = "";
            waypointEditor1.setText(strWaypoint1);
            locker = 0;
        }
        else if (v.getId() == R.id.set_ride_etappiRemoveBtn2)
        {
            waypointEditor2.setVisibility(View.GONE);
            waypointRemoveBtn2.setVisibility(View.GONE);
            strWaypoint2 = "";
            waypointEditor2.setText(strWaypoint2);
        }

        //"GET ROUTE" -button functionality
        else if(v.getId() == R.id.set_ride_haeButton)
        {
            //Set waypoints to null if user research route
            wayPoint1 = null;
            wayPoint2 = null;

            mMap.clear();   // Clearing map markers and polylines
            allPolylines.clear(); // Clearing list containing references to those polylines => frees their memory
            polylineHashMap.clear();

            //Hiding keyboard
            Constant.hideKeyboard(SetRideActivity.this);

            routeDetails.setVisibility(View.GONE);

            /*
            //Lifting up route details element when user press "GET ROUTE" button
            doAnimation(bttAnim);
             */

            strStart = startEditor.getText().toString();
            strDestination = destinationEditor.getText().toString();
            strWaypoint1 = waypointEditor1.getText().toString();
            strWaypoint2 = waypointEditor2.getText().toString();

            //Check if waypoint1 editor is not null
            if(strWaypoint1 != null && !strWaypoint1.isEmpty())
            {
                //Get coordinates to waypoints
                GetWaypointCoordinatesASync getWaypointCoordinatesASync = new GetWaypointCoordinatesASync(new GetWaypointCoordinatesInterface() {
                    @Override
                    public void getWayCoordinates(GetCoordinatesUtility getCoordinatesUtility) {
                        double way1Lat = getCoordinatesUtility.getWayLat();
                        double way1Lng = getCoordinatesUtility.getWayLng();
                        Log.d("mylog", "getCoordinates WAYPOINT1: " + way1Lat + way1Lng);
                        wayPoint1 = new MarkerOptions().position(new LatLng(way1Lat, way1Lng)).title("Pysähdys 1");
                        mMap.addMarker(wayPoint1);
                    }
                }, SetRideActivity.this);
                getWaypointCoordinatesASync.execute(strWaypoint1);
            }
            //Check if waypoint2 editor is not null
            if(strWaypoint2 != null && !strWaypoint2.isEmpty())
            {
                //Get coordinates to waypoints
                GetWaypointCoordinatesASync getWaypointCoordinatesASync = new GetWaypointCoordinatesASync(new GetWaypointCoordinatesInterface() {
                    @Override
                    public void getWayCoordinates(GetCoordinatesUtility getCoordinatesUtility) {
                        double way2Lat = getCoordinatesUtility.getWayLat();
                        double way2Lng = getCoordinatesUtility.getWayLng();
                        Log.d("mylog", "getCoordinates WAYPOINT1: " + way2Lat + way2Lng);
                        wayPoint2 = new MarkerOptions().position(new LatLng(way2Lat, way2Lng)).title("Pysähdys 2");
                        mMap.addMarker(wayPoint2);
                    }
                }, SetRideActivity.this);
                getWaypointCoordinatesASync.execute(strWaypoint2);
            }

            //Get full address to "startPointEditor", example ("kaarnatie 5, 90350 Oulu, Suomi")
            GetFullAddressASync getFullAddressASync = new GetFullAddressASync(new GetFullAddressInterface() {
                @Override
                public void getFullAddress(GetCoordinatesUtility getCoordinatesUtility) {
                    String address = getCoordinatesUtility.getFullAddress();
                    startEditor.setText(address);
                    AutoCompleteStartpointListView.setVisibility(View.INVISIBLE);
                    if(address == null){
                        Toast.makeText(SetRideActivity.this, R.string.setride_check_start_position, Toast.LENGTH_LONG).show();
                    }
                }
            }, SetRideActivity.this);
            getFullAddressASync.execute(strStart);

            //Get full address to "destinationPointEditor", example ("kaarnatie 5, 90350 Oulu, Suomi")
            GetFullAddressASync getFullAddressASync2 = new GetFullAddressASync(new GetFullAddressInterface() {
                @Override
                public void getFullAddress(GetCoordinatesUtility getCoordinatesUtility) {
                    String address = getCoordinatesUtility.getFullAddress();
                    destinationEditor.setText(address);
                    AutoCompleteDestinationListView.setVisibility(View.INVISIBLE);
                    if(address == null){
                        Toast.makeText(SetRideActivity.this, R.string.setride_check_destination_position, Toast.LENGTH_LONG).show();
                    }
                }
            }, SetRideActivity.this);
            getFullAddressASync2.execute(strDestination);

            //Getting start and destination coordinates in asynctask
            GetCoordinatesASync getCoordinatesASync = new GetCoordinatesASync(new GetCoordinatesInterface() {
                @Override
                public void getCoordinates(GetCoordinatesUtility getCoordinatesUtility) {
                    try {
                        startLat = getCoordinatesUtility.getStartLat();
                        startLng = getCoordinatesUtility.getStartLng();
                        stopLat = getCoordinatesUtility.getDestinationLat();
                        stopLng = getCoordinatesUtility.getDestinationLng();

                        place1 = new MarkerOptions().position(new LatLng(startLat, startLng)).title("Location 1");
                        place2 = new MarkerOptions().position(new LatLng(stopLat, stopLng)).title("Location 2");
                        new SetRideFetchURL(SetRideActivity.this).execute(getUrl(place1.getPosition(), place2.getPosition(),"driving"), "driving");

                        mMap.addMarker(place1);
                        mMap.addMarker(place2);
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place2.getPosition(),8));

                        routeDetails.setVisibility(View.VISIBLE);
                    }catch (Exception e){
                        Toast.makeText(SetRideActivity.this, R.string.setride_check_start_and_destination, Toast.LENGTH_LONG).show();
                    }
                }
            }, SetRideActivity.this);

            if(strStart != null && strDestination != null){
                getCoordinatesASync.execute(strStart, strDestination);
            }
            }

        //"CONTINUE TO DETAILS" -button functionality
        else if(v.getId() == R.id.set_ride_nextBtn)
        {
            //Get start- and destination city to helping firestore and move all needed information to "SetRideDetailsActivity"
            GetCityASync getCityASync = new GetCityASync(new GetCityInterface() {
                @Override
                public void getCity(GetCoordinatesUtility getCity) {
                    String startCity = getCity.getStartCity();
                    String endCity = getCity.getDestinationCity();

                    Intent details = new Intent(SetRideActivity.this, SetRideDetailsActivity.class);
                    try{
                        details.putExtra("ALKUOSOITE", strStart);
                        details.putExtra("LOPPUOSOITE", strDestination);
                        details.putExtra("STARTCITY" , startCity);
                        details.putExtra("ENDCITY", endCity);
                        details.putExtra("DISTANCE", currentRoute.rideDistance);
                        details.putExtra("DURATION", currentRoute.rideDuration);
                        details.putExtra("BOUNDS",   currentRoute.bounds);
                        details.putExtra("POINTS", (Serializable) currentRoute.selectPoints);

                        startActivity(details);
                    }catch (Exception e){
                        //Log.d("mylog", "putExtra Failed: ");
                    }

                }
            }, SetRideActivity.this);
            getCityASync.execute(strStart, strDestination);
        }

        //"GET MY LOCATION" -button functionality
        else if(v.getId() == R.id.set_ride_sijaintiButton)
        {

            if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            {
                //Location granted
                Toast.makeText(SetRideActivity.this, R.string.setride_search_location, Toast.LENGTH_LONG).show();

                AsyncTaskGetLocation getLocation = new AsyncTaskGetLocation();
                getLocation.execute();
            }
            else {
                //Location not granted
                askLocationPermission();
            }
        }
    }

    //Ask permission to use location from user
    private void askLocationPermission() {
        if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)){
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            }else{
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
                }
            }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == LOCATION_REQUEST_CODE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                //Permission granted
                Toast.makeText(SetRideActivity.this, R.string.setride_search_location, Toast.LENGTH_LONG).show();

                AsyncTaskGetLocation getLocation = new AsyncTaskGetLocation();
                getLocation.execute();

            }else {
                //Permission not granted
                if(!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)){
                    //This block here means PERMANENTLY DENIED PERMISSION
                    new AlertDialog.Builder(SetRideActivity.this)
                            .setMessage(R.string.setride_location_message)
                            .setPositiveButton(R.string.setride_location_button_positive, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    gotoApplicationSettings();
                                }
                            })
                            .setNegativeButton(R.string.setride_location_button_negative, null)
                            .setCancelable(false)
                            .show();
                } else {
                    Log.d("mylog", "Permission not granted " );
                }
            }
        }
    }

    //This functio move user to phone permissions settings
    private void gotoApplicationSettings(){
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", this.getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);

    }

    // Build and return url from parameters and route details
    private String getUrl(LatLng origin, LatLng dest, String directionMode){

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        // Mode
        String mode = "mode=" + directionMode;
        // Building the parameters to the web service

        //String parameters = str_origin + "&" + str_dest + 64.080600,%2024.533221" + "&" + mode;
        String parameters = str_origin + "&" + str_dest + "&" + mode;

        Log.d("mylog", "WAYPOINTTI 1: " + wayPoint1);
        if(wayPoint1 != null)
        {
            parameters = str_origin + "&" + str_dest + "&waypoints=via:" + wayPoint1.getPosition().latitude
                    + "," + wayPoint1.getPosition().longitude + "&" + mode;
            Log.d("mylog", "getUrl: WAY1  ");
        }
        if(wayPoint1 != null & wayPoint2 != null)
        {
            parameters = str_origin + "&" + str_dest + "&waypoints=via:" + wayPoint1.getPosition().latitude
                    + "," + wayPoint1.getPosition().longitude + "|via:" + wayPoint2.getPosition().latitude
                    + "," + wayPoint2.getPosition().longitude + "&" + mode;
            Log.d("mylog", "getUrl: WAT2 ");
        }
        if(wayPoint2 != null & wayPoint1 == null)
        {
            parameters = str_origin + "&" + str_dest + "&waypoints=via:" + wayPoint2.getPosition().latitude
                    + "," + wayPoint2.getPosition().longitude + "&" + mode;
            Log.d("mylog", "getUrl: WAY1 ja WAY2 ");
        }

        // Output format
        String output = "json";
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters
                + "&alternatives=true&key=" + getString(R.string.google_maps_key);

        Log.d("URL_HAKU", url);
        return url;
    }

    // SetRideTaskLoadedCallback onTaskDone, add route polyline to map if route search success
    // This is called once for each polyline added
    @Override
    public void onTaskDone(Object... values) {

        Route route = (Route) values[0];
        Log.d("mylog", "onTaskDone: " + route.rideDistance + "km ride");

        PolylineOptions polylineOptions = route.getLineOptions();

        Polyline polyline = mMap.addPolyline(polylineOptions);
        allPolylines.add(polyline); // Adding polyline to list of all polylines
        polylineHashMap.put(polyline.getId(), route);

        if(allPolylines.size() == 1)
        {
            currentRoute = route;
            fastestRoute = allPolylines.get(0).getId();
            routeDetails.setVisibility(View.VISIBLE);
            distance.setText(currentRoute.rideDistance + " km");
            duration.setText(currentRoute.rideDuration);
            nextBtn.setEnabled(true);
        }

        polyline.setClickable(true);
    }

    //Sets the clicked route as active
    @Override
    public void onPolylineClick(Polyline clickedPolyline) {

        Log.d("mylog", "onPolylineClick: POLYLINE " + clickedPolyline.getId());

        for(Polyline polyline : allPolylines)
        {
            // Checking for clicked polyline match in list
            if(clickedPolyline.getId().equals(polyline.getId()))
            {
                //Set "duration" text to green, if clicked route is fastest alternative
                if(clickedPolyline.getId().equals(fastestRoute)) {
                    duration.setTextColor(Color.GREEN);
                }else{
                    duration.setTextColor(Color.GRAY);
                }
                polyline.setColor(Color.BLUE);
                polyline.setZIndex(1);
                currentRoute = polylineHashMap.get(polyline.getId());
                distance.setText(currentRoute.rideDistance + " km");
                duration.setText(currentRoute.rideDuration);
            }
            else
            {
                polyline.setColor(Color.GRAY);
                polyline.setZIndex(0);
            }
        }

    }

    // Set autocomplete lisviews invisible if user click anywhere out of listview
    public void anywhereClicked(View view) {
        Log.d("CLICK", "constrainClicked: ");
        AutoCompleteStartpointListView.setVisibility(View.INVISIBLE);
        AutoCompleteDestinationListView.setVisibility(View.INVISIBLE);
    }

    //This funktio will called when user add character in start- or destination editor.
    // Param "character" is that added character and param "selector" is value 0 or 1
    // 0 = startPointEditor and 1 = destinationPointEditor
    private void PlaceAutoComplete(String character, int selector){

        AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();

        RectangularBounds bounds = RectangularBounds.newInstance(
                new LatLng(-33.880490, 151.184363),
                new LatLng(-33.858754, 151.229596));

        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                .setLocationBias(bounds)
                .setCountry("FI") //Finland
                .setSessionToken(token)
                .setQuery(character)
                .build();

        PlacesClient placesClient = Places.createClient(this);
        placesClient.findAutocompletePredictions(request).addOnSuccessListener((response) -> {
            for (AutocompletePrediction prediction : response.getAutocompletePredictions()) {
                autoCompleteValues.add(prediction.getFullText(null).toString());
                Log.i(TAG, prediction.getPlaceId());
                Log.i(TAG, prediction.getPrimaryText(null).toString());
            }
            //Add autoCompleteValues to startPointListView
            if(selector == 0){
                AutoCompleteStartpointListView.setAdapter(autoCompleteListAdapter);
            }
            //Add autoCompleteValues to destinationPointListView
            else if(selector == 1){
                AutoCompleteDestinationListView.setAdapter(autoCompleteListAdapter);
            }
        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                ApiException apiException = (ApiException) exception;
                Log.e(TAG, "Place not found: " + apiException.getStatusCode());
                Toast.makeText(SetRideActivity.this, "Place not found", Toast.LENGTH_LONG).show();
            }
        }
        );
    }

    //Search location to starteditor in asynctask
    private class AsyncTaskGetLocation extends AsyncTask<Void, Void, String> {

        private String geoAddress;
        @Override
        protected String doInBackground(Void... voids) {

            if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                LocationRequest locationRequest = new LocationRequest();
                locationRequest.setInterval(10000);
                locationRequest.setFastestInterval(3000);
                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

                LocationServices.getFusedLocationProviderClient(SetRideActivity.this)
                        .requestLocationUpdates(locationRequest, new LocationCallback(){

                            @Override
                            public void onLocationResult(LocationResult locationResult) {
                                super.onLocationResult(locationResult);
                                LocationServices.getFusedLocationProviderClient(SetRideActivity.this)
                                        .removeLocationUpdates(this);
                                if(locationResult != null && locationResult.getLocations().size() > 0){
                                    int latestLocationIndex = locationResult.getLocations().size() -1;
                                    double latitude =
                                            locationResult.getLocations().get(latestLocationIndex).getLatitude();
                                    double longitude =
                                            locationResult.getLocations().get(latestLocationIndex).getLongitude();
                                    Log.d("mylog", "onLocationResult: " + latitude + longitude);

                                    Location location = new Location("provider");
                                    location.setLatitude(latitude);
                                    location.setLongitude(longitude);

                                    geoAddress = geoCoderHelper.fullAddressLocation(location, SetRideActivity.this);
                                    startEditor.setText(geoAddress);
                                }

                            }
                        }, Looper.getMainLooper());
            }
            return null;
        }
    }
}

