package com.example.edunotesandroidapplication;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.edunotesandroidapplication.adapters.NoteAdapter;
import com.example.edunotesandroidapplication.models.Note;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SavedNotesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView textNoSavedNotes;
    private NoteAdapter noteAdapter;
    private OnlineDBHandler dbHandler;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_notes);

        Toolbar toolbar = findViewById(R.id.savedNotesToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle("Saved Notes");
        toolbar.setNavigationIcon(R.drawable.baseline_arrow_back_24);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        recyclerView = findViewById(R.id.recyclerViewSavedNotes);
        textNoSavedNotes = findViewById(R.id.textNoSavedNotes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        dbHandler = new OnlineDBHandler(this);
        userEmail = getIntent().getStringExtra("email");

        loadSavedNotes();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSavedNotes(); // refresh when returning
    }

    private void loadSavedNotes() {
        dbHandler.fetchSavedNotes(userEmail, response -> {
            List<Note> savedNotes = new ArrayList<>();

            try {
                JSONObject respObj = new JSONObject(response);
                boolean success = respObj.optBoolean("success", false);

                if (!success) {
                    runOnUiThread(() -> {
                        textNoSavedNotes.setText("No saved notes found");
                        textNoSavedNotes.setVisibility(android.view.View.VISIBLE);
                        recyclerView.setVisibility(android.view.View.GONE);
                    });
                    return;
                }

                JSONArray array = respObj.getJSONArray("notes");

                for (int i = 0; i < array.length(); i++) {
                    JSONObject obj = array.getJSONObject(i);

                    Note note = new Note(
                            obj.getInt("note_id"),
                            obj.optString("title", ""),
                            obj.optString("description", ""),
                            "",
                            obj.optString("uploader_email", ""),
                            obj.optString("date_created", "")
                    );

                    // Parse files_json (already decoded in PHP)
                    List<String> files = new ArrayList<>();
                    JSONArray filesArr = obj.optJSONArray("files_json");
                    if (filesArr != null) {
                        for (int j = 0; j < filesArr.length(); j++) {
                            String filePath = filesArr.getString(j);
                            files.add(OnlineDBHandler.getBaseUrl() + filePath);
                        }
                    }
                    for (int j = 0; j < filesArr.length(); j++) {
                        String filePath = filesArr.getString(j);
                        files.add(OnlineDBHandler.getBaseUrl() + filePath);
                    }

                    note.setFiles(files);
                    if (!files.isEmpty()) note.setFirstImage(files.get(0));
                    note.setSaved(true);

                    savedNotes.add(note);
                }

            } catch (JSONException e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this, "Failed to parse saved notes", Toast.LENGTH_SHORT).show()
                );
            }

            runOnUiThread(() -> {
                if (savedNotes.isEmpty()) {
                    textNoSavedNotes.setVisibility(android.view.View.VISIBLE);
                    recyclerView.setVisibility(android.view.View.GONE);
                } else {
                    textNoSavedNotes.setVisibility(android.view.View.GONE);
                    recyclerView.setVisibility(android.view.View.VISIBLE);

                    if (noteAdapter == null) {
                        noteAdapter = new NoteAdapter(this, savedNotes, userEmail);
                        recyclerView.setAdapter(noteAdapter);
                    } else {
                        noteAdapter.updateData(savedNotes);
                    }
                }
            });
        });
    }
}
