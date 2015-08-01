package com.mapster.infowindow.listeners;

import android.view.View;

import com.mapster.itinerary.SuggestionItem;
import com.mapster.itinerary.UserItem;

/**
 * Created by Harriet on 7/29/2015.
 */
public class AddToItineraryButtonListener implements View.OnClickListener {

    private SuggestionItem _itineraryItem;

    public AddToItineraryButtonListener(SuggestionItem item) {
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
    }
}
