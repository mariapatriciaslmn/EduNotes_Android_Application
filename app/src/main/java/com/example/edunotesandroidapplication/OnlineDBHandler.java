package com.example.edunotesandroidapplication;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.edunotesandroidapplication.models.Files;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

public class OnlineDBHandler {

    private static final String BASE_URL = "http://10.166.115.89/edunotes/";
    public static String getBaseUrl() {
        return BASE_URL;
    }
    private final Context context;
    private final RequestQueue queue;

    // -------------------- Interfaces --------------------
    public interface ResponseListener {
        void onResponse(String response);
    }

    public interface LoginListener {
        void onLoginSuccess();
        void onLoginFailed(String errorMessage);
    }

    public interface SignupListener {
        void onSignupSuccess();
        void onSignupFailed(String errorMessage);
    }

    // -------------------- Constructor --------------------
    public OnlineDBHandler(Context context) {
        this.context = context;
        this.queue = Volley.newRequestQueue(context);
    }

    public interface ResponseCallback {
        void onResponse(String response);
    }

    // Inside OnlineDBHandler.java
    private void sendGetRequest(String urlString, ResponseCallback callback) {
        new Thread(() -> {
            StringBuilder result = new StringBuilder();
            try {
                java.net.URL url = new java.net.URL(urlString);
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                int responseCode = conn.getResponseCode();
                java.io.BufferedReader reader;
                if (responseCode == java.net.HttpURLConnection.HTTP_OK) {
                    reader = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getInputStream()));
                } else {
                    reader = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getErrorStream()));
                }

                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                reader.close();
                conn.disconnect();

                // Send response back on main thread
                new android.os.Handler(android.os.Looper.getMainLooper())
                        .post(() -> callback.onResponse(result.toString()));

            } catch (Exception e) {
                e.printStackTrace();
                new android.os.Handler(android.os.Looper.getMainLooper())
                        .post(() -> callback.onResponse("{\"success\":false, \"error\":\"" + e.getMessage() + "\"}"));
            }
        }).start();
    }


    // -------------------- Users --------------------
    public void checkUserLogin(String email, String password, LoginListener listener) {
        String url = BASE_URL + "check_user.php";
        postRequest(url, new HashMap<String, String>() {{
            put("email", email);
            put("password", password);
        }}, response -> {
            Log.d("OnlineDB", "checkUser raw: " + response);
            try {
                JSONObject obj = new JSONObject(response);
                boolean success = obj.optBoolean("success", false);
                if (success) {
                    listener.onLoginSuccess();
                    // start HomeActivity (context must be Application/Activity)
                    Intent intent = new Intent(context, HomeActivity.class);
                    intent.putExtra("email", email);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    context.startActivity(intent);
                } else {
                    String error = obj.optString("error", "Invalid credentials");
                    listener.onLoginFailed(error);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                listener.onLoginFailed("Invalid server response");
            }
        }, "checkUser");
    }

    public void signupUser(String name, String email, String password, SignupListener listener) {
        String url = BASE_URL + "add_user.php";
        postRequest(url, new HashMap<String, String>() {{
            put("name", name);
            put("email", email);
            put("password", password);
        }}, response -> {
            Log.d("OnlineDB", "signup raw: " + response);
            try {
                JSONObject obj = new JSONObject(response);
                boolean success = obj.optBoolean("success", false);
                if (success) {
                    listener.onSignupSuccess();
                    Intent intent = new Intent(context, HomeActivity.class);
                    intent.putExtra("email", email);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    context.startActivity(intent);
                } else {
                    String error = obj.optString("error", "Signup failed");
                    listener.onSignupFailed(error);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                listener.onSignupFailed("Invalid server response");
            }
        }, "addUser");
    }

    public void getUser(String email, ResponseListener listener) {
        String url = BASE_URL + "get_user.php?email=" + email;
        getRequest(url, listener, "getUser");
    }

    public void updateProfilePicture(String email, String uri, ResponseListener listener) {
        String url = BASE_URL + "update_profile_picture.php";
        postRequest(url, new HashMap<String, String>() {{
            put("email", email);
            put("uri", uri);
        }}, listener, "updateProfilePicture");
    }

    // -------------------- Notes --------------------
    /**
     * Legacy addNote (POST fields only, expects server to accept files as JSON/base64).
     * You can keep this for backward compatibility but prefer uploadNoteMultipart() below.
     */
    public void addNote(String title, String description, org.json.JSONArray filesArray, String uploaderEmail, ResponseListener listener) {
        String url = BASE_URL + "add_note.php";
        postRequest(url, new HashMap<String, String>() {{
            put("title", title);
            put("description", description);
            put("files_json", filesArray.toString());
            put("uploader_email", uploaderEmail);
        }}, response -> {
            Log.d("OnlineDB", "addNote response: " + response);
            listener.onResponse(response);
        }, "addNote");
    }

    /**
     * Multipart upload: uploads actual files (binary) with title/description/uploader_email as form fields.
     *
     * @param title        note title
     * @param description  note description
     * @param files        list of Files model (contains Uri and filename)
     * @param uploaderEmail uploader's email
     * @param listener     callback with server response string
     */
    public void uploadNoteMultipart(String title, String description, ArrayList<Files> files, String uploaderEmail, ResponseListener listener) {
        String url = BASE_URL + "add_note.php";

        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST, url,
                response -> {
                    String parsed;
                    try {
                        parsed = new String(response.data, HttpHeaderParser.parseCharset(response.headers, "utf-8"));
                    } catch (Exception e) {
                        parsed = new String(response.data);
                    }
                    Log.d("OnlineDB", "uploadNoteMultipart response: " + parsed);
                    listener.onResponse(parsed);
                },
                error -> {
                    Log.e("OnlineDB", "uploadNoteMultipart error: " + error);
                    // try to extract network response body if available
                    NetworkResponse nr = error.networkResponse;
                    if (nr != null && nr.data != null) {
                        String body = new String(nr.data);
                        listener.onResponse(body);
                    } else {
                        listener.onResponse("{\"success\":false,\"error\":\"network error\"}");
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("title", title);
                params.put("description", description);
                params.put("uploader_email", uploaderEmail);
                return params;
            }

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                int idx = 0;
                for (Files f : files) {
                    try {
                        InputStream is = context.getContentResolver().openInputStream(f.getUri());
                        byte[] bytes = toByteArray(is);
                        is.close();
                        String fieldName = "file" + idx; // server should expect file0,file1,... OR adapt on server
                        params.put(fieldName, new DataPart(f.getName(), bytes, guessMimeType(f.getName())));
                        idx++;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return params;
            }
        };

        // Optional: set retry policy/timeouts here if needed
        queue.add(multipartRequest);
    }

    public void fetchNotes(String sortOrder, ResponseListener listener) {
        String url = BASE_URL + "fetch_notes.php?order=" + sortOrder;
        getRequest(url, listener, "fetchNotes");
    }

    public void fetchNotesByUser(String email, String order, ResponseListener listener) {
        String url = BASE_URL + "fetch_notes_by_user.php?email=" + email + "&order=" + order;
        getRequest(url, listener, "fetchNotesByUser");
    }

    public void getNoteById(int noteId, ResponseListener listener) {
        String url = BASE_URL + "get_note.php?note_id=" + noteId;
        getRequest(url, listener, "getNoteById");
    }

    // -------------------- Saved Notes --------------------
    public void saveNote(int noteId, String userEmail, ResponseListener listener) {
        String url = BASE_URL + "save_note.php";
        postRequest(url, new HashMap<String, String>() {{
            put("note_id", String.valueOf(noteId));
            put("user_email", userEmail);
        }}, listener, "saveNote");
    }

    public void removeSavedNote(int noteId, String userEmail, ResponseListener listener) {
        String url = BASE_URL + "remove_saved_note.php";
        postRequest(url, new HashMap<String, String>() {{
            put("note_id", String.valueOf(noteId));
            put("user_email", userEmail);
        }}, listener, "removeSavedNote");
    }

    // Fetch comments for a specific note
    public void getCommentsByNoteId(int noteId, ResponseCallback callback) {
        String url = BASE_URL + "get_comments.php?note_id=" + noteId;
        sendGetRequest(url, callback);
    }


    public void fetchSavedNotes(String userEmail, ResponseListener listener) {
        String url = BASE_URL + "fetch_saved_notes.php?email=" + userEmail;
        getRequest(url, listener, "fetchSavedNotes");
    }

    // -------------------- Check single note saved status --------------------
    public void isNoteSaved(int noteId, String userEmail, ResponseListener listener) {
        String url = BASE_URL + "check_saved_status.php?note_id=" + noteId + "&email=" + userEmail;
        getRequest(url, response -> {
            try {
                JSONObject obj = new JSONObject(response);
                boolean saved = obj.optBoolean("saved", false);
                listener.onResponse(String.valueOf(saved));
            } catch (JSONException e) {
                e.printStackTrace();
                listener.onResponse("false");
            }
        }, "isNoteSaved");
    }

    // -------------------- Comments --------------------
    public void addComment(int noteId, String userName, String content, ResponseListener listener) {
        String url = BASE_URL + "add_comment.php";
        postRequest(url, new HashMap<String, String>() {{
            put("note_id", String.valueOf(noteId));
            put("user_name", userName);
            put("content", content);
        }}, listener, "addComment");
    }

    public void fetchComments(int noteId, ResponseListener listener) {
        String url = BASE_URL + "fetch_comments.php?note_id=" + noteId;
        getRequest(url, listener, "fetchComments");
    }

    // -------------------- Helper Methods --------------------
    private void postRequest(String url, Map<String, String> params, ResponseListener listener, String tag) {
        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> listener.onResponse(response),
                error -> {
                    Log.e("OnlineDB", tag + " error: " + error);
                    // try to attach network response body when possible
                    NetworkResponse nr = error.networkResponse;
                    if (nr != null && nr.data != null) {
                        listener.onResponse(new String(nr.data));
                    } else {
                        listener.onResponse("{\"success\":false}");
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                return params;
            }
        };
        queue.add(request);
    }

    private void getRequest(String url, ResponseListener listener, String tag) {
        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> listener.onResponse(response),
                error -> {
                    Log.e("OnlineDB", tag + " error: " + error);
                    NetworkResponse nr = error.networkResponse;
                    if (nr != null && nr.data != null) {
                        listener.onResponse(new String(nr.data));
                    } else {
                        listener.onResponse("{\"success\":false}");
                    }
                });
        queue.add(request);
    }

    // Utility: read InputStream into byte[]
    private byte[] toByteArray(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[4096];
        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        return buffer.toByteArray();
    }

    private String guessMimeType(String filename) {
        filename = filename.toLowerCase();
        if (filename.endsWith(".png")) return "image/png";
        if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) return "image/jpeg";
        if (filename.endsWith(".gif")) return "image/gif";
        if (filename.endsWith(".pdf")) return "application/pdf";
        if (filename.endsWith(".doc") || filename.endsWith(".docx")) return "application/msword";
        if (filename.endsWith(".ppt") || filename.endsWith(".pptx")) return "application/vnd.ms-powerpoint";
        if (filename.endsWith(".xls") || filename.endsWith(".xlsx")) return "application/vnd.ms-excel";
        if (filename.endsWith(".zip")) return "application/zip";
        return "application/octet-stream";
    }

    // -------------------- Volley Multipart Request helper --------------------
    /**
     * VolleyMultipartRequest - supports posting text params and file byte parts.
     * Usage: create anonymous subclass and override getByteData() to return Map<String, DataPart>
     */
    public static abstract class VolleyMultipartRequest extends Request<NetworkResponse> {

        private final Response.Listener<NetworkResponse> mListener;
        private final Response.ErrorListener mErrorListener;
        private final Map<String, String> mHeaders = new HashMap<>();

        private static final String TWO_HYPHENS = "--";
        private static final String LINE_END = "\r\n";
        private final String boundary = "apiclient-" + System.currentTimeMillis();

        public VolleyMultipartRequest(int method, String url,
                                      Response.Listener<NetworkResponse> listener,
                                      Response.ErrorListener errorListener) {
            super(method, url, errorListener);
            this.mListener = listener;
            this.mErrorListener = errorListener;
        }

        @Override
        public Map<String, String> getHeaders() throws AuthFailureError {
            return mHeaders;
        }

        @Override
        public String getBodyContentType() {
            return "multipart/form-data;boundary=" + boundary;
        }

        @Override
        public byte[] getBody() throws AuthFailureError {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try {
                Map<String, String> params = getParams();
                if (params != null && params.size() > 0) {
                    for (Map.Entry<String, String> entry : params.entrySet()) {
                        bos.write((TWO_HYPHENS + boundary + LINE_END).getBytes());
                        bos.write(("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"" + LINE_END).getBytes());
                        bos.write((LINE_END).getBytes());
                        bos.write(entry.getValue().getBytes());
                        bos.write(LINE_END.getBytes());
                    }
                }

                Map<String, DataPart> data = getByteData();
                if (data != null && data.size() > 0) {
                    for (Map.Entry<String, DataPart> fileEntry : data.entrySet()) {
                        DataPart part = fileEntry.getValue();
                        bos.write((TWO_HYPHENS + boundary + LINE_END).getBytes());
                        bos.write(("Content-Disposition: form-data; name=\"" + fileEntry.getKey() + "\"; filename=\"" + part.getFileName() + "\"" + LINE_END).getBytes());
                        bos.write(("Content-Type: " + part.getType() + LINE_END).getBytes());
                        bos.write(LINE_END.getBytes());
                        bos.write(part.getContent());
                        bos.write(LINE_END.getBytes());
                    }
                }

                bos.write((TWO_HYPHENS + boundary + TWO_HYPHENS + LINE_END).getBytes());
                return bos.toByteArray();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        // Default empty implementation (subclasses may override)
        @Override
        protected Map<String, String> getParams() throws AuthFailureError {
            return super.getParams();
        }

        protected abstract Map<String, DataPart> getByteData() throws AuthFailureError;

        @Override
        protected Response<NetworkResponse> parseNetworkResponse(NetworkResponse response) {
            return Response.success(response, HttpHeaderParser.parseCacheHeaders(response));
        }

        @Override
        protected void deliverResponse(NetworkResponse response) {
            mListener.onResponse(response);
        }

        @Override
        public void deliverError(VolleyError error) {
            mErrorListener.onErrorResponse(error);
        }

        /**
         * DataPart - simple holder for file data
         */
        public static class DataPart {
            private final String fileName;
            private final byte[] content;
            private final String type;

            public DataPart(String fileName, byte[] content) {
                this.fileName = fileName;
                this.content = content;
                this.type = "application/octet-stream";
            }

            public DataPart(String fileName, byte[] content, String type) {
                this.fileName = fileName;
                this.content = content;
                this.type = type;
            }

            public String getFileName() {
                return fileName;
            }

            public byte[] getContent() {
                return content;
            }

            public String getType() {
                return type;
            }
        }
    }
}