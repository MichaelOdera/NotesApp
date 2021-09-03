package com.michael.notesapp.ui.authentication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.michael.notesapp.R;
import com.michael.notesapp.ui.MainActivity;
import com.michael.notesapp.ui.NotesActivity;

public class RegistrationActivity extends AppCompatActivity implements View.OnClickListener{

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;


    private EditText mNameEditText;
    private EditText mEmailEditText;
    private EditText mPasswordEditText;
    private EditText mConfirmPasswordEditText;

    private String mName;
    private String mEmail;
    private String mPassword;
    private String mConfirmPassword;

    private Button mRegistrationButton;
    private TextView mReDirectToLogin;

    private ProgressDialog mProgressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        mAuth = FirebaseAuth.getInstance();

        createAuthStateListener();

        mProgressDialog = new ProgressDialog(this);

        mRegistrationButton = findViewById(R.id.createUserButton);
        mReDirectToLogin = findViewById(R.id.loginTextView);

        mRegistrationButton.setOnClickListener(this);
        mReDirectToLogin.setOnClickListener(this);

        mNameEditText = findViewById(R.id.nameOfUserEditText);
        mEmailEditText = findViewById(R.id.emailEditText);
        mPasswordEditText = findViewById(R.id.passwordEditText);
        mConfirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);

    }

    private void createAuthStateListener() {

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if(user != null){
                    Intent loginIntent = new Intent(RegistrationActivity.this, MainActivity.class);
                    loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK );
                    RegistrationActivity.this.startActivity(loginIntent);
                    RegistrationActivity.this.finish();


                }
            }
        };
    }


    @Override
    public void onClick(View v) {
        if(v == mRegistrationButton){
            fetchEnteredData();
            boolean isValid = checkIfDataValid();

            if(isValid){
                mProgressDialog.show();
                startRegistrationProcess();
            }
        }

        if(v == mReDirectToLogin){
            Intent loginIntent = new Intent(RegistrationActivity.this, LoginActivity.class);
            loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            RegistrationActivity.this.startActivity(loginIntent);
            RegistrationActivity.this.finish();

        }

    }




    private void fetchEnteredData() {
        mName = mNameEditText.getText().toString();
        mEmail = mEmailEditText.getText().toString();
        mPassword = mPasswordEditText.getText().toString();
        mConfirmPassword = mConfirmPasswordEditText.getText().toString();
    }


    private boolean checkIfDataValid() {
        boolean result = false;
        boolean validName = isValidName(mName);
        boolean validEmail=  isValidEmail(mEmail);
        boolean validPassword = isValidPassword(mPassword, mConfirmPassword);

        if(validEmail && validName && validPassword){
            result = true;
        }
        return result;
    }

    private boolean isValidEmail(String email) {
        boolean isGoodEmail = (email != null && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches());
        if (!isGoodEmail) {
            mEmailEditText.setError("Please enter a valid email address");
            return false;
        }
        return true;
    }

    private boolean isValidName(String name) {
        if (name.equals("")) {
            mNameEditText.setError("Please enter your name");
            return false;
        }
        return true;
    }

    private boolean isValidPassword(String password, String confirmPassword) {
        if (password.length() < 6) {
            mPasswordEditText.setError("Please create a password containing at least 6 characters");
            return false;
        } else if (!password.equals(confirmPassword)) {
            mPasswordEditText.setError("Passwords do not match");
            return false;
        }
        return true;
    }


    private void startRegistrationProcess() {

        mAuth.createUserWithEmailAndPassword(mEmail, mPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    dismissProgressDialog();
                    FirebaseUser user = task.getResult().getUser();
                    createUserProfile(user);
                    Toast.makeText(RegistrationActivity.this, "Successful Sign-Up", Toast.LENGTH_SHORT).show();
                    Intent loginIntent = new Intent(RegistrationActivity.this, MainActivity.class);
                    loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK );
                    RegistrationActivity.this.startActivity(loginIntent);
                    RegistrationActivity.this.finish();
                }

                if(!task.isSuccessful()){
                    dismissProgressDialog();
                    Toast.makeText(RegistrationActivity.this, "Could Not Register", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void createUserProfile(FirebaseUser user) {
        UserProfileChangeRequest addUserName = new UserProfileChangeRequest.Builder()
                .setDisplayName(mName)
                .build();

        user.updateProfile(addUserName).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.d("Registration", "User added successfully");
            }
        });
    }

    private void dismissProgressDialog(){
        mProgressDialog.dismiss();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        dismissProgressDialog();
        if(mAuthStateListener != null)
        mAuth.removeAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        dismissProgressDialog();
        if(mAuthStateListener != null)
        mAuth.removeAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dismissProgressDialog();
        if(mAuthStateListener != null)
            mAuth.removeAuthStateListener(mAuthStateListener);
    }
}