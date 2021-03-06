package com.mapster.suggestions;

import android.content.Context;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.mapster.api.googleplaces.GooglePlaceDetailTask;
import com.mapster.places.GooglePlace;
import com.mapster.places.GooglePlaceDetail;

/**
 * Created by Harriet on 5/25/2015.
 */
public class GooglePlaceSuggestion extends Suggestion {

    // Save data about the Google Place that this marker represents
    private GooglePlace _place;
    private String _category;

    public GooglePlaceSuggestion(GooglePlace place) {
        _category = "attractions";
        _isClicked = false;
        _place = place;
    }

    @Override
    public LatLng getLocation() {
        return _place.getLatLng();
    }

    public float getRating() {
        return _place.getRating();
    }

    @Override
    public String getCategory() {
        return _category;
    }

    public String getPlaceId() {
        return _place.getId();
    }

    public void setPlaceDetail(GooglePlaceDetail detail) {
        _place.setDetail(detail);
    }

    @Override
    public String getThumbnailUrl(Context context) {
        return _place.getThumbnailUrl(context);
    }

    @Override
    public Double getCostPerPerson() {
        /*
        Google offers no pricing information and attractions are too varied to make a meaningful
        estimate
        */
        return null;
    }

    @Override
    public String getCurrencyCode() {
        return null; // No pricing info = no currency code required
    }

    @Override
    public String getWebsite() {
        return _place.getWebsite();
    }

    @Override
    public String getPhoneNumber() {
        return _place.getPhoneNumber();
    }

    @Override
    public String getPriceString(Context context) {
        return null;
    }

    @Override
    public void convertCost(String userCurrencyCode, String localCurrencyCode,
                            TextView conversionView, Marker markerToRefresh) {
    }

    @Override
    public void requestSuggestionInfo(Context context) {
        GooglePlaceDetailTask task = new GooglePlaceDetailTask(context);
        task.execute(this);
    }

    /**
     * Returns a formatted string representation of the place detail and other suggestion state
     * that should be displayed to the user in a marker infowindow.
     * @return
     */
    public String getInfoWindowString() {
        return _place.toString();
    }

    public String getName() {
        return _place.getName();
    }

    @Override
    public Integer getPriceLevel() {
        return _place.getPriceLevel();
    }
}
