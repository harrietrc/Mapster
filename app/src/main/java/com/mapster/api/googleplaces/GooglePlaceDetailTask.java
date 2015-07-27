package com.mapster.api.googleplaces;

import android.content.Context;
import android.os.AsyncTask;

import com.google.android.gms.maps.model.Marker;
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

    public GooglePlaceDetailTask(Context context) {
        _api = new GooglePlaces(context);
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
        Marker marker = suggestion.getMarker();
        String info = suggestion.getInfoWindowString();
        marker.setSnippet(info);
        marker.showInfoWindow(); // Might run into some timing issues here
    }
}
