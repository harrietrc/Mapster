package com.mapster.places;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Harriet on 3/15/2015.
 */
public class GooglePlace {
    public String latitude;
    public String longitude;
    public String name;
    public String id; // Google placeId
    public float rating;
    public String[] categories;
    public String photoReference;

    // Save the details of a place, if they are retrieved.
    private GooglePlaceDetail _detail;

    private Integer _priceLevel; // Rates the expense of the place, with 0 being free and 4 very expensive

    /**
     * Returns the price level (null if not provided, which is the usual case in Auckland at least)
     */
    public Integer getPriceLevel() {
        return _priceLevel;
    }

    public boolean hasDetail() {
        return _detail != null;
    }

    public void setPriceLevel(int priceLevel) {
        if (priceLevel > 5) {
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

    private static String[] _allCategories;

    // Default category for uncategorised places
    public static final String DEFAULT = "establishment";

    public static final HashSet<String> PLACES_OF_WORSHIP = new HashSet<>(Arrays.asList(
        // Not really sure what to do with these - how do you differentiate something like the
        // Sagrada Familia from just your local church? Probably by rating and popularity.
        "church", "hindu_temple", "mosque", "place_of_worship", "synagogue"
    ));

    public static final HashSet<String> ENTERTAINMENT = new HashSet<>(Arrays.asList(
        "bowling_alley", "casino", "movie_theatre", "night_club", "spa"
    ));

    public static final HashSet<String> ATTRACTIONS = new HashSet<>(Arrays.asList(
        "amusement_park", "aquarium", "zoo", "art_gallery", "museum", "park"
    ));

    /**
     * These categories can't be provided to the 'types' parameter of the Places web service, but
     * should still be considered attractions
     */
    public static final HashSet<String> EXTRA_ATTRACTIONS = new HashSet<>(Arrays.asList(
        "point_of_interest", "natural_feature"
    ));

    public static final HashSet<String> DINING = new HashSet<>(Arrays.asList(
        "bakery", "bar", "cafe", "food", "restaurant"
    ));

    public static final HashSet<String> SHOPPING = new HashSet<>(Arrays.asList(
        "book_store", "clothing_store", "department_store", "shopping_mall"
    ));

    public static final HashSet<String> ACCOMMODATION = new HashSet<>(Arrays.asList(
        "campground", "lodging" // Hotels, B&B's, etc. fall under lodging
    ));

    public static String[] getAccommodationCategories() {
        return ACCOMMODATION.toArray(new String[ACCOMMODATION.size()]);
    }

    public static String[] getDiningCategories() {
        return DINING.toArray(new String[DINING.size()]);
    }

    public static String[] getAttractionCategories() {
        return ATTRACTIONS.toArray(new String[ATTRACTIONS.size()]);
    }

    public static String[] getAllCategories() {
        if (_allCategories == null) {
            Set<String> cats = new HashSet<>();
            cats.addAll(ATTRACTIONS);
            cats.addAll(DINING);
            cats.addAll(ACCOMMODATION);
//            cats.add(DEFAULT);
            _allCategories = cats.toArray(new String[cats.size()]);
        }
        return _allCategories;
    }

    public GooglePlace() {
        _detail = null;
    }
}