package com.mapster.itinerary;

import com.mapster.suggestions.Suggestion;

/**
 * Created by Harriet on 6/12/2015.
 * Intended to correspond with suggestions added from the MainActivity. Should be able to be
 * exported to / imported from a database.
 * Separate from Suggestion (and subclasses) because it is situated as part of an itinerary, and
 * thus has data specific to the user's settings (e.g. number of people, time, etc.)
 * TODO Add number of people, time, etc...
 */
public class SuggestionItem extends ItineraryItem {
    // Suggestion corresponding with this itinerary item
    private Suggestion _suggestion;

    // The user-defined place this suggestion is associated with. Could move to Suggestion?
    // This means that there is a bidirectional aggregation relationship between every UserItem
    // object and the SuggestionItems associated with it - which does not seem brilliant.
    private transient UserItem _userItem;

    public SuggestionItem(Suggestion suggestion, UserItem userItem) {
        _suggestion = suggestion;
        _userItem = userItem;
    }

    public Suggestion getSuggestion() {
        return _suggestion;
    }

    public UserItem getUserItem() {
        return _userItem;
    }
}