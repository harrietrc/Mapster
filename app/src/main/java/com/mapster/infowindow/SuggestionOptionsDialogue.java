package com.mapster.infowindow;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.google.android.gms.maps.model.LatLng;
import com.mapster.R;
import com.mapster.infowindow.listeners.AddToItineraryButtonListener;
import com.mapster.itinerary.SuggestionItem;
import com.mapster.infowindow.listeners.CallButtonListener;
import com.mapster.infowindow.listeners.DirectionsButtonListener;
import com.mapster.infowindow.listeners.WebsiteButtonListener;
import com.mapster.suggestions.Suggestion;

/**
 * Created by Harriet on 7/29/2015. Maintains state of and is used to manipulate infowindows for
 * suggestion markers. Requires that the suggestion be associated with an ItineraryItem.
 */
public class SuggestionOptionsDialogue extends SequentialDialogue {

    private Button _websiteButton;
    private Button _callButton;
    private Button _addToItineraryButton;
    private Button _getDirectionsButton;

    public SuggestionOptionsDialogue(Context context, LayoutInflater inflater, SequentialDialogue nextInSequence) {
        super(context, inflater, nextInSequence);
    }

    @Override
    protected View inflateContent() {
        LinearLayout l = new LinearLayout(_context);
        LinearLayout content = (LinearLayout) _inflater.inflate(R.layout.suggestion_options_dialogue, l);

        _callButton = (Button) content.findViewById(R.id.call_button);
        _websiteButton = (Button) content.findViewById(R.id.website_button);
        _addToItineraryButton = (Button) content.findViewById(R.id.itinerary_button);
        _getDirectionsButton = (Button) content.findViewById(R.id.directions_button);

        return content;
    }

    @Override
    protected AlertDialog constructDialogue(View dialogueContent) {
        AlertDialog.Builder builder = new AlertDialog.Builder(_context);
        builder.setView(dialogueContent).setCancelable(false).setPositiveButton(R.string.back, null);
        AlertDialog dialogue = builder.create();
        dialogue.setCanceledOnTouchOutside(true);
        return dialogue;
    }

    public void updateDialogueFromItem(SuggestionItem item) {
        setButtonVisibilitiesFromItem(item);
        configureButtons(item);
    }

    private void setButtonVisibilitiesFromItem(SuggestionItem item) {
        Suggestion suggestion = item.getSuggestion();

        setButtonVisibilityFromProperty(_websiteButton, suggestion.getWebsite());
        setButtonVisibilityFromProperty(_callButton, suggestion.getPhoneNumber());
        setButtonVisibilityFromProperty(_addToItineraryButton, item.isInItinerary());
    }

    /**
     * Convenience method that sets the visibility of a button based on one of its properties,
     * evaulating it to true or false
     */
    private void setButtonVisibilityFromProperty(Button button, Object truthyOrFalsyProperty) {
        try {
            if ((Boolean) truthyOrFalsyProperty) {
                button.setVisibility(View.VISIBLE);
            } else {
                button.setVisibility(View.GONE);
            }
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("'Property' argument has no implicit truth value");
        }
    }

    private void configureButtons(SuggestionItem item) {
        configureDirectionsButton(item);
        if (isButtonVisible(_callButton))
            configureCallButton(item);
        if (isButtonVisible(_websiteButton))
            configureWebsiteButton(item);
        if (isButtonVisible(_addToItineraryButton))
            configureAddToItineraryButton(item);
    }

    private boolean isButtonVisible(Button button) {
        return button.getVisibility() == View.VISIBLE;
    }

    private void configureDirectionsButton(SuggestionItem item) {
        LatLng location = item.getLocation();
        String placeName = item.getName();

        DirectionsButtonListener directionsListener = new DirectionsButtonListener(_context, location, placeName);
        _getDirectionsButton.setOnClickListener(directionsListener);
    }

    private void configureCallButton(SuggestionItem item) {
        String phoneNumber = item.getSuggestion().getPhoneNumber();

        CallButtonListener callListener = new CallButtonListener(_context, phoneNumber);
        _callButton.setOnClickListener(callListener);
    }

    private void configureWebsiteButton(SuggestionItem item) {
        String url = item.getSuggestion().getWebsite();

        WebsiteButtonListener websiteListener = new WebsiteButtonListener(_context, url);
        _websiteButton.setOnClickListener(websiteListener);
    }

    private void configureAddToItineraryButton(SuggestionItem item) {
        AddToItineraryButtonListener addListener = new AddToItineraryButtonListener(item);
        _addToItineraryButton.setOnClickListener(addListener);
    }
}
