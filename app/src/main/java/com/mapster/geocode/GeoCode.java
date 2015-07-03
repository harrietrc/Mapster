package com.mapster.geocode;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by tommyngo on 20/03/15.
 */
public class GeoCode extends AsyncTask<String, Void, String[]> {
    private static final String GEOCODE_API_BASE = "http://maps.google.com/maps/api/geocode";
    private static final String LOG_TAG = "Mapster";
    private static final String OUT_JSON = "/json";
    private HttpURLConnection conn;

    protected String[] doInBackground(String ...strings) {
        return convertAddressToLatLng(strings[0]);
    }

    private String[] convertAddressToLatLng(String address){
        StringBuilder jsonResults;
        URL url = null;
        InputStreamReader in = null;
        String[] coordinate = null;
        try {
            StringBuilder sb = formURLString(address);
            try {
                url = new URL(sb.toString());
                conn = (HttpURLConnection) url.openConnection();
                in = new InputStreamReader(conn.getInputStream());
            } catch (MalformedURLException e){
                e.printStackTrace();
            } catch(IOException e){
                e.printStackTrace();
            }
            jsonResults = convertJsonResultsToStringBuilder(in);
            coordinate = extractLatLngFromResults(jsonResults);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error processing GeoCode API URL", e);
            return coordinate;
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error connecting to GeoCode API", e);
            return coordinate;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return coordinate;
    }

    private StringBuilder formURLString(String input){
        StringBuilder sb = new StringBuilder(GEOCODE_API_BASE + OUT_JSON);
        try {
            sb.append("?address=" + URLEncoder.encode(input, "utf8"));
        } catch (UnsupportedEncodingException e){
            e.printStackTrace();
        }
        return sb;
    }

    private StringBuilder convertJsonResultsToStringBuilder(InputStreamReader in) throws IOException {
        StringBuilder jsonResults = new StringBuilder();
        int read;
        char[] buff = new char[1024];
        while ((read = in.read(buff)) != -1) {
            jsonResults.append(buff, 0, read);
        }
        return jsonResults;
    }

    private String[] extractLatLngFromResults(StringBuilder jsonResults) {
        String[] coordinate = new String[2];
        try {
            // Create a JSON object hierarchy from the results
            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            JSONArray jsonArray = jsonObj.getJSONArray("results");
            for (int i = 0; i < jsonArray.length(); i++) {
                coordinate[0] = jsonArray.getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getString("lat");
                coordinate[1] = jsonArray.getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getString("lng");
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Cannot process JSON results", e);
        }
        return coordinate;
    }
}
