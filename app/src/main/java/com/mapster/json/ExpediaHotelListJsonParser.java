package com.mapster.json;

import com.mapster.expedia.ExpediaHotel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Harriet on 5/25/2015.
 */
public class ExpediaHotelListJsonParser {

    public List<ExpediaHotel> parse(JSONObject json) {
        JSONObject jsonHotels = null;
        StringBuilder sb = new StringBuilder();

        try {
            jsonHotels = json.getJSONObject("result");
        } catch(JSONException e) {
            e.printStackTrace();
        }
        List<ExpediaHotel> hotels = getHotels(jsonHotels);
        return hotels;
    }

    /**
     * Parses a response from Expedia to hotel data.
     * @param jsonHotels JSON representation of a HotelListRequest's response
     * @return A list of hotels (names, addresses, ratings, etc.)
     */
    public List<ExpediaHotel> getHotels(JSONObject jsonHotels) {
        List<ExpediaHotel> hotels = new ArrayList<>();

        if (jsonHotels.has("HotelListResponse") && !jsonHotels.isNull("HotelListResponse")) {
            try {
                JSONObject mainContent = jsonHotels.getJSONObject("HotelListResponse");

                // List of hotel details (summaries)
                if (mainContent.has("HotelList") && !mainContent.isNull("HotelList")) {
                    JSONArray hotelList = mainContent.getJSONArray("HotelList");

                    // Create a hotel object for each result
                    for (int i=0; i<hotelList.length(); i++) {
                        JSONObject hotelJson = (JSONObject) hotelList.get(i);

                        // Only optional properties are null-checked.
                        int hotelId = hotelJson.getInt("hotelId");
                        String name = hotelJson.getString("name");
                        String address = hotelJson.getString("address1");
                        Double latitude = hotelJson.getDouble("latitude");
                        Double longitude = hotelJson.getDouble("longitude");

                        Double rating = null;
                        if (hotelJson.has("hotelRating") && !hotelJson.isNull("hotelRating"))
                            rating = hotelJson.getDouble("hotelRating");

                        String locationDescription = null;
                        if (hotelJson.has("locationDescription") && !hotelJson.isNull("locationDescription"))
                            locationDescription = hotelJson.getString("locationDescription");

                        Double highRate = null;
                        if (hotelJson.has("highRate") && !hotelJson.isNull("highRate"))
                            highRate = hotelJson.getDouble("highRate");

                        Double lowRate = null;
                        if (hotelJson.has("lowRate") && !hotelJson.isNull("lowRate"))
                            lowRate = hotelJson.getDouble("lowRate");

                        String thumbnailUrl = null;
                        if (hotelJson.has("thumbNailUrl") && !hotelJson.isNull("thumbNailUrl"))
                            thumbnailUrl = hotelJson.getString("thumbNailUrl");

                        ExpediaHotel hotel = new ExpediaHotel(hotelId, address, latitude, longitude,
                                rating, lowRate, highRate, locationDescription, thumbnailUrl);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return hotels;
    }
}
