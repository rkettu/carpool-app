package com.example.carpool_app;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SetRidePointsParser extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {
    SetRideTaskLoadedCallback taskCallback;
    String directionMode = "driving";
    //private ArrayList<SetRidePolylineData> polylineData = new ArrayList<>();

    public SetRidePointsParser(Context mContext, String directionMode) {
        this.taskCallback = (SetRideTaskLoadedCallback) mContext;
        this.directionMode = directionMode;
    }

    // Parsing the data in non-ui thread
    @Override
    protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

        JSONObject jObject;
        List<List<HashMap<String, String>>> routes = null;

        try {
            jObject = new JSONObject(jsonData[0]);
            Log.d("mylog", jsonData[0].toString());
            SetRideDataParser parser = new SetRideDataParser();
            Log.d("mylog", parser.toString());

            // Starts parsing data
            routes = parser.parse(jObject);
            Log.d("mylog", "Executing routes");
            Log.d("mylog", "koko = " + routes.size() + " " +routes.toString());

        } catch (Exception e) {
            Log.d("mylog", e.toString());
            e.printStackTrace();
        }
        return routes;
    }

    // Executes in UI thread, after the parsing process
    @Override
    protected void onPostExecute(final List<List<HashMap<String, String>>> result) {

        Log.d("mylog", "onPostExecuteTESTI: " + result.size());
        ArrayList<LatLng> points;
        PolylineOptions lineOptions = null;
        // Traversing through all the routes
        for (int i = 0; i < result.size(); i++) {
            points = new ArrayList<>();
            lineOptions = new PolylineOptions();
            // Fetching i-th route
            List<HashMap<String, String>> path = result.get(i);
            // Fetching all the points in i-th route
            for (int j = 0; j < path.size(); j++) {
                HashMap<String, String> point = path.get(j);
                double lat = Double.parseDouble(point.get("lat"));
                double lng = Double.parseDouble(point.get("lng"));
                LatLng position = new LatLng(lat, lng);
                points.add(position);
            }
            // Adding all the points in the route to LineOptions
            lineOptions.addAll(points);
            Log.d("mytag", "onPostExecute: " + points);
            lineOptions.width(20);
            if(i == 0)
            {
                lineOptions.color(Color.BLUE);
                lineOptions.zIndex(1);

            }
            else {
                lineOptions.color(Color.GRAY);
            }

            //poly.add(new SetRidePolylineData(lineOptions, points));
            SetRidePolylineData data = new SetRidePolylineData(lineOptions, points);

            taskCallback.onTaskDone(data);
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

