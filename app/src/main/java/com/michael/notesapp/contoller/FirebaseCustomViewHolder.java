package com.michael.notesapp.contoller;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.michael.notesapp.R;
import com.michael.notesapp.constants.Constants;
import com.michael.notesapp.model.Note;
import com.michael.notesapp.ui.NoteEditActivity;
import com.squareup.picasso.Picasso;

import org.parceler.Parcels;

import java.util.ArrayList;

public class FirebaseCustomViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
    private final View mView;
    private final Context mContext;


    public FirebaseCustomViewHolder( @NonNull View itemView) {
        super(itemView);
        mView = itemView;
        mContext = itemView.getContext();
        itemView.setOnClickListener(this);
    }

    public void bindNoteItem(Note note) {
        TextView mNoteTitleTextView = mView.findViewById(R.id.noteTitleTextView);
        TextView mNoteBodyTextView = mView.findViewById(R.id.noteBodyTextView);
        ImageView mNoteImageView = mView.findViewById(R.id.noteImageView);

        String noteTitle = note.getmNoteTitle();
        String noteBody = note.getmNoteBody();
        String noteImageUrl = note.getmImageUrl();

        System.out.println("my image uri "+ noteImageUrl);

        mNoteTitleTextView.setText(noteTitle);
        mNoteBodyTextView.setText(noteBody);
        Picasso.get().load(noteImageUrl).into(mNoteImageView);

    }

    @Override
    public void onClick(View v) {
        final ArrayList<Note>  notes = new ArrayList<>();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;
        String uid = user.getUid();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child(uid).child(Constants.NOTES_REFERENCE);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                    notes.add(snapshot.getValue(Note.class));
                }

                int itemPosition = getLayoutPosition();
                dataSnapshot.getKey();





                String key = dataSnapshot.getChildren().iterator().next().getKey();

                Intent intent = new Intent(mContext, NoteEditActivity.class);
                intent.putExtra("position", itemPosition);
                intent.putExtra("key", key);
                intent.putExtra("notes", Parcels.wrap(notes));

                mContext.startActivity(intent);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }


}
