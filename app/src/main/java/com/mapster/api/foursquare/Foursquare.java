package com.mapster.api.foursquare;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;
import com.mapster.R;
import com.mapster.api.Api;
import com.mapster.api.ApiRequest;


/**
 * Created by Harriet on 7/26/2015.
 */
public class Foursquare extends Api {

    private String _clientId;
    private String _secretKey;

    public Foursquare(Context context) {
        _clientId = context.getString(R.string.FOURSQUARE_CLIENT_ID);
        _secretKey = context.getString(R.string.FOURSQUARE_CLIENT_SECRET);
    }

    public ApiRequest exploreNearbyVenuesRequest(LatLng location, int radius, int resultsLimit) {
        FoursquareRequest request = new FoursquareRequest(_clientId, _secretKey, location, radius, resultsLimit);
        return getRequest(request);
    }
}
