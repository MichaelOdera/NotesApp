package com.michael.notesapp.ui;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);

        mStorageReference = FirebaseStorage.getInstance().getReference();

        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

        mAuth = FirebaseAuth.getInstance(FirebaseApp.initializeApp(this));

        Toast.makeText(CreateNoteActivity.this, "Create note", Toast.LENGTH_SHORT).show();


        mSaveNoteButton = findViewById(R.id.saveNoteButton);
        mSelectImageButton = findViewById(R.id.uploadImageButton);

        mNoteTitleEditText = findViewById(R.id.noteTitleDataEditText);
        mNoteBodyEditText = findViewById(R.id.noteBodyDataTextView);

        mImageView = findViewById(R.id.toBeUploadedImageView);

        mSelectImageButton.setOnClickListener(this);
        mSaveNoteButton.setOnClickListener(this);

        setAuthStateListener();


    }

    private void setAuthStateListener() {
        mAuthStateListener = new FirebaseAuth.AuthStateListener(){
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if(user != null){
                    String userId = user.getUid();
                    mUserId = userId.trim();
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
                        e.printStackTrace();
                    }

                }
            });

    private void fetchEnteredData() {
        mNoteTitle = mNoteTitleEditText.getText().toString();
        mNoteBody = mNoteBodyEditText.getText().toString();



        boolean isValidData = checkIfValidData();

        if(isValidData){
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

            StorageReference storageReference = mStorageReference
                    .child(Constants.STORAGE_REFERENCE + System.currentTimeMillis()+ GetImageExtension(mFilePath));

            storageReference.putFile(mFilePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            DatabaseReference databaseReference = mDatabaseReference.child(mUserId).child(Constants.NOTES_REFERENCE);
                            String imageUrl = taskSnapshot.getStorage().getDownloadUrl().toString();

                            Note noteToSave = new Note(mNoteTitle, mNoteBody, imageUrl);

                            databaseReference.push().setValue(noteToSave);


                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(CreateNoteActivity.this, "Could Not Save Note Item", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {

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
            mNoteBodyEditText.setError("Note Body Required");
            result = true;
        }

        return result;

    }

    private boolean isValidBody(String mNoteBody) {
        boolean result;
        if(mNoteBody.equals("")){
            result = false;
        }else{
            result = true;
        }

        return result;
    }


}