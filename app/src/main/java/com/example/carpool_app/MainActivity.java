package com.example.carpool_app;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
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
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;


public class MainActivity extends FragmentActivity {

    private ViewPager ridesViewPager;
    private TabLayout ridesHeader;
    private FragmentPagerAdapter fragmentPagerAdapter;
    private Button getRideBtn, offerRideBtn;
    private boolean userLoggedIn = false;
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
                    //changes boolean to true, so we can load the rides in front screen
                    userLoggedIn = true;
                    loadBookedRides();
                } else {
                    // If user has logged out
                    Log.d("TAG", "onAuthStateChanged: false");
                    FirebaseHelper.loggedIn = false;
                    //changes boolean to false, so program doesn't try to load rides
                    userLoggedIn = false;
                    initMainLayoutItems();
                    initMainLayoutButtons();
                }
            }
        };
        FirebaseAuth.getInstance().addAuthStateListener(als);

        Log.d("TAG", "onCreate1: " + userLoggedIn);
        if(userLoggedIn)
        {

        }
        else {
        }
        Log.d("TAG", "onCreate2: " + userLoggedIn);
    }

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

                        mUsersColRef.document(ride.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if(task.isSuccessful()){
                                    DocumentSnapshot doc = task.getResult();
                                    User user = doc.toObject(User.class);

                                    if(user.getFname() != null){
                                        bookedRideUserArrayList.add(new RideUser(ride, user, rideId));
                                    }
                                }
                            }
                        }).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if(documentSnapshot.exists()){
                                    loadOfferedRides();
                                }
                            }
                        });
                    }
                }
            }
        });
    }

    private void loadOfferedRides()
    {
        Log.d("TAG", "loadOfferedRides: ");
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
                                        offeredRideUserArrayList.add(new RideUser(ride, user, rideId));
                                        counter -= 1;
                                        Log.d("TAG", "onComplete: " + offeredRideUserArrayList);
                                    }
                                }
                                if(counter == 0){

                                    Log.d("TAG", "on counter = 0: ");
                                    initMainLayoutItems();
                                    initMainLayoutButtons();

                                }
                            }
                        }).addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if(documentSnapshot.exists()){
                                    Log.d("TAG", "onSuccess: ");
                                }
                            }
                        });
                    }
                }
            }
        });
    }

    private void initMainLayoutItems() {
        ridesViewPager = findViewById(R.id.main_viewPager);
        fragmentPagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
        ridesViewPager.setAdapter(fragmentPagerAdapter);
        ridesHeader = findViewById(R.id.main_viewPagerHeader);
        ridesHeader.setupWithViewPager(ridesViewPager);
        ridesHeader.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener()
        {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        getRideBtn = findViewById(R.id.main_btnGetRide);
        offerRideBtn = findViewById(R.id.main_btnOfferRide);
    }

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

    public class MyPagerAdapter extends FragmentPagerAdapter{
        private static final int PAGE_COUNT = 2;
        private String[] tabTitles = new String[] {getResources().getString(R.string.main_fragment_tab_booked), getResources().getString(R.string.main_fragment_tab_offered)};
        public MyPagerAdapter(FragmentManager fragmentManager){
            super(fragmentManager);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            //TODO get data from database
            switch(position){
                case 0:
                    return MainActivityFragments.newInstance(bookedRideUserArrayList);

                case 1:
                    return MainActivityFragments.newInstance(offeredRideUserArrayList);

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
