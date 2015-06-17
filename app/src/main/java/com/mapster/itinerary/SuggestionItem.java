package com.mapster.itinerary;

import android.content.Context;

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

    // The user-defined place this suggestion is associated with. Could move to Suggestion?
    // This means that there is a bidirectional aggregation relationship between every UserItem
    // object and the SuggestionItems associated with it - which does not seem brilliant.
    private transient UserItem _userItem;

    public SuggestionItem(Suggestion suggestion, UserItem userItem) {
        _suggestion = suggestion;
        _userItem = userItem;
        _multiplier = 1;
    }

    public Double getActualCost() {
        return _actualCost;
    }

    public void setActualCost(double actualCost) {
        _actualCost = actualCost;
    }

    public void setMultiplier(int multiplier) {
        _multiplier = multiplier;
    }

    // TODO Remove Context argument. See Suggestion.getCostPerPerson()
    public Double getTotalCost(Context context) {
        Double cost = _suggestion.getCostPerPerson(context);
        return cost == null ? null : cost * _multiplier;
    }

    public Suggestion getSuggestion() {
        return _suggestion;
    }

    public UserItem getUserItem() {
        return _userItem;
    }
}
