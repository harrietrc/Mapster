package com.mapster.filters;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ListAdapter;

import com.mapster.R;

/**
 * Created by Harriet on 5/2/2015.
 */
public class FiltersFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // View containing the filter list
        View view = inflater.inflate(R.layout.filters, container, false);

        // Expandable list of filters
        ExpandableListView list = (ExpandableListView) view.findViewById(R.id.filter_list);

        // Adapter to allow expandable list behaviour
        ExpandableListAdapter adapter = new ExpandableFilterListAdapter(getActivity());
        list.setAdapter(adapter);

        return view;
    }
}
