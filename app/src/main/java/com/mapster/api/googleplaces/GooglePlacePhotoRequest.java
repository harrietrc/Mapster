package com.mapster.api.googleplaces;

import com.mapster.api.ApiRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Harriet on 7/26/2015.
 */
public class GooglePlacePhotoRequest extends ApiRequest {

    private static final String PHOTO_SERVICE = "https://maps.googleapis.com/maps/api/place/photo";

    private String _apiKey;

    private static final int MAX_WIDTH = 200;
    private static final int MAX_HEIGHT = 200;

    private String _photoReference;

    public GooglePlacePhotoRequest(String apiKey, String photoReference) {
        _apiKey = apiKey;
        _photoReference = photoReference;
    }

    @Override
    protected String constructBaseUrl() {
        return PHOTO_SERVICE;
    }

    @Override
    public Map<String, String> queryFieldsAsMap() {
        Map<String, String> queryFields = new HashMap<>();

        queryFields.put("maxwidth", Integer.toString(MAX_WIDTH));
        queryFields.put("maxheight", Integer.toString(MAX_HEIGHT));
        queryFields.put("photoreference", _photoReference);
        queryFields.put("key", _apiKey);

        return queryFields;
    }
}
