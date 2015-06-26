package com.mapster.priceestimation.data;

/**
 * Created by Harriet on 6/12/2015.
 */
public class Country {
    private String _name;
    private double _averageMealPrice;
    private String _currencyCode;

    public String getCode() {
        return _name;
    }

    public double getMealPrice() {
        return _averageMealPrice;
    }

    public String getCurrencyCode() {
        return _currencyCode;
    }

    public void setCode(String code) {
        _name = code;
    }

    public void setMealPrice(double mealPrice) {
        _averageMealPrice = mealPrice;
    }

    public void setCurrencyCode(String currencyCode) {
        _currencyCode = currencyCode;
    }
}
