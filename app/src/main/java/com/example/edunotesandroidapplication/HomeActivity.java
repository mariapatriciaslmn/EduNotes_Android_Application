package com.example.edunotesandroidapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.edunotesandroidapplication.adapters.NoteAdapter;
import com.example.edunotesandroidapplication.models.Note;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HomeActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private RecyclerView recyclerView;
    private NoteAdapter noteAdapter;
    private OnlineDBHandler dbHandler;
    private String userEmail;
    private View emptyText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = findViewById(R.id.homeToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle("EduNotes");

        bottomNav = findViewById(R.id.bottom_navigation);
        recyclerView = findViewById(R.id.recyclerViewNotes);
        emptyText = findViewById(R.id.emptyText);

        userEmail = getIntent().getStringExtra("email");
        dbHandler = new OnlineDBHandler(this);

        // Initialize adapter
        noteAdapter = new NoteAdapter(this, new ArrayList<>(), userEmail);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        recyclerView.setAdapter(noteAdapter);
        recyclerView.setHasFixedSize(true);

        loadNotesAndSavedStates("DESC");

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_upload) {
                startActivity(new Intent(HomeActivity.this, UploadNotesActivity.class).putExtra("email", userEmail));
            } else if (id == R.id.nav_saved) {
                startActivity(new Intent(HomeActivity.this, SavedNotesActivity.class).putExtra("email", userEmail));
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(HomeActivity.this, ProfileActivity.class).putExtra("email", userEmail));
            }
            return true;
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // -------------------
        // Observe NoteEventBus for new notes
        NoteEventBus.getInstance().getNewNoteLiveData().observe(this, noteId -> {
            if (noteId != null) handleNewNote(noteId);
        });
    }

    private void loadNotesAndSavedStates(String sortOrder) {
        dbHandler.fetchNotes(sortOrder, notesResponse -> {
            List<Note> allNotes = parseNotes(notesResponse);
            dbHandler.fetchSavedNotes(userEmail, savedResponse -> {
                Set<Integer> savedIds = parseSavedIds(savedResponse);
                for (Note note : allNotes) note.setSaved(savedIds.contains(note.getId()));
                runOnUiThread(() -> {
                    emptyText.setVisibility(allNotes.isEmpty() ? View.VISIBLE : View.GONE);
                    noteAdapter.updateData(allNotes);
                });
            });
        });
    }

    private List<Note> parseNotes(String jsonResponse) {
        List<Note> notes = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(jsonResponse);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                Note note = new Note(
                        obj.getInt("id"),
                        obj.getString("title"),
                        obj.getString("description"),
                        "",
                        obj.getString("uploader_email"),
                        obj.getString("date_created")
                );

                List<String> files = new ArrayList<>();
                JSONArray filesArr = new JSONArray(obj.optString("files_json", "[]"));
                for (int j = 0; j < filesArr.length(); j++) {
                    JSONObject fObj = filesArr.getJSONObject(j);
                    String full = OnlineDBHandler.getBaseUrl() + fObj.getString("path");
                    files.add(full);
                }
                note.setFiles(files);

                if (!files.isEmpty()) note.setFirstImage(files.get(0));

                note.setUploaderName(obj.optString("uploader_name", obj.optString("uploader_email")));
                note.setUploaderProfileUri(obj.optString("uploader_profile_uri", ""));


                notes.add(note);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return notes;
    }

    private Set<Integer> parseSavedIds(String response) {
        Set<Integer> ids = new HashSet<>();
        try {
            JSONArray arr = new JSONArray(response);
            for (int i = 0; i < arr.length(); i++) ids.add(arr.getJSONObject(i).getInt("note_id"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return ids;
    }

    private void handleNewNote(int noteId) {
        dbHandler.getNoteById(noteId, response -> {
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

                List<String> files = new ArrayList<>();
                JSONArray filesArr = new JSONArray(obj.optString("files_json", "[]"));
                for (int j = 0; j < filesArr.length(); j++) files.add(filesArr.getString(j));
                note.setFiles(files);

                dbHandler.fetchSavedNotes(userEmail, savedResponse -> {
                    Set<Integer> savedIds = parseSavedIds(savedResponse);
                    note.setSaved(savedIds.contains(note.getId()));
                    runOnUiThread(() -> {
                        noteAdapter.prependNote(note);
                        emptyText.setVisibility(View.GONE);
                    });
                });

            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_app_bar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_sort) {
            Toolbar toolbar = findViewById(R.id.homeToolbar);
            View anchor = toolbar.findViewById(R.id.action_sort);

            PopupMenu popup = new PopupMenu(this, anchor);
            popup.getMenu().add("Newest → Oldest");
            popup.getMenu().add("Oldest → Newest");

            popup.setOnMenuItemClickListener(menuItem -> {
                if ("Newest → Oldest".equals(menuItem.getTitle())) {
                    loadNotesAndSavedStates("DESC");
                } else {
                    loadNotesAndSavedStates("ASC");
                }
                return true;
            });

            popup.show();
        }
        return super.onOptionsItemSelected(item);
    }
}