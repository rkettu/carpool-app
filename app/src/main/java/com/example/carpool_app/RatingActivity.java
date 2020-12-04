package com.example.carpool_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class RatingActivity extends AppCompatActivity {

    ListView ratingsListView;
    RatingsListAdapter adapter;
    List<RatingItem> rideInfo = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating);

        ratingsListView = (ListView)findViewById(R.id.rating_listView);
        adapter = new RatingsListAdapter(this, this, rideInfo);
        ratingsListView.setAdapter(adapter);

        fillRatingsList();

    }

    @Override
    public void onBackPressed()
    {
        Intent main = new Intent(RatingActivity.this, MainActivity.class);
        startActivity(main);
        finish();
        super.onBackPressed();
    }


    public void fillRatingsList()
    {
        final Context context = this;
        CollectionReference ridesCollection = FirebaseFirestore.getInstance().collection("rides");
        Query q = ridesCollection.whereArrayContains("participants", FirebaseHelper.getUid());
        q.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()) {
                    // Fill list
                    for(QueryDocumentSnapshot doc : task.getResult()) {
                        String uid = (String) doc.get("uid");
                        final String date = CalendarHelper.getDateTimeString((long)doc.get("leaveTime"));
                        final String rideStartDestination = doc.get("startCity") + " - " + doc.get("endCity");
                        final String rideId = doc.getId();
                        FirebaseFirestore.getInstance().collection("users").document(uid)
                                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if(task.isSuccessful()) {
                                    User u = (User)task.getResult().toObject(User.class);
                                    RatingItem ratingItem = new RatingItem();
                                    ratingItem.firstName = u.getFname();
                                    ratingItem.rating = u.getRating();
                                    ratingItem.rideDate = date;
                                    ratingItem.rideStartDestination = rideStartDestination;
                                    ratingItem.imgUri = u.getImgUri();
                                    ratingItem.uid = u.getUid();
                                    ratingItem.associatedRideId = rideId;
                                    rideInfo.add(ratingItem);
                                    // Fetch images before adding to list => no visible loading for them
                                    Picasso.with(context).load(u.getImgUri()).fetch(new Callback() {
                                        @Override
                                        public void onSuccess() {
                                            adapter.notifyDataSetChanged();
                                        }

                                        @Override
                                        public void onError() {
                                            // couldn't load image??
                                            // TODO: probably something
                                        }
                                    });

                                }
                            }
                        });
                    }

                }
            }
        });
    }
}