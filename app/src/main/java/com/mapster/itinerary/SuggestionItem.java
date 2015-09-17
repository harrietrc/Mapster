package com.mapster.itinerary;

import com.google.android.gms.maps.model.LatLng;
import com.mapster.suggestions.Suggestion;

/**
 * Created by Harriet on 6/12/2015.
 * Intended to correspond with suggestions added from the MainActivity. Should be able to be
 * exported to / imported from a database.
 * Separate from Suggestion (and subclasses) because it is situated as part of an itinerary, and
 * thus has data specific to the user's settings (e.g. number of people, time, etc.)
 */
public class SuggestionItem extends ItineraryItem {
    // Suggestion corresponding with this itinerary item
    private Suggestion _suggestion;

    private int _multiplier;
    private Double _actualCost; // Entered by the user
    private boolean _isInItinerary;

    // The user-defined place this suggestion is associated with. Could move to Suggestion?
    // This means that there is a bidirectional aggregation relationship between every UserItem
    // object and the SuggestionItems associated with it - which does not seem brilliant.
    private transient UserItem _userItem;

    public SuggestionItem(Suggestion suggestion, UserItem userItem) {
        _isInItinerary = false;
        _suggestion = suggestion;
        _userItem = userItem;
        _multiplier = 1;
    }

    // TODO Not always accurate! But will be most of the time... e.g. what if suggestions are
    // across borders? They shouldn't be - telling a user to cross a political border is a bit dodgy.
    @Override
    public String getCountryCode() {
        return _userItem.getCountryCode();
    }

    public void setUserItem(UserItem item) {
        _userItem = item;
    }

    public int getMultiplier() {
        return _multiplier;
    }

    public boolean isInItinerary() {
        return _isInItinerary;
    }

    public void setIsInItinerary(boolean isInItinerary) {
        _isInItinerary = isInItinerary;
    }

    public Double getActualCost() {
        return _actualCost;
    }

    public void setActualCost(Double actualCost) {
        _actualCost = actualCost;
    }

    public void setMultiplier(int multiplier) {
        _multiplier = multiplier;
    }

    public Double getTotalCost() {
        Double cost = _suggestion.getCostPerPerson();
        return cost == null ? null : cost * _multiplier;
    }

    public Suggestion getSuggestion() {
        return _suggestion;
    }

    public void setSuggestion(Suggestion suggestion) {
        _suggestion = suggestion;
    }

    public UserItem getUserItem() {
        return _userItem;
    }

    @Override
    public String getName() {
        return _suggestion.getName();
    }

    @Override
    public LatLng getLocation() {
        return _suggestion.getLocation();
    }
}
