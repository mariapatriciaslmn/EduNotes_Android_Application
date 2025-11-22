package com.example.edunotesandroidapplication;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.edunotesandroidapplication.adapters.CommentAdapter;
import com.example.edunotesandroidapplication.adapters.FilesAdapter;
import com.example.edunotesandroidapplication.models.Comment;
import com.example.edunotesandroidapplication.models.Files;
import com.example.edunotesandroidapplication.models.Note;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class NoteDetailsActivity extends AppCompatActivity {

    private TextView noteTitle, noteDescription, uploaderName, noteDate;
    private ImageView uploaderProfileImage, saveButton, commentButton;
    private RecyclerView filesRecyclerView, commentsRecyclerView;
    private EditText commentEditText;
    private Button postCommentButton;
    private TextView emptyCommentsText;

    private OnlineDBHandler dbHandler;
    private int noteId;
    private String userEmail;
    private boolean isSaved = false;

    private CommentAdapter commentAdapter;
    private List<Comment> commentList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_details);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.noteDetailsToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Note Details");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // Views
        noteTitle = findViewById(R.id.noteTitle);
        noteDescription = findViewById(R.id.noteDescription);
        uploaderName = findViewById(R.id.uploaderName);
        uploaderProfileImage = findViewById(R.id.uploaderProfileImage);
        noteDate = findViewById(R.id.noteDate);

        filesRecyclerView = findViewById(R.id.mixedRecyclerView);
        filesRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        saveButton = findViewById(R.id.saveButton);
        commentButton = findViewById(R.id.commentButton);

        commentsRecyclerView = findViewById(R.id.commentsRecyclerView);
        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        commentAdapter = new CommentAdapter(this, commentList);
        commentsRecyclerView.setAdapter(commentAdapter);

        commentEditText = findViewById(R.id.commentEditText);
        postCommentButton = findViewById(R.id.postCommentButton);
        emptyCommentsText = findViewById(R.id.emptyCommentsText);

        dbHandler = new OnlineDBHandler(this);

        // Get noteId and userEmail from intent
        noteId = getIntent().getIntExtra("note_id", -1);
        userEmail = getIntent().getStringExtra("user_email"); // <-- key matches NoteAdapter

        if (noteId == -1 || userEmail == null || userEmail.isEmpty()) {
            Toast.makeText(this, "Invalid Note or User", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadNoteDetails();
        loadComments();

        saveButton.setOnClickListener(v -> toggleSaveState());

        commentButton.setOnClickListener(v -> {
            commentEditText.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.showSoftInput(commentEditText, InputMethodManager.SHOW_IMPLICIT);
        });

        postCommentButton.setOnClickListener(v -> postComment());
    }

    // ==================== LOAD NOTE DETAILS ============================
    private void loadNoteDetails() {
        dbHandler.getNoteById(noteId, response -> {
            try {
                JSONObject obj = new JSONObject(response);

                if (!obj.optBoolean("success", true)) {
                    runOnUiThread(() -> Toast.makeText(this, "Failed to load note", Toast.LENGTH_SHORT).show());
                    return;
                }

                Note note = new Note(
                        obj.getInt("note_id"),
                        obj.getString("title"),
                        obj.getString("description"),
                        "",
                        obj.getString("uploader_email"),
                        obj.getString("date_created")
                );

                note.setUploaderName(obj.optString("uploader_name", note.getUploaderEmail()));
                note.setUploaderProfileUri(obj.optString("uploader_profile_uri", ""));

                List<String> filesList = new ArrayList<>();
                JSONArray arr = new JSONArray(obj.optString("files_json", "[]"));
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject fObj = arr.getJSONObject(i);
                    String fullPath = OnlineDBHandler.getBaseUrl() + fObj.getString("path");
                    filesList.add(fullPath);
                }
                note.setFiles(filesList);

                checkSavedState(note);
                runOnUiThread(() -> displayNoteDetails(note));

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void checkSavedState(Note note) {
        dbHandler.isNoteSaved(note.getId(), userEmail, response -> {
            isSaved = Boolean.parseBoolean(response);
            runOnUiThread(this::updateSaveIcon);
        });
    }

    private void displayNoteDetails(Note note) {
        noteTitle.setText(note.getTitle());
        noteDescription.setText(note.getDescription());
        noteDate.setText(note.getDateCreated());

        uploaderName.setText(note.getUploaderName());
        Glide.with(this)
                .load(note.getUploaderProfileUri())
                .placeholder(R.drawable.profile_logo)
                .circleCrop()
                .into(uploaderProfileImage);

        // FILES
        List<Files> filesObjects = new ArrayList<>();
        if (note.getFiles() != null) {
            for (String url : note.getFiles()) {
                String fileName = url.substring(url.lastIndexOf("/") + 1);
                String ext = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf(".") + 1) : "";
                filesObjects.add(new Files(Uri.parse(url), fileName, ext));
            }
        }

        if (!filesObjects.isEmpty()) {
            FilesAdapter filesAdapter = new FilesAdapter(this, filesObjects);
            filesRecyclerView.setAdapter(filesAdapter);
            filesRecyclerView.setVisibility(View.VISIBLE);
        } else {
            filesRecyclerView.setVisibility(View.GONE);
        }
    }

    // ===================== SAVE NOTE ========================
    private void toggleSaveState() {
        if (isSaved) {
            dbHandler.removeSavedNote(noteId, userEmail, r -> {
                isSaved = false;
                runOnUiThread(() -> {
                    updateSaveIcon();
                    Toast.makeText(this, "Removed from saved notes", Toast.LENGTH_SHORT).show();
                });
            });
        } else {
            dbHandler.saveNote(noteId, userEmail, response -> {
                isSaved = true;
                runOnUiThread(() -> {
                    updateSaveIcon();
                    Toast.makeText(this, "Note saved!", Toast.LENGTH_SHORT).show();
                });
            });
        }
    }

    private void updateSaveIcon() {
        saveButton.setImageResource(isSaved ? R.drawable.saved_logo : R.drawable.unsaved_logo);
    }

    // ======================== COMMENTS ========================
    private void loadComments() {
        dbHandler.getCommentsByNoteId(noteId, response -> {
            try {
                JSONArray arr = new JSONArray(response);
                commentList.clear();
                for (int i = 0; i < arr.length(); i++) {
                    Comment c = Comment.fromJson(arr.getJSONObject(i));
                    commentList.add(c);
                }
                runOnUiThread(() -> {
                    commentAdapter.updateComments(commentList);
                    emptyCommentsText.setVisibility(commentList.isEmpty() ? View.VISIBLE : View.GONE);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void postComment() {
        String content = commentEditText.getText().toString().trim();
        if (content.isEmpty()) {
            Toast.makeText(this, "Comment cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        dbHandler.addComment(noteId, userEmail, content, response -> runOnUiThread(() -> {
            commentEditText.setText("");
            loadComments();
            Toast.makeText(this, "Comment posted", Toast.LENGTH_SHORT).show();
        }));
    }
}