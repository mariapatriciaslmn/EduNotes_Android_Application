package com.example.edunotesandroidapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

// -------------------------
// MODEL CLASS (Note)
// -------------------------
class Note {
    private int id;
    private String title;
    private String description;
    private String imageUrl;
    private String uploaderName;

    // Constructor with all fields (for database use)
    public Note(int id, String title, String description, String imageUrl) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.uploaderName = uploaderName;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getUploaderName() {
        return uploaderName;
    }
}

// -------------------------
// ADAPTER CLASS (NoteAdapter)
// -------------------------
class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    private Context context;
    private List<Note> noteList;

    public NoteAdapter(ProfileActivity profileActivity, List<Note> noteList) {
        this.context = context;
        this.noteList = noteList;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = noteList.get(position);
        holder.noteTitle.setText(note.getTitle());
        holder.noteDescription.setText(note.getDescription());
        holder.uploaderName.setText(note.getUploaderName());
        // For now, skip loading images
    }

    @Override
    public int getItemCount() {
        return noteList.size();
    }

    // ViewHolder class
    public static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView noteTitle, noteDescription, uploaderName;
        ImageView noteImage;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            noteTitle = itemView.findViewById(R.id.noteTitle);
            noteDescription = itemView.findViewById(R.id.noteDescription);
            uploaderName = itemView.findViewById(R.id.uploaderName);
            noteImage = itemView.findViewById(R.id.noteImage);
        }
    }

    // Update RecyclerView data
    public void updateData(List<Note> newNotes) {
        this.noteList = newNotes;
        notifyDataSetChanged();
    }
}
