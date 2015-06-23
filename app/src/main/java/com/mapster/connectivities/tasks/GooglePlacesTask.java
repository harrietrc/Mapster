package com.mapster.connectivities.tasks;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by Harriet on 5/26/2015.
 */
public class GooglePlacesTask extends AsyncTask<LatLng, Void, List<GooglePlace>> {

    private Activity _activity;
    private int _searchRadius;
    private int _numberOfResults;
    private UserItem _item; // The user-defined item that these attractions are suggestions for

    /*
        Two alternatives for retrieving results:
        - True: There are some categories ('point_of_interest', 'natural_feature') that can't be
          queried for in a request to the Places API. This option will filter for them once the
          results are returned and keep 'paging' the API until there are enough results.
        - False: Ignore the extra categories. Gives less relevant results but I found that with
          the first option not enough results were returned, as paging is limited to 3 pages.
     */
    private static final boolean FILTER_RESULTS = false;

    public GooglePlacesTask(Activity activity, int searchRadius, int numberOfResults,
                            UserItem item) {
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
        GooglePlaceJsonParser placeJsonParser = new GooglePlaceJsonParser();
        List<GooglePlace> places = new ArrayList<>();

        String url = buildPlacesUrl(locs[0]);

        // Query the Google Places API to get nearby places
        String response = null;
        com.mapster.connectivities.HttpConnection http = null;
        try {
            http = new com.mapster.connectivities.HttpConnection();
            response = http.readUrl(url);
        } catch (IOException e) {
            Log.d("Background Task", e.toString());
        }

        // Parse JSON response into GooglePlaces
        JSONObject jsonResponse = null;
        try {
            jsonResponse = new JSONObject(response);
            places = placeJsonParser.parse(jsonResponse);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        List<GooglePlace> filteredPlaces = filterPlaces(places);

        if (filteredPlaces.size() < _numberOfResults) {
            // Keep paging the Places API until we have enough results
            while (filteredPlaces.size() < _numberOfResults) {
                String nextPageToken = placeJsonParser.getNextPageToken(jsonResponse);
                if (nextPageToken == null)
                    break;

                // Get and parse the next page of results
                String nextPageUrl = buildNextPageUrl(nextPageToken);
                List<GooglePlace> newPlaces = new ArrayList<>();
                try {
                    String newResponse = http.readUrl(nextPageUrl);
                    jsonResponse = new JSONObject(newResponse);
                    newPlaces = placeJsonParser.parse(jsonResponse);
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }

                // Filter the new results and add the relevant ones to our list
                List<GooglePlace> newFilteredPlaces = filterPlaces(newPlaces);
                filteredPlaces.addAll(newFilteredPlaces);
            }
        } else {
            // Truncate the list of places to be no longer than the specified number of results
            filteredPlaces = new ArrayList<>(filteredPlaces.subList(0, _numberOfResults));
        }

        return filteredPlaces;
    }

    public List<GooglePlace> filterPlaces(List<GooglePlace> places) {
        List<GooglePlace> filteredPlaces = new ArrayList<>();

        if (!FILTER_RESULTS) // See comment for this constant
            return places;

        // The request retrieved places that fit the default category because there are some
        // categories that cannot be queried for. Filter for attractions here
        Set<String> allCategories = GooglePlace.getAttractionCategories();
        for (GooglePlace place: places) {
            Set<String> placeCategories = place.getCategories();

            // If the place fits some of the categories that we recognise as attractions, keep it
            placeCategories.retainAll(allCategories);
            if (placeCategories.size() > 0)
                filteredPlaces.add(place);
        }

        return filteredPlaces;
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

    public String buildNextPageUrl(String nextPageToken) {
        StringBuilder sb = new StringBuilder("https://maps.googleapis.com/maps/api/place"
                + "/nearbysearch/json?");
        sb.append("key=" + _activity.getResources().getString(R.string.API_KEY));
        sb.append("&pagetoken=" + nextPageToken);
        return sb.toString();
    }

    public String buildPlacesUrl(LatLng location) {
        double lat = location.latitude;
        double lng = location.longitude;
        Set<String> types = GooglePlace.ATTRACTIONS;

        StringBuilder sb = new StringBuilder("https://maps.googleapis.com/maps/api/place"
                + "/nearbysearch/json?");
        sb.append("key=" + _activity.getResources().getString(R.string.API_KEY));
        sb.append("&location=" + lat + "," + lng);
        sb.append("&radius=" + _searchRadius);
        sb.append("&rankby=prominence");

        // Essentially renders the paging/filtering stuff useless but it wasn't retrieving enough results
        if (!FILTER_RESULTS)
            if (types.size() > 0) {
                sb.append("&types=");
                String delim = "";
                for (String s: types) {
                    sb.append(delim);
                    sb.append(s);
                    delim = "|";
                }
            }

        return sb.toString();
    }
}