package com.michael.notesapp.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.LayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.michael.notesapp.R;
import com.michael.notesapp.constants.Constants;
import com.michael.notesapp.contoller.FirebaseCustomViewHolder;
import com.michael.notesapp.model.Note;
import com.michael.notesapp.swipecallbacks.SwipeToDeleteCallback;
import com.michael.notesapp.ui.authentication.LoginActivity;

public class NotesActivity extends AppCompatActivity {

    private DatabaseReference mNotesReference;
    private FirebaseRecyclerAdapter<Note, FirebaseCustomViewHolder> mFirebaseAdapter;

    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthStateListener;

    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);



        mAuth = FirebaseAuth.getInstance(FirebaseApp.initializeApp(this));
        String userId = mAuth.getCurrentUser().getUid();

        mNotesReference = FirebaseDatabase.getInstance().getReference().child(userId).child(Constants.NOTES_REFERENCE);
        mRecyclerView = findViewById(R.id.recyclerView);


        setUpAuthStateListener();

        fetchNoteItems();
    }



    private void setUpAuthStateListener() {
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                getSupportActionBar().setTitle(user.getDisplayName());

            }
        };
    }

    private void fetchNoteItems() {
        System.out.println("Fetching items ");
        FirebaseRecyclerOptions<Note> options = new FirebaseRecyclerOptions.Builder<Note>()
                .setQuery(mNotesReference, Note.class).build();

        mFirebaseAdapter = new FirebaseRecyclerAdapter<Note, FirebaseCustomViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FirebaseCustomViewHolder firebaseCustomViewHolder, int i, @NonNull Note note) {
                firebaseCustomViewHolder.bindNoteItem(note);
            }

            @NonNull
            @Override
            public FirebaseCustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.note_item, parent, false);
                return new FirebaseCustomViewHolder(view);
            }
        };

        setUpFirebaseAdapter();
    }

    private void setUpFirebaseAdapter() {

        LayoutManager layoutManager = new LinearLayoutManager(this);

        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mFirebaseAdapter);


        ItemTouchHelper itemTouchHelper = new
                ItemTouchHelper(new SwipeToDeleteCallback(mFirebaseAdapter));
        itemTouchHelper.attachToRecyclerView(mRecyclerView);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            logout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(NotesActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();

    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthStateListener);
        mFirebaseAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthStateListener != null) {
            mAuth.removeAuthStateListener(mAuthStateListener);
        }

        if(mFirebaseAdapter != null) {
            mFirebaseAdapter.stopListening();
        }
    }


}