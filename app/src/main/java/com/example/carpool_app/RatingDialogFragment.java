package com.example.carpool_app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RatingDialogFragment extends DialogFragment {
    ListView ratingsListView;
    RatingsListAdapter adapter;
    List<User> rideOwners = new ArrayList<>();

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        ratingsListView = (ListView) R.id.rating_dialog_listView;
        adapter = new RatingsListAdapter(this, rideOwners);
        ratingsListView.setAdapter(adapter);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        builder.setTitle("Ratings");
        builder.setView(inflater.inflate(R.layout.rating_dialog, null))
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        RatingDialogFragment.this.getDialog().cancel();
                    }
                });
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
                        FirebaseFirestore.getInstance().collection("user").document((String)doc.get("uid"))
                                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if(task.isSuccessful()) {
                                    rideOwners.add((User)task.getResult().toObject(User.class));
                                    adapter.notifyDataSetChanged();
                                }
                            }
                        });
                    }

                }
            }
        });
    }
}
