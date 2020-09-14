package com.example.carpool_app;

import android.util.Log;

import com.google.firebase.functions.FirebaseFunctions;

import java.util.HashMap;
import java.util.Map;

public class CloudFunctionsInterface {

    // TEST FUNCTION
    // delete when not needed?
    public static void testFunction()
    {
        Map<String, Object> data = new HashMap<>();
        data.put("teksti", "heippa");
        data.put("lukum", 5);

        String resultText = FirebaseFunctions.getInstance().getHttpsCallable("testFunction").call(data).getResult().toString();
        Log.d("CLOUDFUNCTIONS_TEST", "Test function result: " + resultText);
    }

    // Applies rating between 1-5 to given target uid's user
    public static void addRating(int rating, String targetUid)
    {
        Map<String, Object> data = new HashMap<>();
        data.put("givenRating", rating);
        data.put("targetUid", targetUid);

        FirebaseFunctions.getInstance().getHttpsCallable("addRating").call(data);

        // Check for success/failure
    }

    // Books ride for current user
    // TODO: Needs further specifying in cloud regarding the participants list
    public static void bookRide(String rideId)
    {
        Map<String, Object> data = new HashMap<>();
        data.put("rideId", rideId);

        FirebaseFunctions.getInstance().getHttpsCallable("bookRide").call(data);

        // Check for success/failure
    }

    // NOT YET IMPLEMENTED IN CLOUD
    // TODO: NEEDS SPECIFYING bookRide FIRST!
    // Cancels specified ride booking for current user
    public static void cancelRide(String rideId)
    {
        // NOT YET IMPLEMENTED IN CLOUD
        Map<String, Object> data = new HashMap<>();
        data.put("rideId", rideId);

        FirebaseFunctions.getInstance().getHttpsCallable("bookRide").call(data);
       // NOT YET IMPLEMENTED IN CLOUD
    }

    // NOT YET IMPLEMENTED IN CLOUD
    // TODO: NEEDS SPECIFYING bookRide AND cancelRide FIRST!
    // Removes ride according to the given rideId
    // Only works for current user's own rides
    public static void removeCreatedRide(String rideId)
    {
        // NOT YET IMPLEMENTED IN CLOUD
        Map<String, Object> data = new HashMap<>();
        data.put("rideId", rideId);

        FirebaseFunctions.getInstance().getHttpsCallable("bookRide").call(data);
        // NOT YET IMPLEMENTED IN CLOUD
    }


    // Creates user document with given info
    // Use when creating new user
    public static void createUserDocument(String fname, String lname, String imgUri, String phone)
    {
        Map<String, Object> data = new HashMap<>();
        data.put("fname", fname);
        data.put("lname", lname);
        data.put("imgUri", imgUri);
        data.put("phone", phone);

        FirebaseFunctions.getInstance().getHttpsCallable("createUserDocument").call(data);

        // Check for success/failure
    }
}
