package com.example.carpool_app;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

public class MainActivityFragments extends Fragment {
    private static ArrayList<RideUser> rideUserArrayList;
    private MainActivityRidesAdapter mainActivityRidesAdapter;

    public static MainActivityFragments newInstance(ArrayList<RideUser> rideUserArrayList){
        MainActivityFragments mainActivityFragments = new MainActivityFragments();
        Bundle args = new Bundle();
        args.putSerializable("rideUser", rideUserArrayList);
        mainActivityFragments.setArguments(args);
        return mainActivityFragments;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rideUserArrayList = (ArrayList<RideUser>) getArguments().getSerializable("rideUser");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.view_pager_main_activity, container, false);

        Log.d("TAG", "onCreateView: " + rideUserArrayList.get(0).getRide().getEndCity());

        ListView rideListView = view.findViewById(R.id.view_pager_main_listView);
        mainActivityRidesAdapter = new MainActivityRidesAdapter(rideUserArrayList, getContext());
        rideListView.setAdapter(mainActivityRidesAdapter);

        return view;
    }
}
