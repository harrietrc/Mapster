package com.mapster.itinerary.utils;

import com.mapster.itinerary.ItineraryItem;

import org.joda.time.DateTime;
import org.joda.time.DateTimeComparator;
import org.joda.time.IllegalFieldValueException;

import java.util.Comparator;

/**
 * Created by Harriet on 6/21/2015.
 */
public class ItineraryItemTimeComparator implements Comparator<ItineraryItem> {

    DateTimeComparator _comparator;

    public ItineraryItemTimeComparator() {
        _comparator = DateTimeComparator.getInstance();
    }

    /**
     * Return -1 if earlier, 0 if same time, 1 if later
     */
    @Override
    public int compare(ItineraryItem lhs, ItineraryItem rhs) {
        DateTime lhsTime = null; DateTime rhsTime = null;
        try {
            lhsTime = lhs.getTime();
            rhsTime = rhs.getTime();
        } catch (IllegalFieldValueException e) {
            // Null values are allowed for date/time (but result in an exception when the
            // JodaTime.DateTime constructor is called) - arbitrarily treat as later in time
            return -1;
        }
        // Orders by itinerary item's time
        return _comparator.compare(lhsTime, rhsTime);
    }
}
