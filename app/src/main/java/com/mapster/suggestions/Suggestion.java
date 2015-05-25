package com.mapster.suggestions;

import android.content.Context;

import com.google.android.gms.maps.model.Marker;

/**
 * Created by Harriet on 3/22/2015.
 */
public abstract class Suggestion {
    protected Marker _marker;
    protected String _category; // TODO change this to an enum
    protected boolean _isClicked;

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
    public abstract String getPhotoReference();

    /**
     * Returns the name of the suggestion place.
     */
    public abstract String getName();

    /**
     * Returns a price level between 1 and 4 (inclusive), where 4 is the most expensive level.
     */
    public abstract Integer getPriceLevel();

    /**
     * Returns the star rating (out of 5)
     */
    public abstract float getRating();

    public Marker getMarker() {
        return _marker;
    }

    public boolean isClicked() {
        return _isClicked;
    }

    public void setClicked(boolean isClicked) {
        _isClicked = isClicked;
    }

}
