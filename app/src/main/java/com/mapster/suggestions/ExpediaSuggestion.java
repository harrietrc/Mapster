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

    /**
     * With hotels, cost per person
     * @return
     */
    @Override
    public Double getCostPerPerson(Context context) {
        return _hotel.estimateAverageRate();
    }

    @Override
    public String getCurrencyCode(Context context) {
        return _hotel.getCurrencyCode();
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
        StringBuilder sb = new StringBuilder(_hotel.toString());
        String priceRange = priceRangeToString();
        if (priceRange != null)
            sb.append("\n" + priceRange);
        return sb.toString();
    }

    /**
     * Returns a string representation of the price range to append to the snippet that gets
     * displayed in a marker's infowindow.
     * @return
     */
    public String priceRangeToString() {
        StringBuilder sb = new StringBuilder();
        Double lowRate = _hotel.getLowRate();
        Double highRate = _hotel.getHighRate();
        String currencySymbol = getCurrencySymbol(null);

        sb.append(lowRate == null ? "" : currencySymbol + lowRate.intValue());

        if (highRate != null)
            sb.append(" - ");
        // TODO This will misrepresent the currency! Need to deal with this. see rateCurrencyCode
        sb.append(highRate == null ? "" : currencySymbol + highRate.intValue());

        if (!(lowRate== null && highRate == null))
            sb.append(" a night");

        return sb.toString();
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
