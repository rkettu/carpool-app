package com.example.carpool_app;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;

import java.util.HashMap;
import java.util.List;


public class SetRidePolylineData {

    private Polyline polyline;
    private List<LatLng> latLngArrayList;
    public static HashMap<String, String> routeInfo = new HashMap<>();


    public static HashMap<String, String> getRouteInfo() { return routeInfo; }

    public List<LatLng> getLatLngArrayList() {
        return latLngArrayList;
    }

    public Polyline getPolyline() { return polyline; }

    public static void setRouteInfo(HashMap<String, String> routeInfo) { SetRidePolylineData.routeInfo = routeInfo; }

    public SetRidePolylineData(Polyline polyline, List<LatLng> latLngArrayList) {
        this.polyline = polyline;
        this.latLngArrayList = latLngArrayList;
    }
}
