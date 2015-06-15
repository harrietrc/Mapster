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

    public SuggestionItem(Suggestion suggestion) {
        _suggestion = suggestion;
    }
}
