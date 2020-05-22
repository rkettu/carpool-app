package com.example.carpool_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthEmailException;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class SignUpActivity extends AppCompatActivity {

    private final String TAG = "SignUpActivity";

    EditText fnameEdit;
    EditText lnameEdit;
    EditText emailEdit;
    EditText phoneEdit;
    EditText passEdit;
    EditText confirmPassEdit;
    CheckBox checkTerms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        InitEditTexts(); // initialize View instances and put into list

        checkTerms = findViewById(R.id.signup_serviceBox);

        Button confirmSignUpButton = findViewById(R.id.signup_confirmSignUpButton);
        confirmSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SignUpFunc();
            }
        });
    }

    // Initialize EditText instances
    private void InitEditTexts()
    {
        fnameEdit = findViewById(R.id.signup_fnameEdit);
        lnameEdit = findViewById(R.id.signup_lnameEdit);
        phoneEdit = findViewById(R.id.signup_phoneEdit);
        emailEdit = findViewById(R.id.signup_emailEdit);
        passEdit = findViewById(R.id.signup_passEdit);
        confirmPassEdit = findViewById(R.id.signup_confirmPassEdit);
    }

    // Called on clicking confirm signup
    private void SignUpFunc()
    {
        boolean allFieldsFilled = true;

        List<String> fieldDataList = new ArrayList<>();

        final String email = emailEdit.getText().toString();
        final String pass1 = passEdit.getText().toString();
        final String pass2 = confirmPassEdit.getText().toString();
        final String phone = phoneEdit.getText().toString();
        final String fname = fnameEdit.getText().toString();
        final String lname = lnameEdit.getText().toString();

        fieldDataList.add(email);
        fieldDataList.add(pass1);
        fieldDataList.add(pass2);
        fieldDataList.add(phone);
        fieldDataList.add(fname);
        fieldDataList.add(lname);


        // Checking that there are no empty fields
        for(String fieldText : fieldDataList)
        {
            if(fieldText.equals(""))
                allFieldsFilled = false;
        }

        // TODO: maybe validate phone number (can only contain numbers, spaces and +) and email (must contain @ and . )

        // Checking that passwords match
        if(!pass1.equals(pass2))
        {
            Toast.makeText(SignUpActivity.this, "Passwords don't match", Toast.LENGTH_SHORT).show();
            return;
        }

        if(!allFieldsFilled)
        {
            Toast.makeText(SignUpActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // All checks passed, continue with user creation
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, pass1).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful())
                {
                    Log.d(TAG, "User created with email: " + email);
                    // FirebaseUser created successfully!

                    // Setting first name as FirebaseUsers display name
                    UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                            .setDisplayName(fname).build();
                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    currentUser.updateProfile(profileUpdate);

                    String uid = currentUser.getUid();

                    User user = new User(fname, lname, phone, email, "This is my bio", Constant.defaultProfileImageAddress, uid, null, 0, 0);
                    user.setProfCreated(false);

                    // Moving to edit profile activity...
                    Intent i = new Intent(getApplicationContext(), EditProfileActivity.class);
                    i.putExtra("USERINFO", user);
                    startActivity(i);

                } else /* failed to create user */ {
                    try {
                        throw task.getException();
                    } catch(FirebaseAuthUserCollisionException e) {
                        Toast.makeText(SignUpActivity.this, "Account already exists for that email", Toast.LENGTH_SHORT).show();
                    } catch(FirebaseAuthWeakPasswordException e) {
                        Toast.makeText(SignUpActivity.this, "Password too weak", Toast.LENGTH_SHORT).show();
                    } catch(FirebaseAuthEmailException e) {
                        Toast.makeText(SignUpActivity.this, "Invalid email address", Toast.LENGTH_SHORT).show();
                    } catch(Exception e) {
                        Log.d(TAG, "Unexpected exception :: sending to Firebase Crashlytics");
                        FirebaseCrashlytics.getInstance().recordException(e);
                        e.printStackTrace();
                        Toast.makeText(SignUpActivity.this, "Unexpected error. Sending error report", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}
