package com.mapster.suggestions;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

/**
 * Created by Harriet on 3/22/2015.
 */
public abstract class Suggestion {
    protected Marker _marker;
    protected boolean _isClicked;
    protected Integer _priceLevel;

    /**
     * Accesses various web API's in order to populate this suggestion with information. Currently
     * retrieves information from Google Places and Expedia.
     */
    public abstract void requestSuggestionInfo(Context context);

    /**
     * Returns a string for the snippet of a marker's infowindow
     */
    public abstract String getInfoWindowString();

    /**
     * Returns the url to a photo illustrating the suggestion
     */
    public abstract String getThumbnailUrl(Context context);

    /**
     * Returns the name of the suggestion place.
     */
    public abstract String getName();

    /**
     * Returns a price level between 1 and 3 (inclusive), where 3 is the most expensive level.
     */
    public Integer getParsedPriceLevel() {
        return parsePriceLevel(getPriceLevel());
    }

    public abstract Integer getPriceLevel();

    /**
     * Returns the star rating (out of 5)
     */
    public abstract float getRating();

    // TODO Change category to an enum
    public abstract String getCategory();

    /**
     * Stored in different places depending on the suggestion
     */
    public abstract LatLng getLocation();

    public void setMarker(Marker marker) {
        _marker = marker;
    }

    public Marker getMarker() {
        return _marker;
    }

    public boolean isClicked() {
        return _isClicked;
    }

    public void setClicked(boolean isClicked) {
        _isClicked = isClicked;
    }

    /**
     * Price levels are saved in place details as either null, 1, 2, or 3 (Google reports them as
     * 0-4, but I don't think we need that many). This does that parsing.
     * @return Parsed Integer: null, 1, 2, 3 (where 3 is expensive)
     */
    public Integer parsePriceLevel(Integer level) {
        if (level == null) {
            return null;
        } else if (level < 2) {
            // Cheap or free
            return new Integer(1);
        } else if (level < 4) {
            // Moderate to expensive
            return new Integer(2);
        } else {
            // Catch all, but not expected to be more than 4 (very expensive)
            return new Integer(3);
        }
    }

}
