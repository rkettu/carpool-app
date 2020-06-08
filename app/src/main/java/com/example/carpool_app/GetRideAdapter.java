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

    private ArrayList<RideUser> rideUserArrayList = new ArrayList<>();
    private Context context;
    private LayoutInflater inflater;
    public static final String TAG = "GetRideAdapter";

    public GetRideAdapter(){
        //Needs constructor for notifyDataSetChanges function
    }

    public GetRideAdapter(Context context, ArrayList<RideUser> rideUserArrayList) {
        this.context = context;
        this.rideUserArrayList = rideUserArrayList;
    }

    public static class ViewHolder
    {
        TextView startPointDestinationTextView, destinationTextView, rideProviderTextView, dateTextView, timeTextView;
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
        final ViewHolder viewHolder;
        if(view == null)
        {
            viewHolder = new ViewHolder();
            view = inflater.from(parent.getContext()).inflate(R.layout.adapter_get_ride, parent, false);
            viewHolder.startPointDestinationTextView = view.findViewById(R.id.getRideAdapter_startPointDestinationTextView);
            //viewHolder.destinationTextView = view.findViewById(R.id.getRideAdapter_destinationTextView);
            viewHolder.rideProviderTextView = view.findViewById(R.id.getRideAdapter_userNameTextView);
            viewHolder.dateTextView = view.findViewById(R.id.getRideAdapter_dateTextView);
            viewHolder.timeTextView = view.findViewById(R.id.getRideAdapter_timeTextView);
            view.setTag(viewHolder);
        }
        else
        {
            viewHolder = (ViewHolder) view.getTag();
        }

        Long date = rideUserArrayList.get(position).getRide().getLeaveTime();
        String newDate = CalendarHelper.getDateTimeString(date);
        String newTime = CalendarHelper.getHHMMString(date);

        //printing data from array lists to list view
        viewHolder.startPointDestinationTextView.setText(rideUserArrayList.get(position).getRide().getStartCity() + " - " + rideUserArrayList.get(position).getRide().getEndCity());
        viewHolder.rideProviderTextView.setText(rideUserArrayList.get(position).getUser().getFname());
        viewHolder.dateTextView.setText(newDate);
        viewHolder.timeTextView.setText(newTime);

        //TODO calendarHelper from git
        //viewHolder.leaveTimeTextView.setText(rideArrayList.get(position).getLeaveTime());

        //Setting onClickListener to elements -> you can go to new activity for more info example
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO go new activity for more info
                //putExtra elements from rideArrayList and userArrayList from their position in list view
            }
        });

        return view;
    }
}
