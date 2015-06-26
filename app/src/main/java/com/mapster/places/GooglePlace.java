package com.mapster.places;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.mapster.R;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Harriet on 3/15/2015.
 */
public class GooglePlace {
    private double _latitude;
    private double _longitude;
    private String _name;
    private String _id; // Google placeId
    private Float _rating;
    private String _photoReference;

    // Save the details of a place, if they are retrieved.
    private GooglePlaceDetail _detail;

    private Integer _priceLevel; // Rates the expense of the place, with 0 being free and 4 very expensive

    // All the categories that this place belongs to
    private Set<String> _categories;

    public GooglePlace(String id, String name, LatLng latLng, Float rating, String photoRef, Set<String> categories) {
        _id = id;
        _name = name;
        _latitude = latLng.latitude;
        _longitude = latLng.longitude;
        _rating = rating;
        _photoReference = photoRef;
        _categories = categories;
    }

    public Set<String> getCategories() {
        return _categories;
    }

    public LatLng getLatLng() {
        return new LatLng(_latitude, _longitude);
    }

    /**
     * Could be expanded to include price detail etc.
     * @return
     */
    public String toString() {
        return _detail == null ? "" : _detail.toString();
    }

    /**
     * Returns the price level (null if not provided, which is the usual case in Auckland at least)
     */
    public Integer getPriceLevel() {
        return _priceLevel;
    }

    public void setPriceLevel(int priceLevel) {
        if (priceLevel > 4) {
            Log.w("GooglePlaceDetail", "Got a value of " + priceLevel + " when 4 is the maximum.");
            _priceLevel = null;
        } else {
            _priceLevel = priceLevel;
        }
    }

    // TODO Composition: is there a best practice for setting the properties of contained objects?
    // I feel like setters and getters are a good option because you can control the access precisely
    // and how fine grained that access will be. It also allows customisation of how values are
    // processed but has the danger of setters/getters that do too much (e.g. change values)
    public void setDetail(GooglePlaceDetail detail) {
        _detail = detail;
    }

    // Default category for uncategorised places
    public static final Set<String> DEFAULT = new HashSet<>(Arrays.asList("establishment"));

    public static final Set<String> PLACES_OF_WORSHIP = new HashSet<>(Arrays.asList(
        // Not really sure what to do with these - how do you differentiate something like the
        // Sagrada Familia from just your local church? Probably by rating and popularity.
        "church", "hindu_temple", "mosque", "place_of_worship", "synagogue"
    ));

    public static final Set<String> ENTERTAINMENT = new HashSet<>(Arrays.asList(
        "bowling_alley", "casino", "movie_theatre", "night_club", "spa"
    ));

    public static final Set<String> ATTRACTIONS = new HashSet<>(Arrays.asList(
        "amusement_park", "aquarium", "zoo", "art_gallery", "museum"//, "park" TOO MANY PARKS!
    ));

    /**
     * These categories can't be provided to the 'types' parameter of the Places web service, but
     * should still be considered attractions
     */
    public static final HashSet<String> EXTRA_ATTRACTIONS = new HashSet<>(Arrays.asList(
        "point_of_interest", "natural_feature"
    ));

    public static final HashSet<String> SHOPPING = new HashSet<>(Arrays.asList(
        "book_store", "clothing_store", "department_store", "shopping_mall"
    ));

    public static Set<String> getAllCategories() {
        Set<String> cats = new HashSet<>();
        cats.addAll(ATTRACTIONS);
        cats.addAll(DEFAULT);
        return cats;
    }

    public String getName() {
        return _name;
    }

    public String getThumbnailUrl(Context context) {
        return buildPhotoUrl(context);
    }

    private String buildPhotoUrl(Context context) {
        int maxWidth = 200;
        int maxHeight = 200;

        StringBuilder sb = new StringBuilder("https://maps.googleapis.com/maps/api/place/photo?");
        sb.append("key=" + context.getResources().getString(R.string.API_KEY));
        sb.append("&photoreference=" + _photoReference);
        sb.append("&maxwidth=" + maxWidth);
        sb.append("&maxheight=" + maxHeight);

        return sb.toString();
    }

    public String getId() {
        return _id;
    }

    public float getRating() {
        return _rating == null ? 0 : _rating;
    }

    public static Set<String> getAttractionCategories() {
        Set<String> allAttractionCategories = new HashSet<>();
        allAttractionCategories.addAll(ATTRACTIONS);
        allAttractionCategories.addAll(EXTRA_ATTRACTIONS);
        return allAttractionCategories;
    }
}