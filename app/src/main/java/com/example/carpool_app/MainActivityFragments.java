package com.example.carpool_app;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MainActivityFragments extends Fragment {
    private static ArrayList<RideUser> bookedRideUserArrayList;
    private static ArrayList<RideUser> offeredRideUserArrayList;
    private int page;
    private MainActivityRidesAdapter mainActivityRidesAdapter;

    //array list is where is all rides data.
    //page is for printing no booked or offered rides in correct page.
    public static MainActivityFragments newInstance(ArrayList<RideUser> bookedRideUserArrayList, ArrayList<RideUser> offeredRideUserArrayList, int page){
        MainActivityFragments mainActivityFragments = new MainActivityFragments();
        //bundles to pass data to on create.
        Bundle args = new Bundle();
        Log.d("TAG", "onCreateView: " + bookedRideUserArrayList.size() + " " + offeredRideUserArrayList.size() + " " + page);
        args.putSerializable("bRideUser", bookedRideUserArrayList);
        args.putSerializable("oRideUser", offeredRideUserArrayList);
        args.putInt("curPage", page);
        mainActivityFragments.setArguments(args);
        return mainActivityFragments;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bookedRideUserArrayList = (ArrayList<RideUser>) getArguments().getSerializable("bRideUser");
        offeredRideUserArrayList = (ArrayList<RideUser>) getArguments().getSerializable("oRideUser");
        Log.d("TAG", "onCreateView2: " + bookedRideUserArrayList.size() + " " + offeredRideUserArrayList.size() + " " + page);

        //Sorting ride lists from lowest to highest
        Collections.sort(bookedRideUserArrayList, new Comparator<RideUser>() {
            @Override
            public int compare(RideUser o1, RideUser o2) {
                String first = String.valueOf(o1.getRide().getLeaveTime());
                String second = String.valueOf(o2.getRide().getLeaveTime());
                return first.compareTo(second);
            }
        });
        Collections.sort(offeredRideUserArrayList, new Comparator<RideUser>() {
            @Override
            public int compare(RideUser o1, RideUser o2) {
                String first = String.valueOf(o1.getRide().getLeaveTime());
                String second = String.valueOf(o2.getRide().getLeaveTime());
                return first.compareTo(second);
            }
        });

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

        Log.d("TAG", "onCreateView3: " + bookedRideUserArrayList.size() + " " + offeredRideUserArrayList.size() + " " + page);

        //if booked ride list size is not equal to 0, print array list
        if(bookedRideUserArrayList.size() != 0 && page == 0)
        {
            noRidesTextView.setVisibility(View.GONE);
            rideListView.setVisibility(View.VISIBLE);
            mainActivityRidesAdapter = new MainActivityRidesAdapter(bookedRideUserArrayList, getContext());
            rideListView.setAdapter(mainActivityRidesAdapter);
        }
        //if offered ride list size is not equal to 0, print array list
        else if(offeredRideUserArrayList.size() != 0 && page == 1)
        {
            noRidesTextView.setVisibility(View.GONE);
            rideListView.setVisibility(View.VISIBLE);
            mainActivityRidesAdapter = new MainActivityRidesAdapter(offeredRideUserArrayList, getContext());
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
