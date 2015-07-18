package com.mapster.itinerary.persistence;

import android.os.AsyncTask;

import com.mapster.activities.MainActivity;
import com.mapster.itinerary.ItineraryItem;
import com.mapster.itinerary.SuggestionItem;
import com.mapster.itinerary.UserItem;

import java.util.ArrayList;
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
public class UpdateMainFromItineraryTask extends AsyncTask<Void, Void, Collection<SuggestionItem>> {

    private MainActivity _activity; // Dodgy as hell

    public UpdateMainFromItineraryTask(MainActivity activity) {
        _activity = activity;
    }

    @Override
    protected Collection<SuggestionItem> doInBackground(Void... params) {
        // TODO Improve this; it's very heavy-handed and messy.
        // Get the itinerary from the database and update the Activity's items here with any changes
        Map<String, SuggestionItem> suggestionItemsByMarkerId = _activity.getSuggestionItemsByMarkerId();
        Map<String, UserItem> userItemsByMarkerId = _activity.getUserItemsByMarkerId();
        ItineraryDataSource itineraryDataSource = _activity.getItineraryDatasource();

        // 'items' is a list of itinerary items from the DB - may be left over from last app run.
        if (suggestionItemsByMarkerId.isEmpty()) {
            // Don't try to load itinerary state from the DB - just return.
            return null;
        }

        List<ItineraryItem> dbItems = itineraryDataSource.getAllItems();
        Collection<UserItem> existingItems = userItemsByMarkerId.values();

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

        // Make a copy of the old ItineraryItems - quickfix for deserialisation issues (_userItem
        // field of Suggestionitem isn't deserialised - will look into fixing this)
        Map<Long, ItineraryItem> oldItems = new HashMap<>();
        oldItems.putAll(allItems);

        /*
        Swap out suggestions in deserialised UserItems for those here (which have markers),
        gradually updating the map of ItineraryItems to the updated items. Suggestions are not
        expected to be modified outside this activity (although any ItineraryItems may be)
        */
        for (ItineraryItem item: dbItems) {
            if (item instanceof UserItem) {
                UserItem userItem = (UserItem) item;
                for (SuggestionItem s : userItem.getSuggestionItems()) {
                    Long id = s.getId();
                    SuggestionItem old = (SuggestionItem) allItems.get(id);
                    if (old != null) {
                        s.setSuggestion(old.getSuggestion());
                        allItems.put(id, s);
                        updatedSuggestionIds.add(id);
                    }
                }
                allItems.put(item.getId(), item);
            }
        }

        // Figure out which suggestions were removed from the itinerary and re-flag them
        oldSuggestionIds.removeAll(updatedSuggestionIds);
        List<SuggestionItem> removedItems = new ArrayList<>();
        for (Long id: oldSuggestionIds) {
            SuggestionItem item = (SuggestionItem) allItems.get(id);
            item.setIsInItinerary(false);
            removedItems.add(item);
        }

        // Update UserItem map
        for (Map.Entry pair : userItemsByMarkerId.entrySet()) {
            Long id = ((UserItem) pair.getValue()).getId();
            userItemsByMarkerId.put((String) pair.getKey(), (UserItem) allItems.get(id));
        }

        // Update SuggestionItem map
        for (Map.Entry pair : suggestionItemsByMarkerId.entrySet()) {
            Long id = ((SuggestionItem) pair.getValue()).getId();
            SuggestionItem dbItem = (SuggestionItem) allItems.get(id);

            // Get the updated item from the DB if available, else use the item that's already there
            SuggestionItem item = dbItem == null ? (SuggestionItem) pair.getValue() : dbItem;

            // For some reason the nested UserItem isn't deserialised. Temporary work-around
            // TODO Consider removing that bidirectional relationship so this isn't necessary
            SuggestionItem oldSuggestion = (SuggestionItem) oldItems.get(item.getId());
            if (oldSuggestion == null)
                oldSuggestion = item;

            UserItem u = oldSuggestion.getUserItem();

            if (u != null) {
                long oldUserItemId = u.getId();
                item.setUserItem((UserItem) allItems.get(oldUserItemId));
                suggestionItemsByMarkerId.put((String) pair.getKey(), item);
            }
        }

        return removedItems;
    }

    @Override
    protected void onPostExecute(Collection<SuggestionItem> items) {
        // Reset the icons for the suggestions that were removed from the itinerary
        for (SuggestionItem s : items)
            _activity.setSuggestionItemMarker(s);
    }
}
