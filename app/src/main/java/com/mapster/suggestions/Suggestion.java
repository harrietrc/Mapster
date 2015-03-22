package com.mapster.suggestions;

import com.google.android.gms.maps.model.Marker;

/**
 * Created by Harriet on 3/22/2015.
 */
public class Suggestion {
    private Marker _marker;
    private String _placeId;
    private float _rating;
    private String _category;

    public Suggestion(Marker marker, String placeId, String category, float rating) {
        _marker = marker;
        _placeId = placeId;
        _category = category;
        _rating = rating;
    }

    public Marker getMarker() {
        return _marker;
    }

    public float getRating() {
        return _rating;
    }

    public String getCategory() {
        return _category;
    }

    public String getPlaceId() {
        return _placeId;
    }
}
