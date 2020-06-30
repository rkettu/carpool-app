package com.example.carpool_app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.RatingBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    CircleImageView profileImageView;
    TextView profileNameTextView;
    TextView profileEmailTextView;
    TextView profilePhoNumTextView;
    TextView profileBioTextView;
    TextView profileRatingTextView;
    RatingBar rating;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        profileImageView = findViewById(R.id.profile_image);
        profileNameTextView = findViewById(R.id.profileNameText);
        profileEmailTextView = findViewById(R.id.profileEmailText);
        profilePhoNumTextView= findViewById(R.id.profilePhoNumText);
        profileBioTextView = findViewById(R.id.profileBioText);
        profileRatingTextView = findViewById(R.id.profileRatingText);
        rating = findViewById(R.id.profile_ratingBar);

        Intent i = getIntent();
        User user = (User) i.getSerializableExtra("JOKUKEY");

        // Applying text based on user's info document
        profileNameTextView.setText(user.getFname() + " " + user.getLname());
        profileEmailTextView.setText(user.getEmail());
        profilePhoNumTextView.setText(user.getPhone());
        profileBioTextView.setText(user.getBio());

        try {
            Picasso.with(ProfileActivity.this).load(user.getImgUri()).into(profileImageView);
        } catch (Exception e) {
            e.printStackTrace();
            // Exception loading user image, applying default profile image
            Picasso.with(ProfileActivity.this).load(Constant.defaultProfileImageAddress).into(profileImageView);
        }
    }
}
