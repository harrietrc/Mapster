package com.mapster.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;

import com.astuetz.PagerSlidingTabStrip;
import com.mapster.R;
import com.mapster.itinerary.ItineraryItem;
import com.mapster.itinerary.persistence.ItineraryDataSource;
import com.mapster.itinerary.ui.BudgetFragment;
import com.mapster.itinerary.ui.BudgetPagerAdapter;
import com.mapster.itinerary.ui.ScheduleFragment;

import java.util.List;

/**
 * Created by Harriet on 6/16/2015.
 */
public class BudgetActivity extends FragmentActivity {

    // Data about the itinerary
    private ItineraryDataSource _itineraryDataSource;
    private List<ItineraryItem> _items;

    // Two fragments: schedule and budget
    ScheduleFragment _scheduleFragment;
    BudgetFragment _budgetFragment;

    public List<ItineraryItem> getItems() {
        return _items;
    }

    public ItineraryDataSource getDataSource() {
        return _itineraryDataSource;
    }

    @Override
    protected void onResume() {
        _items = getItemsFromDatabase();
        _budgetFragment.resetTable();
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set up the data - accessed and shared by both fragments
        _itineraryDataSource = new ItineraryDataSource(this);
        _itineraryDataSource.open();
        _items = getItemsFromDatabase();
        // Note that for whatever reason, SuggestionItem._userItem is set to null when deserialised
        // - possibly because it was a bidirectional relationship? Will look into it. TODO!

        // Set layout
        setContentView(R.layout.activity_budget);

        // Sliding tabs
        BudgetPagerAdapter adapter = new BudgetPagerAdapter(getSupportFragmentManager());
        ViewPager pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(adapter);

        // Bind the tabs to the adapter
        PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        tabs.setViewPager(pager);

        // Grab references to the two fragments
        _scheduleFragment = (ScheduleFragment) adapter.instantiateItem(pager, 0);
        _budgetFragment = (BudgetFragment) adapter.instantiateItem(pager, 1);

        // Required: call through to the superclass method
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onBackPressed() {
        writeItemsToDatabase();
        super.onBackPressed();
    }

    private void writeItemsToDatabase() {
        _itineraryDataSource.recreateItinerary();
        _itineraryDataSource.insertMultipleItineraryItems(_items);
    }

    private List<ItineraryItem> getItemsFromDatabase() {
        return _itineraryDataSource.getAllItems();
    }
}
