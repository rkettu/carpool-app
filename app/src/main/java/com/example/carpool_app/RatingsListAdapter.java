package com.example.carpool_app;

import android.app.Application;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class RatingsListAdapter extends BaseAdapter {

    private List<User> userList = new ArrayList<>();
    private Context context;
    private LayoutInflater inflater;

    public RatingsListAdapter(){
        // Needs constructor for notifyDataSetChanges function
    }

    public GetRideAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup container) {
        if(convertView==null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.ratings_list_item, container, false);
        }

        ((TextView) convertView.findViewById(R.id.ratings_list_item_joku_1)).setText(userList.get(position).getFname());
        ((TextView) convertView.findViewById(R.id.ratings_list_item_joku_1)).setText(userList.get(position).getRating());
        return convertView;
    }
}
