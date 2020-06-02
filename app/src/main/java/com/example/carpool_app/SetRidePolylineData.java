package com.example.carpool_app;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;


public class SetRidePolylineData {

    public PolylineOptions getPolylineOptions() {
        return polylineOptions;
    }

    public ArrayList<LatLng> getLatLngArrayList() {
        return latLngArrayList;
    }

    private PolylineOptions polylineOptions;
    private ArrayList<LatLng> latLngArrayList;

    public SetRidePolylineData(PolylineOptions polylineOptions, ArrayList<LatLng> latLngArrayList) {
        this.polylineOptions = polylineOptions;
        this.latLngArrayList = latLngArrayList;
    }

    @Override
    public String toString() {
        return "PolylineData{" +
                "polyline=" + polylineOptions +
                ", leg=" + latLngArrayList +
                '}';
    }
}
