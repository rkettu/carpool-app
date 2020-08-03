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
    List<HashMap<String, Double>> allPoints;        // All coordinate points of a route
    List<HashMap<String, Double>> selectPoints;    // Every 100th point
    HashMap<String, Double> bounds;                  // Most northern, western etc coordinates, keys are as follows: "north", "south" etc
    String rideDistance;
    String rideDuration;

    PolylineOptions lineOptions;    // Must be set separately!

    Route(List<HashMap<String, Double>> allPoints, List<HashMap<String, Double>> selectPoints,
                        HashMap<String, Double> bounds, String rideDistance, String rideDuration)
    {
        this.allPoints = allPoints;
        this.selectPoints = selectPoints;
        this.bounds = bounds;
        this.rideDistance = rideDistance;
        this.rideDuration = rideDuration;
    }

    public List<HashMap<String, Double>> getAllPoints() {
        return allPoints;
    }

    public List<HashMap<String, Double>> getSelectPoints() {
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
