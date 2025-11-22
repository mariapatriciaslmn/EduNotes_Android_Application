package com.example.edunotesandroidapplication;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.edunotesandroidapplication.adapters.NoteAdapter;
import com.example.edunotesandroidapplication.models.Note;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    private ImageView profilePicture;
    private TextView usernameText, uploadStats, emptyUserNotesText;
    private RecyclerView userNotesRecyclerView;

    private NoteAdapter noteAdapter;
    private OnlineDBHandler dbHandler;
    private String userEmail;

    private ActivityResultLauncher<String> pickImageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Toolbar buttons
        ImageView backButton = findViewById(R.id.backButton);
        ImageView logoutButton = findViewById(R.id.logoutButton);
        backButton.setOnClickListener(v -> finish());
        logoutButton.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(ProfileActivity.this)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to log out?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        startActivity(new android.content.Intent(ProfileActivity.this, LoginActivity.class)
                                .setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK));
                        finish();
                    })
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                    .setCancelable(true)
                    .show();
        });

        // Views
        profilePicture = findViewById(R.id.profilePicture);
        usernameText = findViewById(R.id.usernameText);
        uploadStats = findViewById(R.id.uploadStats);
        emptyUserNotesText = findViewById(R.id.emptyUserNotesText);
        userNotesRecyclerView = findViewById(R.id.userNotesRecyclerView);

        // DB Handler & user email
        dbHandler = new OnlineDBHandler(this);
        userEmail = getIntent().getStringExtra("email");

        // Initialize RecyclerView & adapter
        noteAdapter = new NoteAdapter(this, new ArrayList<>(), userEmail);
        userNotesRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        userNotesRecyclerView.setAdapter(noteAdapter);

        // Edge-to-edge support
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Launcher for picking profile image
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) updateProfilePicture(uri);
                    else Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
                }
        );
        profilePicture.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        // Observe new notes via NoteEventBus
        NoteEventBus.getInstance().getNewNoteLiveData().observe(this, noteId -> {
            if (noteId != null) loadNewNote(noteId);
        });

        // Load profile and notes initially
        loadUserProfile();
        refreshUserNotes();
    }

    private void loadProfileImage(String uri, ImageView imageView) {
        if (uri == null || uri.isEmpty()) {
            imageView.setImageResource(R.drawable.ic_profile_placeholder);
        } else {
            // Use ImageView's context to avoid crashing if activity is destroyed
            Context context = imageView.getContext();
            if (context instanceof Activity) {
                Activity activity = (Activity) context;
                if (activity.isDestroyed() || activity.isFinishing()) {
                    // Don't attempt to load the image if activity is destroyed
                    return;
                }
            }

            Glide.with(context)
                    .load(uri)
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .error(R.drawable.ic_profile_placeholder)
                    .into(imageView);
        }
    }
    private void loadUserProfile() {
        if (userEmail == null) return;
        dbHandler.getUser(userEmail, response -> {
            try {
                JSONObject obj = new JSONObject(response);
                String name = obj.optString("name", "User");
                String uri = obj.optString("profile_picture_uri", "");

                usernameText.setText(name);
                loadProfileImage(uri, profilePicture);

            } catch (JSONException e) {
                e.printStackTrace();
                usernameText.setText("User");
                profilePicture.setImageResource(R.drawable.ic_profile_placeholder);
            }
        });
    }

    private void updateProfilePicture(Uri uri) {
        String imageUri = (uri != null) ? uri.toString() : "";
        loadProfileImage(imageUri, profilePicture);

        dbHandler.updateProfilePicture(userEmail, imageUri, response -> {
            try {
                JSONObject obj = new JSONObject(response);
                boolean success = obj.optBoolean("success", false);
                Toast.makeText(ProfileActivity.this,
                        success ? "Profile picture updated!" : "Failed to update profile picture.",
                        Toast.LENGTH_SHORT).show();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }

    private void refreshUserNotes() {
        if (userEmail == null) return;

        dbHandler.fetchNotesByUser(userEmail, "DESC", response -> {
            List<Note> userNotesList = new ArrayList<>();
            try {
                JSONArray arr = new JSONArray(response);
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.getJSONObject(i);

                    Note note = new Note(
                            obj.getInt("id"),
                            obj.getString("title"),
                            obj.getString("description"),
                            "",
                            obj.getString("uploader_email"),
                            obj.getString("date_created")
                    );

                    List<String> filesList = new ArrayList<>();
                    JSONArray filesArray = new JSONArray(obj.optString("files_json", "[]"));
                    for (int j = 0; j < filesArray.length(); j++) filesList.add(filesArray.getString(j));
                    note.setFiles(filesList);

                    if (!filesList.isEmpty()) note.setFirstImage(filesList.get(0));

                    note.setUploaderName(obj.optString("uploader_name", obj.optString("uploader_email")));
                    note.setUploaderProfileUri(obj.optString("uploader_profile_uri", ""));


                    userNotesList.add(note);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Failed to load notes", Toast.LENGTH_SHORT).show());
            }

            runOnUiThread(() -> {
                noteAdapter.updateData(userNotesList);
                uploadStats.setText("Uploads: " + userNotesList.size());
                emptyUserNotesText.setVisibility(userNotesList.isEmpty() ? View.VISIBLE : View.GONE);
            });
        });
    }

    private void loadNewNote(int noteId) {
        dbHandler.getNoteById(noteId, response -> {
            Note newNote = parseSingleNote(response);
            if (newNote != null && newNote.getUploaderEmail().equals(userEmail)) {
                runOnUiThread(() -> noteAdapter.prependNote(newNote));
            }
        });
    }

    private Note parseSingleNote(String response) {
        try {
            JSONObject obj = new JSONObject(response);
            Note note = new Note(
                    obj.getInt("id"),
                    obj.getString("title"),
                    obj.getString("description"),
                    "",
                    obj.getString("uploader_email"),
                    obj.getString("date_created")
            );

            List<String> filesList = new ArrayList<>();
            JSONArray filesArray = new JSONArray(obj.optString("files_json", "[]"));
            for (int j = 0; j < filesArray.length(); j++) filesList.add(filesArray.getString(j));
            note.setFiles(filesList);

            if (!filesList.isEmpty()) note.setFirstImage(filesList.get(0));

            note.setUploaderName(obj.optString("uploader_name", obj.optString("uploader_email")));
            note.setUploaderProfileUri(obj.optString("uploader_profile_uri", ""));

            return note;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}