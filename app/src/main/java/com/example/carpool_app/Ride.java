package com.example.carpool_app;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Ride implements Serializable {
    private String uid;
    private String duration;
    private int pickUpDistance;
    private long leaveTime;
    private String startAddress;
    private String endAddress;
    private int freeSlots;
    private float price;
    private List<HashMap<String,Double>> points;
    private List<String> waypointAddresses;
    private List<String> participants;
    private double distance;
    private String startCity;
    private String endCity;
    private String departureTxt;
    private String luggageTxt;
    private Boolean pets;
    private HashMap<String,Double> bounds;

    //no-arg constructor so doc.toObject can deserialize
    public Ride(){ }

    // Constructor with all parameters - required for document<->object
    // Use when creating a ride
    public Ride(String uid, String duration, long leaveTime,
                 String startAddress, String endAddress, int freeSlots, float price, double distance,

    List<HashMap<String,Double>> points, HashMap<String,Double> bounds, List<String> waypointAddresses,

    List<String> participants, int pickUpDistance, String startCity, String endCity, String departureTxt, String luggageTxt, Boolean pets)
    {
        this.uid = uid;
        this.duration = duration;
        this.pickUpDistance = pickUpDistance;
        this.leaveTime = leaveTime;
        this.startAddress = startAddress;
        this.endAddress = endAddress;
        this.freeSlots = freeSlots;
        this.price = price;
        this.points = points;
        this.bounds = bounds;
        this.waypointAddresses = waypointAddresses;
        this.participants = participants;
        this.distance = distance;
        this.startCity = startCity;
        this.endCity = endCity;
        this.departureTxt = departureTxt;
        this.luggageTxt = luggageTxt;
        this.pets = pets;
    }

    // Getters for all fields - required for document<->object
    public String getUid() {
        return uid;
    }
    public String getDuration() { return duration; }
    public long getLeaveTime() { return leaveTime; }
    public int getPickUpDistance(){ return pickUpDistance; }
    public String getStartAddress() { return startAddress; }
    public String getEndAddress() { return endAddress; }
    public int getFreeSlots() { return freeSlots; }
    public float getPrice() { return price; }
    public List<HashMap<String,Double>> getPoints() { return points; }
    public HashMap<String, Double> getBounds() {
        return bounds;
    }
    public List<String> getWaypointAddresses() { return waypointAddresses; }
    public List<String> getParticipants() { return participants; }
    public double getDistance() { return distance; }
    public String getStartCity() { return startCity; }
    public String getEndCity() { return endCity; }
    public String getDepartureTxt() { return departureTxt; }
    public String getLuggageTxt() { return luggageTxt; }
    public Boolean getPets() { return pets; }

    public void removeFreeSlot() {
        this.freeSlots--;
    }

    public void addToParticipants(String userId) {
        participants.add(userId);
    }

    public void initParticipants()
    {
        participants = new ArrayList<>();
    }

    public void removeFromParticipants(String userId)
    {
        participants.remove(userId);
    }
}
