package com.mapster.itinerary;

import android.os.AsyncTask;

import com.mapster.activities.MainActivity;
import com.mapster.persistence.ItineraryDataSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by Harriet on 6/22/2015. Updates data structures of ItineraryItems in the MainActivity
 * with new values that were set in other activities, preserving references to markers.
 */
public class UpdateMainFromItineraryTask extends AsyncTask<Void, Void, Collection<SuggestionItem>> {

    private MainActivity _activity;
    private boolean _updateFromPlaces;

    public UpdateMainFromItineraryTask(MainActivity activity) {
        _activity = activity;
    }

    @Override
    protected Collection<SuggestionItem> doInBackground(Void... params) {

        _updateFromPlaces = _activity.getIntent().getBooleanExtra("MOVED_FROM_PLACES", false);
        _activity.getIntent().removeExtra("MOVED_FROM_PLACES");

        // If moved from PlacesActivity, populate the ItineraryItem lists
        if (_updateFromPlaces)
            _activity.getDataFromPlaceActivity();

        // Get the itinerary from the database and update the Activity's items here with any changes
        Map<String, SuggestionItem> suggestionItemsByMarkerId = _activity.getSuggestionItemsByMarkerId();
        Map<String, UserItem> userItemsByMarkerId = _activity.getUserItemsByMarkerId();

        // Items from the database
        ItineraryDataSource itineraryDataSource = _activity.getItineraryDatasource();

        // Must open the database if it isn't already open
        if (!itineraryDataSource.isOpen())
            itineraryDataSource.open();

        List<ItineraryItem> savedItems = itineraryDataSource.getUnsavedAndSavedItems(_activity);

        // Keeps track of SuggestionItems that have been deleted from the itinerary
        List<SuggestionItem> deletedItems = new ArrayList<>();

        // Update MainActivity's collections of user and suggestion items
        for (ItineraryItem item : savedItems) {
            if (item instanceof UserItem) {
                UserItem userItem = (UserItem) item;
                if (userItem.getMarkerId() != null)
                    userItemsByMarkerId.put(userItem.getMarkerId(), userItem);

                for (SuggestionItem suggestionItem : userItem.getSuggestionItems()) {
                    if (!suggestionItem.isInItinerary())
                        deletedItems.add(suggestionItem);
                    if (suggestionItem.getMarkerId() != null)
                        suggestionItemsByMarkerId.put(suggestionItem.getMarkerId(), suggestionItem);
                }
            }
        }

        return deletedItems;
    }

    @Override
    protected void onPostExecute(Collection<SuggestionItem> items) {
        // Reset the icons for the suggestions that were removed from the itinerary
        if (items != null)
            for (SuggestionItem s : items)
                _activity.updateSuggestionItem(s);
        if (_updateFromPlaces) {
            _activity.moveCamera();
            _activity.setupMapFromItinerary();
        }
    }
}
