package com.example.edunotesandroidapplication;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

public class MultipartRequest extends Request<String> {

    private final Response.Listener<String> mListener;
    private final Map<String, String> mStringParts;
    private final Map<String, DataPart> mFileParts;

    private final String boundary = "apiclient-" + System.currentTimeMillis();
    private final String mimeType = "multipart/form-data;boundary=" + boundary;

    public MultipartRequest(
            int method,
            String url,
            Map<String, String> stringParts,
            Map<String, DataPart> fileParts,
            Response.Listener<String> listener,
            Response.ErrorListener errorListener
    ) {
        super(method, url, errorListener);
        this.mListener = listener;
        this.mStringParts = stringParts;
        this.mFileParts = fileParts;
    }

    @Override
    public String getBodyContentType() {
        return mimeType;
    }

    @Override
    public byte[] getBody() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {

            // TEXT FIELDS
            if (mStringParts != null) {
                for (Map.Entry<String, String> entry : mStringParts.entrySet()) {
                    String part = "--" + boundary + "\r\n" +
                            "Content-Disposition: form-data; name=\"" + entry.getKey() + "\"\r\n\r\n" +
                            entry.getValue() + "\r\n";
                    bos.write(part.getBytes());
                }
            }

            // FILES
            if (mFileParts != null) {
                for (Map.Entry<String, DataPart> entry : mFileParts.entrySet()) {
                    DataPart dp = entry.getValue();

                    String header = "--" + boundary + "\r\n" +
                            "Content-Disposition: form-data; name=\"" + entry.getKey() + "\"; filename=\"" + dp.fileName + "\"\r\n" +
                            "Content-Type: " + dp.type + "\r\n\r\n";

                    bos.write(header.getBytes());
                    bos.write(dp.data);
                    bos.write("\r\n".getBytes());
                }
            }

            bos.write(("--" + boundary + "--\r\n").getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bos.toByteArray();
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        try {
            String parsed = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            return Response.success(parsed, HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new com.android.volley.ParseError(e));
        }
    }

    @Override
    protected void deliverResponse(String response) {
        mListener.onResponse(response);
    }

    // DataPart class
    public static class DataPart {
        public String fileName;
        public byte[] data;
        public String type;

        public DataPart(String fileName, byte[] data, String type) {
            this.fileName = fileName;
            this.data = data;
            this.type = type;
        }
    }
}