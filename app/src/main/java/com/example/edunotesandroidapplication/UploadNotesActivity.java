package com.example.edunotesandroidapplication;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ClipData;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.content.Intent;

import com.example.edunotesandroidapplication.adapters.FilesAdapter;
import com.example.edunotesandroidapplication.models.Files;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class UploadNotesActivity extends AppCompatActivity {

    private RecyclerView filesRecyclerView;
    private EditText noteTitleEditText, noteDescriptionEditText;
    private Button selectFilesButton, uploadButton;
    private ArrayList<Files> filesList = new ArrayList<>();
    private FilesAdapter filesAdapter;
    private OnlineDBHandler onlineDBHandler;
    private String userEmail;
    private final int MAX_FILES = 20;

    private final ActivityResultLauncher<Intent> filePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    ClipData clipData = result.getData().getClipData();
                    ArrayList<Files> tempList = new ArrayList<>();

                    if (clipData != null) {
                        for (int i = 0; i < clipData.getItemCount(); i++) {
                            Uri uri = clipData.getItemAt(i).getUri();
                            if (!containsUri(uri)) {
                                String name = getFileName(uri);
                                String ext = getFileExtension(name);
                                tempList.add(new Files(uri, name, ext));
                            }
                        }
                    } else if (result.getData().getData() != null) {
                        Uri uri = result.getData().getData();
                        if (!containsUri(uri)) {
                            String name = getFileName(uri);
                            String ext = getFileExtension(name);
                            tempList.add(new Files(uri, name, ext));
                        }
                    }

                    if (filesList.size() + tempList.size() > MAX_FILES) {
                        Toast.makeText(this, "You can select a maximum of " + MAX_FILES + " files", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    filesList.addAll(tempList);
                    filesAdapter.notifyDataSetChanged();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_notes);

        onlineDBHandler = new OnlineDBHandler(this);
        userEmail = getIntent().getStringExtra("email");

        noteTitleEditText = findViewById(R.id.noteTitleEditText);
        noteDescriptionEditText = findViewById(R.id.noteDescriptionEditText);
        filesRecyclerView = findViewById(R.id.filesRecyclerView);
        selectFilesButton = findViewById(R.id.selectFilesButton);
        uploadButton = findViewById(R.id.uploadButton);

        Toolbar toolbar = findViewById(R.id.uploadNotesToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Upload Notes");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        filesAdapter = new FilesAdapter(this, filesList);
        filesRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        filesRecyclerView.setAdapter(filesAdapter);

        selectFilesButton.setOnClickListener(v -> openFilePicker());
        uploadButton.setOnClickListener(v -> uploadNote());
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);

        String[] mimeTypes = {
                "image/*",
                "application/pdf",
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "text/plain",
                "application/vnd.ms-powerpoint",
                "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                "application/vnd.ms-excel",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "application/zip"
        };
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);

        filePickerLauncher.launch(Intent.createChooser(intent, "Select files"));
    }

    private void uploadNote() {
        String title = noteTitleEditText.getText().toString().trim();
        String description = noteDescriptionEditText.getText().toString().trim();

        if (title.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Please enter title and description", Toast.LENGTH_SHORT).show();
            return;
        }

        if (filesList.isEmpty()) {
            Toast.makeText(this, "Select at least one file", Toast.LENGTH_SHORT).show();
            return;
        }

        onlineDBHandler.uploadNoteMultipart(title, description, filesList, userEmail, response -> {
            try {
                JSONObject obj = new JSONObject(response);
                boolean success = obj.optBoolean("success", false);

                if (success) {
                    // Get the note ID returned by server
                    int noteId = obj.optInt("note_id", -1);

                    // Parse uploaded files if needed
                    JSONArray uploadedFiles = obj.optJSONArray("files_uploaded");
                    ArrayList<Files> uploadedList = new ArrayList<>();
                    if (uploadedFiles != null) {
                        for (int i = 0; i < uploadedFiles.length(); i++) {
                            JSONObject f = uploadedFiles.getJSONObject(i);
                            String name = f.optString("name");
                            String path = f.optString("path");
                            uploadedList.add(new Files(Uri.parse(path), name, getFileExtension(name)));
                        }
                    }

                    // Notify other activities via EventBus
                    if (noteId != -1) {
                        NoteEventBus.getInstance().postNewNote(noteId);
                        NoteEventBus.getInstance().postUploadedFiles(uploadedList);
                    }

                    runOnUiThread(() -> Toast.makeText(this, "Upload successful!", Toast.LENGTH_SHORT).show());
                    finish();

                } else {
                    String error = obj.optString("error", "Upload failed");
                    runOnUiThread(() -> Toast.makeText(this, error, Toast.LENGTH_LONG).show());
                }

            } catch (JSONException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Invalid server response", Toast.LENGTH_SHORT).show());
            }
        });
    }
    private byte[] readBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[4096];
        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        return buffer.toByteArray();
    }

    private String getFileName(Uri uri) {
        String name = "File";
        ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver.query(uri, null, null, null, null);
        if (cursor != null) {
            int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            if (index >= 0 && cursor.moveToFirst()) {
                name = cursor.getString(index);
            }
            cursor.close();
        }
        return name;
    }

    private String getFileExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return (dot != -1) ? fileName.substring(dot + 1) : "";
    }

    private boolean containsUri(Uri uri) {
        for (Files file : filesList) {
            if (file.getUri().equals(uri)) return true;
        }
        return false;
    }
}