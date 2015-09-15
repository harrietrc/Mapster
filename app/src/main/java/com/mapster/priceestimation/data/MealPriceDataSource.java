package com.mapster.priceestimation.data;

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
    private MealPriceAssetHelper _helper;
    private String[] _allColumnsCountry = {MealPriceAssetHelper.COLUMN_COUNTRY_CODE,
            MealPriceAssetHelper.COLUMN_CURRENCY_CODE, MealPriceAssetHelper.COLUMN_MEAL_PRICE};
    private String[] _allColumnsCity = {MealPriceAssetHelper.COLUMN_CITY_NAME,
            MealPriceAssetHelper.COLUMN_MEAL_PRICE, MealPriceAssetHelper.COLUMN_COUNTRY_CODE};

    public MealPriceDataSource(Context context) {
        _helper = new MealPriceAssetHelper(context);
    }

    public void open() throws SQLException {
        _database = _helper.getWritableDatabase();
    }

    public void close() {
        _helper.close();
    }

    public City getCity(String name, String countryCode) {
        Cursor cursor = _database.query(MealPriceAssetHelper.TABLE_CITY, _allColumnsCity,
                MealPriceAssetHelper.COLUMN_CITY_NAME + " = '" + name + "' and " +
                        MealPriceAssetHelper.COLUMN_COUNTRY_CODE + " = '" + countryCode + "'", null,
                null, null, null);
        cursor.moveToFirst();
        City city = cursorToCity(cursor);
        cursor.close();
        return city;
    }

    public Country getCountry(String code) {
        Cursor cursor = _database.query(MealPriceAssetHelper.TABLE_COUNTRY, _allColumnsCountry,
                MealPriceAssetHelper.COLUMN_COUNTRY_CODE + " = '" + code + "'", null, null, null,
                null);
        cursor.moveToFirst();
        Country country = cursorToCountry(cursor);
        cursor.close();
        return country;
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
        country.setCurrencyCode(cursor.getString(1));
        country.setMealPrice(cursor.getDouble(2));
        return country;
    }
}
