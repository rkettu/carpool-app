package com.example.carpool_app;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class RatingsListAdapter extends BaseAdapter {

    private List<RatingItem> userList = new ArrayList<>();
    private Context context;
    private LayoutInflater inflater;

    public RatingsListAdapter(){
        // Needs constructor for notifyDataSetChanges function
    }

    public RatingsListAdapter(Context context, List<RatingItem> userList) {
        this.context = context;
        this.userList = userList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup container) {
        if(convertView==null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.ratings_list_item, container, false);
        }

        RatingItem currItem = userList.get(position);

        ((TextView) convertView.findViewById(R.id.ratings_list_item_joku_1)).setText(currItem.firstName);
        ((TextView) convertView.findViewById(R.id.ratings_list_item_joku_2)).setText(currItem.ratingString);
        ImageView userImg = (ImageView)convertView.findViewById(R.id.ratings_list_joku_img);

        Picasso.with(context).load(currItem.imgUri).into(userImg);

        ((TextView) convertView.findViewById(R.id.ratings_list_rideStartDestination)).setText(currItem.rideStartDestination);
        ((TextView) convertView.findViewById(R.id.ratings_list_rideTime)).setText(currItem.rideDate);

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
