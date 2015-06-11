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
    @Override
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
        // Arbitrary! TODO In the future create a price slider that can also consider other currencies.
        Integer priceLevel;

        if (_hotel.getLowRate() == null) {
            priceLevel = null;
        } else if (_hotel.getLowRate() < 100) {
            priceLevel = 1;
        } else if (_hotel.getLowRate() < 150) {
            priceLevel = 2;
        } else if (_hotel.getLowRate() < 200) {
            priceLevel = 3;
        } else {
            priceLevel = 4;
        }

        return priceLevel;
    }

    @Override
    public float getRating() {
        return _hotel.getRating();
    }
}
