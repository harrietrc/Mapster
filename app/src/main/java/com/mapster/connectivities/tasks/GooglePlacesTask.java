package com.mapster.connectivities.tasks;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.mapster.R;
import com.mapster.activities.MainActivity;
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
    private int _searchRadius;
    private int _numberOfResults;

    public GooglePlacesTask(Activity activity, int searchRadius, int numberOfResults) {
        _activity = activity;
        _searchRadius = searchRadius;
        _numberOfResults = numberOfResults;
    }

    /**
     * Fetches and parses suggestions near a point of interest.
     * @return = The places found
     */
    @Override
    protected List<GooglePlace> doInBackground(LatLng... locs) {
        GooglePlaceJsonParser placeJsonParser = new GooglePlaceJsonParser();
        List<GooglePlace> places = new ArrayList<>();

        List<String> urls = new ArrayList<>();

        urls.add(buildPlacesUrl(locs[0], GooglePlace.getAttractionCategories()));

        for (String url: urls) {
            // Query the Google Places API to get nearby places
            String response = null;
            try {
                com.mapster.connectivities.HttpConnection http = new com.mapster.connectivities.HttpConnection();
                response = http.readUrl(url);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }

            // Parse JSON response into GooglePlaces
            try {
                JSONObject jsonResponse = new JSONObject(response);
                List<GooglePlace> parsed = placeJsonParser.parse(jsonResponse);

                // Limit the number of suggestions per category
                List<GooglePlace> shortList = parsed;
                if (places.size() > _numberOfResults)
                    shortList = parsed.subList(0, _numberOfResults);
                places.addAll(shortList);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
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
            // Save the marker in the correct categories
            String[] categories = place.getCategories();
            String parentCategory = null;
            int iconId = 0;
            // TODO Can probably simplify this a bit
            for (String c : categories) {
                // Not the best way to do this, but ok for 2 categories.
                if (GooglePlace.ATTRACTIONS.contains(c)|GooglePlace.EXTRA_ATTRACTIONS.contains(c)) {
                    parentCategory = "attractions";
                    iconId = R.drawable.flag_export;
                }
            }
            // 'establishment' type is allowed only if it also matches one of the
            // EXTRA_ATTRACTIONS types - if not, discard the Place.
            if (parentCategory != null) {
                GooglePlaceSuggestion suggestion = new GooglePlaceSuggestion(place, parentCategory);
                BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(iconId);
                mainActivity.addSuggestion(suggestion, icon, place.getName());
            }
        }
    }

    public String buildPlacesUrl(LatLng location, String[] types) {
        double lat = location.latitude;
        double lng = location.longitude;

        StringBuilder sb = new StringBuilder("https://maps.googleapis.com/maps/api/place"
                + "/nearbysearch/json?");
        sb.append("key=" + _activity.getResources().getString(R.string.API_KEY));
        sb.append("&location=" + lat + "," + lng);
        sb.append("&radius=" + _searchRadius);
        sb.append("&rankby=prominence");
        if (types.length > 0) {
            sb.append("&types=");
            String delim = "";
            for (String s: types) {
                sb.append(delim);
                sb.append(s);
                delim = "|";
            }
        }
        String url = sb.toString();
        return url;
    }
}