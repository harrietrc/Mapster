package com.mapster.itinerary.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.mapster.R;
import com.mapster.activities.ItineraryActivity;
import com.mapster.itinerary.ItineraryItem;
import com.mapster.itinerary.SuggestionItem;
import com.mapster.itinerary.UserItem;
import com.mapster.suggestions.Suggestion;

import java.util.ArrayList;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Harriet on 6/20/2015.
 */
public class BudgetFragment extends Fragment {

    private LayoutInflater _inflater;
    private LinearLayout _layout;

    // Totals
    Map<String, Double> _totalsMap; // Maintains state for totals list
    List<String> _totalsList;
    ArrayAdapter<String> _totalsListAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _inflater = inflater;

        // Inflate the layout and get a reference to the table
        View v = _inflater.inflate(R.layout.budget_fragment, container, false);
        _layout = (LinearLayout) v.findViewById(R.id.budget_layout);

        // Populate list of totals
        _totalsList = new ArrayList<>();
        _totalsListAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, _totalsList);
        ListView totalsListView = (ListView) v.findViewById(R.id.budget_totals_list);
        totalsListView.setAdapter(_totalsListAdapter);
        _totalsMap = new HashMap<>();

        // Populate the table with the itinerary items from the database
        createRowsFromItems();

        // Display initial values for totals
        updateTotals();

        return v;
    }

    // Not sure whether onResume() is called after the Activity's onResume(), so I replaced it with this
    public void resetTable() {
        // Clear the table and recreate the rows - heavy handed and could be replaced with that flag?
        if (_layout != null) {
            _layout.removeAllViews();
            createRowsFromItems();
        }
    }

    private void formatTotalsAsList() {
        _totalsList.clear();
        for (Map.Entry pair : _totalsMap.entrySet()) {
            String currencySymbol = Currency.getInstance((String) pair.getKey()).getSymbol();
            Double total = (Double) pair.getValue();
            if (total != null) {
                String totalString = String.format("%.2f", total);
                _totalsList.add(currencySymbol + totalString);
            }
        }
    }

    private void createRowsFromItems() {
        // TODO Shift itinerary data out of activity to a datasource class?
        List<ItineraryItem> items = ((ItineraryActivity) getActivity()).getItems();

        for (ItineraryItem item: items) {
            if (item instanceof UserItem) {
                // 'Parent' user-defined destination that the suggestions were retrieved for
                UserItem u = (UserItem) item;
                createUserDestinationRow(u);

                // 'Child' suggestions that the user added to the itinerary for their destination
                for (SuggestionItem s: u.getSuggestionItems()) {
                    // TODO Hack until I figure out why _userItem isn't getting deserialised
                    s.setUserItem(u);
                    createSuggestionRow(s);
                }
            }
        }
        // Update the list of totals
        updateTotals();
    }

    public void updateTotals() {
        calculateTotals();
        formatTotalsAsList();
        _totalsListAdapter.notifyDataSetChanged();
    }

    private void createUserDestinationRow(UserItem item) {
        LinearLayout row = new LinearLayout(getActivity());
        LinearLayout v = (LinearLayout) _inflater.inflate(R.layout.budget_user_destination_row, row, false);

        // Add other info or styling if you want; currently just displays the destination name
        TextView titleView = (TextView) v.findViewById(R.id.budget_col_user_dest_name);
        String title = item.getName();
        titleView.setText(title);

        _layout.addView(v);
    }

    public void createSuggestionRow(final SuggestionItem item) {
        LinearLayout row = new LinearLayout(getActivity());
        final LinearLayout rowView = (LinearLayout) _inflater.inflate(R.layout.budget_suggestion_row, row, false);

        Suggestion suggestion = item.getSuggestion();
        String currencySymbol = suggestion.getCurrencySymbol();

        // TextView: Name of the destination
        TextView nameView = (TextView) rowView.findViewById(R.id.budget_col_suggestion_name);
        String name = suggestion.getName();
        nameView.setText(name);

        // TextView: Cost per person (or base cost with no modifiers)
        TextView baseCostView = (TextView) rowView.findViewById(R.id.budget_col_base_cost);
        Double baseCost = suggestion.getCostPerPerson();
        if (baseCost != null)
            baseCostView.setText(currencySymbol + String.format("%.2f", baseCost));

        // TextView: Calculated total, including multipliers/modifiers
        TextView totalCostView = (TextView) rowView.findViewById(R.id.budget_col_total);
        Double totalCost = item.getTotalCost();
        if (totalCost != null)
            totalCostView.setText(currencySymbol + String.format("%.2f", totalCost));

        // TextView: Displays the actual money spent, if provided by the user
        TextView actualCostView = (TextView) rowView.findViewById(R.id.budget_col_user_value);
        Double actualCost = item.getActualCost();
        if (actualCost != null)
            actualCostView.setText(currencySymbol + String.format("%.2f", actualCost));

        // Button: Opens a dialogue for editing the number of people, money spent, etc.
        final ImageButton editButton = (ImageButton) rowView.findViewById(R.id.budget_col_edit);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buildEditDialogue(item, rowView);
            }
        });

        // Set the edit button's height to be the same as its width
        ViewTreeObserver vto = editButton.getViewTreeObserver();
        vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                int x;
                editButton.getViewTreeObserver().removeOnPreDrawListener(this);
                x = editButton.getMeasuredWidth();
                editButton.setLayoutParams(new LinearLayout.LayoutParams(x, x));
                return true;
            }
        });

        _layout.addView(rowView);
    }

    /**
     * Uses a SuggestionItem to set the editable field text for a table row
     * @param multiplierView EditText for specifying the multiplier
     * @param moneySpentView EditText for specifying the actual cost
     * @param item Item to retrieve the multiplier and money-spent values from
     */
    public void setEditableValues(EditText multiplierView, EditText moneySpentView, SuggestionItem item) {
        multiplierView.setText(Integer.toString(item.getMultiplier()));
        multiplierView.setSelection(multiplierView.getText().length()); // Put the cursor at the end
        Double initialMoneySpent = item.getActualCost();
        if (initialMoneySpent != null)
            moneySpentView.setText(Double.toString(initialMoneySpent));
        moneySpentView.setSelection(moneySpentView.getText().length());
    }

    public void buildEditDialogue(final SuggestionItem item, final LinearLayout row) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Inflate the dialogue layout and get references to all the relevant views
        LinearLayout l = new LinearLayout(getActivity());
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
                String currencySymbol = item.getSuggestion().getCurrencySymbol();

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
                TextView actualCostView = (TextView) row.findViewById(R.id.budget_col_user_value);
                if (moneySpentString.equals("")) {
                    item.setActualCost(0); // Money spent was cleared
                    actualCostView.setText(null);
                } else {
                    double moneySpent = Double.parseDouble(moneySpentString);
                    item.setActualCost(moneySpent);
                    actualCostView.setText(currencySymbol + String.format("%.2f", moneySpent));
                }

                // Update the list of totals
                updateTotals();

                setEditableValues(multiplierView, moneySpentView, item);

                // Update the total cost
                TextView totalCostView = (TextView) row.findViewById(R.id.budget_col_total);
                Double totalCost = item.getTotalCost();
                if (totalCost != null)
                    totalCostView.setText(currencySymbol + String.format("%.2f", totalCost));
            }
        });
        final AlertDialog dialog = builder.create();
        dialog.show(); // Leak?

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserItem userItem = item.getUserItem();
                userItem.removeSuggestionItem(item);

                // Update the list of totals
                updateTotals();

                // Delete the row in the table and hide the dialogue
                _layout.removeView(row);
                dialog.hide();
            }
        });
    }

    /**
     * Recalculates the totals for the entire itinerary
     */
    public void calculateTotals() {
        // Totally reinitialise the map of totals and recreate it from the itinerary
        _totalsMap = new HashMap<>();

        // TODO See other note about datasource
        List<ItineraryItem> items = ((ItineraryActivity) getActivity()).getItems();

        for (ItineraryItem item: items) {
            // This is the only possibility at the moment - UserItems only
            if (item instanceof UserItem)
                for (SuggestionItem s: ((UserItem) item).getSuggestionItems()) {
                    String currencyCode = s.getSuggestion().getCurrencyCode();
                    Double total = _totalsMap.get(currencyCode);

                    // Use the entered value as the cost if available - else use the estimate.
                    Double userCost = s.getActualCost();
                    Double suggestionCost = userCost == null? s.getTotalCost() : userCost;

                    if (total == null && suggestionCost != null) {
                        _totalsMap.put(currencyCode, suggestionCost);
                    } else if (suggestionCost != null) {
                        _totalsMap.put(currencyCode, total + suggestionCost);
                    }
                }
        }
    }
}
