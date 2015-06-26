package com.mapster.json;

import com.google.android.gms.maps.model.LatLng;
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
                    JSONObject hotelList = mainContent.getJSONObject("HotelList");

                    // If we're this deep this shouldn't be null, but check just in case
                    if (hotelList.has("HotelSummary") && !hotelList.isNull("HotelSummary")) {
                        JSONArray hotelArray = hotelList.getJSONArray("HotelSummary");

                        // Create a hotel object for each result
                        for (int i=0; i<hotelArray.length(); i++) {
                            JSONObject hotelJson = (JSONObject) hotelArray.get(i);

                            // Only optional properties are null-checked.
                            int hotelId = hotelJson.getInt("hotelId");
                            String name = hotelJson.getString("name");
                            String address = hotelJson.getString("address1");
                            Double latitude = hotelJson.getDouble("latitude");
                            Double longitude = hotelJson.getDouble("longitude");
                            LatLng location = new LatLng(latitude, longitude);

                            Float rating = null;
                            if (hotelJson.has("hotelRating") && !hotelJson.isNull("hotelRating")) {
                                // Not a safe cast but it doesn't really matter
                                rating = (float) hotelJson.getDouble("hotelRating");
                            }

                            String currencyCode = null;
                            if (hotelJson.has("rateCurrencyCode"))
                                currencyCode = hotelJson.getString("rateCurrencyCode");

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
                                thumbnailUrl = "http://images.travelnow.com" + hotelJson.getString("thumbNailUrl");

                            ExpediaHotel hotel = new ExpediaHotel(hotelId, name, address, location,
                                    rating, lowRate, highRate, locationDescription, thumbnailUrl,
                                    currencyCode);
                            hotels.add(hotel);
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return hotels;
    }
}
