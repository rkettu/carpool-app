package com.example.carpool_app;

import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

interface GetCoordinatesInterface {
    void getCoordinates(GetCoordinatesUtility getCoordinatesUtility);
}

class GetCoordinatesASync extends AsyncTask<String, Integer, GetCoordinatesUtility> {

    private GetCoordinatesInterface getCoordinatesInterface;
    private Context context;
    private ArrayList<Float> coordinates;

    public GetCoordinatesASync(GetCoordinatesInterface getCoordinatesInterface, Context context)
    {
        this.getCoordinatesInterface = getCoordinatesInterface;
        this.context = context;
    }

    @Override
    protected GetCoordinatesUtility doInBackground(String... strings) {

        String startPoint = strings[0];
        String destination = strings[1];

        //uses geocoder class to get coordinates to start point and destination
        float startLat = GeoCoderHelper.getCoordinates(startPoint, context).get(0);
        float startLng = GeoCoderHelper.getCoordinates(startPoint, context).get(1);
        float destinationLat = GeoCoderHelper.getCoordinates(destination, context).get(0);
        float destinationLng = GeoCoderHelper.getCoordinates(destination, context).get(1);

        Log.d("mylog", "doInBackground: " + startLat + startLng + destinationLat + destinationLng);

        GetCoordinatesUtility getCoordinatesUtility = new GetCoordinatesUtility(startLat, startLng, destinationLat, destinationLng);
        return getCoordinatesUtility;
    }

    @Override
    protected void onPostExecute(GetCoordinatesUtility getCoordinatesUtility) {
        super.onPostExecute(getCoordinatesUtility);
        if(getCoordinatesInterface != null)
        {
            getCoordinatesInterface.getCoordinates(getCoordinatesUtility);
        }
    }
}

interface GetCityInterface{
    void getCity(GetCoordinatesUtility getCoordinatesUtility);
}

class GetCityASync extends AsyncTask<String, Integer, GetCoordinatesUtility> {

    private GetCityInterface getCityInterface;
    private Context context;

    public GetCityASync(GetCityInterface getCityInterface, Context context)
    {
        this.getCityInterface = getCityInterface;
        this.context = context;
    }

    @Override
    protected GetCoordinatesUtility doInBackground(String... floats) {

        String place1 = floats[0];
        String place2 = floats[1];

        String startCity = GeoCoderHelper.getCity(place1, context);
        String destinationCity = GeoCoderHelper.getCity(place2, context);

        return new GetCoordinatesUtility(startCity, destinationCity);
    }

    @Override
    protected void onPostExecute(GetCoordinatesUtility getCoordinatesUtility)
    {
        super.onPostExecute(getCoordinatesUtility);
        if(getCityInterface != null)
        {
            getCityInterface.getCity(getCoordinatesUtility);
        }
    }
}
