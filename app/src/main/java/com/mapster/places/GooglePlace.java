package com.mapster.places;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

/**
 * Created by Harriet on 3/15/2015.
 * See documentation for Parcel http://developer.android.com/reference/android/os/Parcel.html
 * From http://wptrafficanalyzer.in/blog/showing-nearby-places-with-photos-at-any-location-in-google-maps-android-api-v2/
 */
public class GooglePlace implements Parcelable {
    public String latitude;
    public String longitude;
    public String name;

    public GooglePlace() {
        // Private otherwise
    }

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    /** Writing Place object data to Parcel */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(latitude);
        dest.writeString(longitude);
        dest.writeString(name);
    }

    /** Initializing Place object from Parcel object */
    private GooglePlace(Parcel in){
        this.latitude = in.readString();
        this.longitude = in.readString();
        this.name = in.readString();
    }

    /** Generates an instance of Place class from Parcel */
    public static final Parcelable.Creator<GooglePlace> CREATOR = new Parcelable.Creator<GooglePlace>(){
        @Override
        public GooglePlace createFromParcel(Parcel source) {
            return new GooglePlace(source);
        }

        @Override
        public GooglePlace[] newArray(int size) {
            // TODO Auto-generated method stub
            return null;
        }
    };
}