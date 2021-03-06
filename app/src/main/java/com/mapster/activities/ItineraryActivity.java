package com.mapster.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.mapster.R;
import com.mapster.itinerary.ItineraryItem;
import com.mapster.persistence.ItineraryDataSource;

import java.util.Collection;
import java.util.List;

/**
 * Created by Harriet on 6/16/2015.
 */
public class ItineraryActivity extends ActionBarActivity {

    // Data about the itinerary
    private ItineraryDataSource _itineraryDataSource;

    private SlidingTabsBasicFragment _fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set up the data - accessed and shared by both fragments
        _itineraryDataSource = new ItineraryDataSource(this);
        _itineraryDataSource.open();

        // Set layout
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_itinerary);

        if (savedInstanceState == null)
            recreateFragment();
    }

    @Override
    protected void onResume() {
        _itineraryDataSource.open();
        super.onResume();
    }

    private void recreateFragment() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        _fragment = new SlidingTabsBasicFragment();
        transaction.replace(R.id.sample_content_fragment, _fragment);
        transaction.commit();
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
        inflater.inflate(R.menu.menu_itinerary, menu);
        return true;
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        Collection<? extends ItineraryItem> items = _fragment.getItems();
        writeItemsToDatabase(items);
        super.onBackPressed();
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

    public void writeItemsToDatabase(Collection<? extends ItineraryItem> items) {
        _itineraryDataSource.deleteUnsavedItineraryItems();
        _itineraryDataSource.insertMultipleItineraryItems(items);
    }

    public List<ItineraryItem> getItemsFromDatabase() {
        // Get itinerary items that match current itinerary name (stored in shared prefs)
        return _itineraryDataSource.getUnsavedAndSavedItems(this);
    }
}
