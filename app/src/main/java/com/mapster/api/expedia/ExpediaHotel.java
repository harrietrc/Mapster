package com.mapster.api.expedia;

import com.google.android.gms.maps.model.LatLng;

import org.apache.commons.lang3.StringEscapeUtils;

/**
 * Created by Harriet on 5/25/2015. Holds information about a hotel, retrieved from Expedia.
 */
public class ExpediaHotel {

    private int _hotelId; // Used in requests to Expedia
    private String _name;
    private String _address;
    private String _thumbnailUrl;
    private Double _lowRate;
    private Double _highRate;
    private double _latitude;
    private double _longitude;
    private Float _rating; // In stars out of 5
    private String _locationDescription; // A short description of the location, if available
    private String _currencyCode;

    /**
     * Everything passed into this constructor is retrievable in a HotelListRequest.
     */
    public ExpediaHotel(Integer hotelId, String name, String address, LatLng latLng, Float rating,
                        Double lowRate, Double highRate, String locationDescription,
                        String thumbnailUrl, String currencyCode) {
        _hotelId = hotelId;
        _name = StringEscapeUtils.unescapeHtml4(name);
        _address = StringEscapeUtils.unescapeHtml4(address);
        _latitude = latLng.latitude;
        _longitude = latLng.longitude;
        _rating = rating;
        _lowRate = lowRate;
        _highRate = highRate;
        _locationDescription = StringEscapeUtils.unescapeHtml4(locationDescription);
        _thumbnailUrl = thumbnailUrl;
        _currencyCode = currencyCode;
    }

    public Double estimateAverageRate() {
        // Might want to refine this
//        return (_lowRate + _highRate) / 2;
        return _lowRate;
    }

    public String getThumbnailUrl() {
        return _thumbnailUrl;
    }

    public Double getLowRate() {
        return _lowRate;
    }

    public Double getHighRate() {
        return _highRate;
    }

    public float getRating() {
        return _rating == null ? 0 : _rating;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(_address);
        if (_locationDescription != null)
            sb.append("\n" + _locationDescription);
        return sb.toString();
    }

    public String getCurrencyCode() {
        return _currencyCode;
    }

    public String getName() {
        return _name;
    }

    public LatLng getLocation() {
        return new LatLng(_latitude, _longitude);
    }
}
