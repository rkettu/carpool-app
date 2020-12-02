package com.example.carpool_app;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;

import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;


import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;


public class MainActivity extends FragmentActivity implements NavigationView.OnNavigationItemSelectedListener {

    // NavigationDrawer menu
    DrawerLayout drawer;
    NavigationView navigationView;


    private ViewPager ridesViewPager;
    private FragmentPagerAdapter fragmentPagerAdapter;
    private LinearLayout getRideBtn, offerRideBtn;
    private CollectionReference mRidesColRef = FirebaseFirestore.getInstance().collection("rides");
    private CollectionReference mUsersColRef = FirebaseFirestore.getInstance().collection("users");
    private ArrayList<RideUser> bookedRideUserArrayList = new ArrayList<>();
    private ArrayList<RideUser> offeredRideUserArrayList = new ArrayList<>();


    ImageView bookBackground;
    ImageView offerbackground;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);


        //NavigationDrawer
        drawer =  findViewById(R.id.drawer_layout);
        navigationView =  findViewById(R.id.nav_view);
        NavigationView navigationView = findViewById(R.id.nav_view);
        Menu menu = navigationView.getMenu();
        MenuItem logged_item = menu.findItem(R.id.nav_loginfo);
        View hView = navigationView.inflateHeaderView(R.layout.navi_header);
        TextView naviUser = (TextView) hView.findViewById(R.id.navi_header_text);

        if (FirebaseAuth.getInstance().getCurrentUser() != null){
            //navigationView.getMenu().clear(); // tyhjää itemit
            naviUser.setText(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
            logged_item.setTitle("LOGGED IN");
            //navigationView.inflateHeaderView(R.layout.navi_header);
        }else {
            //navigationView.getMenu().clear(); // tyhjää itemit
            logged_item.setTitle("LOGGED OUT");
        }
        navigationView.setNavigationItemSelectedListener(this);

        // Introduce first-time users to your app
        String tutorialKey = "SOME_KEY";
        Boolean firstTime = getPreferences(MODE_PRIVATE).getBoolean(tutorialKey, true);
        if (firstTime){
            Intent startIntro = new Intent(MainActivity.this, StartIntroActivity.class);
            startActivity(startIntro);
            getPreferences(MODE_PRIVATE).edit().putBoolean(tutorialKey, false).apply();
        }

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
                }
            }
        };
        FirebaseAuth.getInstance().addAuthStateListener(als);
    }

    public void AppSettings(View v) {
        drawer.openDrawer(Gravity.RIGHT);
    }

    public void SelectBookedTrips(View v) {
        bookBackground.setImageAlpha(255);
        offerbackground.setImageAlpha(128);
        ridesViewPager.setCurrentItem(1);
    }

    public void SelectOfferedTrips(View v) {
        bookBackground.setImageAlpha(128);
        offerbackground.setImageAlpha(255);
        ridesViewPager.setCurrentItem(0);
    }

    public void SelectGetARide(View v) {
        Intent GetRideIntent = new Intent(MainActivity.this, GetRideActivity.class);
        startActivity(GetRideIntent);
    }

    public void SelectOfferARide(View v) {
        Intent SetRideIntent = new Intent(MainActivity.this, SetRideActivity.class);
        startActivity(SetRideIntent);
    }

    //first get rideId's from current user
    //then get rides
    //then get users

    private int NUMBER_OF_BOOKED_TASKS = 0;
    private int bookedCounter = 0;

    private void loadBookedRides() {
        final ArrayList<String> curUserRides = new ArrayList<>();
        Task<DocumentSnapshot> curUserTask = mUsersColRef.document(FirebaseAuth.getInstance().getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    try{
                        curUserRides.addAll((ArrayList<String>) task.getResult().get("bookedRides"));
                        NUMBER_OF_BOOKED_TASKS = curUserRides.size();
                        Log.d("TAG", "12313123: ");
                    }
                    catch (Exception e){
                        e.printStackTrace();
                        loadOfferedRides();
                    }
                }
                else{
                    //task is not successful
                    loadOfferedRides();
                }
            }
        });

        Tasks.whenAll(curUserTask).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    try{
                        if(curUserRides.size() > 0){
                            for(int i = 0; i < curUserRides.size(); i++){
                                mRidesColRef.document(curUserRides.get(i)).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if(task.isSuccessful()){
                                            final Ride ride = task.getResult().toObject(Ride.class);
                                            final String rideId = task.getResult().getId();
                                            mUsersColRef.document(ride.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                    if(task.isSuccessful()) {
                                                        User user = task.getResult().toObject(User.class);
                                                        bookedRideUserArrayList.add(new RideUser(ride, user, rideId));
                                                        taskCompletedBookedRides();
                                                    }
                                                    else {
                                                        //task is not successful
                                                        loadOfferedRides();
                                                    }
                                                }
                                            });
                                        }
                                        else{
                                            //task is not successful
                                            loadOfferedRides();
                                        }
                                    }
                                });
                            }
                        }
                        else{
                            //no booked rides
                            loadOfferedRides();
                        }
                    } catch (Exception e){
                        e.printStackTrace();
                        loadOfferedRides();
                    }
                }
                else{
                    //task is not successful
                    loadOfferedRides();
                }
            }
        });
    }

    private synchronized void taskCompletedBookedRides() {
        bookedCounter ++;
        if(bookedCounter == NUMBER_OF_BOOKED_TASKS){
            loadOfferedRides();
        }
    }

    private static int NUMBER_OF_OFFERED_TASKS = 0;
    private int offeredCounter = 0;

    private void loadOfferedRides() {
        mRidesColRef.whereEqualTo("uid", FirebaseAuth.getInstance().getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    try {
                        NUMBER_OF_OFFERED_TASKS = task.getResult().size();
                        if (NUMBER_OF_OFFERED_TASKS > 0) {
                            Log.d("TAG", "onComplete task size: " + NUMBER_OF_OFFERED_TASKS);
                            try {
                                for (QueryDocumentSnapshot doc : task.getResult()) {
                                    final Ride ride = doc.toObject(Ride.class);
                                    final String rideId = doc.getId();

                                    mUsersColRef.document(ride.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                DocumentSnapshot doc = task.getResult();
                                                User user = doc.toObject(User.class);
                                                offeredRideUserArrayList.add(new RideUser(ride, user, rideId));
                                                Log.d("TAG", "onComplete: ");
                                                taskCompletedOfferedRides();
                                            } else {
                                                //task is not successful
                                                initMainLayoutItems();
                                            }
                                        }
                                    });
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                taskCompletedOfferedRides();
                            }
                        } else {
                            initMainLayoutItems();
                            //No offered rides
                        }
                    }
                    catch (Exception e){
                        e.printStackTrace();
                        initMainLayoutItems();
                    }
                }
                else{
                    initMainLayoutItems();
                    //task is not successful
                }
            }
        });
    }

    private synchronized void taskCompletedOfferedRides() {
        offeredCounter ++;
        if(NUMBER_OF_OFFERED_TASKS == offeredCounter) {
            initMainLayoutItems();
        }
    }

    private void initMainLayoutItems() {
        //setting up viewpager, viewpager header and adapter
        Log.d("TAG", "initMainLayoutItems: ");
        ridesViewPager = findViewById(R.id.main_viewPager);
        fragmentPagerAdapter = new RidesViewPagerAdapter(getSupportFragmentManager());
        ridesViewPager.setAdapter(fragmentPagerAdapter);
        //page change listener is for header layout background color change
        ridesViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if(position == 0)
                {
                    bookBackground.setImageAlpha(128);
                    offerbackground.setImageAlpha(255);
                }
                else{
                    bookBackground.setImageAlpha(255);
                    offerbackground.setImageAlpha(128);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    //Exits application or navigationDrawer when pressed back
    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(Gravity.RIGHT)){
            drawer.closeDrawer(Gravity.RIGHT);
        }else {
            super.onBackPressed();
            finishAffinity();
        }
    }

    // NavigationDrawer items functionality
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.nav_profile:
                Log.d("NAVI", "onNavigationItemSelected: PROFILE");
                break;

            case R.id.nav_rating:
                Log.d("NAVI", "onNavigationItemSelected: RATING ");
                break;

            case R.id.nav_getride:
                drawer.closeDrawer(Gravity.RIGHT);
                SelectGetARide(null);
                break;

            case R.id.nav_setride:
                drawer.closeDrawer(Gravity.RIGHT);
                SelectOfferARide(null);
                break;

        }
        return false;
    }

    //viewpager for rides
    public class RidesViewPagerAdapter extends FragmentPagerAdapter {
        //page count is 2, booked and offered rides.
        private static final int PAGE_COUNT = 2;
        View v;
        //tabs titles will always be booked rides and offered rides.
        private final String[] tabTitles = new String[]{getResources().getString(R.string.main_fragment_tab_booked), getResources().getString(R.string.main_fragment_tab_offered)};

        //constructor for viewpager
        public RidesViewPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @NonNull
        @Override
        public Fragment getItem (int position){
            /**
             * switch case in viewpager to know which data to show.
             * case 0 = booked rides.
             * case 1 = offered rides.
             * The integer is there to tell fragments what to print if the array list size is 0 in
             * MainActivityFragments.java
             */

            Log.d("TAG", "getItem: " + bookedRideUserArrayList.size() + " " + offeredRideUserArrayList.size());

            switch (position) {
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
        public CharSequence getPageTitle ( int position){
            return tabTitles[position];
        }

        @Override
        public int getCount () {
            return PAGE_COUNT;
        }
    }
}