package com.mapster.activities;

import android.app.Activity;
import android.os.Bundle;

import com.mapster.itinerary.ItineraryItem;
import com.mapster.itinerary.persistence.ItineraryDataSource;

import java.util.List;

/**
 * Created by Harriet on 6/16/2015.
 */
public class BudgetActivity extends Activity {

    private ItineraryDataSource _itineraryDataSource;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _itineraryDataSource = new ItineraryDataSource(this);
        _itineraryDataSource.open();

        List<ItineraryItem> items = getItemsFromDatabase();
        System.out.println("SIZE ITEMS: " + items.size());
    }

    public List<ItineraryItem> getItemsFromDatabase() {
        return _itineraryDataSource.getAllItems();
    }
}
