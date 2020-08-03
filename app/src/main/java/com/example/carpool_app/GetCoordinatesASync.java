package com.example.carpool_app;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;


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



interface GetFullAddressInterface{
    void getFullAddress(GetCoordinatesUtility getCoordinatesUtility);
}

class GetFullAddressASync extends AsyncTask<String, Integer, GetCoordinatesUtility> {

    private GetFullAddressInterface getFullAddressInterface;
    private Context context;

    public GetFullAddressASync(GetFullAddressInterface getFullAddressInterface, Context context) {
        this.getFullAddressInterface = getFullAddressInterface;
        this.context = context;
    }

    @Override
    protected GetCoordinatesUtility doInBackground(String... address) {
        String place1 = address[0];

        String fullStartAddress = GeoCoderHelper.fullAddress(place1, context);

        return new GetCoordinatesUtility(fullStartAddress);
    }

    @Override
    protected void onPostExecute(GetCoordinatesUtility getCoordinatesUtility)
    {
        super.onPostExecute(getCoordinatesUtility);
        if(getFullAddressInterface != null)
        {
            getFullAddressInterface.getFullAddress(getCoordinatesUtility);
        }
    }
}
