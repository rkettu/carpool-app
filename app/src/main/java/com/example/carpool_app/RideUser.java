package com.example.carpool_app;

import java.io.Serializable;

public class RideUser implements Serializable {

    private Ride ride;
    private User user;

    public RideUser(Ride ride, User user)
    {
        this.ride = ride;
        this.user = user;
    }

    public Ride getRide() {
        return ride;
    }

    public User getUser() {
        return user;
    }
}
