package com.mapster.infowindow.listeners;

import android.view.View;

import com.mapster.activities.MainActivity;
import com.mapster.infowindow.dialogues.SequentialSuggestionItemDialogue;
import com.mapster.itinerary.SuggestionItem;
import com.mapster.itinerary.UserItem;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Harriet on 7/29/2015.
 */
public class AddToItineraryButtonListener extends SequentialDialogueContentListener {

    private SuggestionItem _itineraryItem;
    private MainActivity _activity; // Need to change marker icon colour on click

    public AddToItineraryButtonListener(MainActivity activity, SequentialSuggestionItemDialogue dialogue,
                                        SuggestionItem item) {
        super(dialogue);
        _itineraryItem = item;
        _activity = activity;
    }

    @Override
    public void onClick(View addButton) {
        // Add the suggestion to the list for the UserItem it is associated with
        UserItem userItem = _itineraryItem.getUserItem();
        userItem.addSuggestionItem(_itineraryItem);
        
        List<SuggestionItem> items = userItem.getSuggestionItems();
        List<SuggestionItem> mainItems = _activity.getUserItemByMarkerId(userItem.getMarkerId()).getSuggestionItems();
        Set<SuggestionItem> combinedItems = new HashSet<>();
        combinedItems.addAll(items); combinedItems.addAll(mainItems);
        userItem.replaceSuggestionItems(combinedItems);
        _activity.addUserItem(userItem);

        // Flag this to not issue this prompt next time
        _itineraryItem.setIsInItinerary(true);

        // Change marker colour
        _activity.updateSuggestionItem(_itineraryItem);

        // Move to the next dialogue in the sequence
        _dialogue.moveToNext();
    }
}
