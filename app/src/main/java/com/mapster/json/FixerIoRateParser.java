package com.mapster.json;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Harriet on 23/08/2015.
 */
public class FixerIoRateParser {

    public double getRate(JSONObject rateData, String fromCurrencyCode, String toCurrencyCode) throws JSONException {
        JSONObject rates = rateData.getJSONObject("rates");

        // These rates may not exist if one of the currencies is Fixer.io's base currency (EUR)
        double toRate, fromRate;

        if (rates.has(toCurrencyCode)) {
            toRate = rates.getDouble(toCurrencyCode);
        } else {
            toRate = 1;
        }

        if (rates.has(fromCurrencyCode)) {
            fromRate = rates.getDouble(fromCurrencyCode);
        } else {
            fromRate = 1;
        }

        return  toRate / fromRate;
    }
}
