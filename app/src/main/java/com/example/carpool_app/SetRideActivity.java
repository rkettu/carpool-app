package com.example.carpool_app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class SetRideActivity extends AppCompatActivity implements Serializable, OnMapReadyCallback, SetRideTaskLoadedCallback, View.OnClickListener, GoogleMap.OnPolylineClickListener {


    private GoogleMap mMap;

    private SearchView lahtoEditori, loppuEditori;

    private String strLahto, strLoppu, startCity, endCity;

    //Layoutin valikon animaation asetukset
    Animation ttbAnim, bttAnim;
    private LinearLayout linearContainer;
    private ConstraintLayout routeDetails;
    private Button drawerButton;
    private ImageButton sijaintiButton;
    private boolean drawer_expand = true;

    List<Polyline> allPolylines = new ArrayList<>(); // Contains all currently drawn polyline data, REMEMBER TO CLEAR

    //Oman sijainnin asetukset
    FusedLocationProviderClient fusedLocationClient;
    GeoCoderHelper geoCoderHelper = new GeoCoderHelper();

    //Reitinhaun muuttujat
    MarkerOptions place1, place2;
    TextView distance, duration;


    private String reitinValinta;
    private HashMap<String, Route> polylineHashMap = new HashMap<>(); // Contains polyline id and matching route info

    private Route currentRoute;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_ride);

        //Buttonien määritykset
        sijaintiButton = findViewById(R.id.set_ride_sijaintiButton);
        sijaintiButton.setOnClickListener(this);
        findViewById(R.id.set_ride_haeButton).setOnClickListener(this);
        findViewById(R.id.set_ride_nextBtn).setOnClickListener(this);

        //editorit
        lahtoEditori = (SearchView) findViewById(R.id.set_ride_lahtoEdit);
        loppuEditori = (SearchView) findViewById(R.id.set_ride_maaranpaaEdit);

        //Kartan asetus set_ride_mapViewiin
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.set_ride_mapView);
        mapFragment.getMapAsync(this);

        //Layoutin valikon animaation asetukset
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


        //Seuraava

    }

    //Layoutin valikon animaation toiminnot
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
        sijaintiButton.startAnimation(anim);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //Zoomaa kartan suomen kohdalle activityn aukaistessa
        LatLng suomi = new LatLng(65.55, 25.55);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(suomi));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(suomi, 5));

        mMap.setOnPolylineClickListener(this);

        //SetRidePolylineDatan tyhjennys. Ehkä turha?
        /*if(mPolylinesData.size() > 0)
        {
            for(SetRidePolylineData polylineData: mPolylinesData)
            {
                polylineData.getPolyline().remove();
            }
            mPolylinesData.clear();
            mPolylinesData = new ArrayList<>();
        }*/
    }


    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.set_ride_haeButton)
        {
            mMap.clear();   // Clearing map markers and polylines
            allPolylines.clear(); // Clearing list containing references to those polylines => frees their memory
            polylineHashMap.clear();
            //Näppäimistön piilotus
            hideKeyboard(SetRideActivity.this);

            //Reitin haku napin toiminnallisuus
            strLahto = lahtoEditori.getQuery().toString();
            strLoppu = loppuEditori.getQuery().toString();

            if (strLahto.trim().equals("") || strLoppu.trim().equals(""))
            {
                Toast.makeText(getApplicationContext(),"Valitse lähtö- ja määränpää pisteet", Toast.LENGTH_SHORT).show();
            }
            else
            {
                try
                {
                    double startLat = getCoordinates(strLahto).get(0);
                    double startLon = getCoordinates(strLahto).get(1);
                    double stopLat = getCoordinates(strLoppu).get(0);
                    double stopLon = getCoordinates(strLoppu).get(1);

                    place1 = new MarkerOptions().position(new LatLng(startLat, startLon)).title("Location 1");
                    place2 = new MarkerOptions().position(new LatLng(stopLat, stopLon)).title("Location 2");
                    new SetRideFetchURL(SetRideActivity.this).execute(getUrl(place1.getPosition(), place2.getPosition(),"driving"), "driving");
                    routeDetails.setVisibility(View.VISIBLE);

                    mMap.addMarker(place1);
                    mMap.addMarker(place2);
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place2.getPosition(),8));


                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
        //Sijainti napin toiminnallisuus
        else if(v.getId() == R.id.set_ride_sijaintiButton)
        {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
            }
            else{
                fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        Log.d("TESTI", "onSuccess: location: " + location);
                        if(location != null){

                            try {
                                String geoAddress = geoCoderHelper.fullAddress(location, SetRideActivity.this);
                                Log.d("TESTI", "Strin geoAddress: " + geoAddress);
                                lahtoEditori.setQuery(geoAddress, false);
                            }
                            catch (Exception e){
                                e.printStackTrace();
                                Toast.makeText(getApplicationContext(),"Location error", Toast.LENGTH_SHORT).show();
                            }
                        }
                        else {
                            Toast.makeText(getApplicationContext(),"Location failed", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        }
        else if(v.getId() == R.id.set_ride_nextBtn)
        {
            Intent details = new Intent(this, SetRideDetailsActivity.class);

            /*
            for(Polyline polyline : allPolylines)
            {
                // Tarkistaa mikä reiteistä on valittuna eli sininen ja asettaa sen id:n reitinvalinta muuttujaan.
                if(polyline.getColor() == Color.BLUE)
                {
                    reitinValinta = polyline.getId();
                    Log.d("mylog", "REITINVALINTA: " + reitinValinta);

                }
            }*/

            //
            try{
                startCity = geoCoderHelper.getCity(strLahto, SetRideActivity.this);
                endCity = geoCoderHelper.getCity(strLoppu, SetRideActivity.this);
                Log.d("mylog", "startCity: " + startCity + " endCity: " + endCity);
            }catch (Exception e){
                Log.d("mylog", "getCity Failed: " );
            }

            try{
                details.putExtra("ALKUOSOITE", strLahto);
                details.putExtra("LOPPUOSOITE", strLoppu);
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

    }

    //Get addresses latitude and longitude using GeoCoderHelper class
    private ArrayList<Float> getCoordinates(String address)
    {
        GeoCoderHelper geoCoderHelper = new GeoCoderHelper();
        return geoCoderHelper.getCoordinates(address, this);
    }
    private String getUrl(LatLng origin, LatLng dest, String directionMode){

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        // Mode
        String mode = "mode=" + directionMode;
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + mode;

        //String parameters = str_origin + "&" + str_dest + 64.080600,%2024.533221" + "&" + mode;
        // Output format
        String output = "json";
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&alternatives=true&key=" + getString(R.string.google_maps_key);
        Log.d("URL_HAKU", url);
        return url;
    }


    //SetRideTaskLoadedCallbackin onTaskDone, piirtää reitin karttaan jos reitin haku onnistuu.
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
            routeDetails.setVisibility(View.VISIBLE);
            distance.setText(currentRoute.rideDistance + " km");
            duration.setText(currentRoute.rideDuration);
        }

        polyline.setClickable(true);
    }

    @Override
    public void onPolylineClick(Polyline clickedPolyline) {

        Log.d("mylog", "onPolylineClick: POLYLINE " + clickedPolyline.getId());

        for(Polyline polyline : allPolylines)
        {
            // Checking for clicked polyline match in list
            if(clickedPolyline.getId().equals(polyline.getId()))
            {
                polyline.setColor(Color.BLUE);
                polyline.setZIndex(1);
                currentRoute = polylineHashMap.get(polyline.getId());
                distance.setText(currentRoute.rideDistance + " km");
                duration.setText(currentRoute.rideDuration);
                Log.d("mylog", "ROUTEINFFOOOOOOOOOOOO: " + polyline.getId());
                Log.d("Polylineclick!", "Clicked polyline with id: " + polyline.getId() + " and route distance: " + polylineHashMap.get(polyline.getId()).rideDistance);
            }
            else
            {
                polyline.setColor(Color.GRAY);
                polyline.setZIndex(0);
            }
        }


        //polyline.setColor(Color.BLUE);
        //polylineHashMap.get(polyline.getId());
        //Log.d("mylog", "onPolylineClick: " + polylineHashMap.get(polyline.getId()));

    }
    public static void hideKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View v = activity.getCurrentFocus();
        if (v == null) { v = new View(activity); }
        inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }
}
