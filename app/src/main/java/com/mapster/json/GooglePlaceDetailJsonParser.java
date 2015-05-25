package com.mapster.json;

import android.util.Log;

import com.mapster.places.GooglePlaceDetail;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Harriet on 3/22/2015.
 * Goes a step further than GooglePlaceJsonParser in that it converts the GooglePlaceDetail to a
 * string (no UI-related processing is required after parsing).
 */
public class GooglePlaceDetailJsonParser {
    public GooglePlaceDetail parse(JSONObject json) {
        JSONObject jsonDetail = null;
        StringBuilder sb = new StringBuilder();

        try {
            jsonDetail = json.getJSONObject("result");
        } catch(JSONException e) {
            e.printStackTrace();
        }
        GooglePlaceDetail detail = getDetail(jsonDetail);
        return detail;
    }

    /**
     * Parses a JSON object (corresponding to the response from querying the Google Places API).
     * @param jsonDetail = JSON representing Place detail
     * @return = A GooglePlaceDetail with the relevant data
     */
    private GooglePlaceDetail getDetail(JSONObject jsonDetail) {
        GooglePlaceDetail detail = null;
        String shortAddress, website, phoneNumber;
        shortAddress = website = phoneNumber = null;

        try {
            // Street address
            if (!jsonDetail.isNull("formatted_address")) {
                String formattedAddress = jsonDetail.getString("formatted_address");
                shortAddress = formattedAddress.split(",")[0]; // First line of the address
            }

            // Phone number
            if (!jsonDetail.isNull("formatted_phone_number")) {
                phoneNumber = jsonDetail.getString("formatted_phone_number");
            }

            // Website
            if (!jsonDetail.isNull("website")) {
                website = jsonDetail.getString("website");
            }

            detail = new GooglePlaceDetail(shortAddress, website, phoneNumber);

        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("EXCEPTION", e.toString());
        }
        return detail;
    }
}
