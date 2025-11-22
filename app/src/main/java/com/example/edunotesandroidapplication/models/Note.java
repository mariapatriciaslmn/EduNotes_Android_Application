package com.example.edunotesandroidapplication.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Note {

    private int id;
    private String title;
    private String description;
    private String firstImage;
    private String uploaderEmail;
    private String uploaderName;
    private String uploaderProfileUri;
    private String dateCreated;
    private List<String> files;
    private boolean isSaved;

    // Constructor
    public Note(int id, String title, String description, String firstImage,
                String uploaderEmail, String dateCreated) {
        this.id = id;
        this.title = (title != null) ? title : "";
        this.description = (description != null) ? description : "";
        this.firstImage = (firstImage != null) ? firstImage : "";
        this.uploaderEmail = (uploaderEmail != null) ? uploaderEmail : "";
        this.dateCreated = (dateCreated != null) ? dateCreated : "";
        this.files = new ArrayList<>();
        this.isSaved = false;
        this.uploaderName = this.uploaderEmail;
        this.uploaderProfileUri = "";
    }

    // -------------------
    // Getters & Setters
    // -------------------
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getFirstImage() { return firstImage; }
    public String getUploaderEmail() { return uploaderEmail; }
    public String getUploaderName() { return uploaderName; }
    public String getUploaderProfileUri() { return uploaderProfileUri; }
    public String getDateCreated() { return dateCreated; }
    public List<String> getFiles() { return (files != null) ? files : new ArrayList<>(); }
    public boolean isSaved() { return isSaved; }

    public void setTitle(String title) { this.title = (title != null) ? title : ""; }
    public void setDescription(String description) { this.description = (description != null) ? description : ""; }
    public void setFirstImage(String firstImage) { this.firstImage = (firstImage != null) ? firstImage : ""; }
    public void setUploaderEmail(String uploaderEmail) { this.uploaderEmail = (uploaderEmail != null) ? uploaderEmail : ""; }
    public void setUploaderName(String uploaderName) { this.uploaderName = (uploaderName != null) ? uploaderName : uploaderEmail; }
    public void setUploaderProfileUri(String uploaderProfileUri) { this.uploaderProfileUri = (uploaderProfileUri != null) ? uploaderProfileUri : ""; }
    public void setDateCreated(String dateCreated) { this.dateCreated = (dateCreated != null) ? dateCreated : ""; }
    public void setFiles(List<String> files) { this.files = (files != null) ? files : new ArrayList<>(); }
    public void setSaved(boolean saved) { this.isSaved = saved; }

    // -------------------
    // Utility
    // -------------------
    public List<String> getImageUrls() {
        List<String> images = new ArrayList<>();
        for (String f : getFiles()) {
            int dot = f.lastIndexOf('.');
            String ext = (dot != -1 && dot + 1 < f.length()) ? f.substring(dot + 1).toLowerCase() : "";
            if (ext.equals("jpg") || ext.equals("jpeg") || ext.equals("png")
                    || ext.equals("gif") || ext.equals("webp")) {
                images.add(f);
            }
        }
        if (images.isEmpty() && firstImage != null && !firstImage.isEmpty()) images.add(firstImage);
        return images;
    }

    public String getThumbnail() {
        if (firstImage != null && !firstImage.isEmpty()) return firstImage;
        List<String> imgs = getImageUrls();
        return (!imgs.isEmpty()) ? imgs.get(0) : "";
    }

    // -------------------
    // JSON Parsing
    // -------------------
    public static Note fromJson(JSONObject obj) throws JSONException {
        Note note = new Note(
                obj.getInt("id"),
                obj.optString("title", ""),
                obj.optString("description", ""),
                "", // firstImage will be set after files
                obj.optString("uploader_email", ""),
                obj.optString("date_created", "")
        );

        // files_json parsing
        List<String> filesList = new ArrayList<>();
        JSONArray filesArr = new JSONArray(obj.optString("files_json", "[]"));
        for (int i = 0; i < filesArr.length(); i++) {
            filesList.add(filesArr.getString(i));
        }
        note.setFiles(filesList);

        if (!filesList.isEmpty()) note.setFirstImage(filesList.get(0));

        note.setUploaderName(obj.optString("uploader_name", note.getUploaderEmail()));
        note.setUploaderProfileUri(obj.optString("uploader_profile_uri", ""));
        return note;
    }
}