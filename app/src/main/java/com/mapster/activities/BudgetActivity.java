package com.mapster.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.mapster.R;
import com.mapster.itinerary.ItineraryItem;
import com.mapster.itinerary.SuggestionItem;
import com.mapster.itinerary.UserItem;
import com.mapster.itinerary.persistence.ItineraryDataSource;
import com.mapster.suggestions.Suggestion;

import java.util.List;

/**
 * Created by Harriet on 6/16/2015.
 */
public class BudgetActivity extends Activity {

    private ItineraryDataSource _itineraryDataSource;
    private List<ItineraryItem> _items;
    private boolean _itineraryUpdateRequired;
    private LayoutInflater _inflater;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _inflater = getLayoutInflater();

        _itineraryDataSource = new ItineraryDataSource(this);
        _itineraryDataSource.open();
        _itineraryUpdateRequired = false;

        _items = getItemsFromDatabase();
        // Note that for whatever reason, SuggestionItem._userItem is set to null when deserialised
        // - possibly because it was a bidirectional relationship? Will look into it. TODO!

        setContentView(R.layout.activity_budget);

        // Populate the table with the itinerary items from the database
        createRowsFromItems();
    }

    public void createRowsFromItems() {
        TableLayout tableLayout = (TableLayout) findViewById(R.id.budget_table);

        for (ItineraryItem item: _items) {
            TableRow newRow = null;
            if (item instanceof UserItem) {
                // 'Parent' user-defined destination that the suggestions were retrieved for
                UserItem u = (UserItem) item;
                newRow = createUserDestinationRow(u);
                tableLayout.addView(newRow);

                // 'Child' suggestions that the user added to the itinerary for their destination
                for (SuggestionItem s: u.getSuggestionItems()) {
                    TableRow childRow = createSuggestionRow(s);
                    tableLayout.addView(childRow);
                }
            }
        }

    }

    public TableRow createUserDestinationRow(UserItem item) {
        TableRow row = new TableRow(this);
        TableRow v = (TableRow) _inflater.inflate(R.layout.budget_user_destination_table_row, row, false);

        // Add other info or styling if you want; currently just displays the destination name
        TextView titleView = (TextView) v.findViewById(R.id.budget_col_user_dest_name);
        String title = item.getName();
        titleView.setText(title);

        return v;
    }

    public TableRow createSuggestionRow(SuggestionItem item) {
        TableRow row = new TableRow(this);
        TableRow v = (TableRow) _inflater.inflate(R.layout.budget_suggestion_table_row, row, false);

        Suggestion suggestion = item.getSuggestion();
        String currencySymbol = suggestion.getCurrencySymbol();

        // TextView: Name of the destination
        TextView nameView = (TextView) v.findViewById(R.id.budget_col_suggestion_name);
        String name = suggestion.getName();
        nameView.setText(name);

        // TextView: Cost per person (or base cost with no modifiers)
        TextView baseCostView = (TextView) v.findViewById(R.id.budget_col_base_cost);
        Double baseCost = suggestion.getCostPerPerson();
        if (baseCost != null)
            baseCostView.setText(currencySymbol + String.valueOf(baseCost));

        // Button: Opens configuration options like number of people
        ImageButton optionsButton = (ImageButton) v.findViewById(R.id.budget_col_button_options);
        optionsButton.setImageResource(R.drawable.ic_plus_circle_grey600_48dp);

        // TextView: Calculated total, including multipliers/modifiers
        TextView totalCostView = (TextView) v.findViewById(R.id.budget_col_total);
        Double totalCost = item.getTotalCost();
        if (totalCost != null)
            totalCostView.setText(currencySymbol + String.valueOf(totalCost));

        // TextView: Displays the actual money spent, if provided by the user
        TextView actualCostView = (TextView) v.findViewById(R.id.budget_col_user_value);
        Double actualCost = item.getActualCost();
        if (actualCost != null)
            actualCostView.setText(currencySymbol + String.valueOf(actualCost));

        // Button: Opens a dialogue that allows the user to enter the actual money spent
        ImageButton editCostButton = (ImageButton) v.findViewById(R.id.budget_col_button_user_value);
        editCostButton.setImageResource(R.drawable.ic_table_edit_grey600_48dp);

        return v;
    }

    public void writeItemsToDatabase() {
        if (_itineraryUpdateRequired) {
            _itineraryDataSource.recreateItinerary();
            _itineraryDataSource.insertMultipleItineraryItems(_items);
        }
    }

    public List<ItineraryItem> getItemsFromDatabase() {
        return _itineraryDataSource.getAllItems();
    }
}
