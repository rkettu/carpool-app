package com.example.carpool_app;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

interface GetRideSortingInterface{
    void GetRideSorting(ArrayList<RideUser> rideUserArrayList);
}

public class GetRideSorting extends AsyncTask<Void, Integer, ArrayList<RideUser>> {

    private ArrayList<RideUser> rideUserArrayList = new ArrayList<>();
    private int sortingInteger;
    private GetRideSortingInterface getRideSortingInterface;
    private Context context;

    public GetRideSorting(GetRideSortingInterface getRideSortingInterface, Context context, int sortingInteger, ArrayList<RideUser> rideUserArrayList) {
        this.getRideSortingInterface = getRideSortingInterface;
        this.context = context;
        this.sortingInteger = sortingInteger;
        this.rideUserArrayList = rideUserArrayList;
    }

    @Override
    protected ArrayList<RideUser> doInBackground(Void... objects) {

        return timeSorting(rideUserArrayList, sortingInteger);
    }

    @Override
    protected void onPostExecute(ArrayList<RideUser> result)
    {
        super.onPostExecute(result);
        if(getRideSortingInterface != null)
        {
            getRideSortingInterface.GetRideSorting(result);
        }
    }

    private ArrayList<RideUser> timeSorting(ArrayList<RideUser> rideUserArrayList, int sortingCase) {
        Log.d("GetRideSorting", "timeSorting: " + sortingCase);
        if (sortingCase == 1) {
            Collections.sort(rideUserArrayList, new Comparator<RideUser>() {
                @Override
                public int compare(RideUser o1, RideUser o2) {
                    String first = String.valueOf(o1.getRide().getLeaveTime());
                    String second = String.valueOf(o2.getRide().getLeaveTime());
                    return first.compareTo(second);
                }
            });
        }
        else if (sortingCase == 2) {
            Collections.sort(rideUserArrayList, new Comparator<RideUser>() {
                @Override
                public int compare(RideUser o1, RideUser o2) {
                    String first = String.valueOf(o1.getRide().getPrice());
                    String second = String.valueOf(o2.getRide().getPrice());
                    return first.compareTo(second);
                }
            });
        }
        else
        {
            //do nothing
        }
        return rideUserArrayList;
    }
}
