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
    User user;

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

            // Putting image to storage
            StorageReference ref = FirebaseStorage.getInstance().getReference().child("profpics/" + user.getUid());
            ref.putFile(uData).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if(task.isSuccessful())
                    {
                        // image uploaded to storage successfully!
                        Log.d("IMAGETASK", "Image upload to storage :: SUCCESS");
                        // Setting profile image to circleview
                        profileImage.setImageURI(uData);
                        // TODO: Set some value such as hasImage to user...
                    } else {
                        // Image upload failed
                        Log.d("IMAGETASK", "Image upload to storage :: FAILURE");
                        // Informing user...
                        Toast.makeText(EditProfileActivity.this, "Image upload failed", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}


















