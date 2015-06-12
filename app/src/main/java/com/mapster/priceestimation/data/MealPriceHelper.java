package com.mapster.priceestimation.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Harriet on 6/12/2015.
 */
public class MealPriceHelper extends SQLiteOpenHelper {

    // Country table: average price for a meal and currency code
    public static final String TABLE_COUNTRY = "Country";
    public static final String COLUMN_COUNTRY_CODE = "CountryCode";
    public static final String COLUMN_MEAL_PRICE = "MealPrice";
    public static final String COLUMN_CURRENCY_CODE = "CurrencyCode";

    // City table: average price for a meal and country
    public static final String TABLE_CITY = "City";
    public static final String COLUMN_CITY_NAME = "CityName";

    private static final String DATABASE_NAME = "mealprices.db";
    private static final int DATABASE_VERSION = 1;

    // True if database needs to be populated
    private boolean _isPopulated;

    // TODO Unlikely to be the best way of doing things. Should import values from file in onCreate()
    public boolean isPopulated() {
        return _isPopulated;
    }
    public void setPopulated() {
        _isPopulated = true;
    }

    private static final String CREATE_COUNTRY = "create table " + TABLE_COUNTRY + "(" +
            COLUMN_COUNTRY_CODE + " text primary key not null, " + COLUMN_MEAL_PRICE + " real, "
            + COLUMN_CURRENCY_CODE + " text not null);";
    private static final String CREATE_CITY = "create table " + TABLE_CITY + "(" +
    COLUMN_CITY_NAME + " text primary key not null, " + COLUMN_MEAL_PRICE + " real, " +
            COLUMN_COUNTRY_CODE + " text not null, foreign key(" + COLUMN_COUNTRY_CODE +
            ") references " + TABLE_COUNTRY + "(" + COLUMN_COUNTRY_CODE + "));";


    public MealPriceHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        _isPopulated = true; // Assume true until onCreate() called
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        // TODO Remove onOpen() when the database is complete (just repopulates it with each run)
        db.execSQL("drop table if exists " + TABLE_COUNTRY);
        db.execSQL("drop table if exists " + TABLE_CITY);
        db.execSQL(CREATE_COUNTRY);
        db.execSQL(CREATE_CITY);
        _isPopulated = false; // Flags the database to be repopulated
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_COUNTRY);
        db.execSQL(CREATE_CITY);
        _isPopulated = false; // Database was just created and needs to be populated
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(MealPriceHelper.class.getName(), "Upgrading database from version " + oldVersion +
                " to " + newVersion + ", which will destroy all old data");
        db.execSQL("drop table if exists " + TABLE_CITY);
        db.execSQL("drop table if exists " + TABLE_COUNTRY);
        onCreate(db);
    }
}
