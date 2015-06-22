package com.mapster.itinerary.persistence;

import android.os.AsyncTask;

import com.mapster.itinerary.ItineraryItem;
import com.mapster.itinerary.SuggestionItem;
import com.mapster.itinerary.UserItem;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Harriet on 6/22/2015. Updates data structures of ItineraryItems in the MainActivity
 * with new values that were set in other activities, preserving references to markers.
 */
public class UpdateMainFromItineraryTask extends AsyncTask<Void, Void, Void> {

    private Map<String, SuggestionItem> _suggestionItemsByMarkerId;
    private Map<String, UserItem> _userItemsByMarkerId;
    private ItineraryDataSource _itineraryDataSource;

    public UpdateMainFromItineraryTask(Map<String, SuggestionItem> suggestionItemsByMarkerId,
                                       Map<String, UserItem> userItemsByMarkerId,
                                       ItineraryDataSource itineraryDataSource) {
        _suggestionItemsByMarkerId = suggestionItemsByMarkerId;
        _userItemsByMarkerId = userItemsByMarkerId;
        _itineraryDataSource = itineraryDataSource;
    }

    @Override
    protected Void doInBackground(Void... params) {
        // TODO Improve this; it's very heavy-handed and messy. At least make it a task.
        // Get the itinerary from the database and update the Activity's items here with any changes

        // 'items' is a list of itinerary items from the DB - may be left over from last app run.
        // TODO: Repopulate map from DB?
        if (_suggestionItemsByMarkerId.isEmpty()) {
            // Don't try to load itinerary state from the DB - just return.
            return null;
        }

        List<ItineraryItem> dbItems = _itineraryDataSource.getAllItems();
        Collection<UserItem> existingItems = _userItemsByMarkerId.values();

        // Sets of suggestion IDs so that we can tell which ones were deleted from the itinerary
        Set<Long> oldSuggestionIds = new HashSet<>();
        Set<Long> updatedSuggestionIds = new HashSet<>();

        // Make a map where existing ItineraryItems are keyed by ID (row ID from database)
        Map<Long, ItineraryItem> allItems = new HashMap<>();
        for (UserItem oldItem: existingItems) {
            allItems.put(oldItem.getId(), oldItem);
            for (SuggestionItem s : oldItem.getSuggestionItems()) {
                long id = s.getId();
                allItems.put(id, s);
                oldSuggestionIds.add(id);
            }
        }

        /*
        Swap out suggestions in deserialised UserItems for those here (which have markers),
        gradually updating the map of ItineraryItems to the updated items. Suggestions are not
        expected to be modified outside this activity (although any ItineraryItems may be)
        */
        for (ItineraryItem item: dbItems) {
            if (item instanceof UserItem) {
                for (SuggestionItem s : (((UserItem) item).getSuggestionItems())) {
                    Long id = s.getId();
                    SuggestionItem old = (SuggestionItem) allItems.get(id);
                    s.setSuggestion(old.getSuggestion());
                    allItems.put(id, s);
                    updatedSuggestionIds.add(id);
                }
                allItems.put(item.getId(), item);
            }
        }

        // Figure out which suggestions were removed from the itinerary and re-flag them
        oldSuggestionIds.removeAll(updatedSuggestionIds);
        for (Long id: oldSuggestionIds)
            ((SuggestionItem) allItems.get(id)).setIsInItinerary(false);

        // Update UserItem map
        for (Map.Entry pair : _userItemsByMarkerId.entrySet()) {
            Long id = ((UserItem) pair.getValue()).getId();
            _userItemsByMarkerId.put((String) pair.getKey(), (UserItem) allItems.get(id));
        }

        // Update SuggestionItem map
        for (Map.Entry pair : _suggestionItemsByMarkerId.entrySet()) {
            Long id = ((SuggestionItem) pair.getValue()).getId();
            SuggestionItem dbItem = (SuggestionItem) allItems.get(id);
            // Get the updated item from the DB if available, else use the item that's already there
            SuggestionItem item = dbItem == null ? (SuggestionItem) pair.getValue() : dbItem;
            _suggestionItemsByMarkerId.put((String) pair.getKey(), item);
        }
        return null;
    }
}
