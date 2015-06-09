package com.mapster.connectivities.tasks;

import android.os.AsyncTask;
import android.util.Log;

/**
 * Extend this if your task involves getting a response from a URL
 */
public class ReadTask extends AsyncTask<String, Void, String> {
    @Override
    protected String doInBackground(String... url) {
        return downloadUrl(url[0]);
    }

    private String downloadUrl(String url) {
        String data = "";
        try {
            com.mapster.connectivities.HttpConnection http = new com.mapster.connectivities.HttpConnection();
            data = http.readUrl(url);
        } catch (Exception e) {
            Log.d("Background Task", e.toString());
        }
        return data;
    }
}