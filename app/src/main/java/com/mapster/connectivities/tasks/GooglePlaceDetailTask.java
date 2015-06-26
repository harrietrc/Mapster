package com.mapster.connectivities.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.Marker;
import com.mapster.R;
import com.mapster.json.GooglePlaceDetailJsonParser;
import com.mapster.places.GooglePlaceDetail;
import com.mapster.suggestions.GooglePlaceSuggestion;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Harriet on 5/24/2015. Takes a suggestion that is expected to correspond with a place
 * (i.e. have a Google place ID) and sets the detail for that suggestion.
 */
public class GooglePlaceDetailTask extends AsyncTask<GooglePlaceSuggestion, Void, GooglePlaceSuggestion> {

    private Context _context;

    public GooglePlaceDetailTask(Context context) {
        _context = context;
    }

    @Override
    protected GooglePlaceSuggestion doInBackground(GooglePlaceSuggestion... suggestions) {
        GooglePlaceDetailJsonParser detailJsonParser = new GooglePlaceDetailJsonParser();
        GooglePlaceSuggestion suggestion = suggestions[0];

        String placeId = suggestion.getPlaceId();
        String url = buildDetailUrl(placeId);
        String response = null;

        try {
            com.mapster.connectivities.HttpConnection http = new com.mapster.connectivities.HttpConnection();
            response = http.readUrl(url);
        } catch (Exception e) {
            Log.d("Background Task", e.toString());
        }

        GooglePlaceDetail detail = null;

        try {
            JSONObject jsonResponse = new JSONObject(response);
            detail = detailJsonParser.parse(jsonResponse);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        suggestion.setPlaceDetail(detail);
        return suggestion;
    }

    /**
     * Update the infowindow with the new detail about the place. Marker.setSnippet() must be called
     * in the UI thread.
     * @param suggestion
     */
    @Override
    protected void onPostExecute(GooglePlaceSuggestion suggestion) {
        Marker marker = suggestion.getMarker();
        String info = suggestion.getInfoWindowString();
        marker.setSnippet(info);
        marker.showInfoWindow(); // Might run into some timing issues here
    }

    public String buildDetailUrl(String placeId) {
        StringBuilder sb = new StringBuilder("https://maps.googleapis.com/maps/api/place/details/json?");
        sb.append("key=" + _context.getResources().getString(R.string.API_KEY));
        sb.append("&placeid=" + placeId);
        String url = sb.toString();
        return url;
    }
}
