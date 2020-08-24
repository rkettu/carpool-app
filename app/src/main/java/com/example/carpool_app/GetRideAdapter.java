package com.example.carpool_app;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
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

    public GetRideAdapter(){
        //Needs constructor for notifyDataSetChanges function
    }

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

        Log.d(TAG, "getView: " + rideUserArrayList.get(position).getRideId());

        //if you press info image next to price, do this
        viewHolder.infoImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "Arvioitu summa 100km kohti.", Toast.LENGTH_SHORT).show();
            }
        });

        //if you press calendar image next to date, do this
        viewHolder.dateImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "Arvioitu lähtöpäivä.", Toast.LENGTH_SHORT).show();
            }
        });

        //if you press clock image next to time, do this
        viewHolder.timeImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "Arvioitu lähtöaika.", Toast.LENGTH_SHORT).show();
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
                    FindRideDetails findRideDetails = new FindRideDetails(context, new RideDetailsInterface() {
                        @Override
                        public void showDialog(AlertDialog alertDialog) {
                            constant.dismissLoadingDialog();
                        }
                    }, rideUserArrayList, position);
                    findRideDetails.execute();
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
