package com.mapster.itinerary.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mapster.activities.BudgetActivity;
import com.mapster.activities.MainActivity;
import com.mapster.itinerary.ItineraryItem;
import com.mapster.itinerary.UserItem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Harriet on 6/20/2015.
 */
public class ScheduleFragment extends Fragment {

    private List<ItineraryItem> _sortedItems;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Construct a list of all the itinerary items, ordered by date
        List<ItineraryItem> items = ((BudgetActivity) getActivity()).getItems();
        _sortedItems = new LinkedList<>();
        // Add the user-defined items
        _sortedItems.addAll(items);
        // Add the suggestion items (children of user-defined items)
        for (ItineraryItem item: items)
            if (item instanceof UserItem)
                _sortedItems.addAll(((UserItem) item).getSuggestionItems());
        Collections.sort(_sortedItems); // Sort by date/time

        return super.onCreateView(inflater, container, savedInstanceState);
    }
}
