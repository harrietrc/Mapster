package com.mapster.itinerary.ui;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mapster.R;
import com.mapster.activities.ItineraryActivity;
import com.mapster.activities.SlidingTabsBasicFragment;
import com.mapster.apppreferences.AppPreferences;
import com.mapster.itinerary.ItineraryItem;
import com.mapster.itinerary.SuggestionItem;
import com.mapster.itinerary.UserItem;
import com.mapster.priceestimation.MealPriceEstimate;
import com.mapster.suggestions.Suggestion;
import com.mapster.tutorial.Tutorial;
import com.mapster.view.SlidingTabLayout;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Currency;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SamplePagerAdapter extends PagerAdapter {
    private FragmentActivity _activity;
    public SamplePagerAdapter(FragmentActivity activity){
        _activity = activity;
    }

    // Totals
    Map<String, Double> _totalsMap; // Maintains state for totals list
    List<String> _totalsList;
    ArrayAdapter<String> _totalsListAdapter;
    private LayoutInflater _inflater;
    private LinearLayout _layout;


    private DateTimeFormatter _timeFormatter; // Prints only the time (no month or day)
    private DateTimeFormatter _dateFormatter; // Prints only the date
    private List<ItineraryItem> _sortedItems;

    /**
     * @return the number of pages to display
     */
    @Override
    public int getCount() {
        return 2;
    }

    /**
     * @return true if the value returned from {@link #instantiateItem(android.view.ViewGroup, int)} is the
     * same object as the {@link android.view.View} added to the {@link android.support.v4.view.ViewPager}.
     */
    @Override
    public boolean isViewFromObject(View view, Object o) {
        return o == view;
    }

    // BEGIN_INCLUDE (pageradapter_getpagetitle)
    /**
     * Return the title of the item at {@code position}. This is important as what this method
     * returns is what is displayed in the {@link com.mapster.view.SlidingTabLayout}.
     * <p>
     * Here we construct one using the position value, but for real application the title should
     * refer to the item's contents.
     */
    @Override
    public CharSequence getPageTitle(int position) {
        if (position == 0){
            return "Schedule";
        }
        return "Budget";
    }
    // END_INCLUDE (pageradapter_getpagetitle)

    /**
     * Instantiate the {@link View} which should be displayed at {@code position}. Here we
     * inflate a layout from the apps resources and then change the text view to signify the position.
     */
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        // Inflate a new layout from our resources
        View view = null;
        if (position == 0) {
            _inflater = _activity.getLayoutInflater();
            view = _activity.getLayoutInflater().inflate(R.layout.schedule_fragment,
                    container, false);

            // Formats arrival time for the UI
            _dateFormatter = DateTimeFormat.mediumDate(); // TODO Fiddle with this
            _timeFormatter = DateTimeFormat.shortTime();

            // Construct a list of all the itinerary items, ordered by date
            refreshDataFromDatabase();

            // Set up the main table view for this fragment
            _layout = (LinearLayout) view.findViewById(R.id.schedule_layout);

            createRowsFromItemsSchedule();
        } else {
            _inflater = _activity.getLayoutInflater();
            view = _inflater.inflate(R.layout.budget_fragment,
                    container, false);
            _layout = (LinearLayout) view.findViewById(R.id.budget_layout);
            // Populate list of totals
            _totalsList = new ArrayList<>();
            _totalsListAdapter = new ArrayAdapter<>(_activity, android.R.layout.simple_list_item_1, _totalsList);
            ListView totalsListView = (ListView) view.findViewById(R.id.budget_totals_list);
            totalsListView.setAdapter(_totalsListAdapter);
            _totalsMap = new HashMap<>();

            // Populate the table with the itinerary items from the database
            createRowsFromItemsBudget();

            // Display initial values for totals
            updateTotals();
        }
        // Add the newly created View to the ViewPager
        container.addView(view);
        // Return the View
        return view;
    }

    /**
     * Destroy the item from the {@link android.support.v4.view.ViewPager}. In our case this is simply removing the
     * {@link View}.
     */
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }
    private void formatTotalsAsList() {
        _totalsList.clear();
        System.out.println(_totalsMap);
        for (Map.Entry pair : _totalsMap.entrySet()) {
            //TODO FIX THIS
            if (pair.getKey() == null)
                break;
            String currencySymbol = Currency.getInstance((String) pair.getKey()).getSymbol();
            Double total = (Double) pair.getValue();
            if (total != null) {
                String totalString = String.format("%.2f", total);
                _totalsList.add(currencySymbol + totalString);
            }
        }
    }

    private void createRowsFromItemsBudget() {
        // TODO Shift itinerary data out of activity to a datasource class?
        List<? extends ItineraryItem> items = ((ItineraryActivity) _activity).getItems();

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
        LinearLayout row = new LinearLayout(_activity);
        LinearLayout v = (LinearLayout) _inflater.inflate(R.layout.budget_user_destination_row, row, false);

        // Add other info or styling if you want; currently just displays the destination name
        TextView titleView = (TextView) v.findViewById(R.id.budget_col_user_dest_name);
        String title = item.getName();
        titleView.setText(title);

        _layout.addView(v);
    }

    public void createSuggestionRow(final SuggestionItem item) {
        LinearLayout row = new LinearLayout(_activity);
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
        AlertDialog.Builder builder = new AlertDialog.Builder(_activity);

        // Inflate the dialogue layout and get references to all the relevant views
        LinearLayout l = new LinearLayout(_activity);
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
        List<? extends ItineraryItem> items = ((ItineraryActivity) _activity).getItems();

        for (ItineraryItem item: items) {
            // This is the only possibility at the moment - UserItems only
            if (item instanceof UserItem) {
                for (SuggestionItem s : ((UserItem) item).getSuggestionItems()) {
                    String currencyCode = s.getSuggestion().getCurrencyCode();
                    Double total = _totalsMap.get(currencyCode);

                    // Use the entered value as the cost if available - else use the estimate.
                    Double userCost = s.getActualCost();
                    Double suggestionCost = userCost == null ? s.getTotalCost() : userCost;

                    if (currencyCode == null)
                        currencyCode = MealPriceEstimate.DEFAULT_CURRENCY_CODE;

                    if (total == null && suggestionCost != null) {
                        _totalsMap.put(currencyCode, suggestionCost);
                    } else if (suggestionCost != null) {
                        _totalsMap.put(currencyCode, total + suggestionCost);
                    }
                }
            }
        }
    }

    private void refreshDataFromDatabase() {
        List<? extends ItineraryItem> items = ((ItineraryActivity) _activity).getItems();
        _sortedItems = new LinkedList<>();
        // Add the user-defined items
        _sortedItems.addAll(items);
        // Add the suggestion items (children of user-defined items)
        for (ItineraryItem item: items)
            if (item instanceof UserItem)
                _sortedItems.addAll(((UserItem) item).getSuggestionItems());
        Collections.sort(_sortedItems); // Sort by date/time
    }

    private void createRowsFromItemsSchedule() {
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
        LinearLayout row = new LinearLayout(_activity);
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
        RelativeLayout row = new RelativeLayout(_activity);
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