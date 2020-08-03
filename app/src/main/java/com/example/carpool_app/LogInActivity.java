package com.example.carpool_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.view.WindowManager;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthWebException;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.crashlytics.internal.model.CrashlyticsReport;

public class LogInActivity extends AppCompatActivity {

    private final String TAG = "LOGIN_TAG";

    EditText userEdit;
    EditText passEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        userEdit = findViewById(R.id.login_usernameEdit);
        passEdit = findViewById(R.id.login_passwordEdit);

        final Button loginBtn = findViewById(R.id.login_loginButton);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login(v);
            }
        });

        final Button signUpBtn = findViewById(R.id.login_signupButton);
        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Go to user creation activity
                Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getApplicationContext().startActivity(intent);
            }
        });
    }

    // Attempting login - success sends user to previous activity
    public void login(View V) {
        String email = userEdit.getText().toString();
        String password = passEdit.getText().toString();

        if (email.equals("")) {
            noEntry(userEdit);
        } else if (password.equals("")) {
            noEntry(passEdit);
        } else /* attempt to login */ {
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful())
                            {
                                // Logged in successfully
                                Log.d(TAG, "Login with email :: success");
                                Log.d(TAG, "Returning to previous activity...");
                                onBackPressed();
                            } else {
                                // Failed to log in, getting error type
                                Log.d(TAG, "Login with email :: failure");
                                try {
                                    throw task.getException();
                                } catch (FirebaseAuthInvalidUserException e) {
                                    Log.d(TAG, "User error");
                                    Toast.makeText(LogInActivity.this, "Invalid username", Toast.LENGTH_SHORT).show();
                                } catch (FirebaseAuthInvalidCredentialsException e) {
                                    Log.d(TAG, "Password error");
                                    Toast.makeText(LogInActivity.this, "Invalid password", Toast.LENGTH_SHORT).show();
                                } catch (FirebaseAuthWebException e) {
                                    Log.d(TAG, "Web error");
                                    Toast.makeText(LogInActivity.this, "Web error, please try again", Toast.LENGTH_SHORT).show();
                                } catch (Exception e) {
                                    Log.d(TAG, "Unexpected exception :: sending to Firebase Crashlytics");
                                    FirebaseCrashlytics.getInstance().recordException(e);
                                    e.printStackTrace();
                                    Toast.makeText(LogInActivity.this, "Unexpected error. Sending error report", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
        }
    }

    //execute if username or password is empty
    public void noEntry(EditText et){
        et.setText("");
        et.setHintTextColor(Color.parseColor("#B75252"));
        if (et.getId() == R.id.login_usernameEdit) userEdit.setHint("Username*");
        else passEdit.setHint("Password*");
    }

    private void CheckProfileCreated()
    {

    }
}
