package com.mapster.suggestions;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;
import com.mapster.foursquare.FoursquareVenue;

/**
 * Created by Harriet on 6/11/2015.
 */
public class FoursquareSuggestion extends Suggestion {

    private FoursquareVenue _venue;

    public FoursquareSuggestion(FoursquareVenue venue) {
        _venue = venue;
    }

    @Override
    public Double getCostPerPerson() {
        return null;
    }

    @Override
    public String getCurrencyCode() {
        return _venue.getCurrencyCode();
    }

    @Override
    public void requestSuggestionInfo(Context context) {
        // Not necessary at the moment (don't need more detailed information)
    }

    @Override
    public String getInfoWindowString() {
        return _venue.toString();
    }

    @Override
    public String getThumbnailUrl(Context context) {
        return _venue.getImageUrl();
    }

    @Override
    public String getName() {
        return _venue.getName();
    }

    @Override
    public Integer getPriceLevel() {
        return _venue.getPriceLevel();
    }

    @Override
    public float getRating() {
        return _venue.getRating();
    }

    @Override
    public String getCategory() {
        return "dining";
    }

    @Override
    public LatLng getLocation() {
        return _venue.getLocation();
    }
}
