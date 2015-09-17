package com.mapster.json;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Harriet on 23/08/2015.
 */
public class FixerIoRateParser {

    public double getRate(JSONObject rateData) throws JSONException {
        JSONArray rates = rateData.getJSONArray("rates");
        double toRate = rates.getDouble(1);
        double fromRate = rates.getDouble(0);
        return  toRate / fromRate;
    }
}
