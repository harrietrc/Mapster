package com.mapster.itinerary;

/**
 * Created by Harriet on 6/12/2015.
 * Add functionality as needed.
 */
public abstract class ItineraryItem {

    private Long _id; // Corresponds with key from itinerary database

    public Long getId() {
        return _id;
    }

    public void setId(long id) {
        _id = id;
    }

}
