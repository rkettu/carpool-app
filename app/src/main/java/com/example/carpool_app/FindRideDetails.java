package com.example.carpool_app;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.Image;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;

interface RideDetailsInterface{
    void showDialog(AlertDialog alertDialog);
}

public class FindRideDetails extends AsyncTask<Void, Void, Bitmap> {

    private TextView startPointDialog, destinationDialog, leaveTimeDialog, durationDialog, priceDialog, freeSeatsDialog, wayPointsDialog, userNameDialog;
    private Button bookRideBtn;
    private ImageView profilePicture, closeDialogBtn;
    private Context context;
    private RideDetailsInterface rideDetailsInterface;
    private ArrayList<RideUser> rideUserArrayList;
    private int position;
    private AlertDialog.Builder builder;

    public FindRideDetails(Context context, RideDetailsInterface rideDetailsInterface, ArrayList<RideUser> rideUserArrayList, int position)
    {
        this.context = context;
        this.rideDetailsInterface = rideDetailsInterface;
        this.rideUserArrayList = rideUserArrayList;
        this.position = position;
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
                //TODO book ride.
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
        userNameDialog.setText(rideUserArrayList.get(position).getUser().getFname() + " " + rideUserArrayList.get(position).getUser().getLname());
        startPointDialog = view.findViewById(R.id.rideDetails_startPoint);
        startPointDialog.setText("Lähtöpaikka: " + rideUserArrayList.get(position).getRide().getStartAddress());
        destinationDialog = view.findViewById(R.id.rideDetails_destination);
        destinationDialog.setText("Määränpää: " + rideUserArrayList.get(position).getRide().getEndAddress());
        leaveTimeDialog = view.findViewById(R.id.rideDetails_leaveTime);
        leaveTimeDialog.setText("Arvioitu lähtöaika: " + CalendarHelper.getDateTimeString(rideUserArrayList.get(position).getRide().getLeaveTime()));
        durationDialog = view.findViewById(R.id.rideDetails_duration);
        durationDialog.setText("Arvioitu kesto: " + rideUserArrayList.get(position).getRide().getDuration());
        priceDialog = view.findViewById(R.id.rideDetails_price);
        priceDialog.setText("Hinta: " + rideUserArrayList.get(position).getRide().getPrice() * 100 + " euroa 100km kohti");
        freeSeatsDialog = view.findViewById(R.id.rideDetails_freeSeats);
        freeSeatsDialog.setText("Vapaita paikkoja: " + rideUserArrayList.get(position).getRide().getFreeSlots());
        wayPointsDialog = view.findViewById(R.id.rideDetails_wayPoints);
        Log.d("TAG", "initDialogLayoutItems1: ");

        //checks is the 0, 1 or 2 way points and print them if there is way points.
        if(rideUserArrayList.get(position).getRide().getWaypointAddresses().size() != 0)
        {
            if(rideUserArrayList.get(position).getRide().getWaypointAddresses().size() == 1)
            {
                wayPointsDialog.setText(rideUserArrayList.get(position).getRide().getWaypointAddresses().get(0));
            }
            else
            {
                wayPointsDialog.setText(rideUserArrayList.get(position).getRide().getWaypointAddresses().get(0) + "\n" +
                        rideUserArrayList.get(position).getRide().getWaypointAddresses().get(1));
            }
        }
        else
        {
            wayPointsDialog.setVisibility(View.GONE);
        }
        bookRideBtn = view.findViewById(R.id.rideDetails_bookRideButton);
        closeDialogBtn =  view.findViewById(R.id.rideDetails_backButton);
        profilePicture = view.findViewById(R.id.rideDetails_profileImage);

        Log.d("TAG", "initDialogLayoutItems2: ");
    }
}
