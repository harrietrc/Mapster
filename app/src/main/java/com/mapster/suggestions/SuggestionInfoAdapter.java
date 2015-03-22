package com.mapster.suggestions;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.mapster.R;

/**
 * Created by Harriet on 3/22/2015.
 * Gotta love that long name
 */
public class SuggestionInfoAdapter implements GoogleMap.InfoWindowAdapter {

    private LayoutInflater _inflater;

    public SuggestionInfoAdapter(LayoutInflater inflater) {
        _inflater = inflater;
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
        // Not ideal for all markers (user-defined ones don't need a snippet or photo) - but it
        // should be possible to hide those empty views, right? i.e. if marker.getSnippet() is
        // null, if that TextView is visible at all with no text.

        View info = _inflater.inflate(R.layout.suggestion_info_window, null);

        TextView title = (TextView) info.findViewById(R.id.title);
        title.setText(marker.getTitle());

        TextView snippet = (TextView) info.findViewById(R.id.snippet);
        snippet.setText(marker.getSnippet());

        RatingBar ratingBar = (RatingBar) info.findViewById(R.id.rating_bar);
        if (ratingBar.getRating() == 0) {
            ratingBar.setVisibility(View.GONE);
        }

        ImageView image = (ImageView) info.findViewById(R.id.image);
        if (image.getDrawable() == null) {
            image.setVisibility(View.GONE);
        }

        return info;
    }
}
