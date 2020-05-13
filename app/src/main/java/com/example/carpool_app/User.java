package com.example.carpool_app;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class User implements Serializable {
    private String uid;
    private String imgUri;
    private String fname;
    private String lname;
    private String email;
    private String phone;
    private String bio;
    private boolean profileCreated;
    private List<String> bookedRides;
    private float rating;
    private int ratingAmount;

    //no-arg constructor so doc.toObject can deserialize
    public User(){ }

    // Constructor with all parameters - required for document<->object
    // Use when creating a user
    public User(String fname, String lname, String phone,
                String email, String bio, String imgUri, String uid,
                List<String> bookedRides, float rating, int ratingNumber)
    {
        this.fname = fname;
        this.lname = lname;
        this.email = email;
        this.phone = phone;
        this.bio = bio;
        this.imgUri = imgUri;
        this.uid = uid;
        this.bookedRides = bookedRides;
        this.rating = rating;
        this.ratingAmount = ratingNumber;
    }
    public String getFname() {
        return fname;
    }
    public String getLname() {
        return lname;
    }
    public String getEmail() { return email; }
    public String getPhone() {
        return phone;
    }
    public String getBio() { return bio; }
    public String getImgUri() { return imgUri; }
    public String getUid() { return uid; }
    public List<String> getBookedRides() { return bookedRides; }
    public float getRating() { return rating; }
    public int getRatingAmount() { return ratingAmount; }
    public boolean getProfileCreated() { return profileCreated; }

    public void setProfCreated(boolean value) {
        profileCreated = value;
    }

    public void setUid(String strUid) {
        uid = strUid;
    }

    public void setImgUid(String strUri) {
        imgUri = strUri;
    }

    public void addToBookedRides(String routeId) {
        bookedRides.add(routeId);
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public void setRatingAmount(int ratingNumber) {
        this.ratingAmount = ratingNumber;
    }
}
