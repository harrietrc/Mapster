package com.mapster.priceestimation;

import android.content.Context;

import com.mapster.priceestimation.data.City;
import com.mapster.priceestimation.data.Country;
import com.mapster.priceestimation.data.MealPriceDataSource;
import com.mapster.suggestions.FoursquareSuggestion;
import com.mapster.suggestions.Suggestion;

/**
 * Created by Harriet on 6/12/2015.
 * TODO Database should be closed onPause and onResume
 */
public class MealPriceEstimate {

    public static final String DEFAULT_COUNTRY_CODE = "NZ";
    public static final double DEFAULT_MEAL_PRICE = 18.0;

    private MealPriceDataSource _dataSource;

    public MealPriceEstimate(Context context) {
        _dataSource = new MealPriceDataSource(context);
        _dataSource.open();
    }

    public String getCurrencyCode(String countryCode) {
        String currencyCode = null;
        if (countryCode == null)
            countryCode = DEFAULT_COUNTRY_CODE;
        Country country = _dataSource.getCountry(countryCode);
        if (country != null)
            currencyCode = country.getCurrencyCode();
        return currencyCode;
    }

    /**
     * Takes a suggestion with a price level between 1 and 4 (NOT 1 and 3) and estimates a meal
     * price in the LOCAL currency of that country.
     * @param restaurant Restaurant with a price level between 1 and 4, where 4 is expensive
     * @return Estimated meal price in the local currency of the country
     */
    public double estimateMealPrice(FoursquareSuggestion restaurant) {
        Integer priceLevel = restaurant.getPriceLevel();
        String countryCode = restaurant.getCountryCode();
        String city = restaurant.getCity();
        double priceEstimate = DEFAULT_MEAL_PRICE;

        if (countryCode == null) {
            countryCode = findCountryCode(restaurant);
        }

        if (priceLevel != null)  {
            double averageMealPrice = getAverageMealPrice(city, countryCode);
            // Fairly arbitrary estimate at the moment. Tweak until more accurate.
            priceEstimate = averageMealPrice + (priceLevel-2)*averageMealPrice/3;
        }

        return priceEstimate;
    }

    public String findCountryCode(Suggestion suggestion) {
        // Should find the country code from the latitude and longitude (can't rely on the user's
        // marker in the case that this restaurant falls over the border of the country
        return "NZ"; // TODO Temporary! Use Google Maps API for geolocation
    }

    public double getAverageMealPrice(String countryCode) {
        double mealPrice = DEFAULT_MEAL_PRICE;
        Country country = _dataSource.getCountry(countryCode);
        if (country != null)
            mealPrice = country.getMealPrice(); // In local currency
        return mealPrice;
    }

    public double getAverageMealPrice(String cityName, String countryCode) {
        double mealPrice;
        City city = _dataSource.getCity(cityName, countryCode);
        if (city == null) {
            // Use the country's meal price instead
            mealPrice = getAverageMealPrice(countryCode);
        } else {
            mealPrice = city.getMealPrice();
        }
        return mealPrice;
    }
}
