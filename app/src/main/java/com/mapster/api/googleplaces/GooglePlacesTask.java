package com.mapster.api.googleplaces;

import android.app.Activity;
import android.os.AsyncTask;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.mapster.R;
import com.mapster.activities.MainActivity;
import com.mapster.itinerary.SuggestionItem;
import com.mapster.itinerary.UserItem;
import com.mapster.json.GooglePlaceJsonParser;
import com.mapster.places.GooglePlace;
import com.mapster.suggestions.GooglePlaceSuggestion;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Harriet on 5/26/2015.
 */
public class GooglePlacesTask extends AsyncTask<LatLng, Void, List<GooglePlace>> {

    private Activity _activity;

    // Has data about the API we're making requests to
    private GooglePlaces _api;

    private int _searchRadius;
    private int _numberOfResults;

    private UserItem _item; // The user-defined item that these attractions are suggestions for

    public GooglePlacesTask(Activity activity, int searchRadius, int numberOfResults,
                            UserItem item) {
        _api = new GooglePlaces(activity);
        _activity = activity;
        _searchRadius = searchRadius;
        _numberOfResults = numberOfResults;
        _item = item;
    }

    /**
     * Fetches and parses suggestions near a point of interest.
     * @return = The places found
     */
    @Override
    protected List<GooglePlace> doInBackground(LatLng... locs) {
        // Make a request for a list of places near the location passed in
        String response = _api.placeListRequest(locs[0], _searchRadius);

        // Parse JSON response into GooglePlaces
        GooglePlaceJsonParser placeJsonParser = new GooglePlaceJsonParser();
        List<GooglePlace> places = new ArrayList<>();
        try {
            JSONObject jsonResponse = new JSONObject(response);
            places = placeJsonParser.parse(jsonResponse);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // If more results than were needed were returned, truncate the list of places
        if (places.size() > _numberOfResults)
            places = new ArrayList<>(places.subList(0, _numberOfResults));

        return places;
    }

    /**
     * Creates a marker from each of the places from the parsed JSON, adding it to the UI
     * @param places = an array of GooglePlaces
     */
    @Override
    protected void onPostExecute(List<GooglePlace> places) {
        MainActivity mainActivity = (MainActivity) _activity;

        for (GooglePlace place: places) {
            GooglePlaceSuggestion suggestion = new GooglePlaceSuggestion(place);
            BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.flag_export);
            SuggestionItem item = new SuggestionItem(suggestion, _item);
            mainActivity.addSuggestionItem(item, icon, place.getName());
        }
    }
}