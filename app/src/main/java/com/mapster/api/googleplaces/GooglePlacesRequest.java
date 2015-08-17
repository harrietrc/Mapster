package com.mapster.api.googleplaces;

import com.google.android.gms.maps.model.LatLng;
import com.mapster.api.ApiRequest;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by Harriet on 7/26/2015.
 */
public class GooglePlacesRequest extends ApiRequest {

    private static final String PLACE_LIST_SERVICE = "https://maps.googleapis.com/maps/api/place/nearbysearch/json";

    private static final String RANK_BY = "prominence";

    public static final Set<String> ATTRACTIONS = new HashSet<>(Arrays.asList(
            "amusement_park", "aquarium", "zoo", "art_gallery", "museum"//, "park" TOO MANY PARKS!
    ));

    private String _apiKey;

    private String _placeId;
    private LatLng _location;
    private int _radius;

    public GooglePlacesRequest(String apiKey, LatLng location, int radius) {
        _apiKey = apiKey;
        _location = location;
        _radius = radius;
    }

    @Override
    protected String constructBaseUrl() {
        return PLACE_LIST_SERVICE;
    }

    @Override
    public Map<String, String> queryFieldsAsMap() {
        Map<String, String> queryFields = new HashMap<>();

        queryFields.put("key", _apiKey);
        queryFields.put("rankby", RANK_BY);
        queryFields.put("location", formatLocation());
        queryFields.put("types", formatPlaceTypes());
        queryFields.put("radius", Integer.toString(_radius));

        return queryFields;
    }

    private String formatLocation() {
        return _location.latitude + "," + _location.longitude;
    }

    private String formatPlaceTypes() {
        return StringUtils.join(ATTRACTIONS, "|");
    }
}
