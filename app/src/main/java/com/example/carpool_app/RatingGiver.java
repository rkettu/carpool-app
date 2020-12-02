package com.example.carpool_app;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;


// Move to CLoud Functions OR set very firm firestore rules
public class RatingGiver {
    private static int minRating = 1;
    private static int maxRating = 5;

    // For doing something after giving the rating
    public interface RatingCallback
    {
        public void doAfterRating();
    }

    public interface ReviewAmountCallback
    {
        public void doAfterGettingAmount(int amount);
    }

    public static void rateRide(float givenRating, String riderId, final String rideId, final RatingCallback ratingCallback)
    {
        // Check for invalid values
        if(givenRating < minRating) givenRating = minRating;
        else if(givenRating > maxRating) givenRating = maxRating;

        final float myRating = (float) givenRating;

        // Getting ride's current rating
        final DocumentReference docRef = FirebaseFirestore.getInstance().collection("users").document(riderId);
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
                        ).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                // removing user from ride participants list
                                final DocumentReference documentRef = FirebaseFirestore.getInstance().collection("rides").document(rideId);
                                documentRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if(task.isSuccessful())
                                        {
                                            DocumentSnapshot document = task.getResult();
                                            Ride r = document.toObject(Ride.class);
                                            r.removeFromParticipants(FirebaseHelper.getUid());
                                            documentRef.update("participants", r.getParticipants()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    // Rating process complete, performing callback function
                                                    ratingCallback.doAfterRating();
                                                }
                                            });
                                        }
                                    }
                                });
                            }
                        });
                    }
                }
            }
        });
    }

    public static void GetAmountOfReviews(String uid)
    {
        CollectionReference ridesCollection = FirebaseFirestore.getInstance().collection("rides");
        Query q = ridesCollection.whereArrayContains("participants", uid);
        q.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    int amount = task.getResult().size();
                    new ReviewAmountCallback().doAfterGettingAmount(amount);
                }
            }
        });
    }
}
