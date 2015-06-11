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
    private Double _rating;
    private String _imageUrl;

    private Integer _priceLevel;

    // Used for estimating pricing
    private String _countryCode;
    private String _city;
    private String _currency;

    public FoursquareVenue(String id, String name, String phoneNumber, String address,
                           LatLng location, String website, Double rating, String imageUrl,
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
        _currency = currency;
    }

}
