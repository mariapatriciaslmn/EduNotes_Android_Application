package com.example.edunotesandroidapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.edunotesandroidapplication.adapters.CommentAdapter;
import com.example.edunotesandroidapplication.models.Comment;
import com.example.edunotesandroidapplication.models.Note;

import java.util.List;

public class NoteDetailsActivity extends AppCompatActivity {

    private TextView titleTextView, descriptionTextView, uploaderTextView, dateTextView;
    private ImageView noteImageView, saveButton, commentButton;
    private RecyclerView commentsRecyclerView;
    private EditText commentEditText;
    private Button postCommentButton;
    private DBHandler dbHandler;
    private CommentAdapter commentAdapter;
    private int noteId;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_details);

        dbHandler = new DBHandler(this);

        // --- Initialize UI ---
        titleTextView = findViewById(R.id.noteTitle);
        descriptionTextView = findViewById(R.id.noteDescription);
        uploaderTextView = findViewById(R.id.uploaderName);
        dateTextView = findViewById(R.id.noteDate);
        noteImageView = findViewById(R.id.noteImage);
        saveButton = findViewById(R.id.saveButton);
        commentButton = findViewById(R.id.commentButton);
        commentsRecyclerView = findViewById(R.id.commentsRecyclerView);
        commentEditText = findViewById(R.id.commentEditText);
        postCommentButton = findViewById(R.id.postCommentButton);
        Toolbar toolbar = findViewById(R.id.noteDetailsToolbar);

        // --- Toolbar setup ---
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Note Details");
        }
        toolbar.setNavigationIcon(R.drawable.baseline_arrow_back_24);
        toolbar.setNavigationOnClickListener(v -> finish());

        // --- Receive Intent data ---
        Intent intent = getIntent();
        if (intent != null) {
            noteId = intent.getIntExtra("noteId", -1);
            userEmail = intent.getStringExtra("userEmail");

            Note note = dbHandler.getNoteById(noteId);
            if (note != null) {
                titleTextView.setText(note.getTitle());
                descriptionTextView.setText(note.getDescription());
                uploaderTextView.setText(dbHandler.getUserNameByEmail(note.getUploaderEmail()));
                dateTextView.setText(formatDate(note.getDateCreated()));

                if (note.getImageUrl() != null && !note.getImageUrl().isEmpty()) {
                    // Load image with Glide/Picasso if URL, else placeholder
                    noteImageView.setImageResource(R.drawable.ic_note_placeholder);
                } else {
                    noteImageView.setImageResource(R.drawable.ic_note_placeholder);
                }
            }
        }

        // --- Comments RecyclerView setup ---
        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        loadComments();

        // --- Post Comment ---
        postCommentButton.setOnClickListener(v -> {
            String commentText = commentEditText.getText().toString().trim();
            if (!commentText.isEmpty()) {
                dbHandler.addComment(noteId, dbHandler.getUserNameByEmail(userEmail), commentText);
                commentEditText.setText("");
                loadComments();
                Toast.makeText(this, "Comment added", Toast.LENGTH_SHORT).show();
            }
        });

        // --- Save/Unsave Note ---
        updateSaveButton();
        saveButton.setOnClickListener(v -> toggleSaveNote());
        commentButton.setOnClickListener(v -> commentsRecyclerView.smoothScrollToPosition(commentAdapter.getItemCount() - 1));
    }

    private void loadComments() {
        List<Comment> commentList = dbHandler.getCommentsForNote(noteId);
        if (commentAdapter == null) {
            commentAdapter = new CommentAdapter(this, commentList);
            commentsRecyclerView.setAdapter(commentAdapter);
        } else {
            commentAdapter.updateComments(commentList);
        }

        findViewById(R.id.emptyCommentsText).setVisibility(commentList.isEmpty() ? android.view.View.VISIBLE : android.view.View.GONE);
    }

    private void updateSaveButton() {
        boolean isSaved = dbHandler.isNoteSaved(noteId, userEmail);
        saveButton.setImageResource(isSaved ? R.drawable.saved_logo : R.drawable.unsaved_logo);
    }

    private void toggleSaveNote() {
        boolean currentlySaved = dbHandler.isNoteSaved(noteId, userEmail);
        if (currentlySaved) {
            dbHandler.removeSavedNoteForUser(noteId, userEmail);
            Toast.makeText(this, "Removed from saved notes", Toast.LENGTH_SHORT).show();
        } else {
            dbHandler.saveNoteForUser(noteId, userEmail);
            Toast.makeText(this, "Saved successfully", Toast.LENGTH_SHORT).show();
        }
        updateSaveButton();
    }

    private String formatDate(String rawDate) {
        // Optional: format "yyyy-MM-dd HH:mm:ss" to "Nov 10, 2025"
        try {
            java.text.SimpleDateFormat inputFormat = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault());
            java.util.Date date = inputFormat.parse(rawDate);
            java.text.SimpleDateFormat outputFormat = new java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault());
            return outputFormat.format(date);
        } catch (Exception e) {
            return rawDate;
        }
    }
}
