package com.mapster.places;

import android.os.Parcel;
import android.os.Parcelable;

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

    private static String[] _allCategories;

    public static final HashSet<String> PLACES_OF_WORSHIP = new HashSet<>(Arrays.asList(
        // Not really sure what to do with these - how do you differentiate something like the
        // Sagrada Familia from just your local church? Probably by rating and popularity.
        "church", "hindu_temple", "mosque", "place_of_worship", "synagogue"
    ));

    public static final HashSet<String> ENTERTAINMENT = new HashSet<>(Arrays.asList(
        "bowling_alley", "casino", "movie_theatre", "night_club", "spa"
    ));

    public static final HashSet<String> ATTRACTIONS = new HashSet<>(Arrays.asList(
        "amusement_park", "aquarium", "park", "zoo", "art_gallery", "museum"
//            "establishment" // Default when uncategorised
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

    public static String[] getAllCategories() {
        if (_allCategories == null) {
            Set<String> cats = new HashSet<>();
            cats.addAll(ATTRACTIONS);
            cats.addAll(DINING);
            cats.addAll(ACCOMMODATION);
            _allCategories = cats.toArray(new String[cats.size()]);
        }
        return _allCategories;
    }

    public GooglePlace() {

    }
}