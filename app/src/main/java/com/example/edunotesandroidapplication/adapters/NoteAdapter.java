package com.example.edunotesandroidapplication.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.edunotesandroidapplication.NoteDetailsActivity;
import com.example.edunotesandroidapplication.R;
import com.example.edunotesandroidapplication.OnlineDBHandler;
import com.example.edunotesandroidapplication.models.Files;
import com.example.edunotesandroidapplication.models.Note;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    private final Context context;
    private List<Note> noteList;
    private final String userEmail;
    private final OnlineDBHandler dbHandler;

    public NoteAdapter(Context context, List<Note> noteList, String userEmail) {
        this.context = context;
        this.noteList = noteList;
        this.userEmail = userEmail;
        this.dbHandler = new OnlineDBHandler(context);
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

        // Set note info
        holder.noteTitle.setText(note.getTitle());
        holder.noteDate.setText(note.getDateCreated());

        // Load uploader name
        holder.uploaderName.setText(note.getUploaderName() != null ? note.getUploaderName() : note.getUploaderEmail());

        // Load uploader profile picture with default fallback
        loadProfileImage(note.getUploaderProfileUri(), holder.uploaderProfileImage);

        // ---------- Files Preview ----------
        List<Files> filesObjects = new ArrayList<>();
        if (note.getFiles() != null) {
            for (String url : note.getFiles()) {
                String fileName = url.substring(url.lastIndexOf('/') + 1);
                String ext = "";
                int dot = fileName.lastIndexOf('.');
                if (dot != -1) ext = fileName.substring(dot + 1);
                filesObjects.add(new Files(Uri.parse(url), fileName, ext));
            }
        }

        List<Files> previewFiles = filesObjects.size() > 3 ? filesObjects.subList(0, 3) : filesObjects;

        if (!previewFiles.isEmpty()) {
            FilesAdapter filesAdapter = new FilesAdapter(context, previewFiles);
            holder.filesRecyclerView.setAdapter(filesAdapter);
            holder.filesRecyclerView.setVisibility(View.VISIBLE);
        } else {
            holder.filesRecyclerView.setVisibility(View.GONE);
        }

        // ---------- Save/Unsave ----------
        updateSaveIcon(holder.buttonSave, note);
        holder.buttonSave.setEnabled(true);

        holder.buttonSave.setOnClickListener(v -> {
            holder.buttonSave.setEnabled(false);

            if (note.isSaved()) {
                // Remove saved note
                dbHandler.removeSavedNote(note.getId(), userEmail, response -> {
                    holder.buttonSave.setEnabled(true);
                    try {
                        JSONObject obj = new JSONObject(response);
                        boolean success = obj.optBoolean("success", false);
                        if (success) {
                            note.setSaved(false);
                            updateSaveIcon(holder.buttonSave, note);
                            Toast.makeText(context, "Removed from saved notes", Toast.LENGTH_SHORT).show();
                        } else {
                            String error = obj.optString("error", "Failed to remove saved note");
                            Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(context, "Invalid server response", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                // Save note
                dbHandler.saveNote(note.getId(), userEmail, response -> {
                    holder.buttonSave.setEnabled(true);
                    try {
                        JSONObject obj = new JSONObject(response);
                        boolean success = obj.optBoolean("success", false);
                        if (success) {
                            note.setSaved(true);
                            updateSaveIcon(holder.buttonSave, note);
                            Toast.makeText(context, "Note saved", Toast.LENGTH_SHORT).show();
                        } else {
                            String error = obj.optString("error", "Failed to save note");
                            Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(context, "Invalid server response", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        // ---------- Comment Button ----------
        holder.buttonComment.setOnClickListener(v -> {
            Intent intent = new Intent(context, NoteDetailsActivity.class);
            intent.putExtra("noteId", note.getId());
            intent.putExtra("userEmail", userEmail);
            context.startActivity(intent);
        });

        // ---------- Entire note click also opens details ----------
        holder.itemView.setOnClickListener(v -> holder.buttonComment.performClick());
    }

    // Helper method to update save icon
    private void updateSaveIcon(ImageView buttonSave, Note note) {
        buttonSave.setImageResource(note.isSaved() ? R.drawable.saved_logo : R.drawable.unsaved_logo);
    }

    // Helper method to load profile image with default
    private void loadProfileImage(String uri, ImageView imageView) {
        if (uri == null || uri.isEmpty()) {
            imageView.setImageResource(R.drawable.ic_profile_placeholder);
        } else {
            Glide.with(context)
                    .load(uri)
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .error(R.drawable.ic_profile_placeholder)
                    .into(imageView);
        }
    }

    @Override
    public int getItemCount() {
        return noteList.size();
    }

    public void updateData(List<Note> newList) {
        this.noteList = newList;
        notifyDataSetChanged();
    }

    public void prependNote(Note newNote) {
        dbHandler.isNoteSaved(newNote.getId(), userEmail, response -> {
            newNote.setSaved(Boolean.parseBoolean(response));
            noteList.add(0, newNote);
            notifyItemInserted(0);
        });
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView noteTitle, uploaderName, noteDate;
        RecyclerView filesRecyclerView;
        ImageView buttonSave, buttonComment, uploaderProfileImage;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            noteTitle = itemView.findViewById(R.id.noteTitle);
            uploaderName = itemView.findViewById(R.id.uploaderName);
            noteDate = itemView.findViewById(R.id.noteDate);
            filesRecyclerView = itemView.findViewById(R.id.previewAttachmentsRecyclerView);
            filesRecyclerView.setLayoutManager(new GridLayoutManager(itemView.getContext(), 3));
            buttonSave = itemView.findViewById(R.id.saveButton);
            buttonComment = itemView.findViewById(R.id.commentButton);
            uploaderProfileImage = itemView.findViewById(R.id.uploaderProfileImage); // new
        }
    }
}