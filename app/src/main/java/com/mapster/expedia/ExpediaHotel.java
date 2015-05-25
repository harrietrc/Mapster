package com.mapster.expedia;

/**
 * Created by Harriet on 5/25/2015. Holds information about a hotel, retrieved from Expedia.
 */
public class ExpediaHotel {

    private int _hotelId; // Used in requests to Expedia
    private String _address;
    private String _thumbnailUrl;
    private Double _lowRate; // Note that these are actually returned as floats/doubles
    private Double _highRate;
    private Double _longitude;
    private Double _latitude;
    private Double _rating; // In stars out of 5
    private String _locationDescription; // A short description of the location, if available

    /**
     * Everything passed into this constructor is retrievable in a HotelListRequest.
     */
    public ExpediaHotel(Integer hotelId, String address, Double latitude, Double longitude, Double rating,
                        Double lowRate, Double highRate, String locationDescription, String thumbnailUrl) {
        _hotelId = hotelId;
        _address = address;
        _latitude = latitude;
        _longitude = longitude;
        _rating = rating;
        _lowRate = lowRate;
        _highRate = highRate;
        _locationDescription = locationDescription;
        _thumbnailUrl = thumbnailUrl;
    }

    public int getHotelId() {
        return _hotelId;
    }

    public String getAddress() {
        return _address;
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

    public Double getLongitude() {
        return _longitude;
    }

    public Double getLatitude() {
        return _latitude;
    }

    public Double getRating() {
        return _rating;
    }

    public String getLocationDescription() {
        return _locationDescription;
    }
}
