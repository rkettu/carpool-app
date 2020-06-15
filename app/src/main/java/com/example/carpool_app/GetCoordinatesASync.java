package com.example.carpool_app;

import android.content.Context;
import android.os.AsyncTask;

import java.util.ArrayList;

interface GetCoordinatesInterface {
    void getCoordinates(GetCoordinatesUtility getCoordinatesUtility);
}

public class GetCoordinatesASync extends AsyncTask<String, Integer, GetCoordinatesUtility> {

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
