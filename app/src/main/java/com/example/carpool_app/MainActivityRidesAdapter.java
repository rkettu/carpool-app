package com.example.carpool_app;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.Inflater;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivityRidesAdapter extends BaseAdapter {

    private ArrayList<RideUser> rideUserArrayList = new ArrayList<>();
    private Context context;
    private LayoutInflater inflater;

    public MainActivityRidesAdapter(ArrayList<RideUser> rideUserArrayList, Context context)
    {
        this.rideUserArrayList = rideUserArrayList;
        this.context = context;
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

    //View holder class for layout elements
    public static class ViewHolderMainActivityRides
    {
        TextView address, date, time, userName;
        CircleImageView userPicture;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        final ViewHolderMainActivityRides viewHolder;
        //init view
        if(view == null){
            viewHolder = new ViewHolderMainActivityRides();
            view = inflater.from(parent.getContext()).inflate(R.layout.adapter_main_activity, parent, false);
            viewHolder.address = view.findViewById(R.id.MainAdapter_startPointDestinationTextView);
            viewHolder.date = view.findViewById(R.id.MainAdapter_dateTextView);
            viewHolder.time = view.findViewById(R.id.MainAdapter_timeTextView);
            viewHolder.userName = view.findViewById(R.id.MainAdapter_userNameTextView);
            viewHolder.userPicture = view.findViewById(R.id.MainAdapter_userPicture);
            view.setTag(viewHolder);
        }
        else{
            viewHolder = (ViewHolderMainActivityRides) view.getTag();
        }

        //set data to list

        //uses calendarHelper class to change time in millis to date time
        long date = rideUserArrayList.get(position).getRide().getLeaveTime();
        String newDate = CalendarHelper.getDateTimeString(date);
        String newTime = CalendarHelper.getHHMMString(date);

        Picasso.with(context).load(rideUserArrayList.get(position).getUser().getImgUri()).into(viewHolder.userPicture);
        viewHolder.address.setText(rideUserArrayList.get(position).getRide().getStartCity());
        viewHolder.userName.setText(rideUserArrayList.get(position).getUser().getFname());
        viewHolder.date.setText(newDate);
        viewHolder.time.setText(newTime);

        return view;
    }
}
