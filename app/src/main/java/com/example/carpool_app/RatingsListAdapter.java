package com.example.carpool_app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.Rating;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class RatingsListAdapter extends BaseAdapter {

    private List<RatingItem> userList = new ArrayList<>();
    private Context context;
    private LayoutInflater inflater;
    private Activity activity;

    public RatingsListAdapter(){
        // Needs constructor for notifyDataSetChanges function
    }

    public RatingsListAdapter(Context context,Activity activity, List<RatingItem> userList) {
        this.context = context;
        this.userList = userList;
        this.activity = activity;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup container) {
        if(convertView==null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.ratings_list_item, container, false);
        }

        final RatingItem currItem = userList.get(position);

        ((TextView) convertView.findViewById(R.id.ratings_list_item_fname)).setText(currItem.firstName);
        ((TextView) convertView.findViewById(R.id.ratings_list_item_rating)).setText(currItem.ratingString);
        ImageView userImg = (ImageView)convertView.findViewById(R.id.ratings_list_joku_img);

        Picasso.with(context).load(currItem.imgUri).into(userImg);

        ((TextView) convertView.findViewById(R.id.ratings_list_rideStartDestination)).setText(currItem.rideStartDestination);
        ((TextView) convertView.findViewById(R.id.ratings_list_rideTime)).setText(currItem.rideDate);

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new RatingDialogFragment(context).show(((FragmentActivity)activity).getSupportFragmentManager(), "MOI");
            }
        });

        return convertView;
    }

    @Override
    public int getCount() {
        return userList.size(); //returns total of items in the list
    }

    @Override
    public Object getItem(int position) {
        return userList.get(position); //returns list item at the specified position
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


}
