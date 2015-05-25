package com.mapster.suggestions;

import android.content.Context;

import com.mapster.expedia.ExpediaHotel;

/**
 * Created by Harriet on 5/25/2015.
 */
public class ExpediaSuggestion extends Suggestion {

    private ExpediaHotel _hotel;

    public ExpediaSuggestion(ExpediaHotel hotel) {
        _hotel = hotel;
    }

    /**
     * Returns a string representation of the price range to append to the snippet that gets
     * displayed in a marker's infowindow.
     * @return
     */
    public String priceRangeToString() {
        String range = "";
        Double lowRate = _hotel.getLowRate();
        Double highRate = _hotel.getHighRate();
        String lowRange = lowRate == null ? "" : "$" + lowRate.intValue();
        String highRange = highRate == null ? "" : " - $" + highRate.intValue();
        if (lowRate != null || highRate != null)
            range = "\n" + lowRange + highRange;
        return range;
    }

    @Override
    public void requestSuggestionInfo(Context context) {

    }

    /**
     * Returns a formatted string representation of the place detail and other suggestion state
     * that should be displayed to the user in a marker infowindow.
     * @return
     */
    public String getInfoWindowString() {
        // TODO
        return "";
    }

    @Override
    public String getPhotoReference() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public Integer getPriceLevel() {
        return null;
    }

    @Override
    public float getRating() {
        return 0;
    }

}
