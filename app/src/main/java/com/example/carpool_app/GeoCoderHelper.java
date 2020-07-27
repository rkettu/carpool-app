package com.example.carpool_app;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GeoCoderHelper {
    //Use this function to get coordinates for specific address
    //If you need latitude only use get(0), If you need longitude only use get(1)
    public static ArrayList<Float> getCoordinates(String address, Context context)
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
            //TODO if grpc failed
            e.printStackTrace();
        }
        return null;
    }

    //You can use this function to get NAME of the CITY only with specific address
    //You may need this if you want print just a city, not full address
    public static String getCity(String address, Context context)
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


    //Palauttaa täydellisen osoitteen oman sijainnin mukaan esim: "Kaarnatie 5, 90530 Oulu, Suomi". Parametrina hakukenttään syötetty osoite
    public static String fullAddress(String address, Context context)
    {
        String geoAddress = "";
        Geocoder geocoder = new Geocoder(context);
        try{
            Log.d("TESTI", "fullAddress try");
            List<Address> addresses2 = geocoder.getFromLocationName(address, 1);
            geoAddress = addresses2.get(0).getAddressLine(0);
            return geoAddress;
        }
        catch (IOException e)
        {
            Log.d("TESTI", "fullAddress catch");
            e.printStackTrace();
        }
        return null;
    }

    //Palauttaa täydellisen osoitteen oman sijainnin mukaan esim: "Kaarnatie 5, 90530 Oulu, Suomi". Parametrina hakukenttään syötetty osoite
    public static String fullAddressLocation(Location location, Context context)
    {
        String geoAddress = "";
        Geocoder geocoder = new Geocoder(context);
        try{
            Log.d("TESTI", "fullAddress try");
            List<Address> addresses2 = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            geoAddress = addresses2.get(0).getAddressLine(0);
            return geoAddress;
        }
        catch (IOException e)
        {
            Log.d("TESTI", "fullAddress catch");
            e.printStackTrace();
        }
        return null;
    }


}
