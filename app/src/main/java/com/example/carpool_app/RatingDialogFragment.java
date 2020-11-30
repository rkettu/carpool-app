package com.example.carpool_app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

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

public class RatingDialogFragment extends DialogFragment {
    ListView ratingsListView;
    RatingsListAdapter adapter;
    List<RatingItem> rideInfo = new ArrayList<>();
    Context context;

    public RatingDialogFragment(Context context)
    {
        super(); // Calling parent class constructor in case it's required
        this.context = context;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.rating_dialog, null);
        ratingsListView = (ListView)v.findViewById(R.id.rating_dialog_listView);
        adapter = new RatingsListAdapter(context, rideInfo);
        ratingsListView.setAdapter(adapter);
        builder.setTitle("Ratings");
        builder.setView(v)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        RatingDialogFragment.this.getDialog().cancel();
                    }
                });

        fillRatingsList();

        return builder.create();

    }

    private void fillRatingsList()
    {
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
                        FirebaseFirestore.getInstance().collection("users").document(uid)
                                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if(task.isSuccessful()) {
                                    User u = (User)task.getResult().toObject(User.class);
                                    RatingItem ratingItem = new RatingItem();
                                    ratingItem.firstName = u.getFname();
                                    ratingItem.ratingString = String.valueOf(u.getRating());
                                    ratingItem.rideDate = date;
                                    ratingItem.rideStartDestination = rideStartDestination;
                                    ratingItem.imgUri = u.getImgUri();
                                    rideInfo.add(ratingItem);
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
