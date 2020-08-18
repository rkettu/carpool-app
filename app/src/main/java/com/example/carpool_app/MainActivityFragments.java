package com.example.carpool_app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

public class MainActivityFragments extends Fragment {
    private static ArrayList<RideUser> rideUserArrayList;
    private int page;
    private MainActivityRidesAdapter mainActivityRidesAdapter;

    //array list is where is all rides data.
    //page is for printing no booked or offered rides in correct page.
    public static MainActivityFragments newInstance(ArrayList<RideUser> rideUserArrayList, int page){
        MainActivityFragments mainActivityFragments = new MainActivityFragments();
        //bundles to pass data to on create.
        Bundle args = new Bundle();
        args.putSerializable("rideUser", rideUserArrayList);
        args.putInt("curPage", page);
        mainActivityFragments.setArguments(args);
        return mainActivityFragments;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rideUserArrayList = (ArrayList<RideUser>) getArguments().getSerializable("rideUser");
        page = (int) getArguments().getInt("curPage");
    }

    //creating view with fragments
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //inflating the view
        View view = inflater.inflate(R.layout.view_pager_main_activity, container, false);
        //init elements in view_pager_main_activity.
        ListView rideListView = view.findViewById(R.id.view_pager_main_listView);
        TextView noRidesTextView = view.findViewById(R.id.view_pager_main_textView);

        //if array list size is not equal to 0, print array list
        if(rideUserArrayList.size() != 0)
        {
            noRidesTextView.setVisibility(View.GONE);
            rideListView.setVisibility(View.VISIBLE);
            mainActivityRidesAdapter = new MainActivityRidesAdapter(rideUserArrayList, getContext());
            rideListView.setAdapter(mainActivityRidesAdapter);
        }
        //if array list size is 0, print "no booked rides" and "no offered rides" depends on fragment (integer used in switch case
        // in MainActivity sub class "RidesViewPagerAdapter).
        else{
            rideListView.setVisibility(View.GONE);
            noRidesTextView.setVisibility(View.VISIBLE);
            if(page == 0){
                noRidesTextView.setText(R.string.main_fragment_no_booked_rides);
            }
            else{
                noRidesTextView.setText(R.string.main_fragment_no_offered_rides);
            }
        }
        return view;
    }
}
