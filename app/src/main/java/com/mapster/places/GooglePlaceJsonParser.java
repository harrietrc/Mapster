package com.mapster.places;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Harriet on 3/15/2015.
 * Copied from http://wptrafficanalyzer.in/blog/showing-nearby-places-with-photos-at-any-location-in-google-maps-android-api-v2/
 */
public class GooglePlaceJsonParser {
    public GooglePlace[] parse(JSONObject json) {
        JSONArray jsonPlaces = null;

        try {
            jsonPlaces = json.getJSONArray("results");
        } catch(JSONException e) {
            e.printStackTrace();
        }
        return getPlaces(jsonPlaces);
    }

    private GooglePlace[] getPlaces(JSONArray jsonPlaces) {
        int placeCount = jsonPlaces.length();
        GooglePlace[] places = new GooglePlace[placeCount];

        for (int i=0; i < placeCount; i++) {
            try {
                places[i] = getPlace((JSONObject) jsonPlaces.get(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return places;
    }

    /**
     * Basic parsing for a single Google Place. Simplified from source, in that it ignores photos
     * for now.
     * @param jsonPlace = A single Google Place as JSON
     * @return = a GooglePlace (model of Google Place)
     */
    private GooglePlace getPlace(JSONObject jsonPlace) {
        GooglePlace place = new GooglePlace();

        try {
            if (!jsonPlace.isNull("name"))
                place.name = jsonPlace.getString("name");

            place.latitude = jsonPlace.getJSONObject("geometry").getJSONObject("location").getString("lat");
            place.longitude = jsonPlace.getJSONObject("geometry").getJSONObject("location").getString("lng");

            place.id = jsonPlace.getString("place_id");

            if (!jsonPlace.isNull("rating"))
                place.rating = (float) jsonPlace.getDouble("rating");

            // Parse the types of the place
            String[] categories = jsonPlace.getJSONArray("types").join(",").split(",");
            String[] trimmedArray = new String[categories.length];
            for (int i=0; i < categories.length; i++)
                trimmedArray[i] = categories[i].replaceAll("\"", "");
            place.categories = trimmedArray;

        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("EXCEPTION", e.toString());
        }
        return place;
    }
}
