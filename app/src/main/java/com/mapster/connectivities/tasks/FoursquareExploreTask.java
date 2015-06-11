package com.mapster.connectivities.tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;
import com.mapster.foursquare.FoursquareApi;
import com.mapster.foursquare.FoursquareVenue;
import com.mapster.json.FoursquareExploreJsonParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Harriet on 6/10/2015.
 */
public class FoursquareExploreTask extends AsyncTask<LatLng, Void, List<FoursquareVenue>> {

    private Context _context;
    private int _radius;
    private int _numberOfResults;

    public FoursquareExploreTask(Context context, int searchRadius, int numberOfResults) {
        _context = context;
        _radius = searchRadius;
        _numberOfResults = numberOfResults;
    }

    @Override
    protected List<FoursquareVenue> doInBackground(LatLng... locations) {
        FoursquareApi four = new FoursquareApi(_context);
        List<FoursquareVenue> restaurants = new ArrayList<>();
        FoursquareExploreJsonParser parser = new FoursquareExploreJsonParser();

        // Make a request to Foursquare to get a list of restaurants (only explores 'food' currently)
        String response = four.exploreRestaurantsNearLocation(locations[0], _radius, _numberOfResults);

        // Parse the response into a list of restaurants
        try {
            JSONObject jsonResponse = new JSONObject(response);
            restaurants = parser.getVenues(jsonResponse);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return restaurants;
    }
}
