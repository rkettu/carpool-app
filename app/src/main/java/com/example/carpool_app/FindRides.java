package com.example.carpool_app;

import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
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
    private static int spinnerCase;
    private static float startLat, startLng, destinationLat, destinationLng;
    private static long date1, date2;
    private static FindRidesInterface findRidesInterface;
    private CollectionReference rideReference = FirebaseFirestore.getInstance().collection("rides");
    private CollectionReference userReference = FirebaseFirestore.getInstance().collection("users");
    private ArrayList<RideUser> rideUserArrayList = new ArrayList<>();
    private List<HashMap<String, String>> points;
    private static String TAG = "FindRides";
    private int counter = 0;
    private DocumentSnapshot lastVisible;
    private boolean foundRide = false;

    public FindRides(){

    }

    public FindRides(ArrayList<RideUser> rideUserArrayList, DocumentSnapshot lastVisible)
    {
        this.lastVisible = lastVisible;
        this.rideUserArrayList = rideUserArrayList;
    }

    public FindRides(float startLat, float startLng, float destinationLat, float destinationLng,
                    long date1, long date2, int spinnerCase, FindRidesInterface findRidesInterface)
    {
        FindRides.startLat = startLat;
        FindRides.startLng = startLng;
        FindRides.destinationLat = destinationLat;
        FindRides.destinationLng = destinationLng;
        FindRides.date1 = date1;
        FindRides.date2 = date2;
        FindRides.spinnerCase = spinnerCase;
        FindRides.findRidesInterface = findRidesInterface;
    }

    public void findRides()
    {
        Log.d(TAG, "findRides: " + date1 + "date2 " + date2);
        Log.d(TAG, "findRides: arraylist size " + rideUserArrayList.size());
        if (lastVisible == null || rideUserArrayList.size() == 0) {
            Log.d(TAG, "findRides: if " + rideUserArrayList.size());
            search(getFirstQuery());
        }
        else {
            search(getNextQuery(lastVisible));
        }
    }

    private Query getFirstQuery() {
        return rideReference
                .whereGreaterThanOrEqualTo("leaveTime", date1)
                .whereLessThanOrEqualTo("leaveTime", date2)
                .orderBy("leaveTime")
                .limit(100);
    }

    private Query getNextQuery(final DocumentSnapshot lastVisible) {
        return rideReference
                .whereGreaterThanOrEqualTo("leaveTime", date1)
                .whereLessThanOrEqualTo("leaveTime", date2)
                .orderBy("leaveTime")
                .startAfter(lastVisible)
                .limit(100);
    }

    private void search(final Query query){
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
                        Log.d(TAG, "onComplete: " + startLat + " " + startLng);
                        if(appMath.isRouteInRange(pickUpDistance, startLat, startLng, destinationLat, destinationLng, points))
                        {
                            Log.d(TAG, "onComplete: " + counter);
                            final Ride ride = rideDoc.toObject(Ride.class);
                            foundRide = true;
                            counter += 1;

                            Log.d(TAG, "onComplete: rideuid == " + ride.getUid());
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
                                        Log.d(TAG, "onComplete: " + counter + "counterin arvo ennen if lausetta" );

                                        if(counter == 0)
                                        {
                                            FindRideDone findRideDone = new FindRideDone(rideUserArrayList, findRidesInterface, lastVisible, spinnerCase, false);
                                            findRideDone.execute();
                                        }
                                    }
                                });
                            }
                            else
                            {
                                //rideUid is null
                                counter -= 1;
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
            if(queryDocumentSnapshots.size() == 0)
            {
                FindRideDone findRideDone = new FindRideDone(rideUserArrayList, findRidesInterface, lastVisible, spinnerCase, true);
                findRideDone.execute();
            }
            else
            {
                lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);
                if(!foundRide)
                {
                    Log.d(TAG, "onSuccess: !foundRide ");
                    FindRides findRides = new FindRides(rideUserArrayList, lastVisible);
                    findRides.findRides();
                }
            }
        }
    }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
            }
        });
    }
}

class FindRideDone extends AsyncTask<Void, Void, Boolean>{

    private ArrayList<RideUser> rideUserArrayList;
    private static String TAG = "FindRideDone";
    private Boolean hasDone;
    private FindRidesInterface findRidesInterface;
    private DocumentSnapshot lastVisible;
    private int spinnerCase;

    public FindRideDone(ArrayList<RideUser> rideUserArrayList, FindRidesInterface findRidesInterface, DocumentSnapshot lastVisible, int spinnerCase, boolean hasDone)
    {
        this.hasDone = hasDone;
        this.rideUserArrayList = rideUserArrayList;
        this.findRidesInterface = findRidesInterface;
        this.lastVisible = lastVisible;
        this.spinnerCase = spinnerCase;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        Log.d(TAG, "doInBackground: " + rideUserArrayList.size());
        Log.d(TAG, "doInBackground: hahha");
        if(rideUserArrayList.size() >= 1 || hasDone)
        {
            Log.d(TAG, "doInBackground: " + rideUserArrayList.size());
            hasDone = true;
        }
        else
        {
            Log.d(TAG, "doInBackground: hidjfiuhgd< h");
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
            FindRides findRides = new FindRides();
            Log.d(TAG, "onPostExecute: pääsenkö tänne?");
            if(findRidesInterface != null)
            {
                Log.d(TAG, "onPostExecute: entä tänne?");
                findRidesInterface.FindRidesResult(rideUserArrayList);
            }
        }
    }
}
