package com.example.carpool_app;

import android.util.Log;

import java.util.HashMap;
import java.util.List;

public class AppMath {

    //Haversine algorithm
    private static double distanceBetweenCoordinates(double lat1, double lng1, double lat2, double lng2)
    {
        double dLat = Math.toRadians(lat2-lat1);
        double dLon = Math.toRadians(lng2-lng1);

        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        double a = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        return c * 6371;
    }

    // Function for checking if searched start and end coordinates are within the bounds of a ride
    public static boolean areCoordinatesWithinBounds(double lat1, double lng1, double lat2, double lng2, HashMap<String,Double> bounds)
    {
        try
        {
            double northernBound = (double) bounds.get("north");
            double westernBound = (double) bounds.get("west");
            double southernBound = (double) bounds.get("south");
            double easternBound = (double) bounds.get("east");

            return ((lat1 < northernBound && lat2 < northernBound)
                    && (lat1 > southernBound && lat2 > southernBound)
                    && (lng1 < easternBound && lng2 < easternBound)
                    && (lng1 > westernBound && lng2 > westernBound));

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean isRouteInRange(double pickupDist, double lat1, double lng1, double lat2, double lng2, List<HashMap<String, Double>> points)
    {
        double minDist1 = 10000000;
        double minDist2 = 10000000;
        int index1 = -1;
        int index2 = -1;
        for(int i = 0; i < points.size(); i++)
        {
            // Comparing user start coordinates one at a time with route coordinates
            double routePointLat = (Double) points.get(i).get("lat");
            double routePointLng = (Double) points.get(i).get("lng");
            double dist1 = distanceBetweenCoordinates(lat1,lng1,routePointLat,routePointLng);
            double dist2 = distanceBetweenCoordinates(lat2,lng2,routePointLat,routePointLng);
            if(dist1<minDist1)
            {
                // Minimum distance between start coordinate and some route coordinate
                minDist1 = dist1;
                index1 = i;
            }
            if(dist2<minDist2)
            {
                // Between end coord and some route coordinate
                minDist2 = dist2;
                index2 = i;
            }
        }

        // If start coordinate is matched before end coordinate route is going the right way...
        if(index1 < index2)
        {
            // If start and end coordinates are within pickup distance from the route points
            if(minDist1 <= pickupDist && minDist2 <= pickupDist)
            {
                return true;
            }
        }
        return false;
    }

    // Returns approximate latitude change based on kilometers traveled on the latitude axis
    // For example 1 kilometer traveled on the latitude axis equals about 1/110.57 degrees of latitude traveled
    public static double getDeltaLatitude(double kilometers)
    {
        return kilometers / 110.57;
    }

    // Returns approximate longitude change based on kilometers traveled on the longitude axis
    // Requires current latitude to be more precise
    public static double getDeltaLongitude(double kilometers, double currentLatitude)
    {
        return (kilometers / 111.32) * Math.cos(Math.toRadians(currentLatitude));
    }
}
