package com.mapster.priceestimation.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by Harriet on 6/12/2015.
 * Very minimal at the moment. Add delete/update methods if necessary. Foreign key in city table
 * isn't actually used for any joins at the moment.
 */
public class MealPriceDataSource {

    private SQLiteDatabase _database;
    private MealPriceHelper _helper;
    private String[] _allColumnsCountry = {MealPriceHelper.COLUMN_COUNTRY_CODE,
            MealPriceHelper.COLUMN_MEAL_PRICE, MealPriceHelper.COLUMN_CURRENCY_CODE};
    private String[] _allColumnsCity = {MealPriceHelper.COLUMN_CITY_NAME,
    MealPriceHelper.COLUMN_MEAL_PRICE, MealPriceHelper.COLUMN_COUNTRY_CODE};

    public MealPriceDataSource(Context context) {
        _helper = new MealPriceHelper(context);
    }

    public void open() throws SQLException {
        _database = _helper.getWritableDatabase();
        populateDatabase();
    }

    public void close() {
        _helper.close();
    }

    /**
     * Sets initial values if the database is empty.
     */
    public void populateDatabase() {
        if (!_helper.isPopulated()) {
            // TODO Should populate from file in MealPriceDataSource.onCreate(), not here

            // Add others beneath this (use numbeo.com to find the average price of a meal)
            // The first argument is the country's ISO 3166 code, the second is its currency code,
            // and the third is its average meal price in the local currency. Currency codes should
            // be ISO 4217 format.
            createCountry("NZ", "NZD", 18.0);
            createCity("Wellington", 19.0, "NZ");
            createCountry("US", "USD", 10.0);
            createCountry("AUS", "AUD", 17.0);

            _helper.setPopulated(); // Flag the database as populated
        }
    }

    public City getCity(String name, String countryCode) {
        Cursor cursor = _database.query(MealPriceHelper.TABLE_CITY, _allColumnsCity,
                MealPriceHelper.COLUMN_CITY_NAME + " = '" + name + "' and " +
                        MealPriceHelper.COLUMN_COUNTRY_CODE + " = '" + countryCode + "'", null,
                null, null, null);
        cursor.moveToFirst();
        City city = cursorToCity(cursor);
        cursor.close();
        return city;
    }

    public Country getCountry(String code) {
        Cursor cursor = _database.query(MealPriceHelper.TABLE_COUNTRY, _allColumnsCountry,
                MealPriceHelper.COLUMN_COUNTRY_CODE + " = '" + code + "'", null, null, null, null);
        cursor.moveToFirst();
        Country country = cursorToCountry(cursor);
        cursor.close();
        return country;
    }

    public Country createCountry(String code, String currency, double mealPrice) {
        ContentValues values = new ContentValues();
        values.put(MealPriceHelper.COLUMN_COUNTRY_CODE, code);
        values.put(MealPriceHelper.COLUMN_MEAL_PRICE, mealPrice);
        values.put(MealPriceHelper.COLUMN_CURRENCY_CODE, currency);
        _database.insert(MealPriceHelper.TABLE_COUNTRY, null, values);
        Cursor cursor = _database.query(MealPriceHelper.TABLE_COUNTRY, _allColumnsCountry,
                MealPriceHelper.COLUMN_COUNTRY_CODE + " = '" + code + "'", null, null, null, null);
        cursor.moveToFirst();
        Country newCountry = cursorToCountry(cursor);
        cursor.close();
        return newCountry;
    }

    public City createCity(String name, double mealPrice, String countryCode) {
        ContentValues values = new ContentValues();
        values.put(MealPriceHelper.COLUMN_CITY_NAME, name);
        values.put(MealPriceHelper.COLUMN_MEAL_PRICE, mealPrice);
        values.put(MealPriceHelper.COLUMN_COUNTRY_CODE, countryCode);
        _database.insert(MealPriceHelper.TABLE_CITY, null, values);
        // TODO Key city differently if we end up with multiple cities with the same name in the same country
        Cursor cursor = _database.query(MealPriceHelper.TABLE_CITY, _allColumnsCity,
                MealPriceHelper.COLUMN_CITY_NAME + " = '" + name + "' and " +
                        MealPriceHelper.COLUMN_COUNTRY_CODE + " = '" + countryCode + "'", null, null, null,
                null);
        cursor.moveToFirst();
        City newCity = cursorToCity(cursor);
        cursor.close();
        return newCity;
    }

    private City cursorToCity(Cursor cursor) {
        if (cursor.getCount() == 0)
            return null;
        City city = new City();
        city.setName(cursor.getString(0));
        city.setMealPrice(cursor.getDouble(1));
        city.setCountryName(cursor.getString(2));
        return city;
    }

    private Country cursorToCountry(Cursor cursor) {
        if (cursor.getCount() == 0)
            return null;
        Country country = new Country();
        country.setCode(cursor.getString(0));
        country.setMealPrice(cursor.getDouble(1));
        country.setCurrencyCode(cursor.getString(2));
        return country;
    }
}
