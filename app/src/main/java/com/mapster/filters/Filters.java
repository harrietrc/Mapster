package com.mapster.filters;

import android.content.Context;
import android.view.View;
import android.widget.ExpandableListView;

import com.mapster.activities.MainActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Harriet on 5/2/2015.
 */
public class Filters {

    private List<String> _filterTitles; // The names of each filter
    private HashMap<String, List<String>> _filterOptions; // Options for each filter (e.g. 'Accommodation')
    private ExpandableFilterListAdapter _adapter; // List adapter - has methods for operating on the filter list
    private ExpandableListView _filterList;

    public Filters(ExpandableListView listView) {
        _filterList = listView;
    }

    public ExpandableListView getFilterList() {
        return _filterList;
    }

    public void populateFilterList(final Context context) {
        // Prep the data
        prepareListData();

        // Adapter to allow expandable list behaviour
        _adapter = new ExpandableFilterListAdapter(context, _filterTitles, _filterOptions);
        _filterList.setAdapter(_adapter);

        _filterList.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            // TODO There might be a more appropriate place to put this
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                if (groupPosition == (_adapter.getGroupCount()-1))
                    ((MainActivity) context).onClearClick(v);
                return false;
            }
        });
    }

    public void refreshFilterRadioButtons() {
        _adapter.refreshRadioButtons();
    }

    /**
     * Unchecks all the filter options in a filter group. Called when 'Clear' is clicked
     * @param filterName The name of the filter group
     */
    public void clearFilterRadioButtons(String filterName) {
        _adapter.clearFilterRadioButtons(filterName);
    }

    public void clearAllFilterRadioButtons() {
        _adapter.clearAllFilterRadioButtons();
    }

    /**
     * Call this when setting a filter option's radio button to checked. Sets the correct checked
     * state for all
     * @param group
     * @param checkedChild
     */
    public void setFilterOptionChecked(String group, String checkedChild) {
        _adapter.setFilterOptionChecked(group, checkedChild);
    }

    /**
     * Prepare all the data for the filter list. Whenever new filters are added, their names and
     * options need to be added here.
     */
    private void prepareListData() {
        _filterTitles = new ArrayList<>();
        _filterOptions = new HashMap<>();

        // Filter names
        _filterTitles.add("Category");
        _filterTitles.add("Price level");
        _filterTitles.add("Clear suggestions");

        // Filter options - category
        List<String> categories = new ArrayList<>();
        categories.add("Attractions");
        categories.add("Dining");
        categories.add("Accommodation");

        // Filter options - price level
        List<String> priceLevels = new ArrayList<>();
        priceLevels.add("Cheap or free");
        priceLevels.add("Moderately priced");
        priceLevels.add("Expensive");

        // Associate filter titles with filter options
        _filterOptions.put(_filterTitles.get(0), categories);
        _filterOptions.put(_filterTitles.get(1), priceLevels);
        _filterOptions.put(_filterTitles.get(2), new ArrayList<String>());
    }
}
