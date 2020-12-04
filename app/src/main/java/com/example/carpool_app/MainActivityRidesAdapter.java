package com.example.carpool_app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivityRidesAdapter extends BaseAdapter {

    private ArrayList<RideUser> rideUserArrayList = new ArrayList<>();
    private Context context;
    private LayoutInflater inflater;
    private Constant constant;
    private int page;

    /**
     *
     * @param rideUserArrayList is the ArrayList containing all the data seen in lists in MainActivity.java
     * @param context we need context for picasso picture load
     */

    public MainActivityRidesAdapter(ArrayList<RideUser> rideUserArrayList, Context context, int page)
    {
        this.rideUserArrayList = rideUserArrayList;
        this.context = context;
        this.page = page;
    }

    @Override
    public int getCount() {
        return rideUserArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return rideUserArrayList.get(position);
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
    public View getView(final int position, View view, ViewGroup parent) {
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

        //uses calendarHelper class to change time in millis to date time
        long date = rideUserArrayList.get(position).getRide().getLeaveTime();
        String newDate = CalendarHelper.getDateTimeString(date);
        String newTime = CalendarHelper.getHHMMString(date);

        Picasso.with(context).load(rideUserArrayList.get(position).getUser().getImgUri()).into(viewHolder.userPicture);
        viewHolder.address.setText(rideUserArrayList.get(position).getRide().getStartCity() + " - " + rideUserArrayList.get(position).getRide().getEndCity());
        viewHolder.userName.setText(rideUserArrayList.get(position).getUser().getFname());
        viewHolder.date.setText(newDate);
        viewHolder.time.setText(newTime);

        constant = new Constant();
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try
                {
                    constant.startLoadingDialog(context);
                    MainActivityRideDetails mainActivityRideDetails = new MainActivityRideDetails(context, new MainActivityRideDetailsInterface() {
                        @Override
                        public void showDialog() {
                            constant.dismissLoadingDialog();
                        }

                        @Override
                        public void whenDone() {
                            notifyDataSetChanged();
                            endDialog();
                        }

                        @Override
                        public void whenFailed() {
                            notifyDataSetChanged();
                            endDialog();
                        }
                    }, rideUserArrayList, position, page);
                    mainActivityRideDetails.execute();
                }
                catch (Exception e){
                    constant.dismissLoadingDialog();
                    Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        });
        return view;
    }

    public void endDialog(){
        if(page == 0){
            Toast.makeText(context, context.getResources().getString(R.string.main_activity_adapter_delete_booked_ride), Toast.LENGTH_LONG).show();
        }
        else{
            Toast.makeText(context, context.getResources().getString(R.string.main_activity_adapter_delete_offered_ride), Toast.LENGTH_LONG).show();
        }
        Intent i = new Intent(context, MainActivity.class);
        context.startActivity(i);
    }
}
