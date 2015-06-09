package com.mapster.places;

/**
 * Created by Harriet on 3/22/2015.
 * Stores/operates on detail related to a GooglePlace. Like GooglePlace, should really only be
 * accessed by the corresponding parser tasks. This data is used to construct a snippet, which will
 * be displayed as Marker info on the map.
 * Note: Photo and rating are stored in GooglePlace. This only populates the InfoWindow snippet.
 */
public class GooglePlaceDetail {
    private String _shortAddress;
    private String _website; // May be null
    private String _phoneNumber; // May be null

    public GooglePlaceDetail(String address, String website, String phoneNumber) {
        _shortAddress = address;
        _website = website;
        _phoneNumber = phoneNumber;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(_shortAddress);
        if (_phoneNumber != null)
            sb.append("\n" + _phoneNumber);
        if (_website != null)
            sb.append("\n" + _website);
        return sb.toString();
    }

}
