package com.mapster.persistence;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Harriet on 6/16/2015.
 */
public class ItineraryHelper extends SQLiteOpenHelper {

    /*
        Currently everything is in one big table. Change schema if we want to have columns
        corresponding with fields (to make it searchable) or if we need different information for
        each ItineraryItem subclass. At the moment things are serialised with gson, which is
        convenient but not very future-proof
    */

    public static final String TABLE_ITINERARY_ITEM = "ItineraryItem";
    public static final String COLUMN_ID = "Id";
    public static final String COLUMN_SERIALISED = "SerialisedItem";

    public static final String TABLE_ITINERARY = "Itinerary";
    public static final String COLUMN_ITINERARY_ID = "ItineraryId";
    public static final String COLUMN_ITINERARY_NAME = "ItineraryName";

    private static final String DATABASE_NAME = "itinerary.db";
    private static final int DATABASE_VERSION = 1;

    private static final String CREATE_ITINERARY_TABLE = "create table if not exists " +
            TABLE_ITINERARY + "(" + COLUMN_ITINERARY_ID + " integer primary key autoincrement, "
            + COLUMN_ITINERARY_NAME + " text not null);";
    private static final String CREATE_ITINERARY_ITEM_TABLE = "create table if not exists " +
            TABLE_ITINERARY_ITEM + "(" + COLUMN_ID + " integer primary key autoincrement, " +
            COLUMN_ITINERARY_ID + " integer," + COLUMN_SERIALISED + " text not null, foreign key("
            + COLUMN_ITINERARY_ID + ") " + "references " + TABLE_ITINERARY + "(" +
            COLUMN_ITINERARY_ID + "));";

    public ItineraryHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_ITINERARY_TABLE);
        db.execSQL(CREATE_ITINERARY_ITEM_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(ItineraryHelper.class.getName(), "Upgrading database from version " + oldVersion +
                " to " + newVersion + ", which will destroy all old data");
        db.execSQL("drop table if exists " + TABLE_ITINERARY_ITEM);
        onCreate(db);
    }
}
