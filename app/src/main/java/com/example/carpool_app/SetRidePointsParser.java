package com.example.carpool_app;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SetRidePointsParser extends AsyncTask<String, Integer, List<Route>> {
    SetRideTaskLoadedCallback taskCallback;
    String directionMode = "driving";


    public SetRidePointsParser(Context mContext, String directionMode) {
        this.taskCallback = (SetRideTaskLoadedCallback) mContext;
        this.directionMode = directionMode;
    }

    // Parsing the data in non-ui thread
    @Override
    protected List<Route> doInBackground(String... jsonData) {

        JSONObject jObject;
        List<Route> routes = new ArrayList<>();

        try {
            jObject = new JSONObject(jsonData[0]);
            Log.d("mylog", jsonData[0].toString());
            SetRideDataParser parser = new SetRideDataParser();
            Log.d("mylog", parser.toString());

            // Starts parsing data
            routes = parser.parse(jObject);
            Log.d("mylog", "Executing routes");

        } catch (Exception e) {
            Log.d("mylog", e.toString());
            e.printStackTrace();
        }
        return routes;
    }

    // Executes in UI thread, after the parsing process
    @Override
    protected void onPostExecute(List<Route> result) {

        Log.d("mylog", "onPostExecuteTESTI: " + result.size());
        ArrayList<LatLng> points;
        PolylineOptions lineOptions = null;
        // Traversing through all the routes
        for (int i = 0; i < result.size(); i++) {
            points = new ArrayList<>();
            lineOptions = new PolylineOptions();
            // Fetching i-th route
            List<HashMap<String, Double>> path = result.get(i).getAllPoints();
            // Fetching all the points in i-th route
            // And converting from hashmap format to a latlng format
            for (int j = 0; j < path.size(); j++) {
                HashMap<String, Double> point = path.get(j);
                Double lat = point.get("lat");
                Double lng = point.get("lng");
                LatLng position = new LatLng(lat, lng);
                points.add(position);
            }
            // We don't need the big coordinate points list in route anymore so let's free some memory
            result.get(i).clearAllPointsList();

            // Adding all the points in the route to LineOptions
            lineOptions.addAll(points);
            Log.d("mytag", "onPostExecute: " + points);
            lineOptions.width(10);
            if(i == 0)
            {
                // Google Maps returns best route first, so we color route at index 0 blue
                lineOptions.color(Color.BLUE);
                lineOptions.zIndex(1);

            }
            else {
                lineOptions.color(Color.GRAY);
                lineOptions.zIndex(0);
            }

            //Polyline polyline = lineOptions
            //poly.add(new SetRidePolylineData(lineOptions, points));
            //SetRidePolylineData data = new SetRidePolylineData(lineOptions, points);

            // Setting LineOptions to holder object
            result.get(i).setLineOptions(lineOptions);

            taskCallback.onTaskDone(result.get(i)); // This function body is implemented in SetRideActivity
            Log.d("mylog", "onPostExecute lineoptions decoded " + lineOptions.toString());
        }

        // Drawing polyline in the Google Map for the i-th route
        if (lineOptions != null) {
            //mMap.addPolyline(lineOptions);


        } else {
            Log.d("mylog", "without Polylines drawn");
        }
    }
}

