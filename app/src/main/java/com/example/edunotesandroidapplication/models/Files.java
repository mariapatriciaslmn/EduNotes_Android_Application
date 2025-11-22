package com.example.edunotesandroidapplication.models;

import android.net.Uri;

public class Files {

    private final Uri uri;
    private final String name;
    private final String extension;

    public Files(Uri uri, String name, String extension) {
        this.uri = uri;
        this.name = name;
        this.extension = extension;
    }

    public Uri getUri() { return uri; }
    public String getName() { return name; }
    public String getExtension() { return extension; }

    // From URL / path
    public static Files fromUrl(String url) {
        String fileName = url.substring(url.lastIndexOf('/') + 1);
        String ext = "";
        int dot = fileName.lastIndexOf('.');
        if (dot != -1 && dot + 1 < fileName.length()) ext = fileName.substring(dot + 1);
        return new Files(Uri.parse(url), fileName, ext);
    }
}