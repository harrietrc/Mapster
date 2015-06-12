package com.mapster.foursquare;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Harriet on 6/10/2015.
 */
public class FoursquareVenue {
    private String _id;
    private String _name;
    private String _phoneNumber;
    private String _address;
    private LatLng _location;
    private String _website;
    private Float _rating;
    private String _imageUrl;

    private Integer _priceLevel;

    // Used for estimating pricing
    private String _countryCode;
    private String _city;
    private String _currencyCode;

    public FoursquareVenue(String id, String name, String phoneNumber, String address,
                           LatLng location, String website, Float rating, String imageUrl,
                           Integer priceLevel, String countryCode, String city, String currency) {
        _id = id;
        _name = name;
        _phoneNumber = phoneNumber;
        _address = address;
        _location = location;
        _website = website;
        _rating = rating;
        _imageUrl = imageUrl;
        _priceLevel = priceLevel;
        _countryCode = countryCode;
        _city = city;
        _currencyCode = currency;
    }

    public String getName() {
        return _name;
    }

    public Integer getPriceLevel() {
        return _priceLevel;
    }

    public String getCurrencyCode() {
        return _currencyCode;
    }

    public float getRating() {
        return _rating == null ? 0 : _rating;
    }

    public String getImageUrl() {
        return _imageUrl;
    }

    public LatLng getLocation() {
        return _location;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (_address != null)
            sb.append(_address);
        if (_phoneNumber != null)
            sb.append("\n" + _phoneNumber);
        if (_website != null)
            sb.append("\n" + _website);
        return sb.toString();
    }
}
