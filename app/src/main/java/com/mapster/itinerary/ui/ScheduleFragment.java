package com.mapster.itinerary.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mapster.R;
import com.mapster.activities.ItineraryActivity;
import com.mapster.itinerary.ItineraryItem;
import com.mapster.itinerary.SuggestionItem;
import com.mapster.itinerary.UserItem;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Harriet on 6/20/2015.
 */
public class ScheduleFragment extends Fragment {

    private List<ItineraryItem> _sortedItems;
    private LinearLayout _layout;
    private LayoutInflater _inflater;
    private DateTimeFormatter _timeFormatter; // Prints only the time (no month or day)
    private DateTimeFormatter _dateFormatter; // Prints only the date

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        _inflater = inflater;

        // Formats arrival time for the UI
        _dateFormatter = DateTimeFormat.mediumDate(); // TODO Fiddle with this
        _timeFormatter = DateTimeFormat.shortTime();

        // Construct a list of all the itinerary items, ordered by date
        refreshDataFromDatabase();

        // Set up the main table view for this fragment
        View v = _inflater.inflate(R.layout.schedule_fragment, container, false);
        _layout = (LinearLayout) v.findViewById(R.id.schedule_layout);

        createRowsFromItems();
        return v;
    }

    private void refreshDataFromDatabase() {
        List<ItineraryItem> items = ((ItineraryActivity) getActivity()).getItems();
        _sortedItems = new LinkedList<>();
        // Add the user-defined items
        _sortedItems.addAll(items);
        // Add the suggestion items (children of user-defined items)
        for (ItineraryItem item: items)
            if (item instanceof UserItem)
                _sortedItems.addAll(((UserItem) item).getSuggestionItems());
        Collections.sort(_sortedItems); // Sort by date/time
    }

    private void createRowsFromItems() {
        DateTime currentTime = new DateTime();
        for (ItineraryItem item: _sortedItems) {
            DateTime itemTime = item.getTime();
            // Add a row with just the date, if this item has a different date to the previous one
            if (currentTime != null)
                if (itemTime == null || !currentTime.toLocalDate().equals(itemTime.toLocalDate()))
                    createDateRow(itemTime);
            currentTime = itemTime;
            // Create a row for the itinerary item with its name
            if (item instanceof UserItem) {
                createRow(item, R.layout.schedule_user_destination_row);
            } else if (item instanceof  SuggestionItem)  {
                createRow(item, R.layout.schedule_suggestion_row);
            }
        }
    }

    /**
     * Creates a row with just the date
     * @param time
     */
    public void createDateRow(DateTime time) {
        LinearLayout row = new LinearLayout(getActivity());
        LinearLayout v = (LinearLayout) _inflater.inflate(R.layout.schedule_date_row, row, false);

        TextView dateView = (TextView) v.findViewById(R.id.date);

        if (time == null) {
            dateView.setText("Unspecified time");
        } else {
            dateView.setText(_dateFormatter.print(time));
        }

        _layout.addView(v);
    }

    public void createRow(ItineraryItem item, int layoutId) {
        RelativeLayout row = new RelativeLayout(getActivity());
        RelativeLayout v = (RelativeLayout) _inflater.inflate(layoutId, row, false);

        // Name of the destination
        TextView titleView = (TextView) v.findViewById(R.id.name);
        titleView.setText(item.getName());

        // Scheduled arrival time
        TextView timeView = (TextView) v.findViewById(R.id.time);
        DateTime time = item.getTime();
        if (time != null) // Set to null if the date is missing
            timeView.setText(_timeFormatter.print(time)); // TODO Change to only display time

        _layout.addView(v);
    }
}
