package com.michael.notesapp.ui;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
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

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.michael.notesapp.R;
import com.michael.notesapp.constants.Constants;
import com.michael.notesapp.contoller.FirebaseCustomViewHolder;
import com.michael.notesapp.model.Note;
import com.squareup.picasso.Picasso;

import org.parceler.Parcels;

import java.io.IOException;
import java.util.ArrayList;

public class NoteEditActivity extends AppCompatActivity implements View.OnClickListener{
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseRecyclerAdapter<Note, FirebaseCustomViewHolder> mFirebaseAdapter;

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

    private Note mNote;

    private int mPosition;
    private String mKey;

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes_edit);



        mProgressDialog = new ProgressDialog(this);

        mStorageReference = FirebaseStorage.getInstance().getReference();

        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

        mAuth = FirebaseAuth.getInstance(FirebaseApp.initializeApp(this));
        mUserId = mAuth.getCurrentUser().getUid();


        mSaveNoteButton = findViewById(R.id.updateNoteButton);
        mSelectImageButton = findViewById(R.id.editImageButton);

        mNoteTitleEditText = findViewById(R.id.editNoteTitleEditText);
        mNoteBodyEditText = findViewById(R.id.editNoteBodyEditText);

        mImageView = findViewById(R.id.toBeEditedImageView);

        mSelectImageButton.setOnClickListener(this);
        mSaveNoteButton.setOnClickListener(this);

        fetchIntentData();

        setBackArrow();

        setAuthStateListener();


    }

    private void fetchIntentData() {
        ArrayList<Note> notes = new ArrayList<>();

        notes = Parcels.unwrap(getIntent().getParcelableExtra("notes"));

        int position = getIntent().getIntExtra("position", 0);
        mPosition = position;

        mNote = notes.get(position);
        mKey = getIntent().getStringExtra("key");


        mNoteTitleEditText.setText(mNote.getmNoteTitle());
        mNoteBodyEditText.setText(mNote.getmNoteBody());

        Picasso.get().load(mNote.getmImageUrl()).into(mImageView);


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

                    //Toast.makeText(CreateNoteActivity.this, "User "+mUserId, Toast.LENGTH_SHORT).show();
                }else{
                    //Toast.makeText(CreateNoteActivity.this, "User  "+mUserId, Toast.LENGTH_SHORT).show();
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

    private void fetchEnteredData() {
        mNoteTitle = mNoteTitleEditText.getText().toString();
        mNoteBody = mNoteBodyEditText.getText().toString();



        boolean isValidData = checkIfValidData();

        if(isValidData){
            mProgressDialog.setTitle("Updating note item");
            mProgressDialog.setMessage("Updating ...");
            mProgressDialog.show();

            uploadImageData();
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
                            Toast.makeText(NoteEditActivity.this, "Successfully Saved", Toast.LENGTH_SHORT).show();

                            DatabaseReference databaseReference = mDatabaseReference.child(mUserId).child(Constants.NOTES_REFERENCE);

                            taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    imageUrl = uri.toString();
                                    Note noteToUpdate = new Note(mNoteTitle, mNoteBody, imageUrl);


                                    databaseReference.child(mKey).setValue(noteToUpdate);


                                }
                            });

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            dismissProgressDialog();
                            Toast.makeText(NoteEditActivity.this, "Could Not Update Note Item", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                            mProgressDialog.show();
                        }
                    });
        }else{

            Toast.makeText(NoteEditActivity.this, "Select Image", Toast.LENGTH_SHORT).show();
        }

    }


    private void fetchImageData() {
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
                        Toast.makeText(NoteEditActivity.this, "Error in choosing file", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }

                }
            });

    public void dismissProgressDialog(){
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
        Intent intent = new Intent(NoteEditActivity.this, MainActivity.class);
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