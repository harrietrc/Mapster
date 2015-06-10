package com.mapster.connectivities.tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;
import com.mapster.foursquare.FoursquareApi;
import com.mapster.foursquare.FoursquareVenue;
import com.mapster.json.FoursquareExploreJsonParser;

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

        String response = four.exploreRestaurantsNearLocation(locations[0], _radius, _numberOfResults);
        System.out.println("FSQUARE " + response);

        return null;
    }
}
