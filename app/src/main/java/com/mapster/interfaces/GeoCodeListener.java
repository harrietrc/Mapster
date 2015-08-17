package com.mapster.interfaces;

import com.mapster.itinerary.UserItem;

import java.util.ArrayList;

/**
 * Created by tommyngo on 13/07/15.
 */
public interface GeoCodeListener {
    void callback(ArrayList<UserItem> userItems);
}
