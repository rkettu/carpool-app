package com.example.carpool_app;

import android.content.Context;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * This class is async task class, where you can sort data from array list.
 * if you have the array list and an integer, which tells the sorting order.
 * example GetRideActivity gives value to an integer when spinner is used.
 * This integer is used to determine what kind of sorting we want to use.
 */

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

    //method, where the sorting happens after the array list is filled from database
    //data will be sorted already (depends on spinners value) when displaying first time
    private ArrayList<RideUser> timeSorting(ArrayList<RideUser> rideUserArrayList, int sortingCase) {
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
                    String first = String.valueOf(o1.getRide().getLeaveTime());
                    String second = String.valueOf(o2.getRide().getLeaveTime());
                    return second.compareTo(first);
                }
            });
        }
        else if (sortingCase == 3){
            Collections.sort(rideUserArrayList, new Comparator<RideUser>() {
                @Override
                public int compare(RideUser o1, RideUser o2) {
                    String first = String.valueOf(o1.getRide().getPrice());
                    String second = String.valueOf(o2.getRide().getPrice());
                    return first.compareTo(second);
                }
            });
        }
        else if (sortingCase == 4){
            Collections.sort(rideUserArrayList, new Comparator<RideUser>() {
                @Override
                public int compare(RideUser o1, RideUser o2) {
                    String first = String.valueOf(o1.getRide().getPrice());
                    String second = String.valueOf(o2.getRide().getPrice());
                    return second.compareTo(first);
                }
            });
        }
        else
        {
            //do nothing
            return null;
        }
        return rideUserArrayList;
    }
}
