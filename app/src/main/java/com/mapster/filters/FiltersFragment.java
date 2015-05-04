package com.mapster.filters;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

import com.mapster.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Harriet on 5/2/2015.
 */
public class FiltersFragment extends Fragment {

    List<String> filterTitles; // The names of each filter
    HashMap<String, List<String>> filterOptions;

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
        ExpandableListAdapter adapter = new ExpandableFilterListAdapter(getActivity(), filterTitles, filterOptions);
        list.setAdapter(adapter);

        return view;
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
    }
}
