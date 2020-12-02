package com.example.carpool_app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

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
    Context context;
    RatingItem ratingItem;
    RatingDialogFragment dialog;
    Activity activity;

    public RatingDialogFragment(Context context, RatingItem ratingItem, Activity activity)
    {
        super(); // Calling parent class constructor in case it's required
        this.context = context;
        this.ratingItem = ratingItem;
        dialog = this;
        this.activity = activity;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.rating_dialog, null);
        builder.setTitle("How would you rate this ride?");
        builder.setView(v)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        RatingDialogFragment.this.getDialog().cancel();
                    }
                });
        ((TextView)v.findViewById(R.id.rating_dialog_fname)).setText(ratingItem.firstName);
        ((TextView)v.findViewById(R.id.rating_dialog_currentRating)).setText(String.valueOf(ratingItem.rating));
        Picasso.with(context).load(ratingItem.imgUri).into((ImageView)v.findViewById(R.id.rating_dialog_profile_img));

        final RatingBar rBar = (RatingBar)v.findViewById(R.id.rating_dialog_ratingBar);
        ((Button)v.findViewById(R.id.rating_dialog_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float setRating = rBar.getRating();
                RatingGiver.rateRide(setRating, ratingItem.uid, ratingItem.associatedRideId, new RatingGiver.RatingCallback() {
                    @Override
                    public void doAfterRating() {
                        // UPDATE LIST => rated user should be removed
                        dialog.dismiss();
                        activity.finish();
                        activity.overridePendingTransition(0, 0);
                        activity.startActivity(activity.getIntent());
                        activity.overridePendingTransition(0, 0);
                    }
                });
            }
        });

        return builder.create();

    }

}
