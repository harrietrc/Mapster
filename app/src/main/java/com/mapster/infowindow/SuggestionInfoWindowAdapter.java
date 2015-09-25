package com.mapster.infowindow;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.mapster.R;
import com.mapster.activities.MainActivity;
import com.mapster.apppreferences.AppPreferences;
import com.mapster.infowindow.dialogues.SuggestionDateDialogue;
import com.mapster.infowindow.dialogues.SuggestionOptionsDialogue;
import com.mapster.infowindow.dialogues.SuggestionTimeDialogue;
import com.mapster.itinerary.SuggestionItem;
import com.mapster.suggestions.Suggestion;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Harriet on 5/24/2015.
 * So ideally this shouldn't do much other than initialise the Options dialogue.
 */
public class SuggestionInfoWindowAdapter implements GoogleMap.InfoWindowAdapter,
        GoogleMap.OnInfoWindowClickListener {

    private LayoutInflater _inflater;

    // This field prevents the ImageView from being garbage collected before its drawable can be
    // set, and the image returned by Picasso displayed. Ignore the warning!
    private ImageView _currentInfoWindowImage;
    private Activity _activity;
    private AppPreferences _prefs;
    private Map<String, View> _windowsByMarkerId;

    public SuggestionInfoWindowAdapter(LayoutInflater inflater, Activity activity) {
        _inflater = inflater;
        _activity = activity;
        _prefs = new AppPreferences(_activity);
        _windowsByMarkerId = new HashMap<>();
    }

    /**
     * Inflates and customises the dialogue with options for this suggestion (call, go to website,
     * add to itinerary)
     */
    @Override
    public void onInfoWindowClick(Marker marker) {
        final MainActivity mainActivity = (MainActivity) _activity;
        final SuggestionItem item = mainActivity.getSuggestionItemByMarker(marker);

        mainActivity.cleanUpTutorial();

        // Ignore all this if the marker is user-defined
        if (item == null)
            return;

        // Create the chain of dialogues that is initiated when the user adds an item to the itinerary
        // Not really sure whether this should go here or in the dialogue class itself
        SuggestionOptionsDialogue optionsDialogue = new SuggestionOptionsDialogue(_activity, _inflater, item);
        SuggestionDateDialogue dateDialogue = new SuggestionDateDialogue(_activity, _inflater, item);
        SuggestionTimeDialogue timeDialogue = new SuggestionTimeDialogue(_activity, _inflater, item);
        optionsDialogue.setNextDialogue(dateDialogue).setNextDialogue(timeDialogue);

        optionsDialogue.show();

        mainActivity.doTutorialActionBar();
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    /**
     * This is only called if getInfoWindow() returns null. If this returns null, the default info
     * window will be displayed.
     */
    @Override
    public View getInfoContents(Marker marker) {
        View oldView = _windowsByMarkerId.get(marker.getId());

        MainActivity activity = (MainActivity) _activity;

        SuggestionItem item = activity.getSuggestionItemByMarker(marker);
        Suggestion suggestion = item == null ? null : item.getSuggestion();

        View info;

        if (oldView == null) {
            info = _inflater.inflate(R.layout.suggestion_info_window, null);
        } else {
            info = oldView;
        }

        TextView title = (TextView) info.findViewById(R.id.title);
        title.setText(marker.getTitle());

        TextView snippet = (TextView) info.findViewById(R.id.snippet);
        snippet.setText(marker.getSnippet());

        // Set the rating of the place, if the place is not user-defined
        RatingBar ratingBar = (RatingBar) info.findViewById(R.id.rating_bar);
        float rating;
        if (suggestion != null) {
            rating = suggestion.getRating();
        } else {
            rating = 0;
        }

        // Hide rating if none if given
        if (rating == 0) {
            ratingBar.setVisibility(View.GONE);
        } else {
            ratingBar.setRating(rating);
        }

        TextView userCost = (TextView) info.findViewById(R.id.user_cost);
        TextView localCost = (TextView) info.findViewById(R.id.converted_cost);

        // Set price, if available
        if (suggestion != null) {
            Double cost = suggestion.getCostPerPerson();

            if (cost != null) {
                String userCurrency = _prefs.getUserCurrency();
                String localCurrency = suggestion.getCurrencyCode();

                userCost.setText(suggestion.getPriceString(_activity));

                // Asynchronously convert the cost
                String localCostText = localCost.getText().toString();
                if (localCostText.equals("") && !userCurrency.equals(localCurrency))
                    suggestion.convertCost(userCurrency, localCurrency, localCost, marker);
            }
        }

        int userCostVis = userCost.getText().equals("") ? View.GONE : View.VISIBLE;
        userCost.setVisibility(userCostVis);
        int localCostVis = localCost.getText().equals("") ? View.GONE : View.VISIBLE;
        localCost.setVisibility(localCostVis);

        ImageView image = (ImageView) info.findViewById(R.id.image);

        // Load the icon into the ImageView of the InfoWindow
        if (suggestion != null) {
            String imageUrl = suggestion.getThumbnailUrl(_activity);
            if (imageUrl != null) {
                if (suggestion.isClicked()) {
                    // Marker has been clicked before - don't need to call the callback to load icon
                    // Picasso has a fit() method for fitting to an ImageView, but it doesn't seem to work.
                    Picasso.with(_activity).load(imageUrl).resize(150, 150).centerCrop().into(image);
                } else {
                    // Marker clicked for first time - download the icon and load it into the view
                    suggestion.setClicked(true);
                    _currentInfoWindowImage = image;
                    Picasso.with(_activity).load(imageUrl).resize(150, 150).centerCrop()
                            .into(_currentInfoWindowImage, new InfoWindowRefresher(marker));
                }
            }
        }

        // Hide the ImageView if it has no image
        if (image.getDrawable() == null) {
            image.setVisibility(View.GONE);
        } else {
            image.setVisibility(View.VISIBLE);
        }

        _windowsByMarkerId.put(marker.getId(), info);

        return info;
    }

    private class InfoWindowRefresher implements Callback {
        private Marker _markerToRefresh;

        private InfoWindowRefresher(Marker markerToRefresh) {
            _markerToRefresh = markerToRefresh;
        }

        @Override
        public void onSuccess() {
            _markerToRefresh.showInfoWindow();
        }

        @Override
        public void onError() {

        }
    }
}