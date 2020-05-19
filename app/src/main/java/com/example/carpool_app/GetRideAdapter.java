package com.example.carpool_app;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class GetRideAdapter extends BaseAdapter {

    private ArrayList<User> userArrayList = new ArrayList<>();
    private ArrayList<Ride> rideArrayList = new ArrayList<>();
    private Context context;
    private LayoutInflater inflater;
    public static final String TAG = "GetRideAdapter";

    public GetRideAdapter(Context context, ArrayList<User> userArrayList, ArrayList<Ride> rideArrayList) {
        this.context = context;
        this.userArrayList = userArrayList;
        this.rideArrayList = rideArrayList;
    }

    public static class ViewHolder
    {
        TextView startPointTextView, destinationTextView, rideProviderTextView, durationTextView, leaveTimeTextView;
    }

    @Override
    public int getCount() {
        return 0;
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
        final ViewHolder viewHolder;
        if(view == null)
        {
            viewHolder = new ViewHolder();
            view = inflater.from(parent.getContext()).inflate(R.layout.adapter_get_ride, parent, false);
            viewHolder.startPointTextView = view.findViewById(R.id.getRideAdapter_startPointTextView);
            viewHolder.destinationTextView = view.findViewById(R.id.getRideAdapter_destinationTextView);
            viewHolder.rideProviderTextView = view.findViewById(R.id.getRideAdapter_userNameTextView);
            viewHolder.durationTextView = view.findViewById(R.id.getRideAdapter_durationTextView);
            viewHolder.leaveTimeTextView = view.findViewById(R.id.getRideAdapter_timeTextView);
            view.setTag(viewHolder);
        }
        else
        {
            viewHolder = (ViewHolder) view.getTag();
        }

        Log.d(TAG, "getView: taalla ollaan adapterissa");
        //printing data from array lists
        viewHolder.startPointTextView.setText(rideArrayList.get(position).getStartCity());
        viewHolder.destinationTextView.setText(rideArrayList.get(position).getEndCity());
        viewHolder.rideProviderTextView.setText(userArrayList.get(position).getFname());
        viewHolder.durationTextView.setText(rideArrayList.get(position).getDuration());

        //TODO calendarHelper from git
        //viewHolder.leaveTimeTextView.setText(rideArrayList.get(position).getLeaveTime());

        //Setting onClickListener to elements -> you can go to new activity for more info example
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO go new activity for more info
            }
        });

        return view;
    }
}
