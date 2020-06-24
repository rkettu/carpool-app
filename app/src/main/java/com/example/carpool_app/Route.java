package com.example.carpool_app;

import android.util.Log;

import com.google.android.gms.maps.model.PolylineOptions;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

// Not to be confused with "Ride" -class
// Class for holding information about a singular route (coordinate points, distance, duration...)
// Routes are obtained from parsing Google Maps request result json data
// As of 08/06/2020 json request returns 0-3 distinct routes
public class Route implements Serializable {
    List<HashMap<String, String>> allPoints;        // All coordinate points of a route
    List<HashMap<String, String>> selectPoints;    // Every 100th point
    String rideDistance;
    String rideDuration;

    PolylineOptions lineOptions;    // Must be set separately!

    Route(List<HashMap<String, String>> allPoints, List<HashMap<String, String>> selectPoints,
                        String rideDistance, String rideDuration)
    {
        this.allPoints = allPoints;
        this.selectPoints = selectPoints;
        this.rideDistance = rideDistance;
        this.rideDuration = rideDuration;
    }

    public List<HashMap<String, String>> getAllPoints() {
        return allPoints;
    }

    public List<HashMap<String, String>> getSelectPoints() {
        return selectPoints;
    }

    public String getRideDistance() {
        return rideDistance;
    }

    public String getRideDuration() {
        return rideDuration;
    }

    // Function for clearing "all points" -list when not needed
    // Frees up a lot of memory
    public void clearAllPointsList()
    {
        try {
            allPoints.clear();
        } catch(Exception e) {
            // allpoints couldn't be cleared => hasnt been initialized?
            Log.d("ROUTE", "all points couldn't be cleared: " +  e.toString());
        }
    }

    public PolylineOptions getLineOptions() {
        return lineOptions;
    }

    public void setLineOptions(PolylineOptions lineOptions) {
        this.lineOptions = lineOptions;
    }
}
