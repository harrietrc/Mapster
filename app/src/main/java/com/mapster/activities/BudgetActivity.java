package com.mapster.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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
    private TableLayout _tableLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _inflater = getLayoutInflater();

        _itineraryDataSource = new ItineraryDataSource(this);
        _itineraryDataSource.open();
        _itineraryUpdateRequired = false; // TODO use this?

        _items = getItemsFromDatabase();
        // Note that for whatever reason, SuggestionItem._userItem is set to null when deserialised
        // - possibly because it was a bidirectional relationship? Will look into it. TODO!

        setContentView(R.layout.activity_budget);

        // Populate the table with the itinerary items from the database
        createRowsFromItems();
    }

    @Override
    public void onResume() {
        _items = getItemsFromDatabase();
        // Clear the table and recreate the rows - heavy handed and could be replaced with that flag?
        _tableLayout.removeAllViews();
        createRowsFromItems();
        super.onResume();
    }

    public void createRowsFromItems() {
        _tableLayout = (TableLayout) findViewById(R.id.budget_table);

        for (ItineraryItem item: _items) {
            TableRow newRow = null;
            if (item instanceof UserItem) {
                // 'Parent' user-defined destination that the suggestions were retrieved for
                UserItem u = (UserItem) item;
                newRow = createUserDestinationRow(u);
                _tableLayout.addView(newRow);

                // 'Child' suggestions that the user added to the itinerary for their destination
                for (SuggestionItem s: u.getSuggestionItems()) {
                    // TODO Hack until I figure out why _userItem isn't getting deserialised
                    s.setUserItem(u);
                    TableRow childRow = createSuggestionRow(s);
                    _tableLayout.addView(childRow);
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

    public TableRow createSuggestionRow(final SuggestionItem item) {
        TableRow row = new TableRow(this);
        final TableRow rowView = (TableRow) _inflater.inflate(R.layout.budget_suggestion_table_row, row, false);

        Suggestion suggestion = item.getSuggestion();
        String currencySymbol = suggestion.getCurrencySymbol();

        // TextView: Name of the destination
        TextView nameView = (TextView) rowView.findViewById(R.id.budget_col_suggestion_name);
        String name = suggestion.getName();
        nameView.setText(name);

        // TextView: Cost per person (or base cost with no modifiers)
        TextView baseCostView = (TextView) rowView.findViewById(R.id.budget_col_base_cost);
        Double baseCost = suggestion.getCostPerPerson(this);
        if (baseCost != null)
            baseCostView.setText(currencySymbol + String.format("%.2f", baseCost));

        // TextView: Calculated total, including multipliers/modifiers
        TextView totalCostView = (TextView) rowView.findViewById(R.id.budget_col_total);
        Double totalCost = item.getTotalCost(this);
        if (totalCost != null)
            totalCostView.setText(currencySymbol + String.format("%.2f", totalCost));

        // TextView: Displays the actual money spent, if provided by the user
        TextView actualCostView = (TextView) rowView.findViewById(R.id.budget_col_user_value);
        Double actualCost = item.getActualCost();
        if (actualCost != null)
            actualCostView.setText(currencySymbol + String.format("%.2f", actualCost));

        // Button: Opens a dialogue for editing the number of people, money spent, etc.
        ImageButton editButton = (ImageButton) rowView.findViewById(R.id.budget_col_edit);
        editButton.setImageResource(R.drawable.ic_table_edit_grey600_48dp);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buildEditDialogue(item, rowView);
            }
        });

        return rowView;
    }

    /**
     * Uses a SuggestionItem to set the editable field text for a table row
     * @param multiplierView EditText for specifying the multiplier
     * @param moneySpentView EditText for specifying the actual cost
     * @param item Item to retrieve the multiplier and money-spent values from
     */
    public void setEditableValues(EditText multiplierView, EditText moneySpentView, SuggestionItem item) {
        multiplierView.setText(Integer.toString(item.getMultiplier()));
        Double initialMoneySpent = item.getActualCost();
        if (initialMoneySpent != null)
            moneySpentView.setText(Double.toString(initialMoneySpent));
    }

    public void buildEditDialogue(final SuggestionItem item, final TableRow row) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Inflate the dialogue layout and get references to all the relevant views
        LinearLayout l = new LinearLayout(this);
        LinearLayout content = (LinearLayout) _inflater.inflate(R.layout.budget_edit_dialogue, l);
        final EditText multiplierView = (EditText) content.findViewById(R.id.budget_multiplier_field);
        final EditText moneySpentView = (EditText) content.findViewById(R.id.budget_money_spent_field);
        Button deleteButton = (Button) content.findViewById(R.id.budget_remove_item_button);

        // Set EditText values to existing values
        setEditableValues(multiplierView, moneySpentView, item);

        builder.setView(content);
        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Save the value for the multiplier (number of people)
                String multiplierString = multiplierView.getText().toString();
                if (multiplierString.equals("")) {
                    item.setMultiplier(1); // Back to the default value
                } else {
                    int multiplier = Integer.parseInt(multiplierString);
                    item.setMultiplier(multiplier);
                }

                // Save the value for the actual amount of money spent
                String moneySpentString = moneySpentView.getText().toString();
                if (moneySpentString.equals("")) {
                    item.setActualCost(0); // Money spent was cleared
                } else {
                    double moneySpent = Double.parseDouble(moneySpentString);
                    item.setActualCost(moneySpent);
                }

                // TODO Recalculate
                setEditableValues(multiplierView, moneySpentView, item);
            }
        });
        final AlertDialog dialog = builder.create();
        dialog.show();

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserItem userItem = item.getUserItem();
                userItem.removeSuggestionItem(item);

                // Delete the row in the table and hide the dialogue
                _tableLayout.removeView(row);
                dialog.hide();
            }
        });
    }

    @Override
    public void onBackPressed() {
        writeItemsToDatabase(); // Need to debug this TODO
        super.onBackPressed();
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
