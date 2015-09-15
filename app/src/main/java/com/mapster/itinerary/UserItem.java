package com.mapster.itinerary;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Harriet on 6/12/2015.
 * Intended to correspond with places that the user adds using Autocomplete in the PlacesActivity
 * Should be saved in a database
 */
public class UserItem extends ItineraryItem implements Parcelable {

    // TODO Not really necessary to use Parcelable (we're already serialising using GSON). My
    // intention was to use Parcelable to help speed up passing these between Activities. None of
    // the other ItineraryItem classes need to be passed from PlacesActivity to MainActivity. The
    // only disadvantage of GSON is that it is slow (Parcelable is comparatively fast)

    private String _name;
    private double _latitude;
    private double _longitude;
    private String _travelMode;

    // Represents any saved suggestions that were suggested from this destination
    private List<SuggestionItem> _suggestionItems;

    public UserItem(String name, LatLng latLng, String travelMode) {
        _name = name;
        _latitude = latLng.latitude;
        _longitude = latLng.longitude;
        _travelMode = travelMode;
        // Linked list to speed up removals a little
        _suggestionItems = new LinkedList<>();
    }

    public UserItem(Parcel source) {
        _name = source.readString();

        // Order matters, be careful! Must match write order.
        _latitude = source.readDouble();
        _longitude = source.readDouble();
        _travelMode = source.readString();
        _suggestionItems = new LinkedList<>();
    }

    public void removeSuggestionItem(SuggestionItem item) {
        if(_suggestionItems.remove(item))
            item.setIsInItinerary(false);
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
