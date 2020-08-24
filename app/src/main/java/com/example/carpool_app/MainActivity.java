package com.example.carpool_app;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import android.util.Log;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;


import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends FragmentActivity {

    private ViewPager ridesViewPager;
    private FragmentPagerAdapter fragmentPagerAdapter;
    private LinearLayout getRideBtn, offerRideBtn;
    private CollectionReference mRidesColRef = FirebaseFirestore.getInstance().collection("rides");
    private CollectionReference mUsersColRef = FirebaseFirestore.getInstance().collection("users");
    private ArrayList<RideUser> bookedRideUserArrayList = new ArrayList<>();
    private ArrayList<RideUser> offeredRideUserArrayList = new ArrayList<>();
    private int bookedCounter = 0;
    private int offeredCounter = 0;

    ImageView bookBackground;
    ImageView offerbackground;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //findViewById(R.id.main_btnGetRide).setOnClickListener(this);
        //findViewById(R.id.main_btnOfferRide).setOnClickListener(this);

        //setting up buttons
        getRideBtn = findViewById(R.id.main_btnGetRide);
        offerRideBtn = findViewById(R.id.main_btnOfferRide);

        bookBackground = findViewById(R.id.bookBackground);
        offerbackground = findViewById(R.id.offerBackground);
        bookBackground.setImageAlpha(128);

        bookedRideUserArrayList.clear();
        offeredRideUserArrayList.clear();

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

    public void  AppSettings(View v)
    {
        ActivitySwitcher.GoToProfileActivity(this, FirebaseHelper.getUid());
    }

    public void SelectBookedTrips(View v)
    {
        bookBackground.setImageAlpha(255);
        offerbackground.setImageAlpha(128);
        ridesViewPager.setCurrentItem(1);
    }

    public void SelectOfferedTrips(View v) {
        bookBackground.setImageAlpha(128);
        offerbackground.setImageAlpha(255);
        ridesViewPager.setCurrentItem(0);
        /*
        // Go to log in... for testing purposes
        Intent i = new Intent(MainActivity.this, LogInActivity.class);
        startActivity(i);
         */
        // Go to profile test
        //ActivitySwitcher.GoToProfileActivity(getApplicationContext(), FirebaseHelper.getUid());
    }

    public void SelectGetARide(View v){
        Intent GetRideIntent = new Intent(MainActivity.this, GetRideActivity.class);
        startActivity(GetRideIntent);
    }

    public void SelectOfferARide(View v){
        Intent SetRideIntent = new Intent(MainActivity.this, SetRideActivity.class);
        startActivity(SetRideIntent);
    }

        //TODO fix array lists
        //if user is logged in, do db search from booked trips
        private void loadBookedRides()
        {
            mUsersColRef.document(FirebaseAuth.getInstance().getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){
                        try {
                            DocumentSnapshot doc = task.getResult();
                            final User user = doc.toObject(User.class);
                            bookedCounter = user.getBookedRides().size();

                            Log.d("TAG", "onComplete: " + bookedCounter);

                            if(user.getBookedRides().size() != 0){
                                for(int i = 0; i < user.getBookedRides().size(); i++){
                                    mRidesColRef.document(user.getBookedRides().get(i)).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if(task.isSuccessful()){
                                                DocumentSnapshot doc = task.getResult();
                                                Ride ride = doc.toObject(Ride.class);

                                                if(ride.getUid() != null){
                                                    bookedRideUserArrayList.add(new RideUser(ride, user, doc.getId()));
                                                    Log.d("TAG", "onComplete: " + bookedRideUserArrayList.size());
                                                    bookedCounter -= 1;
                                                }
                                            }
                                            if(ride.getUid() != null){
                                                bookedRideUserArrayList.add(new RideUser(ride, user, doc.getId()));
                                                Log.d("TAG", "onComplete14244141: " + bookedRideUserArrayList.size());
                                            }
                                            bookedCounter -= 1;
                                        }
                                    });
                                }
                            }
                            else{
                                Log.d("TAG", "onComplet213143141451rewe: ");
                                loadOfferedRides();
                            }
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                            loadOfferedRides();
                        }

                    }
                }
            });
        }

    //db search for offered rides
    private void loadOfferedRides()
    {
        Log.d("TAG", "onComplete: ");
        Query query = mRidesColRef.whereEqualTo("uid", FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    try{
                        for(QueryDocumentSnapshot doc : task.getResult()){
                            Log.d("TAG", "onComplete22: " + doc.exists());
                            final Ride ride = doc.toObject(Ride.class);
                            final String rideId = doc.getId();
                            Log.d("TAG", "onComplete: " + rideId);
                            offeredCounter += 1;
                            Log.d("TAG", "onComplete: ");
                            mUsersColRef.document(ride.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if(task.isSuccessful()){
                                        DocumentSnapshot doc = task.getResult();
                                        User user = doc.toObject(User.class);
                                        Log.d("TAG", "onComplete: ");

                                        if(user.getFname() != null){
                                            //adds rides to RideUser class
                                            offeredRideUserArrayList.add(new RideUser(ride, user, rideId));
                                        }

                                        Log.d("TAG", "onComplete: " + offeredCounter);
                                        //when done, initializing MainActivity layout elements
                                        if(offeredCounter == 0){
                                            Log.d("TAG", "onComple21314521  5125436tyagz<df §1 te: ");
                                            initMainLayoutItems();
                                            //initMainLayoutButtons();
                                        }
                                    }
                                });
                            }
                            if(task.getResult().getDocuments().size() == 0){
                                initMainLayoutItems();
                                initMainLayoutButtons();
                            }
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                        if(task.getResult().getDocuments().size() == 0){
                            initMainLayoutItems();
                            initMainLayoutButtons();
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            });
        }

        private void initMainLayoutItems() {
        //setting up viewpager, viewpager header and adapter
        Log.d("TAG", "initMainLayoutItems: ");
        ridesViewPager = findViewById(R.id.main_viewPager);
        fragmentPagerAdapter = new RidesViewPagerAdapter(getSupportFragmentManager());
        ridesViewPager.setAdapter(fragmentPagerAdapter);

    }

        //buttons on click events
        /*private void initMainLayoutButtons() {
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
    }*/

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
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

            Log.d("TAG", "getItem: " + bookedRideUserArrayList.size() + " " + offeredRideUserArrayList.size());

            switch(position){
                case 0:
                    return MainActivityFragments.newInstance(bookedRideUserArrayList, offeredRideUserArrayList, 0);

                case 1:
                    return MainActivityFragments.newInstance(bookedRideUserArrayList, offeredRideUserArrayList, 1);

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

                Log.d("TAG", "getItem: " + bookedRideUserArrayList.size() + " " + offeredRideUserArrayList.size());

                switch(position){
                    case 0:
                        return MainActivityFragments.newInstance(bookedRideUserArrayList, offeredRideUserArrayList, 0);

                    case 1:
                        return MainActivityFragments.newInstance(bookedRideUserArrayList, offeredRideUserArrayList, 1);

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
