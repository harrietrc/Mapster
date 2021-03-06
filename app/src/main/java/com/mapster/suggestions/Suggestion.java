package com.mapster.suggestions;

import android.content.Context;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.mapster.itinerary.SuggestionItem;

import java.util.Currency;

/**
 * Created by Harriet on 3/22/2015.
 */
public abstract class Suggestion {
    protected boolean _isClicked;

    protected transient SuggestionItem _item; // These references are getting messy

    // Budgeting
    protected String _currencySymbol;

    private String _markerId;

    public SuggestionItem getItem() {
        return _item;
    }

    public void setItem(SuggestionItem item) {
        _item = item;
    }

    public String getMarkerId() {
        return _markerId;
    }

    public void setMarkerId(String markerId) {
        _markerId = markerId;
    }

    public abstract Double getCostPerPerson();

    public abstract String getCurrencyCode();

    public abstract String getWebsite();

    public abstract String getPhoneNumber();

    public abstract String getPriceString(Context context);

    public abstract void convertCost(String userCurrencyCode, String localCurrencyCode,
                                     TextView conversionView, Marker markerToRefresh);

    /**
     * Returns the currency symbol that corresponds to the currency code, if it is non-null and
     * valid
     * @return The currency symbol as a string
     */
    public String getCurrencySymbol() {
        if (_currencySymbol == null) {
            String currencyCode = getCurrencyCode();
            if (currencyCode != null) {
                // Try to retrieve the symbol that corresponds with the currency code
                try {
                    Currency c = Currency.getInstance(currencyCode);
                    return c.getSymbol();
                } catch (IllegalArgumentException e) {
                    // Currency was invalid - fall through to dollars
                }
            }
        } else {
            // Assume the currency symbol is already set to something valid
            return _currencySymbol;
        }
        // Falls back to dollars if there is no match
        return "$";
    }

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

    /**
     * Returns a price level between 1 and 4
     */
    public abstract Integer getPriceLevel();

    /**
     * Returns the star rating (out of 5)
     */
    public abstract float getRating();

    public abstract String getCategory();

    /**
     * Stored in different places depending on the suggestion
     */
    public abstract LatLng getLocation();

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
