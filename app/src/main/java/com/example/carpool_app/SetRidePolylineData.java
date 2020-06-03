package com.example.carpool_app;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;


public class SetRidePolylineData {


    public List<LatLng> getLatLngArrayList() {
        return latLngArrayList;
    }

    public Polyline getPolyline() { return polyline; }

    private Polyline polyline;
    private List<LatLng> latLngArrayList;

    public List<String> getDuration() {
        return duration;
    }

    public void setDuration(List<String> duration) {
        this.duration = duration;
    }

    private List<String> duration;

    public SetRidePolylineData(Polyline polyline, List<LatLng> latLngArrayList) {
        this.polyline = polyline;
        this.latLngArrayList = latLngArrayList;
    }

    @Override
    public String toString() {
        return "PolylineData{" +
                "polyline=" + polyline +
                ", leg=" + latLngArrayList +
                '}';
    }
}
