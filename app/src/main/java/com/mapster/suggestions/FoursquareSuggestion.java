package com.mapster.suggestions;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;
import com.mapster.foursquare.FoursquareVenue;
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
    public Double getCostPerPerson(Context context) {
        // TODO Temporary! Sorry. For serialisation - see superclass comment.
        if (_priceEstimator == null)
            _priceEstimator = new MealPriceEstimate(context);
        return _priceEstimator.estimateMealPrice(this);
    }

    /**
     * This is used during deserialisation to reinitialise _priceEstimator (transient)
     */
    public void setPriceEstimator(Context context) {
        _priceEstimator = new MealPriceEstimate(context);
    }

    @Override
    public String getCurrencyCode(Context context) {
        if (_priceEstimator == null)
            _priceEstimator = new MealPriceEstimate(context);
        return _priceEstimator.getCurrencyCode(_venue.getCountryCode());
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
