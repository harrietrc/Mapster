package com.mapster.connectivities.tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.google.android.gms.maps.model.Marker;
import com.mapster.suggestions.ExpediaSuggestion;

/**
 * Created by Harriet on 5/24/2015.
 */
public class ExpediaHotelInfoTask extends AsyncTask<ExpediaSuggestion, Void, ExpediaSuggestion> {

    private Context _context;

    public ExpediaHotelInfoTask(Context context) {
        _context = context;
    }

    @Override
    protected ExpediaSuggestion doInBackground(ExpediaSuggestion... suggestions) {
        // Use this if more information about the hotel is needed - make an info request
        // This will be required or useful in the future (e.g. for phone numbers/websites)
        return suggestions[0];
    }

    @Override
    protected void onPostExecute(ExpediaSuggestion suggestion) {
        Marker marker = suggestion.getMarker();
        String info = suggestion.getInfoWindowString();
        marker.setSnippet(info);
        marker.showInfoWindow();
    }
}