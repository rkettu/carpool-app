package com.example.carpool_app;

import android.content.Context;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

interface GetRideSortingInterface{
    void GetRideSorting(ArrayList<RideUser> rideUserArrayList);
}

public class GetRideSorting extends AsyncTask<Object, Integer, ArrayList<RideUser>> {

    private ArrayList<RideUser> rideUserArrayList;
    private int sortingCase;
    private GetRideSortingInterface getRideSortingInterface;
    private Context context;

    public GetRideSorting(GetRideSortingInterface getRideSortingInterface, Context context) {
        this.getRideSortingInterface = getRideSortingInterface;
        this.context = context;
    }

    @Override
    protected ArrayList<RideUser> doInBackground(Object... objects) {
        objects[0] = rideUserArrayList;
        objects[1] = sortingCase;

        return timeSorting(rideUserArrayList, sortingCase);
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
