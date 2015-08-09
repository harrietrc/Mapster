package com.mapster.persistence;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

import com.mapster.R;
import com.mapster.activities.PlacesActivity;
import com.mapster.android.gui.util.clearableautocompletetextview.ClearableAutoCompleteTextView;
import com.mapster.geocode.GeocodeAndSaveItineraryTask;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Harriet on 9/08/2015. Access this to display save/load itinerary dialogues (which also
 * write/read from the Itinerary database). If the dialogue code gets too sprawly chuck it in some
 * other classes (seems like at least a 'Save' and a 'Load' class would make sense)
 */
public class LoadAndSaveHelper {

    private Context _context;
    private LayoutInflater _inflater;
    private ItineraryDataSource _datasource; // TODO Should be open - add check?

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
    }

    public void showLoadDialogue() {
        AlertDialog.Builder builder = new AlertDialog.Builder(_context);
        builder.setTitle(R.string.load_itinerary);
        builder.setCancelable(true);
        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(R.string.load, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                loadItinerary();
            }
        });
    }

    private void loadItinerary() {

    }
}
