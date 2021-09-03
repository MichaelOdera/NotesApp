package com.michael.notesapp.ui;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuth.AuthStateListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.michael.notesapp.R;
import com.michael.notesapp.constants.Constants;
import com.michael.notesapp.model.Note;

import java.io.IOException;

public class CreateNoteActivity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    private Button mSaveNoteButton;
    private Button mSelectImageButton;

    private EditText mNoteTitleEditText;
    private EditText mNoteBodyEditText;

    private ImageView mImageView;

    private Uri mFilePath;
    private DatabaseReference mDatabaseReference;
    private StorageReference mStorageReference;

    private String mNoteTitle;
    private String mNoteBody;

    private String mUserId;

    private ProgressDialog mProgressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);

        mProgressDialog = new ProgressDialog(this);

        mStorageReference = FirebaseStorage.getInstance().getReference();

        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

        mAuth = FirebaseAuth.getInstance(FirebaseApp.initializeApp(this));
        mUserId = mAuth.getCurrentUser().getUid();


        mSaveNoteButton = findViewById(R.id.saveNoteButton);
        mSelectImageButton = findViewById(R.id.uploadImageButton);

        mNoteTitleEditText = findViewById(R.id.noteTitleDataEditText);
        mNoteBodyEditText = findViewById(R.id.noteBodyDataTextView);

        mImageView = findViewById(R.id.toBeUploadedImageView);

        mSelectImageButton.setOnClickListener(this);
        mSaveNoteButton.setOnClickListener(this);

        setBackArrow();

        setAuthStateListener();


    }

    private void setBackArrow() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeAsUpIndicator(R.drawable.back_arrow);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    private void setAuthStateListener() {
        mAuthStateListener = new FirebaseAuth.AuthStateListener(){
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null){
                    String userId = user.getUid();
                    mUserId = userId.trim();
                    Toast.makeText(CreateNoteActivity.this, "User "+mUserId, Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(CreateNoteActivity.this, "User >>> "+mUserId, Toast.LENGTH_SHORT).show();
                }

            }
        };
    }

    @Override
    public void onClick(View v) {
        if(v == mSelectImageButton){
            fetchImageData();

        }

        if(v == mSaveNoteButton){
            fetchEnteredData();
        }

    }

    private void fetchImageData() {
        // Setting intent type as image to select image from phone storage.
        mGetContent.launch("image/*");


    }

    ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onActivityResult(Uri uri) {
                    // Handle the returned Uri
                    mFilePath = uri;
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), mFilePath);
                        mImageView.setImageBitmap(bitmap);

                        mSelectImageButton.setText("Image Selected");
                    } catch (IOException e) {
                        Toast.makeText(CreateNoteActivity.this, "Error in choosing file", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }

                }
            });

    private void fetchEnteredData() {
        mNoteTitle = mNoteTitleEditText.getText().toString();
        mNoteBody = mNoteBodyEditText.getText().toString();



        boolean isValidData = checkIfValidData();

        if(isValidData){
            mProgressDialog.setTitle("Saving note item");
            mProgressDialog.setMessage("Saving ...");
            mProgressDialog.show();

            uploadImageData();
        }
    }

    public String GetImageExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();

        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri)) ;

    }

    private void uploadImageData() {

        if(mFilePath != null){

            StorageReference storageReference = mStorageReference.child(mUserId)
                    .child(Constants.STORAGE_REFERENCE + System.currentTimeMillis()+ GetImageExtension(mFilePath));



            storageReference.putFile(mFilePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {

                        String imageUrl;
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            dismissProgressDialog();
                            Toast.makeText(CreateNoteActivity.this, "Successfully Saved", Toast.LENGTH_SHORT).show();

                            DatabaseReference databaseReference = mDatabaseReference.child(mUserId).child(Constants.NOTES_REFERENCE);

                            taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                  //  System.out.println("My uri path +++++++++++++++ >>>>>>> ++++++"+uri.toString());
                                    imageUrl = uri.toString();
                                    Note noteToSave = new Note(mNoteTitle, mNoteBody, imageUrl);

                                    databaseReference.push().setValue(noteToSave);
                                }
                            });
                           // String imageUrl = taskSnapshot.getStorage().getDownloadUrl().toString();






                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            dismissProgressDialog();
                            Toast.makeText(CreateNoteActivity.this, "Could Not Save Note Item", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                                mProgressDialog.show();
                        }
                    });
        }else{
            Toast.makeText(CreateNoteActivity.this, "Select Image", Toast.LENGTH_SHORT).show();
        }


    }


    private boolean checkIfValidData() {
        boolean result;
        boolean validTitle =  isValidTitle(mNoteTitle);
        boolean validBody =  isValidBody(mNoteBody);

        if(validTitle && validBody){
            result = true;
        }else{
            result = false;
        }

        return result;
    }

    private boolean isValidTitle(String mNoteTitle) {
        boolean result;
        if(mNoteTitle.equals("")){
            mNoteTitleEditText.setError("Title Required");
            result = false;
        }else{
            result = true;
        }

        return result;

    }

    private boolean isValidBody(String mNoteBody) {
        boolean result;
        if(mNoteBody.equals("")){
            mNoteBodyEditText.setError("Note Body Required");
            result = false;
        }else{
            result = true;
        }

        return result;
    }

    private void dismissProgressDialog() {
        mProgressDialog.dismiss();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            gotToMain();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void gotToMain() {
        Intent intent = new Intent(CreateNoteActivity.this, MainActivity.class);
        startActivity(intent);
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