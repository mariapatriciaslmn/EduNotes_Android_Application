package com.example.edunotesandroidapplication.models;

public class Note {

    private int id;
    private String title;
    private String description;
    private String imageUrl;
    private String uploaderEmail;
    private String dateCreated;

    // Full constructor (for DB fetching)
    public Note(int id, String title, String description, String imageUrl, String uploaderEmail, String dateCreated) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.uploaderEmail = uploaderEmail;
        this.dateCreated = dateCreated;
    }

    // Constructor (for creating new note before saving)
    public Note(String title, String description, String imageUrl, String uploaderEmail) {
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.uploaderEmail = uploaderEmail;
        this.dateCreated = "";
    }

    // Getters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getImageUrl() { return imageUrl; }
    public String getUploaderEmail() { return uploaderEmail; }
    public String getDateCreated() { return dateCreated; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setUploaderEmail(String uploaderEmail) { this.uploaderEmail = uploaderEmail; }
    public void setDateCreated(String dateCreated) { this.dateCreated = dateCreated; }
}
