package com.mapster.places.autocomplete;

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
import java.util.ArrayList;

/**
 * Created by tommyngo on 19/03/15.
 */
public class AutoCompletePlaces {
    private static final String LOG_TAG = "Mapster";
    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    private static final String OUT_JSON = "/json";

    private String _apiKey;

    public AutoCompletePlaces() {
        // Use the default API key. May not want to hardcode it like this.
        this._apiKey = "AIzaSyDGbryAn8AT_UliJQk6OnCQ0b-CMLJhNFw";
    }

    public AutoCompletePlaces(String apiKey) {
        this._apiKey = apiKey;
    }

    public ArrayList<String> autocomplete(String input) {
        HttpURLConnection conn = null;
        ArrayList<String> resultList = null;
        StringBuilder jsonResults;
        URL url = null;
        InputStreamReader in = null;
        try {
            StringBuilder sb = formURLString(input);
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
            resultList = extractPlaceDescriptionsFromResults(jsonResults);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error processing Places API URL", e);
            return resultList;
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error connecting to Places API", e);
            return resultList;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return resultList;
    }

    private StringBuilder formURLString(String input){
        StringBuilder sb = new StringBuilder(PLACES_API_BASE + TYPE_AUTOCOMPLETE + OUT_JSON);
        try {
            sb.append("?input=" + URLEncoder.encode(input, "utf8"));
            sb.append("&key=" + _apiKey);
        } catch (UnsupportedEncodingException e){
            e.printStackTrace();
        }
        return sb;
    }

    private StringBuilder convertJsonResultsToStringBuilder(InputStreamReader in) throws IOException{
        StringBuilder jsonResults = new StringBuilder();
        int read;
        char[] buff = new char[1024];
        while ((read = in.read(buff)) != -1) {
            jsonResults.append(buff, 0, read);
        }
        return jsonResults;
    }

    private ArrayList<String> extractPlaceDescriptionsFromResults(StringBuilder jsonResults) {
        ArrayList<String> resultList = null;
        try {
            // Create a JSON object hierarchy from the results
            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");

            // Extract the Place descriptions from the results
            resultList = new ArrayList<String>(predsJsonArray.length());
            for (int i = 0; i < predsJsonArray.length(); i++) {
                resultList.add(predsJsonArray.getJSONObject(i).getString("description"));
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Cannot process JSON results", e);
        }
        return resultList;
    }

}
