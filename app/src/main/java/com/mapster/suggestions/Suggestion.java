package com.mapster.suggestions;

import com.google.android.gms.maps.model.Marker;
import com.mapster.places.GooglePlace;
import com.mapster.places.GooglePlaceDetail;

/**
 * Created by Harriet on 3/22/2015.
 */
public class Suggestion {
    private Marker _marker;
    private String _category;
    private boolean _isClicked;

    // Save data about the Google Place that this marker represents
    private GooglePlace _place;

    public Suggestion(Marker marker, GooglePlace place, String category) {
        _marker = marker;
        // TODO Set category from GooglePlace as well
        _category = category;
        _isClicked = false;
        _place = place;
    }

    public void setPlaceDetail(GooglePlaceDetail detail) {
        _place.setDetail(detail);
    }

    public String getPhotoReference() {
        return _place.photoReference;
    }

    public Marker getMarker() {
        return _marker;
    }

    public float getRating() {
        return _place.rating;
    }

    public String getCategory() {
        return _category;
    }

    public String getPlaceId() {
        return _place.id;
    }

    public boolean isClicked() {
        return _isClicked;
    }

    public void setClicked(boolean isClicked) {
        _isClicked = isClicked;
    }

}
