package com.example.carpool_app;

import android.content.Context;
import android.os.AsyncTask;

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

        GetCoordinatesUtility getCoordinatesUtility = null;

        //uses geocoder class to get coordinates to start point and destination
        try {
            float startLat = GeoCoderHelper.getCoordinates(startPoint, context).get(0);
            float startLng = GeoCoderHelper.getCoordinates(startPoint, context).get(1);
            float destinationLat = GeoCoderHelper.getCoordinates(destination, context).get(0);
            float destinationLng = GeoCoderHelper.getCoordinates(destination, context).get(1);

            getCoordinatesUtility = new GetCoordinatesUtility(startLat, startLng, destinationLat, destinationLng);
        } catch(Exception e) { e.printStackTrace(); }
        return getCoordinatesUtility;
    }

    @Override
    protected void onPostExecute(GetCoordinatesUtility getCoordinatesUtility) {
        super.onPostExecute(getCoordinatesUtility);
        if(getCoordinatesInterface != null && getCoordinatesUtility != null)
        {
            getCoordinatesInterface.getCoordinates(getCoordinatesUtility);
        }
    }
}

interface GetCityInterface{
    void getCity(GetCoordinatesUtility getCoordinatesUtility);
}

class GetCityASync extends AsyncTask<Float, Integer, GetCoordinatesUtility> {

    private GetCityInterface getCityInterface;
    private Context context;

    public GetCityASync(GetCityInterface getCityInterface, Context context)
    {
        this.getCityInterface = getCityInterface;
        this.context = context;
    }

    @Override
    protected GetCoordinatesUtility doInBackground(Float... floats) {

        float startLat = floats[0];
        float startLng = floats[1];
        float destinationLat = floats[2];
        float destinationLng = floats[3];

        String startCity = GeoCoderHelper.getCity(startLat, startLng, context);
        String destinationCity = GeoCoderHelper.getCity(destinationLat, destinationLng, context);

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
