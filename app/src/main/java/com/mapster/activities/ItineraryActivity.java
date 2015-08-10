package com.mapster.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;

import com.astuetz.PagerSlidingTabStrip;
import com.mapster.R;
import com.mapster.itinerary.ItineraryItem;
import com.mapster.itinerary.SuggestionItem;
import com.mapster.itinerary.UserItem;
import com.mapster.persistence.ItineraryDataSource;
import com.mapster.itinerary.ui.BudgetFragment;
import com.mapster.itinerary.ui.BudgetPagerAdapter;
import com.mapster.itinerary.ui.ScheduleFragment;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Harriet on 6/16/2015.
 */
public class ItineraryActivity extends FragmentActivity {

    // Data about the itinerary
    private ItineraryDataSource _itineraryDataSource;
    private List<? extends ItineraryItem> _items; // Itinerary items (ordered by date in onCreate())
    private android.support.v7.app.ActionBar mActionBar;
    // Two fragments: schedule and budget
    ScheduleFragment _scheduleFragment;
    BudgetFragment _budgetFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set up the data - accessed and shared by both fragments
        _itineraryDataSource = new ItineraryDataSource(this);
        _itineraryDataSource.open();
        _items = getItemsFromDatabase();
        // Note that for whatever reason, SuggestionItem._userItem is set to null when deserialised
        // - possibly because it was a bidirectional relationship? Will look into it. TODO!

        // Construct a list of all the itinerary items, ordered by date
        refreshDataFromDatabase();

        // Set layout
        setContentView(R.layout.activity_itinerary);

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

    /**
     * @return The time-sorted itinerary items list (shared between fragments)
     */
    public List<? extends ItineraryItem> getItems() {
        return _items;
    }

    /**
     * Refreshes the data from the database, sorting by date and updating the list of items in the
     * itinerary
     */
    private void refreshDataFromDatabase() {
        List<ItineraryItem> sortedItems = new LinkedList<>();

        // Reconstruct the itinerary (children of user-defined items)
        for (ItineraryItem item: _items)
            if (item instanceof UserItem) {
                UserItem u = (UserItem) item;
                sortedItems.add(u);
                for (SuggestionItem s : u.getSuggestionItems()) {
                    // TODO Hack until I figure out why userItems aren't deserialised
                    s.setUserItem(u);
                    sortedItems.add(s);
                }
            }
        Collections.sort(sortedItems); // Sort by date/time
        _items = sortedItems;
    }

    @Override
    protected void onResume() {
        _itineraryDataSource.open();
        super.onResume();
    }

    @Override
    protected void onPause() {
        _itineraryDataSource.close();
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        writeItemsToDatabase();
        super.onBackPressed();
    }

    private void writeItemsToDatabase() {
        _itineraryDataSource.deleteUnsavedItineraryItems();
        _itineraryDataSource.insertMultipleItineraryItems(_items);
    }

    private List<ItineraryItem> getItemsFromDatabase() {
        // Get itinerary items that match current itinerary name (stored in shared prefs)
        String sharedPrefsName = getResources().getString(R.string.shared_prefs);
        String itineraryNamePrefs = getResources().getString(R.string.itinerary_name_prefs);
        SharedPreferences settings = getSharedPreferences(sharedPrefsName, 0);
        String currentItineraryName = settings.getString(itineraryNamePrefs, null);
        return _itineraryDataSource.getItemsByItineraryName(currentItineraryName);
    }
}
