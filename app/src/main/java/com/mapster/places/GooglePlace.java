package com.mapster.places;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

/**
 * Created by Harriet on 3/15/2015.
 * From http://wptrafficanalyzer.in/blog/showing-nearby-places-with-photos-at-any-location-in-google-maps-android-api-v2/
 */
public class GooglePlace {
    public String latitude;
    public String longitude;
    public String name;

    public GooglePlace() {
        // Private otherwise
    }
}