package com.mapster.itinerary;

import com.google.android.gms.maps.model.LatLng;
import com.mapster.itinerary.utils.ItineraryItemTimeComparator;

import org.joda.time.DateTime;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Harriet on 6/12/2015.
 * Add functionality as needed.
 */
public abstract class ItineraryItem implements Comparable<ItineraryItem> {

    /*
    Unique identifier for itinerary item, used when matching itinerary items when carrying them
    between activities. Can't use the ID from the database because the tables are occasionally
    dropped to reset them.
    */
    static final AtomicLong NEXT_ID = new AtomicLong(0);
    final long _id = NEXT_ID.getAndIncrement();

    // Time stuff (could encapsulate in a separate class)
    private Integer _year, _month, _day, _hour, _minute;

    /**
     * Compares the itinerary item based on its time/date fields
     */
    @Override
    public int compareTo(ItineraryItem another) {
        ItineraryItemTimeComparator c = new ItineraryItemTimeComparator();
        return c.compare(this, another);
    }

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
        if (_year == null || _month == null || _day == null) {
            return null;
        } else {
            // Default values for hours and minutes
            if (_hour == null)
                _hour = 0;
            if (_minute == null)
                _minute = 0;
            return new DateTime(_year, _month, _day, _hour, _minute);
        }
    }

    /**
     * Extracts time values from the DateTime object, essentially to simplify serialisation.
     * @param time A date and time for the item. Not all properties are necessarily non-null
     */
    public void setDateTime(DateTime time) {
        _year = time.getYear();
        _month = time.getMonthOfYear();
        _day = time.getDayOfMonth();
        _hour = time.getHourOfDay();
        _minute = time.getMinuteOfHour();
    }

    public long getId() {
        return _id;
    }

    public abstract String getName();

    public abstract LatLng getLocation();
}
