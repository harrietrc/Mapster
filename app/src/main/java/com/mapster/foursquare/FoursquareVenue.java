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
    private double _latitude;
    private double _longitude;
    private String _website;
    private Float _rating;
    private String _imageUrl;

    private Integer _priceLevel;

    // Used for estimating pricing
    private String _countryCode;
    private String _city;

    public FoursquareVenue(String id, String name, String phoneNumber, String address,
                           LatLng latLng, String website, Float rating, String imageUrl,
                           Integer priceLevel, String countryCode, String city) {
        _id = id;
        _name = name;
        _phoneNumber = phoneNumber;
        _address = address;
        _latitude = latLng.latitude;
        _longitude = latLng.longitude;
        _website = website;
        _rating = rating;
        _imageUrl = imageUrl;
        _priceLevel = priceLevel;
        _countryCode = countryCode;
        _city = city;
    }

    public String getName() {
        return _name;
    }

    public String getCountryCode() {
        return _countryCode;
    }

    public Integer getPriceLevel() {
        return _priceLevel;
    }

    public float getRating() {
        return _rating == null ? 0 : _rating;
    }

    public String getImageUrl() {
        return _imageUrl;
    }

    public LatLng getLocation() {
        return new LatLng(_latitude, _longitude);
    }

    public String getWebsite() {
        return _website;
    }

    public String getPhoneNumber() {
        return _phoneNumber;
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

    public String getCity() {
        return _city;
    }
}
