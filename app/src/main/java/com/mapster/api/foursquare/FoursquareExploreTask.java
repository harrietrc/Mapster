package com.mapster.api.foursquare;

import android.app.Activity;
import android.os.AsyncTask;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.mapster.R;
import com.mapster.activities.MainActivity;
import com.mapster.itinerary.SuggestionItem;
import com.mapster.itinerary.UserItem;
import com.mapster.json.FoursquareExploreJsonParser;
import com.mapster.suggestions.FoursquareSuggestion;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Harriet on 6/10/2015.
 */
public class FoursquareExploreTask extends AsyncTask<LatLng, Void, List<FoursquareVenue>> {

    private Foursquare _api;

    private Activity _activity;

    private int _radius;
    private int _numberOfResults;
    private UserItem _item; // The user-defined destination that these venues are suggestions for

    public FoursquareExploreTask(Activity activity, int searchRadius, int numberOfResults,
                                 UserItem item) {
        _api = new Foursquare(activity);
        _activity = activity;
        _radius = searchRadius;
        _numberOfResults = numberOfResults;
        _item = item;
    }

    @Override
    protected List<FoursquareVenue> doInBackground(LatLng... locations) {
        // Make a request to Foursquare to get a list of restaurants (only explores 'food' currently)
        String response = _api.exploreNearbyVenuesRequest(locations[0], _radius, _numberOfResults).getResponse();

        // Parse the response into a list of restaurants
        List<FoursquareVenue> restaurants = new ArrayList<>();
        FoursquareExploreJsonParser parser = new FoursquareExploreJsonParser();
        try {
            JSONObject jsonResponse = new JSONObject(response);
            restaurants = parser.getVenues(jsonResponse);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return restaurants;
    }

    @Override
    protected void onPostExecute(List<FoursquareVenue> venues) {
        MainActivity mainActivity = (MainActivity) _activity;

        for (FoursquareVenue v: venues) {
            FoursquareSuggestion suggestion = new FoursquareSuggestion(v, _activity);
            BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.restaurant);
            SuggestionItem item = new SuggestionItem(suggestion, _item);
            mainActivity.addSuggestionItem(item, icon, v.getName());
        }
    }
}
