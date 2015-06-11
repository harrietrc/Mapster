package com.mapster.json;

import com.google.android.gms.maps.model.LatLng;
import com.mapster.foursquare.FoursquareVenue;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Harriet on 6/10/2015.
 */
public class FoursquareExploreJsonParser {

    /**
     * Parses a venues response (either from /explore or possibly /search) into a list of venues
     * @param jsonVenues JSON blob representing the response
     * @return List of venues retrieved from the response
     */
    public List<FoursquareVenue> getVenues(JSONObject jsonVenues) {
        List<FoursquareVenue> venues = new ArrayList<>();
        if (jsonVenues.has("response") && !jsonVenues.isNull("response")) {
            try {
                JSONObject mainContent = jsonVenues.getJSONObject("response");
                if (mainContent.has("groups") && !mainContent.isNull("groups")) {
                    JSONArray groups = mainContent.getJSONArray("groups");
                    if (groups.length() > 0) {
                        JSONObject blob = groups.getJSONObject(0);
                        if (blob.has("items") && !blob.isNull("items")) {
                            JSONArray items = blob.getJSONArray("items");
                            for (int i=0; i< items.length(); i++) {
                                JSONObject jsonVenue = items.getJSONObject(i).getJSONObject("venue");

                                // Basic attributes; always present
                                String id = jsonVenue.getString("id");
                                String name = jsonVenue.getString("name");

                                JSONObject location = jsonVenue.getJSONObject("location");
                                String address = location.getString("address");
                                double lat = location.getDouble("lat");
                                double lng = location.getDouble("lng");
                                LatLng latLng = new LatLng(lat, lng);
                                String countryCode = location.getString("cc");
                                String city = location.getString("city");

                                // Attributes that could conceivably be null
                                String phone, currency, website, imageUrl;
                                phone = currency = website = imageUrl = null;
                                Double rating = null;
                                Integer priceLevel = null;

                                JSONObject price = jsonVenue.getJSONObject("price");
                                if (price.has("tier") && !price.isNull("tier"))
                                    priceLevel = price.getInt("tier");
                                if (price.has("currency"))
                                    currency = price.getString("currency");

                                JSONObject contact = jsonVenue.getJSONObject("contact");
                                if (contact.has("formattedPhone"))
                                    phone = contact.getString("formattedPhone");

                                if (jsonVenue.has("url"))
                                    website = jsonVenue.getString("url");

                                if (jsonVenue.has("rating") && !jsonVenue.isNull("rating"))
                                    rating = jsonVenue.getDouble("rating");

                                JSONArray featuredPhotos = jsonVenue.getJSONObject("featuredPhotos")
                                        .getJSONArray("items");
                                if (featuredPhotos.length() > 0) {
                                    JSONObject photo = featuredPhotos.getJSONObject(0);
                                    String prefix = photo.getString("prefix").replace("\\", "");
                                    String suffix = photo.getString("suffix").replace("\\", "");
                                    imageUrl = prefix + "100x100" + suffix;
                                }

                                FoursquareVenue fs = new FoursquareVenue(id, name, phone, address,
                                        latLng, website, rating, imageUrl, priceLevel,
                                        countryCode, city, currency);
                                venues.add(fs);
                            }
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return  venues;
    }
}
