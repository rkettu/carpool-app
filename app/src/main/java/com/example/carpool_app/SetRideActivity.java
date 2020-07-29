package com.example.carpool_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.media.audiofx.Equalizer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class SetRideActivity extends AppCompatActivity implements Serializable, OnMapReadyCallback, SetRideTaskLoadedCallback, View.OnClickListener, GoogleMap.OnPolylineClickListener {


    private GoogleMap mMap;

    private SearchView lahtoEditori, loppuEditori, waypointEditor1, waypointEditor2;

    private String strLahto, strLoppu, strWaypoint1, strWaypoint2;

    //Layoutin valikon animaation asetukset
    Animation ttbAnim, bttAnim;
    private LinearLayout linearContainer;
    private ConstraintLayout routeDetails;
    private Button drawerButton, nextBtn;
    private ImageButton sijaintiButton, waypointRemoveBtn1, waypointRemoveBtn2;
    private boolean drawer_expand = true;

    List<Polyline> allPolylines = new ArrayList<>(); // Contains all currently drawn polyline data, REMEMBER TO CLEAR

    //Oman sijainnin asetukset
    FusedLocationProviderClient fusedLocationClient;
    GeoCoderHelper geoCoderHelper = new GeoCoderHelper();
    int LOCATION_REQUEST_CODE = 10001;

    //Reitinhaun muuttujat
    private MarkerOptions place1, place2, wayPoint1, wayPoint2;
    TextView distance, duration;
    private int lukitus = 0;

    private double startLat, startLng, stopLat, stopLng;

    private String fastestRoute;
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
        findViewById(R.id.set_ride_etappiBtn).setOnClickListener(this);
        waypointRemoveBtn1 = (ImageButton)findViewById(R.id.set_ride_etappiRemoveBtn);
        waypointRemoveBtn2 = (ImageButton)findViewById(R.id.set_ride_etappiRemoveBtn2);
        waypointRemoveBtn1.setOnClickListener(this);
        waypointRemoveBtn2.setOnClickListener(this);
        nextBtn = (Button) findViewById(R.id.set_ride_nextBtn);
        nextBtn.setOnClickListener(this);
        nextBtn.setEnabled(false);

        //editorit
        lahtoEditori = (SearchView) findViewById(R.id.set_ride_lahtoEdit);
        loppuEditori = (SearchView) findViewById(R.id.set_ride_maaranpaaEdit);
        waypointEditor1 = (SearchView) findViewById(R.id.set_ride_etappiEdit);
        waypointEditor2 = (SearchView) findViewById(R.id.set_ride_etappiEdit2);

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
        if (v.getId() == R.id.set_ride_etappiBtn & lukitus == 0)
        {
            waypointEditor1.setVisibility(View.VISIBLE);
            waypointRemoveBtn1.setVisibility(View.VISIBLE);
            lukitus = 1;

        }
        else if (v.getId() == R.id.set_ride_etappiBtn & lukitus == 1)
        {
            waypointEditor2.setVisibility(View.VISIBLE);
            waypointRemoveBtn2.setVisibility(View.VISIBLE);
        }
        else if (v.getId() == R.id.set_ride_etappiRemoveBtn)
        {
            waypointEditor1.setVisibility(View.GONE);
            waypointRemoveBtn1.setVisibility(View.GONE);
            strWaypoint1 = "";
            waypointEditor1.setQuery(strWaypoint1, true);
            lukitus = 0;
        }
        else if (v.getId() == R.id.set_ride_etappiRemoveBtn2)
        {
            waypointEditor2.setVisibility(View.GONE);
            waypointRemoveBtn2.setVisibility(View.GONE);
            strWaypoint2 = "";
            waypointEditor2.setQuery(strWaypoint2, true);
        }
        else if(v.getId() == R.id.set_ride_haeButton)
        {
            //Waypointtien tyhjennys, mikäli hakee reittiä uudelleen
            wayPoint1 = null;
            wayPoint2 = null;

            //Reitin haku napin toiminnallisuus
            mMap.clear();   // Clearing map markers and polylines
            allPolylines.clear(); // Clearing list containing references to those polylines => frees their memory
            polylineHashMap.clear();

            //Näppäimistön piilotus
            Constant.hideKeyboard(SetRideActivity.this);

            //Nostaa reitin tiedot elementin ylös painettua "hae reitti" nappia
            //doAnimation(bttAnim);

            strLahto = lahtoEditori.getQuery().toString();
            strLoppu = loppuEditori.getQuery().toString();
            strWaypoint1 = waypointEditor1.getQuery().toString();
            strWaypoint2 = waypointEditor2.getQuery().toString();

            if(strWaypoint1 != null && !strWaypoint1.isEmpty())
            {
                //Hakee coordinaatit waypointeille
                GetCoordinatesASync getCoordinatesASync1 = new GetCoordinatesASync(new GetCoordinatesInterface() {
                    @Override
                    public void getCoordinates(GetCoordinatesUtility getCoordinatesUtility1) {
                        double way1Lat = getCoordinatesUtility1.getStartLat();
                        double way1Lng = getCoordinatesUtility1.getStartLng();
                        double way2Lat = getCoordinatesUtility1.getDestinationLat();
                        double way2Lng = getCoordinatesUtility1.getDestinationLng();
                        Log.d("mylog", "getCoordinates: " + way1Lat + " " + way1Lng + " " + way2Lat);
                        wayPoint1 = new MarkerOptions().position(new LatLng(way1Lat, way1Lng)).title("Pysähdys 1");
                        wayPoint2 = new MarkerOptions().position(new LatLng(way2Lat, way2Lng)).title("Pysähdys 2");

                    }
                }, SetRideActivity.this);
                getCoordinatesASync1.execute(strWaypoint1, strWaypoint2);
            }


            //Hakee täydellisen osoitteen ("kaarnatie 5, 90350 Oulu, Suomi") LÄHTÖPISTE tekstikenttään
            GetFullAddressASync getFullAddressASync = new GetFullAddressASync(new GetFullAddressInterface() {
                @Override
                public void getFullAddress(GetCoordinatesUtility getCoordinatesUtility) {
                    String address = getCoordinatesUtility.getFullAddress();
                    lahtoEditori.setQuery(address,false);
                    if(address == null){
                        Toast.makeText(SetRideActivity.this, "Tarkista lähtöosoite", Toast.LENGTH_LONG).show();
                    }
                }
            }, SetRideActivity.this);
            getFullAddressASync.execute(strLahto);

            //Hakee täydellisen osoitteen ("kaarnatie 5, 90350 Oulu, Suomi") MÄÄRÄNPÄÄ tekstikenttään
            GetFullAddressASync getFullAddressASync2 = new GetFullAddressASync(new GetFullAddressInterface() {
                @Override
                public void getFullAddress(GetCoordinatesUtility getCoordinatesUtility) {
                    String address = getCoordinatesUtility.getFullAddress();
                    loppuEditori.setQuery(address,false);
                    if(address == null){
                        Toast.makeText(SetRideActivity.this, "Tarkista määränpään osoite", Toast.LENGTH_LONG).show();
                    }
                }
            }, SetRideActivity.this);
            getFullAddressASync2.execute(strLoppu);

            //Getting start and destination coordinates in asynctask
            GetCoordinatesASync getCoordinatesASync = new GetCoordinatesASync(new GetCoordinatesInterface() {
                @Override
                public void getCoordinates(GetCoordinatesUtility getCoordinatesUtility) {
                    Log.d("mylog", "täälääääfdsafasdfa " );

                    try {
                        startLat = getCoordinatesUtility.getStartLat();
                        startLng = getCoordinatesUtility.getStartLng();
                        stopLat = getCoordinatesUtility.getDestinationLat();
                        stopLng = getCoordinatesUtility.getDestinationLng();

                        Log.d("mylog", "getCoordinates: " + startLat + startLng + stopLat + stopLng);


                        place1 = new MarkerOptions().position(new LatLng(startLat, startLng)).title("Location 1");
                        place2 = new MarkerOptions().position(new LatLng(stopLat, stopLng)).title("Location 2");
                        new SetRideFetchURL(SetRideActivity.this).execute(getUrl(place1.getPosition(), place2.getPosition(),"driving"), "driving");
                        routeDetails.setVisibility(View.VISIBLE);

                        mMap.addMarker(place1);
                        mMap.addMarker(place2);
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place2.getPosition(),8));
                    }catch (Exception e){
                        //ei toimi
                        Toast.makeText(SetRideActivity.this, "Tarkista lähtö- ja määränpää osoitteet", Toast.LENGTH_LONG).show();
                    }

                }
            }, SetRideActivity.this);
            getCoordinatesASync.execute(strLahto, strLoppu);

            }


        else if(v.getId() == R.id.set_ride_nextBtn)
        {

            //Hakee lähtö ja määränpää kaupungit kirjoitetun osoitteen perusteella, minkä jälkeen siirtyy Details activityyn.
            GetCityASync getCityASync = new GetCityASync(new GetCityInterface() {
                @Override
                public void getCity(GetCoordinatesUtility getCity) {
                    String startCity = getCity.getStartCity();
                    String endCity = getCity.getDestinationCity();

                    Log.d("mylog", "startCity: " + startCity + " endCity: " + endCity);

                    Intent details = new Intent(SetRideActivity.this, SetRideDetailsActivity.class);

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
            }, SetRideActivity.this);
            getCityASync.execute(strLahto, strLoppu);
        }

        //Sijainti napin toiminnallisuus
        else if(v.getId() == R.id.set_ride_sijaintiButton)
        {
            if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            {
                //Location granted
                AsyncTaskGetLocation getLocation = new AsyncTaskGetLocation();
                getLocation.execute();
            }
            else {
                //Location not granted
                askLocationPermission();
            }
        }

    }

    //Kysyy käyttäjältä luvan käyttää sijaintia
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
                AsyncTaskGetLocation getLocation = new AsyncTaskGetLocation();
                getLocation.execute();

            }else {
                //Permission not granted
                if(!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)){
                    //This block here means PERMANENTLY DENIED PERMISSION
                    new AlertDialog.Builder(SetRideActivity.this)
                            .setMessage("You have permanently danied this permission, go to settings to enable this permission")
                            .setPositiveButton("Go to settings", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    gotoApplicationSettings();
                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .setCancelable(false)
                            .show();
                } else {
                    Log.d("mylog", "Permission not granted " );
                }
            }
        }
    }

    //Tämä funktio heittää ohjelman asetuksiin
    private void gotoApplicationSettings(){
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", this.getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);

    }


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

        if(wayPoint1 != null)
        {
            parameters = str_origin + "&" + str_dest + "&waypoints=via:" + wayPoint1.getPosition().latitude
                    + "," + wayPoint1.getPosition().longitude + "&" + mode;
        }
        if(wayPoint1 != null & wayPoint2 != null)
        {
            parameters = str_origin + "&" + str_dest + "&waypoints=via:" + wayPoint1.getPosition().latitude
                    + "," + wayPoint1.getPosition().longitude + "|via:" + wayPoint2.getPosition().latitude
                    + "," + wayPoint2.getPosition().longitude + "&" + mode;
        }
        if(wayPoint2 != null & wayPoint1 == null)
        {
            parameters = str_origin + "&" + str_dest + "&waypoints=via:" + wayPoint2.getPosition().latitude
                    + "," + wayPoint2.getPosition().longitude + "&" + mode;
        }

        // Output format
        String output = "json";
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters
                + "&alternatives=true&key=" + getString(R.string.google_maps_key);

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
            fastestRoute = allPolylines.get(0).getId();
            routeDetails.setVisibility(View.VISIBLE);
            duration.setTextColor(Color.GREEN);
            distance.setText(currentRoute.rideDistance + " km");
            duration.setText(currentRoute.rideDuration);
            nextBtn.setEnabled(true);
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
                //Vaihtaa durationin vihreäksi jos klikattu reitti on nopein
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


    private class AsyncTaskGetLocation extends AsyncTask<Void, Void, String> {

        private String geoAddress;
        @Override
        protected String doInBackground(Void... voids) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(SetRideActivity.this);
            Task<Location> locationTask = fusedLocationClient.getLastLocation();
            locationTask.addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if(location != null){
                        //we have a location
                        Log.d("mylog", "LOCATION found: " + location.toString());
                        geoAddress = geoCoderHelper.fullAddressLocation(location, SetRideActivity.this);
                        lahtoEditori.setQuery(geoAddress, false);
                    }else {
                        Log.d("mylog", "LOCATION was null... ");

                    }
                }
            });

            locationTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("mylog", "LOCATION onFailure: " + e.getLocalizedMessage());
                }
            });

            return null;
        }

    }
}

