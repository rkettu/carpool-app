package com.example.carpool_app;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainActivityAdapter extends FragmentPagerAdapter {

    private List<Fragment> fragments = new ArrayList<>();

    public MainActivityAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    //add page
    public void addFragment(Fragment f){
        fragments.add(f);
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return fragments.get(position).toString();
    }
}

class MainActivityCustomAdapter extends BaseAdapter{

    private Context context;
    private ArrayList<RideUser> rideUserArrayList;
    private LayoutInflater layoutInflater;

    public MainActivityCustomAdapter(Context context, ArrayList<RideUser> rideUserArrayList){
        this.context = context;
        this.rideUserArrayList = rideUserArrayList;
    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(layoutInflater == null)
        {
            layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        if(convertView == null)
        {
            convertView = layoutInflater.inflate(R.layout.adapter_main_activity, parent, false);
        }

        TextView dateTV = (TextView) convertView.findViewById(R.id.MainAdapter_dateTextView);
        TextView timeTV = (TextView) convertView.findViewById(R.id.MainAdapter_timeTextView);
        TextView rideTV = (TextView) convertView.findViewById(R.id.MainAdapter_startPointDestinationTextView);
        TextView userNameTv = (TextView) convertView.findViewById(R.id.MainAdapter_userNameTextView);

        //uses calendarHelper class to change time in millis to date time
        long date = rideUserArrayList.get(position).getRide().getLeaveTime();
        String newDate = CalendarHelper.getDateTimeString(date);
        String newTime = CalendarHelper.getHHMMString(date);

        dateTV.setText(newDate);
        timeTV.setText(newTime);
        rideTV.setText(rideUserArrayList.get(position).getRide().getStartCity() + " - " + rideUserArrayList.get(position).getRide().getEndCity());
        userNameTv.setText(rideUserArrayList.get(position).getUser().getFname());

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO ride details
            }
        });

        return convertView;
    }
}

class BookedRides extends Fragment{

    private ArrayList<RideUser> rideUsers;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.adapter_booked_rides, container, false);
        ListView listView = (ListView) rootView.findViewById(R.id.bookedRidesListView);
        MainActivityCustomAdapter adapter = new MainActivityCustomAdapter(this.getContext(), getBookedRides(rideUsers));
        listView.setAdapter(adapter);
        return rootView;
    }

    private ArrayList<RideUser> getBookedRides(final ArrayList<RideUser> rideUsers){
        Query query = FirebaseFirestore.getInstance().collection("rides").whereArrayContains("participants", FirebaseAuth.getInstance().getCurrentUser());
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(QueryDocumentSnapshot doc : task.getResult()){
                        final Ride ride = doc.toObject(Ride.class);
                        final String rideId = doc.getId();

                        FirebaseFirestore.getInstance().collection("users").document(ride.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if(task.isSuccessful()){
                                    DocumentSnapshot userDoc = task.getResult();
                                    if(userDoc.exists()){
                                        final User user = userDoc.toObject(User.class);
                                        rideUsers.add(new RideUser(ride, user, rideId));
                                    }
                                }
                            }
                        });
                    }
                }
            }
        });
        return rideUsers;
    }
}

class OfferedRides extends Fragment{
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.adapter_main_activity, container, false);

        return rootView;
    }
}
