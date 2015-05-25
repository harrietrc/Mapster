package com.mapster.suggestions;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;
import com.mapster.connectivities.tasks.ExpediaHotelInfoTask;
import com.mapster.expedia.ExpediaHotel;

/**
 * Created by Harriet on 5/25/2015.
 */
public class ExpediaSuggestion extends Suggestion {

    private ExpediaHotel _hotel;

    public ExpediaSuggestion(ExpediaHotel hotel) {
        _hotel = hotel;
    }

    @Override
    public void requestSuggestionInfo(Context context) {
        ExpediaHotelInfoTask task = new ExpediaHotelInfoTask(context);
        task.execute(this);
    }

    /**
     * Returns a formatted string representation of the place detail and other suggestion state
     * that should be displayed to the user in a marker infowindow.
     * @return
     */
    public String getInfoWindowString() {
        return _hotel.toString();
    }

    @Override
    public LatLng getLocation() {
        return _hotel.getLocation();
    }

    @Override
    public String getThumbnailUrl(Context context) {
        return _hotel.getThumbnailUrl();
    }

    @Override
    public String getName() {
        return _hotel.getName();
    }

    @Override
    public String getCategory() {
        return "accommodation";
    }

    @Override
    public Integer getPriceLevel() {
        return null;
    }

    @Override
    public float getRating() {
        return _hotel.getRating();
    }
}
