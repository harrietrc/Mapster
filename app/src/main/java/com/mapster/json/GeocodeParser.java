package com.mapster.json;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Harriet on 17/09/2015. Extracts the ISO 3166-1 country code from Google Geocode
 * response. Might want to move other JSON parsing stuff for GeoCode in here.
 */
public class GeocodeParser {

    public String getCountryCode(JSONObject jsonGeocode) {
        String countryCode = null;

        try {
            if (jsonGeocode.has("results") && !jsonGeocode.isNull("results")) {
                JSONArray results = jsonGeocode.getJSONArray("results");

                // First result should have the actual results
                JSONObject result = results.getJSONObject(0);

                if (result.has("address_components") && !result.isNull("address_components")) {
                    JSONArray addressComponents = result.getJSONArray("address_components");
                    countryCode = parseAddressComponents(addressComponents);
                }
            }
        } catch (JSONException e ) {
            e.printStackTrace();
        }

        return countryCode;
    }

    private String parseAddressComponents(JSONArray addressComponents) throws JSONException {
        String countryCode = null;

        for (int i=0; i<addressComponents.length(); i++) {
            JSONObject component = addressComponents.getJSONObject(i);
            JSONArray types = component.getJSONArray("types");
            if (types.getString(0).equals("country"))
                countryCode = component.getString("short_name");
        }

        return countryCode;
    }
}
