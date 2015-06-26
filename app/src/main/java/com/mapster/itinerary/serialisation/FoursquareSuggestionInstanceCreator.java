package com.mapster.itinerary.serialisation;

import android.content.Context;

import com.google.gson.InstanceCreator;
import com.mapster.suggestions.FoursquareSuggestion;

import java.lang.reflect.Type;

/**
 * Created by Harriet on 6/17/2015. Required to recreate the MealPriceEstimator, which is marked
 * as transient.
 */
public class FoursquareSuggestionInstanceCreator implements InstanceCreator<FoursquareSuggestion> {

    private Context _context;

    public FoursquareSuggestionInstanceCreator(Context context) {
        _context = context;
    }

    @Override
    public FoursquareSuggestion createInstance(Type type) {
        return new FoursquareSuggestion(null, _context);
    }
}
