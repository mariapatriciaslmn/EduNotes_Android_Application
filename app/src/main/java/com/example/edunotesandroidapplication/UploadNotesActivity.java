package com.example.edunotesandroidapplication;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import java.io.IOException;

public class UploadNotesActivity extends AppCompatActivity {

    private ImageView imagePreview;
    private EditText noteTitleEditText, noteDescriptionEditText;
    private Button selectImageButton, uploadButton;
    private Uri imageUri;
    private DBHandler dbHandler;
    private String userEmail;

    // Launcher for selecting image from gallery
    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                        imagePreview.setImageBitmap(bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_notes);

        // Initialize DB
        dbHandler = new DBHandler(this);

        // Get logged-in user email (from intent)
        Intent intent = getIntent();
        userEmail = intent.getStringExtra("email");

        // Initialize views
        imagePreview = findViewById(R.id.imagePreview);
        noteTitleEditText = findViewById(R.id.noteTitleEditText);
        noteDescriptionEditText = findViewById(R.id.noteDescriptionEditText);
        selectImageButton = findViewById(R.id.selectImageButton);
        uploadButton = findViewById(R.id.uploadButton);

        // Toolbar back button
        findViewById(R.id.uploadNotesToolbar).setOnClickListener(v -> onBackPressed());

        // Select image
        selectImageButton.setOnClickListener(v -> openImagePicker());

        // Upload note
        uploadButton.setOnClickListener(v -> uploadNote());
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void uploadNote() {
        String title = noteTitleEditText.getText().toString().trim();
        String description = noteDescriptionEditText.getText().toString().trim();
        String imageUrl = (imageUri != null) ? imageUri.toString() : "";

        if (title.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Please enter both title and description", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save note in SQLite
        dbHandler.addNewNote(title, description, imageUrl, userEmail);
        Toast.makeText(this, "Note uploaded successfully!", Toast.LENGTH_SHORT).show();

        // Redirect to Profile or Home
        Intent intent = new Intent(this, ProfileActivity.class);
        intent.putExtra("email", userEmail);
        startActivity(intent);
        finish();
    }
}
