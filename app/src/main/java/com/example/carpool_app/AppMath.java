package com.example.carpool_app;

import android.util.Log;

import java.util.HashMap;
import java.util.List;

public class AppMath {

    //Haversine algorithm
    private double distanceBetweenCoordinates(double lat1, double lng1, double lat2, double lng2)
    {
        double dLat = Math.toRadians(lat2-lat1);
        double dLon = Math.toRadians(lng2-lng1);

        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        double a = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        return c * 6371;
    }

    private boolean isRouteInRange(float pickupDist, double lat1, double lng1, double lat2, double lng2, List<HashMap<String,String>> points)
    {
        Log.d("HEREWEARE", "again");
        double minDist1 = 10000000;
        double minDist2 = 10000000;
        int index1 = -1;
        int index2 = -1;
        for(int i = 0; i < points.size(); i++)
        {
            // Comparing user start coordinates one at a time with route coordinates
            double routePointLat = Double.parseDouble(points.get(i).get("lat"));
            double routePointLng = Double.parseDouble(points.get(i).get("lng"));
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

        Log.d("HEIHEIHEI", (minDist1 + " " + minDist2 + " " + pickupDist + " " + index1 + " " +index2));
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
}
