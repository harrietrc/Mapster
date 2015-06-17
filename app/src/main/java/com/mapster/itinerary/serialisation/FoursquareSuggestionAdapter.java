package com.mapster.itinerary.serialisation;

import android.content.Context;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mapster.suggestions.FoursquareSuggestion;

import java.lang.reflect.Type;

/**
 * Created by Harriet on 6/17/2015. Required to recreate the MealPriceEstimator, which is marked
 * as transient.
 */
public class FoursquareSuggestionAdapter {//} extends SuggestionAdapter {

    private Context _context;

    public FoursquareSuggestionAdapter(Context context) {
        _context = context;
    }

    public FoursquareSuggestion deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        // Use the custom Suggestion deserialiser to get the deserialisation result
        // Results in an infinite loop! Can't call context.deserialize() here - TODO fix later
        FoursquareSuggestion suggestion = context.deserialize(json, typeOfT);
        // Reinitialise the MealPriceEstimator (was transient)
        suggestion.setPriceEstimator(_context);
        return suggestion;
    }

}
