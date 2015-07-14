package com.mapster.priceestimation.data;

import android.content.Context;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

/**
 * Created by Harriet on 15/07/2015. Used by SQLiteOpenHelper to load the meal price database
 */
public class MealPriceAssetHelper extends SQLiteAssetHelper {

    private static final String DATABASE_NAME = "meal_prices";
    private static final int DATABASE_VERSION = 1;

    // Country table: average price for a meal and currency code
    public static final String TABLE_COUNTRY = "Country";
    public static final String COLUMN_COUNTRY_CODE = "CountryCode";
    public static final String COLUMN_CURRENCY_CODE = "CurrencyCode";
    public static final String COLUMN_MEAL_PRICE = "MealPrice";

    // City table: average price for a meal and country
    public static final String TABLE_CITY = "City";
    public static final String COLUMN_CITY_NAME = "CityName";

    public MealPriceAssetHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
}