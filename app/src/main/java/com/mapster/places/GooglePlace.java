package com.mapster.places;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

/**
 * Created by Harriet on 3/15/2015.
 * From http://wptrafficanalyzer.in/blog/showing-nearby-places-with-photos-at-any-location-in-google-maps-android-api-v2/
 */
public class GooglePlace {
    public String latitude;
    public String longitude;
    public String name;

    public static final String[] PLACES_OF_WORSHIP =  {
        // Not really sure what to do with these - how do you differentiate something like the
        // Sagrada Familia from just your local church? Probably by rating and popularity.
        "church", "hindu_temple", "mosque", "place_of_worship", "synagogue"
    };

    public static final String[] ENTERTAINMENT = {
        "bowling_alley", "casino", "movie_theatre", "night_club", "spa"
    };

    public static final String[] ATTRACTIONS = {
        "amusement_park", "aquarium", "park", "zoo", "art_gallery", "museum",
            "establishment" // Default when uncategorised
    };

    public static final String[] DINING = {
        "bakery", "bar", "cafe", "food", "restaurant"
    };

    public static final String[] SHOPPING = {
        "book_store", "clothing_store", "department_store", "shopping_mall"
    };

    public static final String[] ACCOMMODATION = {
            "campground", "lodging" // Hotels, B&B's, etc. fall under lodging
    };

    public GooglePlace() {
        // Private otherwise
    }
}