package com.example.carpool_app;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

// For doing something after giving the rating
public interface RatingCallback
{
    public void doAfterRating();
}

// Move to CLoud Functions OR set very firm firestore rules
public class RatingGiver {
    private static int minRating = 1;
    private static int maxRating = 5;


    public static void rateRide(int givenRating, String userId, final RatingCallback ratingCallback)
    {
        // Check for invalid values
        if(givenRating < minRating) givenRating = minRating;
        else if(givenRating > maxRating) givenRating = maxRating;

        final float myRating = (float) givenRating;

        // Getting ride's current rating
        final DocumentReference docRef = FirebaseFirestore.getInstance().collection("users").document(userId);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if(doc.exists())
                    {
                        User u = (User) doc.toObject(User.class);
                        float rating = u.getRating();
                        float ratingAmount = (float) u.getRatingAmount();

                        // Calculating new rating
                        float newRating = (rating * ratingAmount + myRating) / (ratingAmount+1);
                        float newRatingAmount = ratingAmount + 1;

                        // Updating rating fields
                        docRef.update(
                                "rating", newRating,
                                "ratingAmount", newRatingAmount
                        );

                        // Rating complete, performing callback function
                        ratingCallback.doAfterRating();

                    }
                }
            }
        });
    }
}
