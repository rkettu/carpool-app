package com.example.carpool_app;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GeoCoderHelper {

    //Use this function to get coordinates for specific address
    //If you need latitude only use get(0), If you need longitude only use get(1)
    public ArrayList<Float> getCoordinates(String address, Context context)
    {
        try{
            Geocoder geocoder = new Geocoder(context);
            List<Address> addressPoint = geocoder.getFromLocationName(address, 1);
            Address newAddress = addressPoint.get(0);

            float latitude = (float) newAddress.getLatitude();
            float longitude = (float) newAddress.getLongitude();

            ArrayList<Float> result = new ArrayList<>();
            result.add(latitude);
            result.add(longitude);

            return result;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }


    //You can use this function to get NAME of the CITY only with specific address
    public String getCity(String address, Context context)
    {
        String city = "";
        Geocoder geocoder = new Geocoder(context);
        try{
            List<Address> addresses = geocoder.getFromLocationName(address, 1);
            city = addresses.get(0).getLocality();
            return city;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }
}
