package com.mapster.activities;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ViewAnimator;

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
public class ItineraryActivity extends ActionBarActivity {

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
    protected void onCreate(Bundle savedInstanceState) {
        // Set up the data - accessed and shared by both fragments
        _itineraryDataSource = new ItineraryDataSource(this);
        _itineraryDataSource.open();
        _items = getItemsFromDatabase();
        // Note that for whatever reason, SuggestionItem._userItem is set to null when deserialised
        // - possibly because it was a bidirectional relationship? Will look into it. TODO!

        System.out.println("WHY THE FUCK DONT YOU SHOW UP 1");
        // Set layout
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget);

//        // Sliding tabs
//        BudgetPagerAdapter adapter = new BudgetPagerAdapter(getSupportFragmentManager());
//        ViewPager pager = (ViewPager) findViewById(R.id.pager);
//        pager.setAdapter(adapter);
//
//        // Bind the tabs to the adapter
//        PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
//        tabs.setViewPager(pager);
//
//        // Grab references to the two fragments
//        _scheduleFragment = (ScheduleFragment) adapter.instantiateItem(pager, 0);
//        _budgetFragment = (BudgetFragment) adapter.instantiateItem(pager, 1);
        // Required: call through to the superclass method
        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            SlidingTabsBasicFragment fragment = new SlidingTabsBasicFragment();
            transaction.replace(R.id.sample_content_fragment, fragment);
            transaction.commit();
        }
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
    protected void onStart(){
        super.onStart();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        System.out.println("WHY THE FUCK DONT YOU SHOW UP");
        inflater.inflate(R.menu.menu_itinerary, menu);
        return true;
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
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
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_settings:{
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
