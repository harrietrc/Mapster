package com.mapster.places;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.mapster.api.googleplaces.GooglePlaces;

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
    private String _photoUrl;

    // Save the details of a place, if they are retrieved.
    private GooglePlaceDetail _detail;

    private Integer _priceLevel; // Rates the expense of the place, with 0 being free and 4 very expensive

    public GooglePlace(String id, String name, LatLng latLng, Float rating, String photoRef, Set<String> categories) {
        _id = id;
        _name = name;
        _latitude = latLng.latitude;
        _longitude = latLng.longitude;
        _rating = rating;
        _photoReference = photoRef;
    }

    public LatLng getLatLng() {
        return new LatLng(_latitude, _longitude);
    }

    public String getWebsite() {
        return _detail.getWebsite();
    }

    public String getPhoneNumber() {
        return _detail.getPhoneNumber();
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

    public void setDetail(GooglePlaceDetail detail) {
        _detail = detail;
    }

    public String getName() {
        return _name;
    }

    public String getThumbnailUrl(Context context) {
        if (_photoUrl == null) {
            GooglePlaces api = new GooglePlaces(context);
            _photoUrl = api.placePhotoUrl(_photoReference);
        }
        return _photoUrl;
    }

    public String getId() {
        return _id;
    }

    public float getRating() {
        return _rating == null ? 0 : _rating;
    }
}