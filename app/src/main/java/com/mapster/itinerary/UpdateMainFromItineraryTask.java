package com.mapster.itinerary;

import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.annimon.stream.Stream;
import com.mapster.R;
import com.mapster.activities.MainActivity;
import com.mapster.persistence.ItineraryDataSource;

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
//        if (suggestionItemsByMarkerId.isEmpty()) {
//            // Don't try to load itinerary state from the DB - just return.
//            return null;
//        }

        String sharedPrefsName = _activity.getResources().getString(R.string.shared_prefs);
        String itineraryNamePrefs = _activity.getResources().getString(R.string.itinerary_name_prefs);
        SharedPreferences settings = _activity.getSharedPreferences(sharedPrefsName, 0);
        String currentItineraryName = settings.getString(itineraryNamePrefs, null);
        List<ItineraryItem> unsavedItems = itineraryDataSource.getItemsByItineraryName(null);
        List<ItineraryItem> savedItems = itineraryDataSource.getItemsByItineraryName(currentItineraryName);

        // TODO Hacky - Gets items from database, combining 'null' itinerary and current named one
        Map<String, ItineraryItem> savedItemsMap = new HashMap<>();
        for (ItineraryItem item : savedItems)
            savedItemsMap.put(item.getName(), item);
        for (ItineraryItem unsavedItem : unsavedItems) {
            ItineraryItem savedItem = savedItemsMap.get(unsavedItem.getName());
            if (savedItem != null) {
                List<SuggestionItem> suggestions = ((UserItem) unsavedItem).getSuggestionItems();
                List<SuggestionItem> savedSuggestions = ((UserItem) savedItem).getSuggestionItems();
                Set<SuggestionItem> combinedItems = new HashSet<>();
                combinedItems.addAll(suggestions); combinedItems.addAll(savedSuggestions);
                ((UserItem) savedItem).replaceSuggestionItems(combinedItems);
                if (unsavedItem.getTime() != null)
                    savedItem.setDateTime(unsavedItem.getTime());
            }
        }

        Collection<UserItem> existingItems = userItemsByMarkerId.values();

        // Sets of suggestion IDs so that we can tell which ones were deleted from the itinerary
        Set<Long> updatedSuggestionIds = new HashSet<>();

        // Make a set of existing ItineraryItems (from MainActivity) - both SuggestionItems and UserItems
        Set<ItineraryItem> allItems = new HashSet<>();
        for (UserItem oldItem: existingItems) {
            allItems.add(oldItem);
            allItems.addAll(oldItem.getSuggestionItems());
        }


        // Make a copy of the old ItineraryItems - quickfix for deserialisation issues (_userItem
        // field of Suggestionitem isn't deserialised - will look into fixing this) TODO
        Set<ItineraryItem> oldItems = new HashSet<>();
        oldItems.addAll(allItems);

        /*
        Swap out suggestions in deserialised UserItems for those here (which have markers),
        gradually updating the map of ItineraryItems to the updated items. Suggestions are not
        expected to be modified outside this activity (although any ItineraryItems may be)
        */
        // Goes through saved itinerary items in DB
        for (ItineraryItem item: savedItems) {
            if (item instanceof UserItem) {
                UserItem userItem = (UserItem) item;
                // userItem is a UserItems retrieved from the DB
                for (SuggestionItem s : userItem.getSuggestionItems()) {
                    for (ItineraryItem old : oldItems)
                        if (old.equals(s)) { // Compares type, name, location
                            s.setSuggestion(((SuggestionItem) old).getSuggestion());
                            allItems.add(s);
                            break;
                        }
                }
                allItems.add(item);
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
        if (items != null)
            for (SuggestionItem s : items)
                _activity.updateSuggestionItem(s);
    }
}
