package com.example.carpool_app;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.RestrictionEntry;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.core.Query;

import java.util.ArrayList;

interface FindRideInterface{
    void getRideData(Ride ride);
    void getUserData(User user);
}

public class FindRideASync extends AsyncTask<String, Integer, Void> {

    private FindRideInterface findRideInterface;
    private Context context;
    private CollectionReference rideReference = FirebaseFirestore.getInstance().collection("rides");
    private CollectionReference userReference = FirebaseFirestore.getInstance().collection("users");
    private final static String TAG = "FindRideASync";
    private float startLatitude, startLongitude, destinationLatitude, destinationLongitude;
    private ProgressDialog progressDialog;

    //ASyncTasks constructor
    public void FindRideASync(FindRideInterface findRideInterface, Context context)
    {
        this.findRideInterface = findRideInterface;
        this.context = context;
    }

    //If you have to do something before executing ASyncTask
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Finding matching routes");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    //If you have to do something in apps background
    @Override
    protected Void doInBackground(String... strings)
    {
        String startPoint = strings[0];
        String destination = strings[1];

        startLatitude = getCoordinates(startPoint).get(0);
        startLongitude = getCoordinates(startPoint).get(1);
        destinationLatitude = getCoordinates(destination).get(0);
        destinationLongitude = getCoordinates(destination).get(1);

        //TODO get time in millis
        try
        {
             getMatchingRides(startLatitude, startLongitude, destinationLatitude, destinationLongitude);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            progressDialog.dismiss();
        }

        return null;
    }

    //Do something on post execute
    @Override
    protected void onPostExecute(Void aVoid)
    {
        super.onPostExecute(aVoid);
        progressDialog.dismiss();
    }

    //TODO find matching routes from database
    private void getMatchingRides(final float startLat, final float startLng, final float destinationLat, final float destinationLng)
    {
        //TODO algorithm, exceptions and testing
       rideReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
           @Override
           public void onComplete(@NonNull Task<QuerySnapshot> task)
           {
               if(task.isSuccessful())
               {
                   for(QueryDocumentSnapshot rideDoc : task.getResult()){
                       try
                       {
                           //Adds data to ride class from database
                           Ride ride = rideDoc.toObject(Ride.class);
                           final String rideUid = ride.getUid();
                           findRideInterface.getRideData(ride);

                           //uses uid to get correct provider data from database
                           userReference.document(rideUid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                               @Override
                               public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                   DocumentSnapshot userDoc = task.getResult();
                                   try
                                   {
                                       if(userDoc.exists())
                                       {
                                           //Adds data to user class from database
                                           User user = userDoc.toObject(User.class);
                                           findRideInterface.getUserData(user);
                                       }
                                   }
                                   catch (Exception e)
                                   {
                                       //Cant find userDoc DocumentSnapshot
                                       e.printStackTrace();
                                       Log.d(TAG, "userReference" + e.toString());
                                   }
                               }
                           });
                       }
                       catch (Exception e)
                       {
                           //Cannot Add data to ride class
                           e.printStackTrace();
                           Log.d(TAG, "rideReference" + e.toString());
                       }
                   }
               }
               else{
                   //Task is not successful
               }
           }
       });
    }

    //Get addresses latitude and longitude using GeoCoderHelper class
    private ArrayList<Float> getCoordinates(String address)
    {
        GeoCoderHelper geoCoderHelper = new GeoCoderHelper();
        return geoCoderHelper.getCoordinates(address, context);
    }
}
