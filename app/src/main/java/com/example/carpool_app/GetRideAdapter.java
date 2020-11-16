package com.example.carpool_app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class GetRideAdapter extends BaseAdapter {

    private ArrayList<RideUser> rideUserArrayList = new ArrayList<>();
    private Context context;
    private LayoutInflater inflater;
    public static final String TAG = "GetRideAdapter";
    private Constant constant;

    public GetRideAdapter(Context context, ArrayList<RideUser> rideUserArrayList) {
        this.context = context;
        this.rideUserArrayList = rideUserArrayList;
    }

    public static class ViewHolderGetRideAdapter
    {
        TextView startPointDestinationTextView, rideProviderTextView, dateTextView, timeTextView, priceTextView;
        ImageView infoImageView, dateImageView, timeImageView;
    }

    @Override
    public int getCount() {
        return rideUserArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent)
    {
        //initializing list item elements
        final ViewHolderGetRideAdapter viewHolder;
        if(view == null)
        {
            viewHolder = new ViewHolderGetRideAdapter();
            view = inflater.from(parent.getContext()).inflate(R.layout.adapter_get_ride, parent, false);
            viewHolder.startPointDestinationTextView = view.findViewById(R.id.getRideAdapter_startPointDestinationTextView);
            viewHolder.priceTextView = view.findViewById(R.id.getRideAdapter_priceTextView);
            viewHolder.rideProviderTextView = view.findViewById(R.id.getRideAdapter_userNameTextView);
            viewHolder.dateTextView = view.findViewById(R.id.getRideAdapter_dateTextView);
            viewHolder.timeTextView = view.findViewById(R.id.getRideAdapter_timeTextView);
            viewHolder.infoImageView = view.findViewById(R.id.getRideAdapter_infoImageView);
            viewHolder.timeImageView = view.findViewById(R.id.getRideAdapter_timeImageView);
            viewHolder.dateImageView = view.findViewById(R.id.getRideAdapter_dateImageView);
            view.setTag(viewHolder);
        }
        else
        {
            viewHolder = (ViewHolderGetRideAdapter) view.getTag();
        }

        //uses calendarHelper class to change time in millis to date time
        long date = rideUserArrayList.get(position).getRide().getLeaveTime();
        String newDate = CalendarHelper.getDateTimeString(date);
        String newTime = CalendarHelper.getHHMMString(date);

        //changes long to string and estimate price per 100km
        String price = String.format("%.2f", rideUserArrayList.get(position).getRide().getPrice() * 100);

        //printing data from array lists to list view
        viewHolder.startPointDestinationTextView.setText(rideUserArrayList.get(position).getRide().getStartCity() + " - " + rideUserArrayList.get(position).getRide().getEndCity());
        viewHolder.rideProviderTextView.setText(rideUserArrayList.get(position).getUser().getFname());
        viewHolder.dateTextView.setText(newDate);
        viewHolder.timeTextView.setText(newTime);
        viewHolder.priceTextView.setText(price + "€");

        //if you press info image next to price, do this
        viewHolder.infoImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, context.getResources().getString(R.string.getrideadapter_estimated_price_per_100), Toast.LENGTH_SHORT).show();
            }
        });

        //if you press calendar image next to date, do this
        viewHolder.dateImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, context.getResources().getString(R.string.getrideadapter_estimated_leave_day), Toast.LENGTH_SHORT).show();
            }
        });

        //if you press clock image next to time, do this
        viewHolder.timeImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, context.getResources().getString(R.string.getrideadapter_estimated_leave_time), Toast.LENGTH_SHORT).show();
            }
        });

        constant = new Constant();
        //Setting onClickListener to elements -> you can go to new activity for more info example
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try
                {
                    //use async task to show alert dialog with ride details.
                    //using async because of picture fetch from database takes time.
                    constant.startLoadingDialog(context);
                    GetRideRideDetails getRideRideDetails = new GetRideRideDetails(context, new GetRideRideDetailsInterface() {
                        @Override
                        public void showDialog(AlertDialog alertDialog) {
                            constant.dismissLoadingDialog();
                        }

                        //when you press book ride in FindRideDetails.java and the data has been added to database
                        @Override
                        public void whenDone() {
                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            final LayoutInflater inflater = LayoutInflater.from(context);
                            View dialogView = inflater.inflate(R.layout.adapter_get_ride_success_dialog, null);
                            builder.setView(dialogView);

                            Button btnOk = (Button) dialogView.findViewById(R.id.getride_success_buttonOk);
                            btnOk.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(context, MainActivity.class);
                                    context.startActivity(intent);
                                }
                            });
                            AlertDialog alertDialog = builder.show();
                            alertDialog.show();
                        }

                        //if FindRideDetails.java task is not successful
                        @Override
                        public void whenFailed() {
                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            final LayoutInflater inflater = LayoutInflater.from(context);
                            View dialogView = inflater.inflate(R.layout.adapter_get_ride_failed, null);
                            builder.setView(dialogView);

                            Button btnOk = (Button) dialogView.findViewById(R.id.getride_failed_Ok);
                            btnOk.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(context, GetRideActivity.class);
                                    context.startActivity(intent);
                                }
                            });
                            AlertDialog alertDialog = builder.show();
                            alertDialog.show();
                        }
                    }, rideUserArrayList, position);
                    getRideRideDetails.execute();
                }
                catch (Exception e)
                {
                    //if it fails for some reason, to this.
                    constant.dismissLoadingDialog();
                    Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        });
        return view;
    }
}
