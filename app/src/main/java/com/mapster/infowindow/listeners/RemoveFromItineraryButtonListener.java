package com.mapster.infowindow.listeners;

import android.view.View;

import com.mapster.activities.MainActivity;
import com.mapster.infowindow.dialogues.SequentialSuggestionItemDialogue;
import com.mapster.itinerary.SuggestionItem;
import com.mapster.itinerary.UserItem;

/**
 * Created by Harriet on 6/09/2015.
 */
public class RemoveFromItineraryButtonListener extends SequentialDialogueContentListener {

    private SuggestionItem _itineraryItem;
    private MainActivity _activity; // Need to change marker icon colour on click

    public RemoveFromItineraryButtonListener(MainActivity activity, SequentialSuggestionItemDialogue dialogue,
                                        SuggestionItem item) {
        super(dialogue);
        _itineraryItem = item;
        _activity = activity;
    }

    @Override
    public void onClick(View removeButton) {
        // Remove suggestion from itinerary
        _itineraryItem.setIsInItinerary(false);

        // Change marker colour
        _activity.updateSuggestionItem(_itineraryItem);
    }
}
