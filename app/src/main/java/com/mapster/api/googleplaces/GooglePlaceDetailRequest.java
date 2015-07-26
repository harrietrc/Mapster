package com.mapster.api.googleplaces;

import com.mapster.api.ApiRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Harriet on 7/26/2015.
 */
public class GooglePlaceDetailRequest extends ApiRequest {

    private static final String SERVICE = "https://maps.googleapis.com/maps/api/place/details/json";

    private String _apiKey;
    private String _placeId;

    public GooglePlaceDetailRequest(String apiKey, String placeId) {
        _apiKey = apiKey;
        _placeId = placeId;
    }

    @Override
    protected String constructBaseUrl() {
        return SERVICE;
    }

    @Override
    protected Map<String, String> queryFieldsAsMap() {
        Map<String, String> queryFields = new HashMap<>();

        queryFields.put("key", _apiKey);
        queryFields.put("placeid", _placeId);

        return queryFields;
    }
}
