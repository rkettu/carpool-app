package com.example.carpool_app;

import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

interface FindRidesInterface{
    void FindRidesResult(ArrayList<RideUser> result);
}

class FindRides
{
    private int spinnerCase;
    private float startLat, startLng, destinationLat, destinationLng;
    private long date1, date2;
    private CollectionReference rideReference = FirebaseFirestore.getInstance().collection("rides");
    private CollectionReference userReference = FirebaseFirestore.getInstance().collection("users");
    private ArrayList<RideUser> rideUserArrayList = new ArrayList<>();
    private List<HashMap<String, String>> points;
    private static String TAG = "FindRides";
    private FindRidesInterface findRidesInterface;
    private int counter = 0;
    private DocumentSnapshot lastVisible;

    public FindRides(ArrayList<RideUser> rideUserArrayList, DocumentSnapshot lastVisible)
    {
        this.lastVisible = lastVisible;
        this.rideUserArrayList = rideUserArrayList;
    }

    public FindRides(float startLat, float startLng, float destinationLat, float destinationLng,
                    long date1, long date2, int spinnerCase, FindRidesInterface findRidesInterface)
    {
        this.startLat = startLat;
        this.startLng = startLng;
        this.destinationLat = destinationLat;
        this.destinationLng = destinationLng;
        this.date1 = date1;
        this.date2 = date2;
        this.spinnerCase = spinnerCase;
        this.findRidesInterface = findRidesInterface;
    }

    private Query getFirstQuery(){
        Query query = rideReference
                .whereGreaterThanOrEqualTo("leaveTime", date1)
                .whereLessThanOrEqualTo("leaveTime", date2)
                .orderBy("leaveTime");
        return query;
    }

    //TODO startAt and limit
    private Query getNextQuery(final DocumentSnapshot lastVisiblee){
        Query query = rideReference
                .whereGreaterThanOrEqualTo("leaveTime", date1)
                .whereLessThanOrEqualTo("leaveTime", date2)
                .orderBy("leaveTime");
        return query;
    }

    public void findRides()
    {
        Log.d(TAG, "findRides: arraylist size " + rideUserArrayList.size());
        if (rideUserArrayList.size() == 0) {
            Log.d(TAG, "findRides: " + rideUserArrayList.size());
            search(getFirstQuery());
        } else {
            Log.d(TAG, "findRides: " + rideUserArrayList.get(rideUserArrayList.size() - 1).getRide().getLeaveTime());
            search(getNextQuery(lastVisible));
        }
    }


    private void search(Query query){
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
        @Override
        public void onComplete(@NonNull Task<QuerySnapshot> task) {
            Log.d(TAG, "onComplete: task<QuerySnapshot>" + task.isSuccessful());
            if(task.isSuccessful())
            {
                Log.d(TAG, "onComplete: task is successful" + task.getResult());
                Log.d(TAG, "onComplete: task is successful" + task.getException());
                Log.d(TAG, "onComplete: " );
                for(QueryDocumentSnapshot rideDoc : task.getResult())
                {
                    Log.d(TAG, "onComplete: task getResults");
                    try
                    {
                        //takes pickUpDistance and points from rides so we can use our algorithm to filter matching routes
                        float pickUpDistance = (long) rideDoc.get("pickUpDistance");
                        points = (List<HashMap<String, String>>) rideDoc.get("points");
                        Log.d(TAG, "onComplete: ollaan ennen appmath if lausetta");

                        //algorithm (in appMath class)
                        AppMath appMath = new AppMath();
                        if(appMath.isRouteInRange(pickUpDistance, startLat, startLng, destinationLat, destinationLng, points))
                        {
                            counter += 1;
                            final Ride ride = rideDoc.toObject(Ride.class);
                            if(ride.getUid() != null)
                            {
                                userReference.document(ride.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        counter -= 1;
                                        Log.d(TAG, "onComplete: counter= " + counter);
                                        if(task.isSuccessful())
                                        {
                                            DocumentSnapshot userDoc = task.getResult();

                                            if(userDoc.exists())
                                            {
                                                final User user = userDoc.toObject(User.class);

                                                if(user.getFname() != null)
                                                {
                                                    rideUserArrayList.add(new RideUser(ride, user));
                                                    Log.d(TAG, "onComplete: " + ride.getLeaveTime() + " " + user.getFname() + " " + ride.getDuration());
                                                }
                                                Log.d(TAG, "onComplete: jos != null");
                                            }
                                            else
                                            {
                                                //userDoc doesn't exist.
                                            }
                                            Log.d(TAG, "onComplete: if userDoc.exists jälkeen");
                                        }
                                        else
                                        {
                                            //task is not successful
                                        }
                                        Log.d(TAG, "onComplete: task is successful jälkeen");
                                    }
                                }).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        Log.d(TAG, "onComplete: finduser toinen oncomplete");
                                        if(counter == 0)
                                        {
                                            FindRideDone findRideDone = new FindRideDone(rideUserArrayList, findRidesInterface, lastVisible);
                                            findRideDone.execute();
                                        }
                                    }
                                });
                            }
                            else
                            {
                                //rideUid is null
                            }
                        }
                    }
                    catch (Exception e)
                    {

                    }
                }
                Log.d(TAG, "onComplete: " + task);
                Log.d(TAG, "onComplete: " + task.isSuccessful());
                Log.d(TAG, "onComplete: " + task.isComplete());
                Log.d(TAG, "onComplete: " + task.toString());
                Log.d(TAG, "onComplete: " + task.getResult());
            }
            else
            {
                //Task is not successful
            }
            Log.d(TAG, "onComplete: " + task.getException());
        }
    }).addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
        @Override
        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
            Log.d(TAG, "onSuccess: " + queryDocumentSnapshots.size());
            Log.d(TAG, "onSuccess: " + queryDocumentSnapshots.getQuery());
            Log.d(TAG, "onSuccess: " + queryDocumentSnapshots.getDocumentChanges());
            lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);
            Log.d(TAG, "onSuccess: " + lastVisible.toString());
        }
    });
    }
}

class FindRideDone extends AsyncTask<Void, Void, Boolean>{

    private int spinnerCase;
    private ArrayList<RideUser> rideUserArrayList = new ArrayList<>();
    private static String TAG = "FindRideDone";
    private Boolean hasDone = false;
    private FindRidesInterface findRidesInterface;
    private DocumentSnapshot lastVisible;

    public FindRideDone(ArrayList<RideUser> rideUserArrayList, FindRidesInterface findRidesInterface, DocumentSnapshot lastVisible)
    {
        this.rideUserArrayList = rideUserArrayList;
        this.findRidesInterface = findRidesInterface;
        this.lastVisible = lastVisible;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {

        Log.d(TAG, "doInBackground: " + rideUserArrayList.size());
        Log.d(TAG, "doInBackground: hahha");
        if(rideUserArrayList.size() >= 3)
        {
            Log.d(TAG, "doInBackground: " + rideUserArrayList.size());
            hasDone = true;
        }
        else
        {
            FindRides findRides = new FindRides(rideUserArrayList, lastVisible);
            findRides.findRides();
        }
        Log.d(TAG, "doInBackground: " + hasDone);
        return hasDone;
    }

    @Override
    protected void onPostExecute(Boolean result)
    {
        Log.d(TAG, "onPostExecute: " + "täällä ollaan vikassa");
        super.onPostExecute(result);
        if(result)
        {
            Log.d(TAG, "onPostExecute: pääsenkö tänne?");
            if(findRidesInterface != null)
            {
                findRidesInterface.FindRidesResult(rideUserArrayList);
            }
        }
    }
}
