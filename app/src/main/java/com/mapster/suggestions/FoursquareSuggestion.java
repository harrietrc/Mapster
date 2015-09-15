package com.mapster.suggestions;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;
import com.mapster.api.foursquare.FoursquareVenue;
import com.mapster.priceestimation.MealPriceEstimate;

/**
 * Created by Harriet on 6/11/2015.
 */
public class FoursquareSuggestion extends Suggestion {

    private FoursquareVenue _venue;
    private transient MealPriceEstimate _priceEstimator;

    public FoursquareSuggestion(FoursquareVenue venue, Context context) {
        _venue = venue;
        _priceEstimator = new MealPriceEstimate(context);
    }

    public String getCountryCode() {
        return _venue.getCountryCode();
    }

    public String getCity() {
        return _venue.getCity();
    }

    @Override
    public Double getCostPerPerson() {
        return _priceEstimator.estimateMealPrice(this);
    }

    @Override
    public String getCurrencyCode() {
        return _priceEstimator.getCurrencyCode(_venue.getCountryCode());
    }

    @Override
    public String getWebsite() {
        return _venue.getWebsite();
    }

    @Override
    public String getPhoneNumber() {
        return _venue.getPhoneNumber();
    }

    @Override
    public void requestSuggestionInfo(Context context) {
        // Not necessary at the moment (don't need more detailed information)
    }

    @Override
    public String getCurrencySymbol() {
        return _priceEstimator.getCurrencySymbol(_venue.getCountryCode());
    }

    @Override
    public String getInfoWindowString() {
        String info = _venue.toString();
        info += String.format("\n~%s%.2f per person", getCurrencySymbol(), getCostPerPerson());
        return info;
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
