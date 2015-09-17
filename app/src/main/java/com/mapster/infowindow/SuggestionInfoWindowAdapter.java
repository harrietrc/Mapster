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
import com.mapster.api.fixerio.FixerIoRateTask;
import com.mapster.apppreferences.AppPreferences;
import com.mapster.infowindow.dialogues.SuggestionDateDialogue;
import com.mapster.infowindow.dialogues.SuggestionOptionsDialogue;
import com.mapster.infowindow.dialogues.SuggestionTimeDialogue;
import com.mapster.itinerary.SuggestionItem;
import com.mapster.suggestions.Suggestion;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.concurrent.ExecutionException;

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
    private Activity _activity; // Not great TODO Separate Marker state into a class
    private AppPreferences _prefs;

    public SuggestionInfoWindowAdapter(LayoutInflater inflater, Activity activity) {
        _inflater = inflater;
        _activity = activity;
        _prefs = new AppPreferences(_activity);
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

        // TEST CODE PLEASE IGNORE
        // Test for currency conversion
        AppPreferences prefs = new AppPreferences(_activity);
        String userCurrency = prefs.getUserCurrency();
        String suggestionCurrency = item.getCurrencyCode();
        FixerIoRateTask task = new FixerIoRateTask(10, userCurrency, suggestionCurrency, null);
        double converted = 0;
        try {
            converted = task.execute().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        // Create the chain of dialogues that is initiated when the user adds an item to the itinerary
        // Not really sure whether this should go here or in the dialogue class itself
        // TODO Probably move this; class should be responsible for InfoWindow only (no subsequent dialogues)
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
     * TODO Refactor; too long and doing too much.
     */
    @Override
    public View getInfoContents(Marker marker) {
        MainActivity activity = (MainActivity) _activity;

        SuggestionItem item = activity.getSuggestionItemByMarker(marker);
        Suggestion suggestion = item == null ? null : item.getSuggestion();

        View info = _inflater.inflate(R.layout.suggestion_info_window, null);

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

        // Set price, if available
        // TODO Null checks not necessary
        if (suggestion != null) {
            Double cost = suggestion.getCostPerPerson();

            if (cost != null) {
                TextView userCost = (TextView) info.findViewById(R.id.user_cost);
                TextView localCost = (TextView) info.findViewById(R.id.converted_cost);

                String userCurrency = _prefs.getUserCurrency();
                String localCurrency = suggestion.getCurrencyCode();

                userCost.setText(suggestion.getPriceString());

                // Asynchronously convert the cost
                // TODO Not a fan of having this method in the superclass
                if (!userCurrency.equals(localCurrency))
                    suggestion.convertCost(userCurrency, localCurrency, localCost);
            }
        }

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
        if (image.getDrawable() == null)
            image.setVisibility(View.GONE);

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