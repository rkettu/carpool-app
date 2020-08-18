package com.example.carpool_app;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;

import android.util.Log;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;


import android.widget.Button;

import java.util.ArrayList;


public class MainActivity extends FragmentActivity {

    private ViewPager ridesViewPager;
    private TabLayout ridesHeader;
    private FragmentPagerAdapter fragmentPagerAdapter;
    private Button getRideBtn, offerRideBtn;
    private CollectionReference mRidesColRef = FirebaseFirestore.getInstance().collection("rides");
    private CollectionReference mUsersColRef = FirebaseFirestore.getInstance().collection("users");
    private ArrayList<RideUser> bookedRideUserArrayList = new ArrayList<>();
    private ArrayList<RideUser> offeredRideUserArrayList = new ArrayList<>();
    private int counter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setting auth state listener
        // onAuthStateChanged is called when user logs in / out
        // onAuthStateChanged is also called when starting app
        FirebaseAuth.AuthStateListener als = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                    // If user has logged in
                    Log.d("TAG", "onAuthStateChanged: true");
                    // Checking that user has created a profile
                    // Sends user to profile edit if not
                    FirebaseHelper.checkProfileCreated(getApplicationContext());
                    //start loading rides to view pager if logged in.
                    loadBookedRides();
                } else {
                    // If user has logged out
                    Log.d("TAG", "onAuthStateChanged: false");
                    FirebaseHelper.loggedIn = false;
                    //skips the ride loading if not signed in.
                    initMainLayoutItems();
                    initMainLayoutButtons();
                }
            }
        };
        FirebaseAuth.getInstance().addAuthStateListener(als);
    }

    //TODO fix array lists
    //if user is logged in, do db search from booked trips
    private void loadBookedRides()
    {
        Log.d("TAG", "loadBookedRides: ");
        Query bookedQuery = mRidesColRef.whereArrayContains("participants", FirebaseAuth.getInstance().getCurrentUser().getUid());
        bookedQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(QueryDocumentSnapshot doc : task.getResult()){
                        final Ride ride = doc.toObject(Ride.class);
                        final String rideId = doc.getId();
                        counter += 1;

                        if(ride.getUid() != null){
                            mUsersColRef.document(ride.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if(task.isSuccessful()){
                                        DocumentSnapshot doc = task.getResult();
                                        User user = doc.toObject(User.class);

                                        //adds found rides to RideUser class.
                                        bookedRideUserArrayList.add(new RideUser(ride, user, rideId));
                                        counter -= 1;
                                    }
                                    if(counter == 0){
                                        //when done, search offered rides
                                        loadOfferedRides();
                                    }
                                }
                            });
                        }
                    }
                }
            }
        });
    }

    //db search for offered rides
    private void loadOfferedRides()
    {
        Query query = mRidesColRef.whereEqualTo("uid", FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(QueryDocumentSnapshot doc : task.getResult()){
                        final Ride ride = doc.toObject(Ride.class);
                        final String rideId = doc.getId();
                        counter += 1;

                        mUsersColRef.document(ride.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if(task.isSuccessful()){
                                    DocumentSnapshot doc = task.getResult();
                                    User user = doc.toObject(User.class);

                                    if(user.getFname() != null){
                                        //adds rides to RideUser class
                                        offeredRideUserArrayList.add(new RideUser(ride, user, rideId));
                                        counter -= 1;
                                    }
                                }
                                //when done, initializing MainActivity layout elements
                                if(counter == 0){
                                    initMainLayoutItems();
                                    initMainLayoutButtons();
                                }
                            }
                        });
                    }
                }
            }
        });
    }

    private void initMainLayoutItems() {
        //setting up viewpager, viewpager header and adapter
        ridesViewPager = findViewById(R.id.main_viewPager);
        fragmentPagerAdapter = new RidesViewPagerAdapter(getSupportFragmentManager());
        ridesViewPager.setAdapter(fragmentPagerAdapter);
        ridesHeader = findViewById(R.id.main_viewPagerHeader);
        ridesHeader.setupWithViewPager(ridesViewPager);

        //setting up buttons
        getRideBtn = findViewById(R.id.main_btnGetRide);
        offerRideBtn = findViewById(R.id.main_btnOfferRide);
    }

    //buttons on click events
    private void initMainLayoutButtons() {
        getRideBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent GetRideIntent = new Intent(MainActivity.this, GetRideActivity.class);
                startActivity(GetRideIntent);
            }
        });

        offerRideBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent SetRideIntent = new Intent(MainActivity.this, SetRideActivity.class);
                startActivity(SetRideIntent);
            }
        });
    }

    //viewpager for rides
    public class RidesViewPagerAdapter extends FragmentPagerAdapter{
        //page count is 2, booked and offered rides.
        private static final int PAGE_COUNT = 2;
        //tabs titles will always be booked rides and offered rides.
        private final String[] tabTitles = new String[] {getResources().getString(R.string.main_fragment_tab_booked), getResources().getString(R.string.main_fragment_tab_offered)};
        //constructor for viewpager
        public RidesViewPagerAdapter(FragmentManager fragmentManager){
            super(fragmentManager);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            /**
             * switch case in viewpager to know which data to show.
             * case 0 = booked rides.
             * case 1 = offered rides.
             * The integer is there to tell fragments what to print if the array list size is 0 in
             * MainActivityFragments.java
             */

            try {
                Log.d("TAG", "getItem: " + bookedRideUserArrayList.size() + " " + bookedRideUserArrayList.get(0).getUser().getFname());
                Log.d("TAG", "getItem: " + offeredRideUserArrayList.size() + " " + offeredRideUserArrayList.get(0).getUser().getFname());
            }catch (Exception e){
                e.printStackTrace();
            }

            switch(position){
                case 0:
                    return MainActivityFragments.newInstance(bookedRideUserArrayList, offeredRideUserArrayList, 0);

                case 1:
                    return MainActivityFragments.newInstance(offeredRideUserArrayList, offeredRideUserArrayList, 1);

                default:
                    return null;
            }
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return tabTitles[position];
        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }
    }
}
