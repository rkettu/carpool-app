package com.example.carpool_app;

import android.content.Context;
import android.content.Intent;
import android.telecom.Call;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

// Class for switching to a new activity
// Ensures data is preloaded before moving to that activity
public class ActivitySwitcher {

    // Go to profile by knowing user id
    public static void GoToProfileActivity(final Context context, String uid)
    {
        // Fetching profile info
        FirebaseFirestore.getInstance().collection("users").document(uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful())
                {
                    final Intent intent = new Intent(context, ProfileActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    try{
                        User user = (User) task.getResult().toObject(User.class);
                        intent.putExtra("JOKUKEY", user);
                        // Preloading profile image
                        Picasso.with(context).load(user.getImgUri()).fetch(new Callback() {
                            @Override
                            public void onSuccess() {
                                // Successfully cached profile image
                                context.startActivity(intent);
                            }

                            @Override
                            public void onError() {

                            }
                        });
                    } catch(Exception e) {
                        e.printStackTrace();
                        // Couldn't load url or apply toObject to document results
                        // => caching default image
                        Picasso.with(context).load(Constant.defaultProfileImageAddress).fetch(new Callback() {
                            @Override
                            public void onSuccess() {
                                // Successfully cached profile image
                                context.startActivity(intent);
                            }

                            @Override
                            public void onError() {

                            }
                        });
                    }

                }
            }
        });
    }
}
