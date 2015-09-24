package com.mapster.itinerary;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Harriet on 6/12/2015.
 * Intended to correspond with places that the user adds using Autocomplete in the PlacesActivity
 * Should be saved in a database
 */
public class UserItem extends ItineraryItem implements Parcelable {

    private String _name;
    private double _latitude;
    private double _longitude;
    private String _travelMode;
    private String _countryCode; // ISO 3166-1

    private String _fullAddress;

    private String _markerId;

    // Represents any saved suggestions that were suggested from this destination
    private List<SuggestionItem> _suggestionItems;

    public void addSuggestionItems(Collection<SuggestionItem> items) {
        _suggestionItems.addAll(items);
    }

    public void replaceSuggestionItems(Collection<SuggestionItem> items) {
        _suggestionItems = new ArrayList<>();
        _suggestionItems.addAll(items);
    }

    public UserItem(String name, LatLng latLng, String travelMode, String countryCode, String fullAddress) {
        _name = name;
        _latitude = latLng.latitude;
        _longitude = latLng.longitude;
        _travelMode = travelMode;
        // Linked list to speed up removals a little
        _suggestionItems = new LinkedList<>();
        _fullAddress = fullAddress;

        if (countryCode == null)
            throw new IllegalArgumentException("Something is up with GeoCode - no country code.");
        _countryCode = countryCode;
    }

    public UserItem(Parcel source) {
        _name = source.readString();

        // Order matters, be careful! Must match write order.
        _latitude = source.readDouble();
        _longitude = source.readDouble();
        _travelMode = source.readString();
        _suggestionItems = new LinkedList<>();
        _countryCode = source.readString();
        _fullAddress = source.readString();
    }

    public String getFullAddress() {
        return _fullAddress;
    }

    @Override
    public String getMarkerId() {
        return _markerId;
    }

    @Override
    public void setMarkerId(String markerId) {
        _markerId = markerId;
    }

    @Override
    public String getCountryCode() {
        return _countryCode == null ? "NZ" : _countryCode;
    }

    public void setCountryCode(String countryCode) {
        _countryCode = countryCode;
    }

    public void removeSuggestionItem(SuggestionItem item) {
        if(_suggestionItems.remove(item))
            item.setIsInItinerary(false);
    }

    public void setTravelMode(String mode){
        _travelMode = mode;
    }

    public List<SuggestionItem> getSuggestionItems() {
        return _suggestionItems;
    }

    public void addSuggestionItem(SuggestionItem item) {
        _suggestionItems.add(item);
    }

    @Override
    public String getName() {
        return _name;
    }

    @Override
    public LatLng getLocation() {
        return new LatLng(_latitude, _longitude);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getTravelMode(){
        return _travelMode;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // Remember to write any fields from the superclass if they are added in the future.
        dest.writeString(_name);
        dest.writeDouble(_latitude);
        dest.writeDouble(_longitude);
        dest.writeString(_travelMode);
        dest.writeString(_countryCode);
        dest.writeString(_fullAddress);
    }

    public transient static final Parcelable.Creator<UserItem> CREATOR = new Parcelable.Creator<UserItem>() {

        @Override
        public UserItem createFromParcel(Parcel source) {
            return new UserItem(source);
        }

        @Override
        public UserItem[] newArray(int size) {
            return new UserItem[size];
        }
    };
}
