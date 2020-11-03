package com.example.carpool_app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Interface uses 3 different functions
 * showDialog(AlertDialog alertDialog) = is for cancelling progress dialog
 * whenDone() = is called when booking is successful
 * whenFailed() = is called when error occur in booking
 */

interface MainActivityRideDetailsInterface{
    void showDialog(AlertDialog alertDialog);
    void whenDone();
    void whenFailed();
}

/**
 * Async is used for fetching image in background
 * The dialog building happens in pre execute and the dialog is showing in post execute
 *
 * if you press book ride button and confirm it, it will execute the BookTrip() function
 * for database push
 */


public class MainActivityRideDetails extends AsyncTask<Void, Void, Bitmap> {

    private TextView startPointDialog, destinationDialog, leaveTimeDialog, durationDialog, priceDialog, freeSeatsDialog, wayPointsDialog, userNameDialog, distanceDialog, petsDialog;
    private Button bookRideBtn;
    private ImageView profilePicture, closeDialogBtn;
    private Context context;
    private RideDetailsInterface rideDetailsInterface;
    private ArrayList<RideUser> rideUserArrayList;
    private int position;
    private int page;
    private AlertDialog.Builder builder;

    public MainActivityRideDetails(Context context, RideDetailsInterface rideDetailsInterface, ArrayList<RideUser> rideUserArrayList, int position, int page)
    {
        this.context = context;
        this.rideDetailsInterface = rideDetailsInterface;
        this.rideUserArrayList = rideUserArrayList;
        this.position = position;
        this.page = page;
    }

    @Override
    protected void onPreExecute()
    {
        super.onPreExecute();
        //building the alert dialog and giving it layout.
        builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogLayout = inflater.inflate(R.layout.dialog_ride_details, null);

        builder.setView(dialogLayout);
        initDialogLayoutItems(dialogLayout, rideUserArrayList, position);
    }

    @Override
    protected Bitmap doInBackground(Void... voids) {
        //fetching image from database storage and returning it.
        Bitmap fetchImage = null;
        if(rideUserArrayList.get(position).getUser().getImgUri() != null)
        {
            try
            {
                fetchImage = Picasso.with(context).load(rideUserArrayList.get(position).getUser().getImgUri()).get();
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            //blank image in profile
        }
        return fetchImage;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap)
    {
        super.onPostExecute(bitmap);
        final AlertDialog alertDialog = builder.show();
        //set profile picture by using bitmap.
        profilePicture.setImageBitmap(bitmap);
        Log.d("TAG", "onPostE32424xecute: " + rideUserArrayList.get(position).getRideId());
        //adding listener to back arrow
        closeDialogBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        bookRideBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Booked rides list page
                if(page == 0) {
                    try{
                        //Remove the ride from database
                        FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).update(
                                "bookedRides", FieldValue.arrayRemove(rideUserArrayList.get(position).getRideId()));
                        //Add one free seat to database
                        FirebaseFirestore.getInstance().collection("rides").document(rideUserArrayList.get(position).getRideId()).update(
                                "freeSlots", (rideUserArrayList.get(position).getRide().getFreeSlots() + 1));
                        //Remove the current user from participants in ride collection
                        FirebaseFirestore.getInstance().collection("rides").document(rideUserArrayList.get(position).getRideId()).update(
                                "participants", FieldValue.arrayRemove(FirebaseAuth.getInstance().getCurrentUser().getUid()));
                        rideUserArrayList.remove(position);
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                    finally {
                        //notify adapter and go back to Main Activity
                        MainActivityRidesAdapter adapter = new MainActivityRidesAdapter();
                        adapter.notifyDataSetChanged();
                        Intent i = new Intent(context, MainActivity.class);
                        context.startActivity(i);
                    }
                }
                //Offered rides list page
                else {
                    //Delete the ride for each user
                    FirebaseFirestore.getInstance().collection("users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if(task.isSuccessful()) {
                                try{
                                    //for loop for every document, if field "bookedRides" contains the ride user is deleting, remove it.
                                    for(QueryDocumentSnapshot doc : task.getResult()) {
                                        ArrayList<String> rides = (ArrayList<String>) doc.get("bookedRides");
                                        if(rides != null) {
                                            if(rides.contains(rideUserArrayList.get(position).getRideId())) {
                                                FirebaseFirestore.getInstance().collection("users").document(doc.getId()).update(
                                                        "bookedRides", FieldValue.arrayRemove(rideUserArrayList.get(position).getRideId())
                                                );
                                            }
                                        }
                                    }
                                }
                                catch (Exception e) {
                                    e.printStackTrace();
                                }
                                finally {
                                    //Delete the ride from database
                                    FirebaseFirestore.getInstance().collection("rides").document(rideUserArrayList.get(position).getRideId()).delete();
                                    MainActivityRidesAdapter adapter = new MainActivityRidesAdapter();
                                    adapter.notifyDataSetChanged();
                                    Intent i = new Intent(context, MainActivity.class);
                                    context.startActivity(i);
                                }
                            }
                        }
                    });
                }
            }
        });

        //show the dialog
        alertDialog.show();
        alertDialog.getWindow().setBackgroundDrawableResource(R.drawable.background_rating);
        if(rideDetailsInterface != null)
        {
            rideDetailsInterface.showDialog(alertDialog);
        }
    }

    //initializing layout items to alert dialog and give them correct texts.
    private void initDialogLayoutItems(View view, ArrayList<RideUser> rideUserArrayList, int position) {
        userNameDialog = view.findViewById(R.id.rideDetails_userName);
        String userName = rideUserArrayList.get(position).getUser().getFname() + " " + rideUserArrayList.get(position).getUser().getLname();
        userNameDialog.setText(userName);
        startPointDialog = view.findViewById(R.id.rideDetails_startPoint);
        String startPoint = context.getResources().getString(R.string.find_ride_details_startpoint);
        startPointDialog.setText(startPoint + ": " + rideUserArrayList.get(position).getRide().getStartAddress());
        destinationDialog = view.findViewById(R.id.rideDetails_destination);
        String destination = context.getResources().getString(R.string.find_rides_details_destination);
        destinationDialog.setText(destination + ": " + rideUserArrayList.get(position).getRide().getEndAddress());
        leaveTimeDialog = view.findViewById(R.id.rideDetails_leaveTime);
        String leaveTime = context.getResources().getString(R.string.find_ride_details_est_time);
        leaveTimeDialog.setText(leaveTime + ": " + CalendarHelper.getDateTimeString(rideUserArrayList.get(position).getRide().getLeaveTime()));
        durationDialog = view.findViewById(R.id.rideDetails_duration);
        String duration = context.getResources().getString(R.string.find_rides_details_est_duration);
        durationDialog.setText(duration + ": " + rideUserArrayList.get(position).getRide().getDuration());
        distanceDialog = view.findViewById(R.id.rideDetails_distance);
        String distance = context.getResources().getString(R.string.find_ride_details_distance);
        distanceDialog.setText(distance + ": " + rideUserArrayList.get(position).getRide().getDistance());
        priceDialog = view.findViewById(R.id.rideDetails_price);
        String pricePer100 = String.format("%.2f", rideUserArrayList.get(position).getRide().getPrice() * 100);
        String price = context.getResources().getString(R.string.find_ride_details_price);
        String per100 = context.getResources().getString(R.string.find_ride_details_per_100_km);
        priceDialog.setText(price + ": " + pricePer100 + " " + per100);
        freeSeatsDialog = view.findViewById(R.id.rideDetails_freeSeats);
        String freeSeat = context.getResources().getString(R.string.find_ride_details_free_seats);
        freeSeatsDialog.setText(freeSeat + ": " + rideUserArrayList.get(position).getRide().getFreeSlots());
        petsDialog = view.findViewById(R.id.rideDetails_pets);
        if(rideUserArrayList.get(position).getRide().getPets()){
            petsDialog.setText(context.getResources().getString(R.string.find_ride_details_pet_true));
        }
        else{
            petsDialog.setText(context.getResources().getString(R.string.find_ride_details_pet_false));
        }
        wayPointsDialog = view.findViewById(R.id.rideDetails_wayPoints);
        Log.d("TAG", "initDialogLayoutItems1: ");
        Log.d("TAG", "initDialogLayoutItems: " + rideUserArrayList.get(position).getRide().getWaypointAddresses());
        //checks is the 0, 1 or 2 way points and print them if there is way points.
        if(rideUserArrayList.get(position).getRide().getWaypointAddresses().size() != 0)
        {
            wayPointsDialog.setVisibility(View.VISIBLE);
            if(rideUserArrayList.get(position).getRide().getWaypointAddresses().size() == 1)
            {
                Log.d("TAG", "initDialogLayoutItems: if lause");
                wayPointsDialog.setText(rideUserArrayList.get(position).getRide().getWaypointAddresses().get(0));
            }
            else
            {
                Log.d("TAG", "initDialogLayoutItems: else lause");
                wayPointsDialog.setText(rideUserArrayList.get(position).getRide().getWaypointAddresses().get(0) + "\n" +
                        rideUserArrayList.get(position).getRide().getWaypointAddresses().get(1));
            }
        }
        else
        {
            Log.d("TAG", "initDialogLayoutItems: visibility gone");
            wayPointsDialog.setVisibility(View.GONE);
        }
        bookRideBtn = view.findViewById(R.id.rideDetails_bookRideButton);
        bookRideBtn.setText(context.getResources().getString(R.string.main_activity_ride_details_cancel_trip));
        closeDialogBtn =  view.findViewById(R.id.rideDetails_backButton);
        profilePicture = view.findViewById(R.id.rideDetails_profileImage);

        Log.d("TAG", "initDialogLayoutItems2: ");
    }
}

