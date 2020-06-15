package com.example.carpool_app;

public class GetCoordinatesUtility {

    private float startLat;
    private float startLng;
    private float destinationLat;
    private float destinationLng;

    public GetCoordinatesUtility(float startLat, float startLng, float destinationLat, float destinationLng)
    {
        this.startLat = startLat;
        this.startLng = startLng;
        this.destinationLat = destinationLat;
        this.destinationLng = destinationLng;
    }

    public float getStartLat() {
        return startLat;
    }

    public float getStartLng() {
        return startLng;
    }

    public float getDestinationLat() {
        return destinationLat;
    }

    public float getDestinationLng() {
        return destinationLng;
    }
}
