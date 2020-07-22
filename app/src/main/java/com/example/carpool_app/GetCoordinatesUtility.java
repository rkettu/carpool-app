package com.example.carpool_app;

public class GetCoordinatesUtility {

    private float startLat;
    private float startLng;
    private float destinationLat;
    private float destinationLng;
    private String startCity;
    private String destinationCity;

    public GetCoordinatesUtility(float startLat, float startLng, float destinationLat, float destinationLng)
    {
        this.startLat = startLat;
        this.startLng = startLng;
        this.destinationLat = destinationLat;
        this.destinationLng = destinationLng;
    }

    public GetCoordinatesUtility(String startCity, String destinationCity)
    {
        this.startCity = startCity;
        this.destinationCity = destinationCity;
    }

    public String getStartCity() {
        return startCity;
    }

    public String getDestinationCity() {
        return destinationCity;
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
