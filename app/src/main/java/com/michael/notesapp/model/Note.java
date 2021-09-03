package com.michael.notesapp.model;

import org.w3c.dom.Node;

import java.util.Objects;

public class Note {
    private String mNoteBody;
    private String mNoteTitle;
    private String mImageUrl;

    public Note(){

    }


    public Note(String noteTitle, String noteBody, String imageUrl) {
        mNoteTitle = noteTitle;
        mNoteBody = noteBody;
        mImageUrl = imageUrl;

    }

    public String getmNoteBody() {
        return mNoteBody;
    }

    public void setmNoteBody(String mNoteBody) {
        this.mNoteBody = mNoteBody;
    }

    public String getmNoteTitle() {
        return mNoteTitle;
    }

    public void setmNoteTitle(String mNoteTitle) {
        this.mNoteTitle = mNoteTitle;
    }

    public String getmImageUrl() {
        return mImageUrl;
    }

    public void setmImageUrl(String mImageUrl) {
        this.mImageUrl = mImageUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Note note = (Note) o;
        return Objects.equals(mNoteBody, note.mNoteBody) &&
                Objects.equals(mNoteTitle, note.mNoteTitle) &&
                Objects.equals(mImageUrl, note.mImageUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mNoteBody, mNoteTitle, mImageUrl);
    }
}
