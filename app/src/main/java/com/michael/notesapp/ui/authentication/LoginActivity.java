package com.michael.notesapp.ui.authentication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.michael.notesapp.R;
import com.michael.notesapp.ui.MainActivity;
import com.michael.notesapp.ui.NotesActivity;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    private EditText mEmailEditText;
    private EditText mPasswordEditText;

    private String mEmail;
    private String mPassword;


    private Button mLoginButton;
    private TextView mReDirectToRegister;

    private ProgressDialog mProgressDialog;

    @SuppressLint("CutPasteId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        FirebaseApp.initializeApp(this);

        createAuthStateListener();

        mProgressDialog = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance(FirebaseApp.initializeApp(this));

        mEmailEditText = findViewById(R.id.signInEmailEditText);
        mPasswordEditText = findViewById(R.id.signInEmailEditText);

        mLoginButton = findViewById(R.id.signInButton);
        mReDirectToRegister = findViewById(R.id.registerTextView);

        mLoginButton.setOnClickListener(this);
        mReDirectToRegister.setOnClickListener(this);


    }

    private void createAuthStateListener() {
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if(user != null){
                    Intent loginIntent = new Intent(LoginActivity.this, MainActivity.class);
                    loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK );
                    LoginActivity.this.startActivity(loginIntent);
                    LoginActivity.this.finish();


                }
            }
        };
    }


    @Override
    public void onClick(View v) {
        if(v == mLoginButton){
            fetchEnteredData();
            boolean validData = validateData();

            if(validData){
                mProgressDialog.setTitle("Logging ...");
                mProgressDialog.show();
                proceedToLogin();
            }
        }

        if(v == mReDirectToRegister){
            redirectToRegistration();
        }

    }




    private void fetchEnteredData() {
        mEmail = mEmailEditText.getText().toString();
        mPassword = mPasswordEditText.getText().toString();
    }


    private boolean validateData() {
        boolean result = false;
        boolean validEmail = isValidEmail(mEmail);
        boolean validPassword = isValidPassword(mPassword);

        if(validEmail && validPassword){
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

    private boolean isValidPassword(String password) {
        if (password.length() < 6) {
            mPasswordEditText.setError("Please create a password containing at least 6 characters");
            return false;
        }
        return true;
    }

    private void proceedToLogin() {
        mAuth.signInWithEmailAndPassword(mEmail, mPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    dismissProgressDialog();
                    Toast.makeText(LoginActivity.this, "Successful Login", Toast.LENGTH_SHORT).show();
                    FirebaseUser user = task.getResult().getUser();
                    Intent loginIntent = new Intent(LoginActivity.this, MainActivity.class);
                    String userName = user.getDisplayName();

                    loginIntent.putExtra("displayName", userName);
                    loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    LoginActivity.this.startActivity(loginIntent);
                    LoginActivity.this.finish();
                }

                if(!task.isSuccessful()){
                    dismissProgressDialog();
                    Toast.makeText(getApplicationContext(), "An Error Occurred",Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void redirectToRegistration() {
        Intent registrationIntent = new Intent(LoginActivity.this, RegistrationActivity.class);
        registrationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        LoginActivity.this.startActivity(registrationIntent);
        LoginActivity.this.finish();
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