package com.mapster.itinerary;

import org.joda.time.DateTime;

/**
 * Created by Harriet on 6/12/2015.
 * Add functionality as needed.
 */
public abstract class ItineraryItem {

    private Long _id; // Corresponds with key from itinerary database

    // Time stuff (could encapsulate in a separate class)
    private int _year, _month, _day, _hour, _minute;

    /**
     * Time fields are kept primitive/separate rather than saving as a DateTime field in order to
     * make serialisation easier. An alternative is to use something like this
     * https://github.com/gkopff/gson-jodatime-serialisers for serialising JodaTime.
     */
    public void setDate(int year, int month, int day) {
        _year = year; _month = month; _day = day;
    }

    public void setTime(int hour, int minute) {
        _hour = hour; _minute = minute;
    }

    public DateTime getTime() {
        return new DateTime(_year, _month, _day, _hour, _minute);
    }

    public Long getId() {
        return _id;
    }

    public void setId(long id) {
        _id = id;
    }

}
