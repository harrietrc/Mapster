package com.mapster.expedia;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Harriet on 5/25/2015. Holds information about a hotel, retrieved from Expedia.
 */
public class ExpediaHotel {

    private int _hotelId; // Used in requests to Expedia
    String _name;
    private String _address;
    private String _thumbnailUrl;
    private Double _lowRate; // Note that these are actually returned as floats/doubles
    private Double _highRate;
    private LatLng _location;
    private Float _rating; // In stars out of 5
    private String _locationDescription; // A short description of the location, if available

    /**
     * Everything passed into this constructor is retrievable in a HotelListRequest.
     */
    public ExpediaHotel(Integer hotelId, String name, String address, LatLng latLng, Float rating,
                        Double lowRate, Double highRate, String locationDescription,
                        String thumbnailUrl) {
        _hotelId = hotelId;
        _name = name;
        _address = address;
        _location = latLng;
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

    public float getRating() {
        return _rating == null ? 0 : _rating;
    }

    public String getLocationDescription() {
        return _locationDescription;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(_address);
        if (_locationDescription != null)
            sb.append("\n" + _locationDescription);
        String priceRange = priceRangeToString();
        if (priceRange != null)
            sb.append("\n" + priceRange);
        return sb.toString();
    }

    /**
     * Returns a string representation of the price range to append to the snippet that gets
     * displayed in a marker's infowindow.
     * @return
     */
    public String priceRangeToString() {
        StringBuilder sb = new StringBuilder();
        Double lowRate = getLowRate();
        Double highRate = getHighRate();

        sb.append(lowRate == null ? "" : "$" + lowRate.intValue());

        if (highRate != null)
            sb.append(" - ");

        sb.append(highRate == null ? "" : "$" + highRate.intValue());

        if (!(lowRate== null && highRate == null))
            sb.append(" a night");

        return sb.toString();
    }

    public String getName() {
        return _name;
    }

    public LatLng getLocation() {
        return _location;
    }
}