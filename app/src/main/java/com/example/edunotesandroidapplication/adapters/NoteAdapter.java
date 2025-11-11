package com.example.edunotesandroidapplication.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.edunotesandroidapplication.NoteDetailsActivity;
import com.example.edunotesandroidapplication.OnlineDBHandler;
import com.example.edunotesandroidapplication.R;
import com.example.edunotesandroidapplication.models.Note;

import org.json.JSONException;
import org.json.JSONObject;

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

        holder.noteTitle.setText(note.getTitle());
        holder.noteDescription.setText(note.getDescription());
        holder.uploaderName.setText(note.getUploaderEmail());
        holder.noteDate.setText(note.getDateCreated());

        // Load image with Glide
        if (note.getImageUrl() != null && !note.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(note.getImageUrl())
                    .placeholder(R.drawable.ic_note_placeholder)
                    .into(holder.noteImage);
        } else {
            holder.noteImage.setImageResource(R.drawable.ic_note_placeholder);
        }

        // Check if saved
        dbHandler.fetchSavedNotes(userEmail, response -> {
            try {
                JSONObject obj = new JSONObject(response);
                boolean isSaved = obj.optBoolean("saved_" + note.getId(), false);
                holder.saveButton.setImageResource(isSaved ? R.drawable.saved_logo : R.drawable.unsaved_logo);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });

        // Handle save/unsave clicks
        holder.saveButtonLayout.setOnClickListener(v -> {
            dbHandler.fetchSavedNotes(userEmail, response -> {
                try {
                    JSONObject obj = new JSONObject(response);
                    boolean isSaved = obj.optBoolean("saved_" + note.getId(), false);

                    if (isSaved) {
                        dbHandler.removeSavedNote(note.getId(), userEmail, r -> {
                            holder.saveButton.setImageResource(R.drawable.unsaved_logo);
                            Toast.makeText(context, "Removed from saved", Toast.LENGTH_SHORT).show();
                        });
                    } else {
                        dbHandler.saveNote(note.getId(), userEmail, r -> {
                            holder.saveButton.setImageResource(R.drawable.saved_logo);
                            Toast.makeText(context, "Saved successfully", Toast.LENGTH_SHORT).show();
                        });
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });
        });

        // Open Note Details
        holder.commentButtonLayout.setOnClickListener(v -> {
            Intent intent = new Intent(context, NoteDetailsActivity.class);
            intent.putExtra("noteId", note.getId());
            intent.putExtra("userEmail", userEmail);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return noteList.size();
    }

    public void updateData(List<Note> newList) {
        this.noteList = newList;
        notifyDataSetChanged();
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView noteTitle, noteDescription, uploaderName, noteDate;
        ImageView noteImage, saveButton, commentButton;
        LinearLayout saveButtonLayout, commentButtonLayout;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            noteTitle = itemView.findViewById(R.id.noteTitle);
            noteDescription = itemView.findViewById(R.id.noteDescription);
            uploaderName = itemView.findViewById(R.id.uploaderName);
            noteDate = itemView.findViewById(R.id.noteDate);
            noteImage = itemView.findViewById(R.id.noteImage);
            saveButton = itemView.findViewById(R.id.saveButton);
            commentButton = itemView.findViewById(R.id.commentButton);
            saveButtonLayout = itemView.findViewById(R.id.saveButtonLayout);
            commentButtonLayout = itemView.findViewById(R.id.commentButtonLayout);
        }
    }
}
