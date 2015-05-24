package com.mapster.connectivities.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.mapster.R;
import com.mapster.places.GooglePlaceDetail;
import com.mapster.places.GooglePlaceDetailJsonParser;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Harriet on 5/24/2015.
 */
public class PlaceDetailTask extends AsyncTask<String, Void, GooglePlaceDetail> {

    private Context _context;

    public PlaceDetailTask(Context context) {
        _context = context;
    }

    @Override
    protected GooglePlaceDetail doInBackground(String... placeIds) {
        GooglePlaceDetailJsonParser detailJsonParser = new GooglePlaceDetailJsonParser();
        String url = buildDetailUrl(placeIds[0]);
        String response = downloadUrl(url);
        GooglePlaceDetail detail = null;

        try {
            JSONObject jsonResponse = new JSONObject(response);
            detail = detailJsonParser.parse(jsonResponse);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return detail;
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

    public String buildDetailUrl(String placeId) {
        StringBuilder sb = new StringBuilder("https://maps.googleapis.com/maps/api/place/details/json?");
        sb.append("key=" + _context.getResources().getString(R.string.API_KEY));
        sb.append("&placeid=" + placeId);
        String url = sb.toString();
        return url;
    }
}
