package com.example.carpool_app;

import java.io.Serializable;

public class RideUser implements Serializable {

    private Ride ride;
    private User user;
    private String rideId;

    public RideUser(Ride ride, User user, String rideId)
    {
        this.ride = ride;
        this.user = user;
        this.rideId = rideId;
    }

    public String getRideId() {
        return rideId;
    }

    public Ride getRide() {
        return ride;
    }

    public User getUser() {
        return user;
    }
}
