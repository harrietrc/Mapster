package com.mapster.priceestimation.data;

/**
 * Created by Harriet on 6/12/2015.
 */
public class City {
    private String _name;
    private String _country;
    private double _averageMealPrice;

    public String getName() {
        return _name;
    }

    public String getCountryName() {
        return _country;
    }

    public double getMealPrice() {
        return _averageMealPrice;
    }

    public void setName(String name) {
        _name = name;
    }

    public void setCountryName(String countryName) {
        _country = countryName;
    }

    public void setMealPrice(double mealPrice) {
        _averageMealPrice = mealPrice;
    }
}
