package com.mapster.infowindow.listeners;

import android.view.View;

import com.mapster.infowindow.dialogues.SequentialSuggestionItemDialogue;
import com.mapster.itinerary.SuggestionItem;
import com.mapster.itinerary.UserItem;

/**
 * Created by Harriet on 7/29/2015.
 */
public class AddToItineraryButtonListener extends SequentialDialogueContentListener {

    private SuggestionItem _itineraryItem;

    public AddToItineraryButtonListener(SequentialSuggestionItemDialogue dialogue, SuggestionItem item) {
        super(dialogue);
        _itineraryItem = item;
    }

    @Override
    public void onClick(View addButton) {
        // Add the suggestion to the list for the UserItem it is associated with
        UserItem userItem = _itineraryItem.getUserItem();
        userItem.addSuggestionItem(_itineraryItem);

        // Flag this to not issue this prompt next time
        _itineraryItem.setIsInItinerary(true);

        // Hide the button - add option no longer available/necessary
        addButton.setVisibility(View.GONE);

        // Move to the next dialogue in the sequence
        _dialogue.moveToNext();
    }
}
