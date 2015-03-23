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
    private String _photoReference;
    private boolean _isClicked;

    public Suggestion(Marker marker, String placeId, String category, float rating, String photoReference) {
        _marker = marker;
        _placeId = placeId;
        _category = category;
        _rating = rating;
        _photoReference = photoReference;
        _isClicked = false;
    }

    public String getPhotoReference() {
        return _photoReference;
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

    public boolean isClicked() {
        return _isClicked;
    }

    public void setClicked(boolean isClicked) {
        _isClicked = isClicked;
    }

}
