package com.example.carpool_app;

import android.os.AsyncTask;
import android.os.Debug;
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
    void FindRidesFailed(String report);
}

class FindRides
{
    private static float startLat, startLng, destinationLat, destinationLng;
    private static long date1, date2;
    private static FindRidesInterface findRidesInterface;
    private CollectionReference rideReference = FirebaseFirestore.getInstance().collection("rides");
    private CollectionReference userReference = FirebaseFirestore.getInstance().collection("users");
    private ArrayList<RideUser> rideUserArrayList = new ArrayList<>();
    private List<HashMap<String, Double>> points;
    private double pickUpDistance;
    private HashMap<String, Double> bounds;
    private static final String TAG = "FindRides";
    private static final int queryLimit = 100;
    private int counter = 0;
    private DocumentSnapshot lastVisible;
    private boolean foundRide = false;

    /** Uses two constructors, the first one with only two params is for loop the database search if the array list size
     * is lower than we wanted. The array list contains all the ride and user data for each matching ride.
     * Second constructor is when the FindRides function is called first time. We get the user info like start point, destination and
     * date between two times. these are saved into static variables for the db search.
     *
     * function findRides() is the deciding are we using getFirstQuery() or getNextQuery function. It chooses the correct function based
     * on lastVisible variables (last used object in database) and array list size.
     *
     * The search() function is where the database search, algorithm and saving matching rides to array list happens.
     * First it runs the rideReference query in onComplete function to get all the matching ride data. After that it
     * goes to onSuccessListener, where
     * 1. no matching rides found -> do next search using lastVisible variable and startAfter in query
     * 2. if there is matching ride, get the user data and save that into array list
     *  2.1. observe if array list size is bigger or equal to 50, if correct no more db search
     * 3. if there is no rides left in database, print all available rides from array list
     *  3.1. if there is no matching rides, GetRideActivity toasts "no matching rides"
     */


    //use this call if you are looping
    FindRides(ArrayList<RideUser> rideUserArrayList, DocumentSnapshot lastVisible)
    {
        this.lastVisible = lastVisible;
        this.rideUserArrayList = rideUserArrayList;
    }

    //use when called first time to save algorithm objects
    FindRides(float startLat, float startLng, float destinationLat, float destinationLng,
                    long date1, long date2, FindRidesInterface findRidesInterface)
    {
        FindRides.startLat = startLat;
        FindRides.startLng = startLng;
        FindRides.destinationLat = destinationLat;
        FindRides.destinationLng = destinationLng;
        FindRides.date1 = date1;
        FindRides.date2 = date2;
        FindRides.findRidesInterface = findRidesInterface;
    }

    //function called when using the db search
    void findRides()
    {
        Log.d(TAG, "onComplete: latlng: " + date1 + " " + date2 + " " +startLat + " " + startLng + " " + destinationLat + " " + destinationLng);
        //first it will go to else condition to get the first query.
        //the first query will get data to lastVisible and after that
        //program will use getNextQuery, where we take next queryLimit much
        //data from database.
        if (lastVisible != null || rideUserArrayList.size() != 0) {
            search(getNextQuery(lastVisible));
        }
        else {
            search(getFirstQuery());
        }

    }

    //the first query
    private Query getFirstQuery() {
        return rideReference
                .whereGreaterThanOrEqualTo("leaveTime", date1)
                .whereLessThanOrEqualTo("leaveTime", date2)
                .orderBy("leaveTime")
                .limit(queryLimit);
    }

    //the "next" query which uses startAfter, lastVisible was the last document seen in db last search
    private Query getNextQuery(final DocumentSnapshot lastVisible) {
        return rideReference
                .whereGreaterThanOrEqualTo("leaveTime", date1)
                .whereLessThanOrEqualTo("leaveTime", date2)
                .orderBy("leaveTime")
                .startAfter(lastVisible)
                .limit(queryLimit);
    }

    //Database search, algorithm to matching routes(use of appMath class) and listener if there is
    //new rides found in query
    private void search(final Query query){
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
        @Override
        public void onComplete(@NonNull Task<QuerySnapshot> task) {
            if(task.isSuccessful())
            {
                Log.d(TAG, "onComplete: " + task.isSuccessful() + task.getResult());
                //if the task is successful, do forEach to every result
                for(final QueryDocumentSnapshot rideDoc : task.getResult())
                {
                    Log.d(TAG, "onComplete: " + task.getResult());
                    try
                    {
                        Log.d(TAG, "onComplete: ");
                        //takes pickUpDistance and points from rides so we can use our algorithm to filter matching routes
                        bounds = (HashMap<String, Double>) rideDoc.get("bounds");
                        pickUpDistance = (long) rideDoc.get("pickUpDistance");
                        Log.d(TAG, "onComplete: ennen points");
                        points = (ArrayList<HashMap<String, Double>>) rideDoc.get("points");

                        //TODO bounds
                            //algorithm (in appMath class)
                            Log.d(TAG, "onComplete: ollaan ennen appmath if lausetta");
                            if(AppMath.isRouteInRange(pickUpDistance, startLat, startLng, destinationLat, destinationLng, points))
                            {
                                //checks if there is user id in ride
                                if(rideDoc.get("uid") != null)
                                {
                                    //places rideDoc (QueryDocumentSnapshot) object into Ride class. foundRide changes from false to true
                                    //and counter add one for one found ride.
                                    final Ride ride = rideDoc.toObject(Ride.class);
                                    final String rideId = rideDoc.getId();
                                    Log.d(TAG, "onComplete: " + rideId);
                                    foundRide = true;
                                    counter += 1;

                                    userReference.document(ride.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            counter -= 1;
                                            if(task.isSuccessful())
                                            {
                                                DocumentSnapshot userDoc = task.getResult();
                                                if(userDoc.exists())
                                                {
                                                    //if task is successful and DocumentSnapshot exists in this ride, add user
                                                    final User user = userDoc.toObject(User.class);
                                                    if(user.getFname() != null)
                                                    {
                                                        //if there is first name in user object, add the ride and user into list.
                                                        rideUserArrayList.add(new RideUser(ride, user, rideId));
                                                        Log.d(TAG, "onComplete: " + ride.getLeaveTime() + " " + user.getFname() + " " + ride.getDuration());
                                                    }
                                                }
                                                else
                                                {
                                                    //userDoc doesn't exist.
                                                    Log.d(TAG, "userDoc.exist(): " + userDoc.exists());
                                                }
                                            }
                                            else
                                            {
                                                //task is not successful
                                                Log.d(TAG, "userReference onComplete task.getException(): " + task.getException());
                                            }

                                            //the counter is integer to indicate how many rides have been added to list.
                                            //counter += 1 when adding ride object to Ride class
                                            if(counter == 0)
                                            {
                                                FindRideDone findRideDone = new FindRideDone(rideUserArrayList, findRidesInterface, lastVisible, false);
                                                findRideDone.execute();
                                            }
                                        }
                                    });
                                }
                                //TODO if the user is same
                                else
                                {
                                    //if ride doesn't have uid
                                }
                            }

                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
            else
            {
                //Task is not successful
                Log.d(TAG, "onComplete: " + task.getException());
            }
        }
    }).addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
        @Override
        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
            Log.d(TAG, "queryDocumentSnapshot size in onSuccess: " + queryDocumentSnapshots.size());
            Log.d(TAG, "onSuccess: " + queryDocumentSnapshots.getQuery() + " " + queryDocumentSnapshots.getDocuments());
            //query size is 0 if there is no more rides in database.
            if(queryDocumentSnapshots.size() == 0)
            {
                FindRideDone findRideDone = new FindRideDone(rideUserArrayList, findRidesInterface, lastVisible, true);
                findRideDone.execute();
            }
            //if there is information in query
            else
            {
                //lastVisible is query size - 1, because size 0 is when there is no data but getDocument 0 is the first document
                lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);
                if(!foundRide)
                {
                    //if foundRide is false (it is true if one ride passes algorithm) so we run search() with new query using lastVisible.
                    Log.d(TAG, "onSuccess: !foundRide " + lastVisible);
                    FindRides findRides = new FindRides(rideUserArrayList, lastVisible);
                    findRides.findRides();
                }
            }
        }
    }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
                if(findRidesInterface != null)
                {
                    findRidesInterface.FindRidesFailed(e.toString());
                }
            }
        });
    }

}


class FindRideDone extends AsyncTask<Void, Void, Boolean>{

    private ArrayList<RideUser> rideUserArrayList;
    private static final String TAG = "FindRideDone";
    private Boolean hasDone;
    private FindRidesInterface findRidesInterface;
    private DocumentSnapshot lastVisible;

    /** Use the arrayListMaxSize integer for the minimum array list size.
     * //TODO when ready, use value of 50 instead of 1.
     * //Use value 1 if you dont want your db to get many search per time
     * */
    private final int arrayListMinSize = 1;

    FindRideDone(ArrayList<RideUser> rideUserArrayList, FindRidesInterface findRidesInterface, DocumentSnapshot lastVisible, boolean hasDone)
    {
        this.hasDone = hasDone;
        this.rideUserArrayList = rideUserArrayList;
        this.findRidesInterface = findRidesInterface;
        this.lastVisible = lastVisible;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        //if array list size is smaller than array list minimun size, or query size is 0 (hasDone will be true is query size is 0)
        //change hasDone to true, so we can pass data to activity using interface.
        //if the condition are not met, it will do the db search again.
        if(rideUserArrayList.size() >= arrayListMinSize || hasDone)
        {
            //array list size is bigger than minimum size or all the data from database has been wrought
            hasDone = true;
        }
        else
        {
            //There is need to run database search again
            FindRides findRides = new FindRides(rideUserArrayList, lastVisible);
            findRides.findRides();
        }
        return hasDone;
    }

    @Override
    protected void onPostExecute(Boolean result)
    {
        super.onPostExecute(result);
        //if all the db data is ran through algorithm or the array list size is bigger than minimum value, do this.
        if(result)
        {
            if(findRidesInterface != null)
            {
                findRidesInterface.FindRidesResult(rideUserArrayList);
            }
        }
    }
}
