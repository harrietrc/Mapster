package com.mapster.connectivities.tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;
import com.mapster.yelp.YelpApi;
import com.mapster.yelp.YelpBusiness;

import java.util.List;

/**
 * Created by Harriet on 6/10/2015.
 */
public class YelpBusinessListTask extends AsyncTask<LatLng, Void, List<YelpBusiness>> {

    private Context _context;

    /**
     * TODO Could change this to take the API keys as parameters instead of the context
     * @param context Context, used to access strings.xml string resources
     */
    public YelpBusinessListTask(Context context) {
        _context = context;
    }

    @Override
    protected List<YelpBusiness> doInBackground(LatLng... params) {
        LatLng loc = params[0];
        YelpApi yelpApi = new YelpApi(_context);
        yelpApi.queryAPI("food", loc, 2000);
        return null;
    }
}
