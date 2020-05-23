package com.example.carpool_app;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileActivity extends AppCompatActivity {
    private static final int IMAGE_PICK_CODE = 1000;
    private static final int PERMISSION_CODE = 1001;


    CircleImageView profileImage;
    EditText fNameEdit;
    EditText lNameEdit;
    EditText eMailEdit;
    EditText cellEdit;
    EditText bioEdit;
    EditText passEdit;
    EditText passConfEdit;
    User mUser = null;
    String uid = null;

    ArrayList<EditText> textEditArray;

    private FirebaseAuth mAuth;
    private Button applyButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        mAuth = FirebaseAuth.getInstance();

        profileImage = findViewById(R.id.editprofile_profile_image);
        fNameEdit = findViewById(R.id.editprofile_firstNameedit);
        lNameEdit = findViewById(R.id.editprofile_lastNameedit);
        eMailEdit = findViewById(R.id.editprofile_emailedit);
        cellEdit = findViewById(R.id.editprofile_phoNumedit);
        bioEdit = findViewById(R.id.editprofile_bioedit);
        passEdit = findViewById(R.id.editprofile_passedit);
        passConfEdit = findViewById(R.id.editprofile_passtwoedit);

        textEditArray = new ArrayList<>();
        textEditArray.add(fNameEdit);
        textEditArray.add(lNameEdit);
        textEditArray.add(eMailEdit);
        textEditArray.add(cellEdit);
        textEditArray.add(bioEdit);

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Attempting to fill fields with user's info...
        mUser = (User) getIntent().getSerializableExtra("USERINFO");
        if(mUser != null)
        {
            try {
                // Loading profile image...
                String imgUri = mUser.getImgUri();
                Picasso.with(EditProfileActivity.this).load(imgUri).into(profileImage);
                profileImage.setTag(imgUri);
            } catch(Exception e) {
                // Failed to load profile image
                e.printStackTrace();
            }
            fNameEdit.setText(mUser.getFname());
            lNameEdit.setText(mUser.getLname());
            eMailEdit.setText(mUser.getEmail());
            cellEdit.setText(mUser.getPhone());
            bioEdit.setText(mUser.getBio());
        } else {
            // No user info? Generating User instance with placeholder variables...
            mUser = new User("Fname", "Lname", "0450450450", "placeholder", "placeholder",
                    Constant.defaultProfileImageAddress, uid, null, 0, 0);
        }

        applyButton = findViewById(R.id.editprofile_saveProfileDetailButton);
        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ApplyChanges();
            }
        });
    }

    // Function for saving changes made to profile and updating them to database
    // Also sets user logged in to True on success
    private void ApplyChanges()
    {
        boolean hasEmptyFields = false;

        for(EditText et : textEditArray)
        {
            if(et.getText().equals(""))
            {
                // Some text field is empty...
                Toast.makeText(EditProfileActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        final User user = mUser;

        // All necessary fields are filled...
        // Attempting to set new user info
        user.setProfCreated(true);
        user.setFname(fNameEdit.getText().toString());
        user.setLname(lNameEdit.getText().toString());
        user.setBio(bioEdit.getText().toString());
        user.setPhone(cellEdit.getText().toString());
        user.setEmail(eMailEdit.getText().toString());

        final StorageReference ppRef = FirebaseStorage.getInstance().getReference().child("profpics/"+uid);

        // Attempting to save profile image to storage
        final String imgUri = (String) profileImage.getTag();
        ppRef.putFile(Uri.parse(imgUri)).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful())
                {
                    // Image uploaded to storage succesfully!
                    // Getting image download address...
                    FirebaseStorage.getInstance().getReference("profpics/"+uid).getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {

                            if(task.getResult() != null)
                            {
                                // Image download address acquired
                                // Now let's apply all changes to user's own document
                                user.setImgUid(String.valueOf(task.getResult()));
                                FirebaseFirestore.getInstance().collection("users").document(uid).set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful())
                                        {
                                            Toast.makeText(EditProfileActivity.this, "Changes applied successfully", Toast.LENGTH_SHORT).show();
                                            FirebaseHelper.loggedIn = true; // User now has full logged-in permissions
                                        } else {
                                            // error setting user info...
                                            Toast.makeText(EditProfileActivity.this, "Saving changes failed... please try again", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        }
                    });


                }
            }
        });

    }




/////////////Check permissions for picking images from phones external storage. Nothing else below/////////////////

    public void pickImage(View v){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_DENIED){
                //permissions not granted
                String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};

                requestPermissions(permissions, PERMISSION_CODE);
            }else{
                //permissions granted
                pickImageFromGallery();
            }
        }else {
            pickImageFromGallery();
        }
    }

    private void pickImageFromGallery() {
        Intent pickIntent = new Intent(Intent.ACTION_PICK);
        pickIntent.setType("image/*");
        startActivityForResult(pickIntent, IMAGE_PICK_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case PERMISSION_CODE:{
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    pickImageFromGallery();
                }else {
                    Toast.makeText(this, "permission denied!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK && requestCode == IMAGE_PICK_CODE){
            final Uri uData = data.getData();
            Log.d("DATAAAAAAAAAAAAAAA", uData.toString());
            profileImage.setImageURI(uData);
            profileImage.setTag(uData);
        }
    }
}


















