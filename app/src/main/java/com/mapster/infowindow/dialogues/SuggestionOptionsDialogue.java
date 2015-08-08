package com.mapster.infowindow.dialogues;

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

import org.apache.commons.lang3.StringUtils;

/**
 * Created by Harriet on 7/29/2015. Maintains state of and is used to manipulate infowindows for
 * suggestion markers. Requires that the suggestion be associated with an ItineraryItem.
 * Listeners for buttons in this dialogue are set up here.
 */
public class SuggestionOptionsDialogue extends SequentialSuggestionItemDialogue {

    private Button _websiteButton;
    private Button _callButton;
    private Button _addToItineraryButton;
    private Button _getDirectionsButton;

    public SuggestionOptionsDialogue(Context context, LayoutInflater inflater, SuggestionItem item) {
        super(context, inflater, item);
    }

    @Override
    protected View inflateContent(LayoutInflater inflater) {
        LinearLayout l = new LinearLayout(_context);
        LinearLayout content = (LinearLayout) inflater.inflate(R.layout.suggestion_options_dialogue, l);

        _callButton = (Button) content.findViewById(R.id.call_button);
        _websiteButton = (Button) content.findViewById(R.id.website_button);
        _addToItineraryButton = (Button) content.findViewById(R.id.itinerary_button);
        _getDirectionsButton = (Button) content.findViewById(R.id.directions_button);

        return content;
    }

    @Override
    protected void processItem() {
        setButtonVisibilitiesFromItem();
        configureButtons();
    }

    @Override
    protected AlertDialog constructDialogue(View dialogueContent) {
        AlertDialog.Builder builder = new AlertDialog.Builder(_context);
        builder.setView(dialogueContent).setCancelable(false).setPositiveButton(R.string.back, null);
        AlertDialog dialogue = builder.create();
        dialogue.setCanceledOnTouchOutside(true);
        return dialogue;
    }

    private void setButtonVisibilitiesFromItem() {
        Suggestion suggestion = _item.getSuggestion();

        boolean itemNotAlreadyInItinerary = !_item.isInItinerary();

        setButtonVisibilityFromProperty(_websiteButton, suggestion.getWebsite());
        setButtonVisibilityFromProperty(_callButton, suggestion.getPhoneNumber());
        setButtonVisibilityFromProperty(_addToItineraryButton, itemNotAlreadyInItinerary);
    }

    /**
     * Convenience method that sets the visibility of a button based on one of its properties,
     * evaulating it to true or false
     */
    private void setButtonVisibilityFromProperty(Button button, Object truthyOrFalsyProperty) {
        boolean buttonShouldBeVisible = false;

        try {
            if ((Boolean) truthyOrFalsyProperty)
                buttonShouldBeVisible = true;
        } catch (ClassCastException e) {
            // Not very pretty. I wanted this to be more general but can't find an elegant way to
            // handle all objects.
            if (truthyOrFalsyProperty instanceof String) {
                if (!StringUtils.isBlank((String) truthyOrFalsyProperty))
                    buttonShouldBeVisible = true;
            } else {
                String className = truthyOrFalsyProperty.getClass().getName();
                throw new IllegalArgumentException("" + className + " argument (value " +
                        truthyOrFalsyProperty + ") has no implicit truth value");
            }
        }

        int visibility = buttonShouldBeVisible ? View.VISIBLE : View.GONE;
        button.setVisibility(visibility);
    }

    private void configureButtons() {
        configureDirectionsButton();
        if (isButtonVisible(_callButton))
            configureCallButton();
        if (isButtonVisible(_websiteButton))
            configureWebsiteButton();
        if (isButtonVisible(_addToItineraryButton))
            configureAddToItineraryButton();
    }

    private boolean isButtonVisible(Button button) {
        return button.getVisibility() == View.VISIBLE;
    }

    private void configureDirectionsButton( ) {
        LatLng location = _item.getLocation();
        String placeName = _item.getName();

        DirectionsButtonListener directionsListener = new DirectionsButtonListener(_context, location, placeName);
        _getDirectionsButton.setOnClickListener(directionsListener);
    }

    private void configureCallButton() {
        String phoneNumber = _item.getSuggestion().getPhoneNumber();

        CallButtonListener callListener = new CallButtonListener(_context, phoneNumber);
        _callButton.setOnClickListener(callListener);
    }

    private void configureWebsiteButton() {
        String url = _item.getSuggestion().getWebsite();

        WebsiteButtonListener websiteListener = new WebsiteButtonListener(_context, url);
        _websiteButton.setOnClickListener(websiteListener);
    }

    private void configureAddToItineraryButton() {
        AddToItineraryButtonListener addListener = new AddToItineraryButtonListener(this, _item);
        _addToItineraryButton.setOnClickListener(addListener);
    }
}
