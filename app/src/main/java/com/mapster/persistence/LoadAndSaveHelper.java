package com.mapster.persistence;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;

import com.mapster.R;
import com.mapster.activities.PlacesActivity;
import com.mapster.android.gui.util.clearableautocompletetextview.ClearableAutoCompleteTextView;
import com.mapster.geocode.GeocodeAndSaveItineraryTask;
import com.mapster.itinerary.ItineraryItem;
import com.mapster.itinerary.SuggestionItem;
import com.mapster.itinerary.UserItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Harriet on 9/08/2015. Access this to display save/load itinerary dialogues (which also
 * write/read from the Itinerary database). If the dialogue code gets too sprawly chuck it in some
 * other classes (seems like at least a 'Save' and a 'Load' class would make sense)
 */
public class LoadAndSaveHelper {

    private Context _context;
    private LayoutInflater _inflater;
    private ItineraryDataSource _datasource;

    // References to GUI elements that are populated with itinerary data. Data is grabbed from here
    // after the user chooses an itinerary name.
    LinkedList<ClearableAutoCompleteTextView> _autoCompleteTextViews;
    List<RadioGroup> _transportModes;

    public LoadAndSaveHelper(Context context, LayoutInflater inflater, ItineraryDataSource datasource,
                             LinkedList<ClearableAutoCompleteTextView> autoCompleteTextViewLinkedList,
                             List<RadioGroup> transportModeViewList) {
        _context = context;
        _inflater = inflater;
        _datasource = datasource;
        _autoCompleteTextViews = autoCompleteTextViewLinkedList;
        _transportModes = transportModeViewList;

    }

    /**
     * Tad messy.
     */
    public void showSaveDialogue() {
        AlertDialog.Builder builder = new AlertDialog.Builder(_context);
        builder.setTitle(R.string.save_itinerary);
        builder.setCancelable(true);
        builder.setNegativeButton(R.string.cancel, null);

        final LinearLayout l = new LinearLayout(_context);
        View content = _inflater.inflate(R.layout.save_itinerary_dialogue, l);
        builder.setView(content);

        // Set to save itinerary to database with the name specified in the dialogue's text view
        builder.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditText itineraryNameView = (EditText) l.findViewById(R.id.itinerary_name);
                String itineraryName = itineraryNameView.getText().toString();
                saveItinerary(itineraryName);
            }
        });

        AlertDialog saveDialogue = builder.create();
        saveDialogue.show();
    }

    /**
     * Saves current itinerary to the database, using the itinerary name provided (empty string
     * currently accepted, and uniqueness not enforced)
     */
    private void saveItinerary(String itineraryName) {
        PlacesActivity activity = (PlacesActivity) _context;
        GeocodeAndSaveItineraryTask geocodeTask =
                new GeocodeAndSaveItineraryTask(_autoCompleteTextViews, _transportModes, activity,
                        _datasource, itineraryName);
        geocodeTask.execute();
        writeItineraryNameToSettings(itineraryName);
    }

    private void writeItineraryNameToSettings(String itineraryName) {
        String sharedPrefsName = _context.getResources().getString(R.string.shared_prefs);
        String itineraryNamePrefs = _context.getResources().getString(R.string.itinerary_name_prefs);
        SharedPreferences settings = _context.getSharedPreferences(sharedPrefsName, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(itineraryNamePrefs, itineraryName);
        editor.apply();
    }

    public void showLoadDialogue() {
        AlertDialog.Builder builder = new AlertDialog.Builder(_context);
        builder.setTitle(R.string.load_itinerary);
        builder.setCancelable(true);
        builder.setNegativeButton(R.string.cancel, null);

        LinearLayout l = new LinearLayout(_context);
        View content = _inflater.inflate(R.layout.load_dialogue, l);
        builder.setView(content);
        ListView loadList = (ListView) l.findViewById(R.id.saved_itinerary_list);

        AlertDialog loadDialogue = builder.create();

        // Get itinerary names from the database and update the list view
        loadItineraryList(loadList, loadDialogue);

        loadDialogue.show();
    }

    /**
     * Loads up a list of itinerary names from the database into the provided ListView
     */
    private void loadItineraryList(final ListView listView, final AlertDialog dialogueToDismiss) {
        List<String> itineraryNames = _datasource.getAllNames();
        ArrayAdapter<String> listAdapter = new ArrayAdapter<>(_context,
                android.R.layout.simple_list_item_1, itineraryNames);
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String itineraryName = (String) listView.getItemAtPosition(position);
                loadItinerary(itineraryName);
                dialogueToDismiss.dismiss();
            }
        });
    }

    private void loadItinerary(String itineraryName) {
        List<ItineraryItem> itineraryItems = _datasource.getItemsByItineraryName(itineraryName);

        ArrayList<UserItem> userItems = new ArrayList<>();
        for (ItineraryItem item : itineraryItems)
            userItems.add((UserItem) item);

        // Update itinerary name
        writeItineraryNameToSettings(itineraryName);

        // Gets rid of itinerary items that weren't part of the loaded itinerary
        _datasource.deleteUnsavedItineraryItems();

        ((PlacesActivity) _context).updateFieldsFromItinerary(userItems); // Updates the UI
        ((PlacesActivity) _context).callback(userItems);
    }
}
