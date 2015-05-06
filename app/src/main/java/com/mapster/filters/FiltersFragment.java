package com.mapster.filters;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.mapster.R;
import com.mapster.activities.MainActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Harriet on 5/2/2015.
 */
public class FiltersFragment extends Fragment {

    List<String> filterTitles; // The names of each filter
    HashMap<String, List<String>> filterOptions; // Options for each filter (e.g. 'Accommodation')
    ExpandableFilterListAdapter adapter; // List adapter - has methods for operating on the filter list

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // View containing the filter list
        View view = inflater.inflate(R.layout.filters, container, false);

        // Prep the data
        prepareListData();

        // Expandable list of filters
        ExpandableListView list = (ExpandableListView) view.findViewById(R.id.filter_list);

        // Adapter to allow expandable list behaviour
        adapter = new ExpandableFilterListAdapter(getActivity(), filterTitles, filterOptions);
        list.setAdapter(adapter);

        list.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            // TODO There might be a more appropriate place to put this
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                if (groupPosition == (adapter.getGroupCount()-1))
                    ((MainActivity) getActivity()).onClearClick(v);
                return false;
            }
        });

        return view;
    }

    /**
     * Unchecks all the filter options in a filter group. Called when 'Clear' is clicked
     * @param filterName The name of the filter group
     */
    public void clearFilterRadioButtons(String filterName) {
        adapter.clearFilterRadioButtons(filterName);
    }

    public void clearAllFilterRadioButtons() {
        adapter.clearAllFilterRadioButtons();
    }

    /**
     * Call this when setting a filter option's radio button to checked. Sets the correct checked
     * state for all
     * @param group
     * @param checkedChild
     */
    public void setFilterOptionChecked(String group, String checkedChild) {
        adapter.setFilterOptionChecked(group, checkedChild);
    }

    /**
     * Prepare all the data for the filter list. Whenever new filters are added, their names and
     * options need to be added here.
     */
    private void prepareListData() {
        filterTitles = new ArrayList<>();
        filterOptions = new HashMap<>();

        // Filter names
        filterTitles.add("Category");
        filterTitles.add("Price level");
        filterTitles.add("Clear suggestions");

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
        filterOptions.put(filterTitles.get(0), categories);
        filterOptions.put(filterTitles.get(1), priceLevels);
        filterOptions.put(filterTitles.get(2), new ArrayList<String>());
    }
}
