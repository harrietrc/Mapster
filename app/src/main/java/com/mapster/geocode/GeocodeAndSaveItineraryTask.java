package com.mapster.geocode;

import android.widget.RadioGroup;

import com.mapster.activities.PlacesActivity;
import com.mapster.android.gui.util.clearableautocompletetextview.ClearableAutoCompleteTextView;
import com.mapster.itinerary.UserItem;
import com.mapster.persistence.ItineraryDataSource;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Harriet on 9/08/2015. Geocodes addresses and saves the itinerary to the database
 * without moving to the MainActivity.
 */
public class GeocodeAndSaveItineraryTask extends GeoCode {

    private String _itineraryName;
    private ItineraryDataSource _datasource;

    public GeocodeAndSaveItineraryTask(LinkedList<ClearableAutoCompleteTextView> autoCompleteTextViewLinkedList,
                                       List<RadioGroup> transportModeViewList, PlacesActivity activity,
                                       ItineraryDataSource datasource, String itineraryName) {
        super(autoCompleteTextViewLinkedList, transportModeViewList, activity);
        _itineraryName = itineraryName;
        _datasource = datasource;
    }

    /**
     * Called after task execution. Might want to move it into the task itself so it doesn't run in
     * the UI thread.
     * @param userItems ItineraryItem representation of addresses taken from the text fields.
     */
    @Override
    protected void callback(ArrayList<UserItem> userItems) {
        long id = _datasource.createAndGetItineraryId(_itineraryName);
        _datasource.insertMultipleItineraryItems(userItems, id);
    }
}
