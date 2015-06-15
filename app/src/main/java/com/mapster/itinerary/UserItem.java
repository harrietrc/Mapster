package com.mapster.itinerary;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Harriet on 6/12/2015.
 * Intended to correspond with places that the user adds using Autocomplete in the PlacesActivity
 * Should be saved in a database
 */
public class UserItem extends ItineraryItem {

    private String _name;
    private double _latitude;
    private double _longitude;

    public UserItem(String name, LatLng latLng) {
        _name = name;
        _latitude = latLng.latitude;
        _longitude = latLng.longitude;
    }

    public String getName() {
        return _name;
    }

    public LatLng getLocation() {
        return new LatLng(_latitude, _longitude);
    }
}
