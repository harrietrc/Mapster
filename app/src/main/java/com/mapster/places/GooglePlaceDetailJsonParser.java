package com.mapster.places;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Harriet on 3/22/2015.
 * Goes a step further than GooglePlaceJsonParser in that it converts the GooglePlaceDetail to a
 * string (no UI-related processing is required after parsing).
 */
public class GooglePlaceDetailJsonParser {
    public String parse(JSONObject json) {
        JSONObject jsonDetail = null;
        StringBuilder sb = new StringBuilder();

        try {
            jsonDetail = json.getJSONObject("result");
        } catch(JSONException e) {
            e.printStackTrace();
        }
        GooglePlaceDetail detail = getDetail(jsonDetail);

        sb.append(detail.shortAddress);
        if (detail.phoneNumber != null)
            sb.append("\n" + detail.phoneNumber);
        if (detail.website != null)
            sb.append("\n" + detail.website);
        return sb.toString();
    }

    /**
     * Parses a JSON object (corresponding to the response from querying the Google Places API).
     * @param jsonDetail = JSON representing Place detail
     * @return = A GooglePlaceDetail with the relevant data
     */
    private GooglePlaceDetail getDetail(JSONObject jsonDetail) {
        GooglePlaceDetail detail = new GooglePlaceDetail();

        try {
            // Street address
            if (!jsonDetail.getString("formatted_address").equals(JSONObject.NULL)) {
                String formattedAddress = jsonDetail.getString("formatted_address");
                detail.shortAddress = formattedAddress.split(",")[0]; // First line of the address
            }

            // Phone number
            if (!jsonDetail.isNull("formatted_phone_number")) {
                detail.phoneNumber = jsonDetail.getString("formatted_phone_number");
            }

            // Website
            if (!jsonDetail.isNull("website")) {
                detail.website = jsonDetail.getString("website");
            }

            // TODO: opening hours (will be more complicated if we're to show only today's hours)

        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("EXCEPTION", e.toString());
        }
        return detail;
    }
}