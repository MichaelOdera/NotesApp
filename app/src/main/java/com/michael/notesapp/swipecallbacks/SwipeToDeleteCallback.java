package com.michael.notesapp.swipecallbacks;

import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.michael.notesapp.contoller.FirebaseCustomViewHolder;
import com.michael.notesapp.model.Note;

public class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {

    private final FirebaseRecyclerAdapter<Note, FirebaseCustomViewHolder> mFirebaseAdapter;

    public SwipeToDeleteCallback(FirebaseRecyclerAdapter<Note, FirebaseCustomViewHolder> firebaseAdapter) {
        super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        mFirebaseAdapter = firebaseAdapter;
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        int position = viewHolder.getAdapterPosition();
        mFirebaseAdapter.getRef(position).removeValue();

    }
}
