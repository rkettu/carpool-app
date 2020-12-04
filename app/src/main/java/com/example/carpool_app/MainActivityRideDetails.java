package com.example.carpool_app;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.NestedScrollView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Interface uses 3 different functions
 * showDialog(AlertDialog alertDialog) = is for cancelling progress dialog
 * whenDone() = is called when booking is successful
 * whenFailed() = is called when error occur in booking
 */

interface MainActivityRideDetailsInterface{
    void showDialog();
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

public class MainActivityRideDetails extends AsyncTask<Void, Void, Bitmap>{

    private TextView startPointDialog, destinationDialog, leaveTimeDialog, durationDialog, priceDialog, participantsDialog,
                freeSeatsDialog, wayPointsDialog, userNameDialog, phoneNumberDialog, distanceDialog, petsDialog, departureTxt, luggageTxt;
    private Button posBtn, negBtn;
    private ImageView profilePicture, closeDialogBtn;
    private NestedScrollView detailsView;
    private ConstraintLayout layoutView;
    private View dialogLayout;
    private Context context;
    private MainActivityRideDetailsInterface mainActivityRideDetailsInterface;
    private ArrayList<RideUser> rideUserArrayList;
    private int position;
    private int page;
    private AlertDialog.Builder builder;
    private ArrayList<User> participantsList = new ArrayList<>();

    public MainActivityRideDetails(Context context, MainActivityRideDetailsInterface mainActivityRideDetailsInterface, ArrayList<RideUser> rideUserArrayList, int position, int page)
    {
        this.context = context;
        this.mainActivityRideDetailsInterface = mainActivityRideDetailsInterface;
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
        dialogLayout = inflater.inflate(R.layout.dialog_main_activity_ride_details, null);

        //init layout items
        detailsView = dialogLayout.findViewById(R.id.main_ride_details_scrollview);
        userNameDialog = dialogLayout.findViewById(R.id.main_ride_details_user_name);
        String userName = rideUserArrayList.get(position).getUser().getFname() + " " + rideUserArrayList.get(position).getUser().getLname();
        userNameDialog.setText(userName);
        phoneNumberDialog = dialogLayout.findViewById(R.id.main_ride_details_phone_number);
        phoneNumberDialog.setVisibility(View.GONE);
        participantsDialog = dialogLayout.findViewById(R.id.main_ride_participants);
        participantsDialog.setVisibility(View.GONE);
        if(page == 0){
            phoneNumberDialog.setVisibility(View.VISIBLE);
            phoneNumberDialog.setText(rideUserArrayList.get(position).getUser().getPhone());
        }
        else if(page == 1 && rideUserArrayList.get(position).getRide().getParticipants().size() > 0){
            participantsDialog.setVisibility(View.VISIBLE);
        }
        else{
            participantsDialog.setVisibility(View.VISIBLE);
            participantsDialog.setText("No participants");
        }
        startPointDialog = dialogLayout.findViewById(R.id.main_ride_details_start_point);
        String startPoint = context.getResources().getString(R.string.find_ride_details_startpoint);
        startPointDialog.setText(startPoint + ": " + rideUserArrayList.get(position).getRide().getStartAddress());
        destinationDialog = dialogLayout.findViewById(R.id.main_ride_details_destination);
        String destination = context.getResources().getString(R.string.find_rides_details_destination);
        destinationDialog.setText(destination + ": " + rideUserArrayList.get(position).getRide().getEndAddress());
        leaveTimeDialog = dialogLayout.findViewById(R.id.main_ride_details_leave_time);
        String leaveTime = context.getResources().getString(R.string.find_ride_details_est_time);
        leaveTimeDialog.setText(leaveTime + ": " + CalendarHelper.getDateTimeString(rideUserArrayList.get(position).getRide().getLeaveTime()));
        durationDialog = dialogLayout.findViewById(R.id.main_ride_details_duration);
        String duration = context.getResources().getString(R.string.find_rides_details_est_duration);
        durationDialog.setText(duration + ": " + rideUserArrayList.get(position).getRide().getDuration());
        distanceDialog = dialogLayout.findViewById(R.id.main_ride_details_distance);
        String distance = context.getResources().getString(R.string.find_ride_details_distance);
        distanceDialog.setText(distance + ": " + rideUserArrayList.get(position).getRide().getDistance());
        priceDialog = dialogLayout.findViewById(R.id.main_ride_details_price);
        String pricePer100 = String.format("%.2f", rideUserArrayList.get(position).getRide().getPrice() * 100);
        String price = context.getResources().getString(R.string.find_ride_details_price);
        String per100 = context.getResources().getString(R.string.find_ride_details_per_100_km);
        priceDialog.setText(price + ": " + pricePer100 + " " + per100);
        freeSeatsDialog = dialogLayout.findViewById(R.id.main_ride_details_free_seats);
        String freeSeat = context.getResources().getString(R.string.find_ride_details_free_seats);
        freeSeatsDialog.setText(freeSeat + ": " + rideUserArrayList.get(position).getRide().getFreeSlots());
        petsDialog = dialogLayout.findViewById(R.id.main_ride_details_pets);
        if(rideUserArrayList.get(position).getRide().getPets()){
            petsDialog.setText(context.getResources().getString(R.string.find_ride_details_pet_true));
        }
        else{
            petsDialog.setText(context.getResources().getString(R.string.find_ride_details_pet_false));
        }
        wayPointsDialog = dialogLayout.findViewById(R.id.main_ride_details_way_points);
        wayPointsDialog.setVisibility(View.GONE);
        //checks is the 0, 1 or 2 way points and print them if there is way points.
        if(rideUserArrayList.get(position).getRide().getWaypointAddresses().size() > 0)
        {
            wayPointsDialog.setVisibility(View.VISIBLE);
            for(int i = 0; i < rideUserArrayList.get(position).getRide().getWaypointAddresses().size(); i++){
                wayPointsDialog.append(rideUserArrayList.get(position).getRide().getWaypointAddresses().get(i));
            }
        }

        departureTxt = dialogLayout.findViewById(R.id.main_ride_details_departure_txt);
        if(rideUserArrayList.get(position).getRide().getDepartureTxt() != null){
            departureTxt.setMovementMethod(new ScrollingMovementMethod());
            departureTxt.setText(rideUserArrayList.get(position).getRide().getDepartureTxt());
        }
        else{
            departureTxt.setText(context.getResources().getString(R.string.find_ride_details_no_departure));
        }

        luggageTxt = dialogLayout.findViewById(R.id.main_ride_details_luggage_txt);
        if(rideUserArrayList.get(position).getRide().getLuggageTxt() != null){
            luggageTxt.setMovementMethod(new ScrollingMovementMethod());
            luggageTxt.setText(rideUserArrayList.get(position).getRide().getLuggageTxt());
        }
        else{
            luggageTxt.setText(context.getResources().getString(R.string.find_ride_details_no_luggage));
        }

        posBtn = dialogLayout.findViewById(R.id.main_ride_details_pos_btn);
        posBtn.setText(context.getResources().getString(R.string.main_activity_ride_details_cancel_trip));
        negBtn = dialogLayout.findViewById(R.id.main_ride_details_neg_btn);
        negBtn.setText(context.getResources().getString(R.string.getride_dialog_close));

        closeDialogBtn =  dialogLayout.findViewById(R.id.main_ride_details_back_arrow);
        profilePicture = dialogLayout.findViewById(R.id.main_ride_details_picture);
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
        profilePicture.setImageBitmap(bitmap);

        final AlertDialog alertDialog = builder.create();
        //adding listener to back arrow
        closeDialogBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        //cancel the trip button
        posBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Booked rides list page
                AlertDialog.Builder adb = new AlertDialog.Builder(context);
                if(page == 0) {
                    adb.setCancelable(true);
                    adb.setTitle(context.getResources().getString(R.string.warning));
                    adb.setMessage(context.getResources().getString(R.string.main_activity_ride_details_delete_booked_ride));
                    adb.setNegativeButton(context.getResources().getText(R.string.no), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

                    adb.setPositiveButton(context.getResources().getText(R.string.yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
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
                                mainActivityRideDetailsInterface.whenFailed();
                            }
                            finally {
                                mainActivityRideDetailsInterface.whenDone();
                            }
                        }
                    });
                }
                //Offered rides list page
                else {
                    adb.setCancelable(true);
                    adb.setTitle(context.getResources().getString(R.string.warning));
                    adb.setMessage(context.getResources().getString(R.string.main_activity_ride_details_delete_offered_ride));
                    adb.setNegativeButton(context.getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

                    adb.setPositiveButton(context.getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
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
                                            mainActivityRideDetailsInterface.whenFailed();
                                        }
                                        finally {
                                            //Delete the ride from database
                                            FirebaseFirestore.getInstance().collection("rides").document(rideUserArrayList.get(position).getRideId()).delete();
                                            mainActivityRideDetailsInterface.whenDone();
                                        }
                                    }
                                }
                            });
                        }
                    });
                }
                AlertDialog dialog = adb.show();
                dialog.show();
            }
        });

        //close dialog button
        negBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.cancel();
            }
        });

        alertDialog.getWindow().setBackgroundDrawableResource(R.drawable.background_rating);

        //can add all the information about ride participants
        if(rideUserArrayList.get(position).getRide().getParticipants().size() > 0) {
            final int[] taskCount = {rideUserArrayList.get(position).getRide().getParticipants().size()};
            for (int i = 0; i < rideUserArrayList.get(position).getRide().getParticipants().size(); i++) {
                FirebaseFirestore.getInstance().collection("users").document(rideUserArrayList.get(position).getRide().getParticipants().get(i)).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.d("TAG", "onPostExecute: " + taskCount[0]);
                            DocumentSnapshot doc = task.getResult();
                            if (doc.exists()) {
                                User user = doc.toObject(User.class);
                                participantsDialog.append(user.getFname() + " " + user.getLname() + ", " + user.getPhone() + "\n");
                                taskCount[0]--;
                            } else {
                                //doc doesn't exist
                            }
                        } else {
                            //task is not successful
                            taskCount[0]--;
                        }
                    }
                }).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            if (taskCount[0] == 0) {
                                alertDialog.show();
                                alertDialog.getWindow().setBackgroundDrawableResource(R.drawable.background_rating);
                                alertDialog.getWindow().setLayout(ConstraintLayout.LayoutParams.WRAP_CONTENT, (int) (context.getResources().getDisplayMetrics().heightPixels * 0.80));
                                alertDialog.setContentView(dialogLayout);
                                mainActivityRideDetailsInterface.showDialog();
                            }
                        }
                    }
                });
            }
        }
        else{
            participantsDialog.setText(context.getResources().getString(R.string.no_participants));
            alertDialog.show();
            alertDialog.getWindow().setBackgroundDrawableResource(R.drawable.background_rating);
            alertDialog.getWindow().setLayout(ConstraintLayout.LayoutParams.WRAP_CONTENT, (int) (context.getResources().getDisplayMetrics().heightPixels * 0.80));
            alertDialog.setContentView(dialogLayout);
            mainActivityRideDetailsInterface.showDialog();
        }
    }
}

