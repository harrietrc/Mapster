package com.mapster.persistence;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mapster.R;
import com.mapster.itinerary.ItineraryItem;
import com.mapster.itinerary.SuggestionItem;
import com.mapster.itinerary.UserItem;
import com.mapster.itinerary.serialisation.FoursquareSuggestionInstanceCreator;
import com.mapster.itinerary.serialisation.ItineraryItemAdapter;
import com.mapster.itinerary.serialisation.SuggestionAdapter;
import com.mapster.suggestions.FoursquareSuggestion;
import com.mapster.suggestions.Suggestion;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Harriet on 6/16/2015.
 */
public class ItineraryDataSource {

    private SQLiteDatabase _database;
    private ItineraryHelper _helper;
    private Gson _gson;
    private String[] _allColumnsItineraryItem = {ItineraryHelper.COLUMN_ID,
            ItineraryHelper.COLUMN_ITINERARY_ID, ItineraryHelper.COLUMN_SERIALISED};

    public ItineraryDataSource(Context context) {
        _helper = new ItineraryHelper(context);
        _gson = new GsonBuilder().registerTypeAdapter(ItineraryItem.class,
                new ItineraryItemAdapter()).registerTypeAdapter(Suggestion.class,
                new SuggestionAdapter()).registerTypeAdapter(FoursquareSuggestion.class,
                new FoursquareSuggestionInstanceCreator(context))
                .excludeFieldsWithModifiers(Modifier.TRANSIENT, Modifier.STATIC).create();
    }

    public List<ItineraryItem> getUnsavedAndSavedItems(Context context) {

        String sharedPrefsName = context.getResources().getString(R.string.shared_prefs);
        String itineraryNamePrefs = context.getResources().getString(R.string.itinerary_name_prefs);
        SharedPreferences settings = context.getSharedPreferences(sharedPrefsName, 0);
        String currentItineraryName = settings.getString(itineraryNamePrefs, null);
        List<ItineraryItem> unsavedItems = getItemsByItineraryName(null);
        List<ItineraryItem> savedItems = getItemsByItineraryName(currentItineraryName);

        Map<String, ItineraryItem> savedItemsMap = new HashMap<>();
        for (ItineraryItem item : savedItems)
            savedItemsMap.put(item.getName(), item);
        for (ItineraryItem unsavedItem : unsavedItems) {
            UserItem savedItem = (UserItem) savedItemsMap.get(unsavedItem.getName());
            if (savedItem != null) {
                List<SuggestionItem> suggestions = ((UserItem) unsavedItem).getSuggestionItems();
                List<SuggestionItem> savedSuggestions = savedItem.getSuggestionItems();
                Set<SuggestionItem> combinedItems = new HashSet<>();
                combinedItems.addAll(suggestions); combinedItems.addAll(savedSuggestions);
                savedItem.replaceSuggestionItems(combinedItems);
                if (unsavedItem.getTime() != null)
                    savedItem.setDateTime(unsavedItem.getTime());
            } else {
                savedItems.add(1, unsavedItem);
            }
        }

        return savedItems;
    }

    public List<UserItem> getUnsavedAndSavedUserItems(Context context) {
        List<ItineraryItem> databaseItems = getUnsavedAndSavedItems(context);

        // Annoying cast - should really change schema
        List<UserItem> databaseUserItems = new ArrayList<>();
        for (ItineraryItem item : databaseItems)
            databaseUserItems.add((UserItem) item);

        return databaseUserItems;
    }

    public List<String> getAllNames() {
        Cursor cursor = queryItineraryNames();
        return cursorToItineraryNameList(cursor);
    }

    private List<ItineraryItem> cursorToItemList(Cursor cursor) {
        List<ItineraryItem> items = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            ItineraryItem item = cursorToItem(cursor);
            items.add(item);
            cursor.moveToNext();
        }
        cursor.close();
        return items;
    }

    private List<String> cursorToItineraryNameList(Cursor cursor) {
        List<String> itineraryNames = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            String name = cursorToItineraryName(cursor);
            itineraryNames.add(name);
            cursor.moveToNext();
        }
        cursor.close();
        return itineraryNames;
    }

    /**
     * Inserts multiple records into the database. Faster than doing each insert in separate
     * transactions
     */
    public void insertMultipleItineraryItems(Collection<? extends ItineraryItem> items) {
        try {
            _database.beginTransaction();
            for (ItineraryItem item: items)
                if (item instanceof UserItem) // Ignore SuggestionItems for now
                    insertItineraryItem(item);
            _database.setTransactionSuccessful();
        } finally {
            _database.endTransaction();
        }
    }

    /**
     * Used to insert into the ItineraryItems database (contains the places that make up the
     * itineraries) with an existing Itinerary ID - i.e. we want to save this as an itinerary.
     * @param items Itinerary items to insert
     * @param itineraryId
     */
    public void insertMultipleItineraryItems(Collection<? extends ItineraryItem> items, long itineraryId) {
        try {
            _database.beginTransaction();
            for (ItineraryItem item: items)
                insertItineraryItem(item, itineraryId);
            _database.setTransactionSuccessful();
        } finally {
            _database.endTransaction();
        }
    }

    /**
     * Creates a row in the Itinerary table
     * @param itineraryName The name this itinerary is labelled with (set by user)
     * @return The autogenerated ID for that row created
     */
    public long createAndGetItineraryId(String itineraryName) {
        ContentValues values = new ContentValues();
        values.put(ItineraryHelper.COLUMN_ITINERARY_NAME, itineraryName);
        return _database.insert(ItineraryHelper.TABLE_ITINERARY, null, values);
    }

    /**
     * Inserts an ItineraryItem into the database, serialising it using GSON.
     */
    private void insertItineraryItem(ItineraryItem item) {
        String serialisedItem = _gson.toJson(item, ItineraryItem.class);
        ContentValues values = new ContentValues();
        values.put(ItineraryHelper.COLUMN_SERIALISED, serialisedItem);
        _database.insert(ItineraryHelper.TABLE_ITINERARY_ITEM, null, values);
    }

    private void insertItineraryItem(ItineraryItem item, long itineraryId) {
        String serialisedItem = _gson.toJson(item, ItineraryItem.class);
        ContentValues values = new ContentValues();
        values.put(ItineraryHelper.COLUMN_SERIALISED, serialisedItem);
        values.put(ItineraryHelper.COLUMN_ITINERARY_ID, itineraryId);
        _database.insert(ItineraryHelper.TABLE_ITINERARY_ITEM, null, values);
    }

    public void deleteUnsavedItineraryItems() {
        String whereClause = ItineraryHelper.COLUMN_ITINERARY_ID + " is null";
        _database.delete(ItineraryHelper.TABLE_ITINERARY_ITEM, whereClause, null);
    }

    public List<ItineraryItem> getItemsByItineraryName(String itineraryName) {
        Cursor cursor;

        // Select serialised item from ItineraryItem where the itinerary name matches the one given
        if (itineraryName == null) {
            cursor = queryItemsWithNoItinerary();
        } else {
            cursor = queryItemsByItineraryName(itineraryName);
        }

        return cursorToItemList(cursor);
    }

    /**
     * Assumes a non-null itinerary name (see public version above)
     */
    private Cursor queryItemsByItineraryName(String itineraryName) {
        String query = "select " + formatAllItineraryItemColumns() + " from " +
                ItineraryHelper.TABLE_ITINERARY_ITEM+ "," + ItineraryHelper.TABLE_ITINERARY +
                " where " + ItineraryHelper.TABLE_ITINERARY + "." +
                ItineraryHelper.COLUMN_ITINERARY_ID + "=" + ItineraryHelper.TABLE_ITINERARY_ITEM
                + "." + ItineraryHelper.COLUMN_ITINERARY_ID + " and " +
                ItineraryHelper.TABLE_ITINERARY + "." + ItineraryHelper.COLUMN_ITINERARY_NAME +
                "=\""  + itineraryName + "\";";
        return _database.rawQuery(query, null);
    }

    /**
     * @return Cursor with all the names of itineraries in the database
     */
    private Cursor queryItineraryNames() {
        String query = "select " + ItineraryHelper.COLUMN_ITINERARY_NAME + " from " +
                ItineraryHelper.TABLE_ITINERARY;
        return _database.rawQuery(query, null);
    }

    /**
     * For cases in which items are not associated with an itinerary (not saved)
     */
    private Cursor queryItemsWithNoItinerary() {
        String query = "select " + formatAllItineraryItemColumns() + " from " +
                ItineraryHelper.TABLE_ITINERARY_ITEM + " where " + ItineraryHelper.COLUMN_ITINERARY_ID
                + " is null;";
        return _database.rawQuery(query, null);
    }

    /**
     * My select queries are retrieving all 3 columns for consistency, even though only the
     * one with the serialised item is really necessary. This just formats the column names for
     * those queries.
     */
    private String formatAllItineraryItemColumns() {
        String[] formattedColumns = new String[_allColumnsItineraryItem.length];
        for (int i=0; i<_allColumnsItineraryItem.length; i++)
            formattedColumns[i] = ItineraryHelper.TABLE_ITINERARY_ITEM + "." +
                    _allColumnsItineraryItem[i];
        return StringUtils.join(formattedColumns, ',');
    }

    /**
     * Deserialises the SerialisedItem field and returns that object. Bit of type information lost.
     */
    private ItineraryItem cursorToItem(Cursor cursor) {
        String serialisedItem = cursor.getString(2);
        ItineraryItem item = _gson.fromJson(serialisedItem, ItineraryItem.class);
        return item;
    }

    private String cursorToItineraryName(Cursor cursor) {
        return cursor.getString(0);
    }

    public void open() throws SQLException {
        _database = _helper.getWritableDatabase();
    }

    public boolean isOpen() {
        return _database != null && _database.isOpen();
    }

    public void close() {
        _helper.close();
    }
}
