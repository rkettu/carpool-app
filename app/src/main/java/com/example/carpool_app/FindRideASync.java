package com.example.carpool_app;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;

public class FindRideASync extends AsyncTask<String, Integer, String> {

    private Context context;
    private final static String TAG = "FindRideASync";

    public void FindRideASync(Context context)
    {
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... strings)
    {
        String startPoint = strings[0];
        String destination = strings[1];

        float startLatitude = getCoordinates(startPoint).get(0);
        float startLongitude = getCoordinates(startPoint).get(1);
        float destinationLatitude = getCoordinates(destination).get(0);
        float destinationLongitude = getCoordinates(destination).get(1);

        Log.d(TAG, "doInBackground: " + startLatitude + " " + startLongitude + " " + destinationLatitude + " " + destinationLongitude);

        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
    }

    //TODO find matching routes from database
    private void getMatchingRides(final float startLat, final float startLng, final float destinationLat, final float destinationLng)
    {

    }

    //Get addresses latitude and longitude
    private ArrayList<Float> getCoordinates(String address)
    {
        GeoCoderHelper geoCoderHelper = new GeoCoderHelper();
        return geoCoderHelper.getCoordinates(address, context);
    }
}
