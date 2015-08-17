package com.mapster.api.foursquare;

import com.google.android.gms.maps.model.LatLng;
import com.mapster.api.ApiRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Harriet on 6/10/2015.
 */
public class FoursquareRequest extends ApiRequest {

    private static final String SERVICE = "https://api.foursquare.com/";
    private static final String VERSION = "v2/";
    private static final String EXPLORE_PATH = "venues/explore";

    private static final String SECTION = "food";
    private static final int VENUE_PHOTOS = 1;
    private static final int V = 20150610;

    private String _clientId;
    private String _secretKey;

    // Vary for each request
    private int _radius;
    private LatLng _ll;
    private int _resultsLimit;

    public FoursquareRequest(String clientId, String secretKey, LatLng location, int radius, int resultsLimit) {
        _clientId = clientId;
        _secretKey = secretKey;
        _ll = location;
        _radius = radius;
        _resultsLimit = resultsLimit;
    }

    public String constructBaseUrl() {
        return SERVICE + VERSION + EXPLORE_PATH;
    }

    @Override
    public Map<String, String> queryFieldsAsMap() {
        Map<String, String> queryFields = new HashMap<>();

        queryFields.put("client_id", _clientId);
        queryFields.put("client_secret", _secretKey);
        queryFields.put("section", SECTION);
        queryFields.put("venuePhotos", Integer.toString(VENUE_PHOTOS));
        queryFields.put("v", Integer.toString(V));
        queryFields.put("radius", Integer.toString(_radius));
        queryFields.put("ll", formatLocation());
        queryFields.put("limit", Integer.toString(_resultsLimit));

        return queryFields;
    }

    private String formatLocation() {
        String lat = String.format("%.2f", _ll.latitude);
        String lng = String.format("%.2f", _ll.longitude);
        return lat + "," + lng;
    }
}
