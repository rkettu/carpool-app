package com.example.carpool_app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SearchView;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class SetRideActivity extends AppCompatActivity implements OnMapReadyCallback, SetRideTaskLoadedCallback, View.OnClickListener, GoogleMap.OnPolylineClickListener {

    private GoogleMap mMap;

    private SearchView lahtoEditori, loppuEditori;
    Polyline currentPolyline = null;
    //Muuttujat
    private String strLahto, strLoppu;

    //Layoutin valikon animaation asetukset
    Animation ttbAnim, bttAnim;
    private LinearLayout linearContainer;
    private ConstraintLayout routeDetails;
    private Button drawerButton;
    private ImageButton sijaintiButton;
    private boolean drawer_expand = true;

    //Oman sijainnin asetukset
    private FusedLocationProviderClient fusedLocationClient;

    //Reitinhaun muuttujat
    MarkerOptions place1, place2;

    private HashMap<String, ArrayList<LatLng>> polylineHashMap = new HashMap<>();
    private ArrayList<SetRidePolylineData> mPolylinesData = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_ride);

        //Buttonien määritykset
        sijaintiButton = findViewById(R.id.set_ride_sijaintiButton);
        sijaintiButton.setOnClickListener(this);
        findViewById(R.id.set_ride_haeButton).setOnClickListener(this);

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
            mMap.clear();

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
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place1.getPosition(),10));


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
                                GeoCoderHelper geoCoderHelper = new GeoCoderHelper();
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
        return url;
    }


    //SetRideTaskLoadedCallbackin onTaskDone, piirtää reitin karttaan jos reitin haku onnistuu.
    @Override
    public void onTaskDone(Object... values) {

        Log.d("mylog", "onTaskDone: " + values[1]);
        //SetRidePolylineData polylineData = (SetRidePolylineData) values;
        //currentPolyline = mMap.addPolyline(new PolylineOptions().addAll(polylineData.getPolyline()));
        currentPolyline = mMap.addPolyline(new PolylineOptions().addAll((Iterable<LatLng>) values[1]));
        currentPolyline.setColor(Color.GRAY);
        currentPolyline.setClickable(true);

        mPolylinesData.add(new SetRidePolylineData(currentPolyline, (List<LatLng>) values[1]));
        Log.d("mylog", "POLYLINE LISÄTTY " + currentPolyline.getId());

        //polylineHashMap.put(currentPolyline.getId(), polylineData.getLatLngArrayList());
    }

    @Override
    public void onPolylineClick(Polyline polyline) {
        Log.d("mylog", "onPolylineClick: POLYLINE " + polyline.getId());
        //Log.d("mylog", "onPolylineClick HASHMAPPI: " );

        polyline.setColor(Color.BLUE);
        for(SetRidePolylineData polylineData: mPolylinesData)
        {
            if(polyline.getId().equals(polylineData.getPolyline().getId()))
            {
                polylineData.getPolyline().setColor(Color.BLUE);
                polylineData.getPolyline().setZIndex(1);


            }
            else
            {
                polylineData.getPolyline().setColor(Color.GRAY);
                polylineData.getPolyline().setZIndex(0);
            }
        }


        //polyline.setColor(Color.BLUE);
        //polylineHashMap.get(polyline.getId());
        //Log.d("mylog", "onPolylineClick: " + polylineHashMap.get(polyline.getId()));

    }

}
