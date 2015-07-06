package com.mapster.suggestions;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.mapster.R;
import com.mapster.activities.MainActivity;
import com.mapster.itinerary.ItineraryItem;
import com.mapster.itinerary.SuggestionItem;
import com.mapster.itinerary.UserItem;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

/**
 * Created by Harriet on 5/24/2015.
 */
public class SuggestionInfoAdapter implements GoogleMap.InfoWindowAdapter,
        GoogleMap.OnInfoWindowClickListener {

    private LayoutInflater _inflater;

    // This field prevents the ImageView from being garbage collected before its drawable can be
    // set, and the image returned by Picasso displayed.
    private ImageView _currentInfoWindowImage;
    private Activity _activity; // Not great TODO Separate Marker state into a class

    public SuggestionInfoAdapter(LayoutInflater inflater, Activity activity) {
        _inflater = inflater;
        _activity = activity;
    }

    /**
     * Called when the info window of a marker (displays the image, contact details, address, etc.
     * of a suggestion) is clicked. Should only act on interactions with suggestion infowindows.
     * @param marker
     */
    @Override
    public void onInfoWindowClick(Marker marker) {
        final MainActivity mainActivity = (MainActivity) _activity;

        final SuggestionItem item = mainActivity.getSuggestionItemByMarker(marker);

        if (item != null && !item.isInItinerary()) {
            // Marker is a suggestion marker so we should process the event
            AlertDialog.Builder builder = new AlertDialog.Builder(_activity);
            String name = item.getSuggestion().getName();
            builder.setMessage("Add " + name + " to itinerary?");

            builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Add the suggestion to the list for the UserItem it is associated with
                    UserItem userItem = item.getUserItem();
                    userItem.addSuggestionItem(item);

                    // Prompt the user to enter a date for the suggestion (currently optional)
                    showDateDialogue(item);

                    // Flag this to not issue this prompt next time
                    item.setIsInItinerary(true);

                    mainActivity.setSuggestionItemMarker(item);
                    mainActivity.setItineraryUpdateRequired();
                }
            });
            builder.setNegativeButton(R.string.no, null);

            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    /**
     * Second stage in the call chain of dialogues that is invoked when an item is added to the
     * itinerary
     * @param item Suggestion added to the itinerary
     */
    public void showDateDialogue(final ItineraryItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(_activity);
        builder.setTitle("Arrival date");

        // View for this dialogue - a date picker, which sets the item's date fields
        View v = _inflater.inflate(R.layout.date_dialogue, null, false);
        final DatePicker picker = (DatePicker) v.findViewById(R.id.date_picker);
        builder.setView(v);

        // Listeners for button presses - save state and transition to the next dialogue
        builder.setNegativeButton(R.string.skip, null).setPositiveButton(R.string.ok,
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Save date picker state to itinerary item and go to the time picker
                item.setDate(picker.getYear(), picker.getMonth(), picker.getDayOfMonth());
                showTimePicker(item);
            }
        });

        AlertDialog dialogue = builder.create();
        dialogue.show();
    }

    /**
     * Third stage in the call chain that is invoked when an item is added to the itinerary. Allows
     * the user to set a time for the item. Currently skippable.
     * @param item
     */
    public void showTimePicker(final ItineraryItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(_activity);
        builder.setTitle("Arrival time");

        // View for this dialogue - time picker used to set item's time fields
        View v = _inflater.inflate(R.layout.time_dialogue, null, false);
        final TimePicker picker = (TimePicker) v.findViewById(R.id.time_picker);
        builder.setView(v);

        // Save time values (or skip and let control fall back to activity)
        builder.setNegativeButton(R.string.skip, null).setPositiveButton(R.string.ok,
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                item.setTime(picker.getCurrentHour(), picker.getCurrentMinute());
            }
        });

        AlertDialog dialogue = builder.create();
        dialogue.show();
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    /**
     * This is only called if getInfoWindow() returns null. If this returns null, the default info
     * window will be displayed.
     */
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