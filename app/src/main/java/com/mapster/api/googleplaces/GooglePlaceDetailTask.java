package com.mapster.api.googleplaces;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.google.android.gms.maps.model.Marker;
import com.mapster.activities.MainActivity;
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

    private GooglePlaces _api;
    private ProgressDialog _dialog;
    private Context _context;

    public GooglePlaceDetailTask(Context context) {
        _context = context;
        _api = new GooglePlaces(context);
    }

    @Override
    public void onPreExecute(){
        _dialog = new ProgressDialog(_context);
        _dialog.setMessage("Please wait...");
        _dialog.show();
    }

    @Override
    protected GooglePlaceSuggestion doInBackground(GooglePlaceSuggestion... suggestions) {
        GooglePlaceSuggestion suggestion = suggestions[0];
        String placeId = suggestion.getPlaceId();

        // Make a request to the Google Places API
        String response = _api.placeDetailRequest(placeId).getResponse();

        // Parse the response, extracting details about the place
        GooglePlaceDetailJsonParser detailJsonParser = new GooglePlaceDetailJsonParser();
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
        Marker marker = ((MainActivity) _context).getMarkerById(suggestion.getMarkerId());
        String info = suggestion.getInfoWindowString();
        marker.setSnippet(info);
        _dialog.dismiss();
        marker.showInfoWindow(); // Might run into some timing issues here
    }
}
