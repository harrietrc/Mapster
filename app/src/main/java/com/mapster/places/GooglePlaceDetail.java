package com.mapster.places;

import android.util.Log;

import java.util.HashMap;

/**
 * Created by Harriet on 3/22/2015.
 * Stores/operates on detail related to a GooglePlace. Like GooglePlace, should really only be
 * accessed by the corresponding parser tasks. This data is used to construct a snippet, which will
 * be displayed as Marker info on the map.
 * Note: Photo and rating are stored in GooglePlace. This only populates the InfoWindow snippet.
 */
public class GooglePlaceDetail {
    // TODO Make these private
    public String shortAddress;
    public String website; // May be null
    public String phoneNumber; // May be null

    // TODO: show today's hours in InfoWindow of suggestion markers
    private HashMap<String, String> _openHours;

    // TODO: reviews summary (Google offers this as a premium service - worth looking into?)

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(shortAddress);
        if (phoneNumber != null)
            sb.append("\n" + phoneNumber);
        if (website != null)
            sb.append("\n" + website);
        return sb.toString();
    }

}
