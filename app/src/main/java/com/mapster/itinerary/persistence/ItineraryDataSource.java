package com.mapster.itinerary.persistence;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mapster.itinerary.ItineraryItem;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Harriet on 6/16/2015.
 */
public class ItineraryDataSource {

    private SQLiteDatabase _database;
    private ItineraryHelper _helper;
    private Gson _gson;
    private String[] _allColumnsItineraryItem = {ItineraryHelper.COLUMN_ID,
            ItineraryHelper.COLUMN_SERIALISED};

    public ItineraryDataSource(Context context) {
        _helper = new ItineraryHelper(context);
        _gson = new GsonBuilder().excludeFieldsWithModifiers(Modifier.TRANSIENT).create();
    }

    /**
     * Returning the ItineraryItem type means type information is lost. Will necessitate instanceof
     * checks later.
     */
    public List<ItineraryItem> getAllItems() {
        List<ItineraryItem> items = new ArrayList<>();

        Cursor cursor = _database.query(ItineraryHelper.TABLE_ITINERARY_ITEM,
                _allColumnsItineraryItem, null, null, null, null, null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            ItineraryItem item = cursorToItem(cursor);
            items.add(item);
            cursor.moveToNext();
        }
        cursor.close();
        return items;
    }

    /**
     * Inserts an ItineraryItem into the database, serialising it using GSON.
     */
    private void insertItineraryItem(ItineraryItem item) {
        String serialisedItem = _gson.toJson(item);
        System.out.println(serialisedItem); // debugging
        ContentValues values = new ContentValues();
        values.put(ItineraryHelper.COLUMN_SERIALISED, serialisedItem);
        _database.insert(ItineraryHelper.TABLE_ITINERARY_ITEM, null, values);
    }

    /**
     * Deserialises the SerialisedItem field and returns that object. Bit of type information lost.
     */
    private ItineraryItem cursorToItem(Cursor cursor) {
        String serialisedItem = cursor.getString(1);
        ItineraryItem item = _gson.fromJson(serialisedItem, ItineraryItem.class);
        return item;
    }

    public void open() throws SQLException {
        _database = _helper.getWritableDatabase();
    }

    public void close() {
        _helper.close();
    }
}
