package com.mapster.api.googleplaces;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;
import com.mapster.R;
import com.mapster.api.Api;

/**
 * Created by Harriet on 7/26/2015.
 */
public class GooglePlaces extends Api {

    private String _apiKey;

    public GooglePlaces(Context context) {
        _apiKey = context.getResources().getString(R.string.API_KEY);
    }

    public String placeListRequest(LatLng location, int radius) {
        GooglePlacesRequest request = new GooglePlacesRequest(_apiKey, location, radius);
        return getRequest(request);
    }

    public String placeDetailRequest(String placeId) {
        GooglePlaceDetailRequest request = new GooglePlaceDetailRequest(_apiKey, placeId);
        return getRequest(request);
    }

    /**
     * @return The URL of the image, NOT the response of a get request made using this URL.
     */
    public String placePhotoUrl(String photoReference) {
        GooglePlacePhotoRequest request = new GooglePlacePhotoRequest(_apiKey, photoReference);
        return request.constructUrl();
    }

}
