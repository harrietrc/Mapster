package com.mapster.suggestions;

import android.content.Context;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.mapster.api.expedia.ExpediaHotel;
import com.mapster.api.expedia.ExpediaHotelInfoTask;
import com.mapster.api.fixerio.FixerIoRateTask;

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
    public Double getCostPerPerson() {
        return _hotel.estimateAverageRate();
    }

    @Override
    public String getCurrencyCode() {
        return _hotel.getCurrencyCode();
    }

    @Override
    public String getWebsite() {
        return null; // TODO can we get a website? Most probably
    }

    @Override
    public String getPhoneNumber() {
        return null; // TODO Same as above
    }

    @Override
    public String getPriceString() {
        return priceRangeToString();
    }

    @Override
    public void convertCost(String userCurrencyCode, String localCurrencyCode, TextView conversionView) {
        // Want to convert from user to local, as user currency is specified in the request
        FixerIoRateTask task = new FixerIoRateTask(getCostPerPerson(), userCurrencyCode, localCurrencyCode, conversionView);
        task.execute();
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

    /**
     * Returns a string representation of the price range to append to the snippet that gets
     * displayed in a marker's infowindow.
     * @return
     */
    public String priceRangeToString() {
        StringBuilder sb = new StringBuilder();
        Double lowRate = _hotel.getLowRate();
        Double highRate = _hotel.getHighRate();
        String currencySymbol = getCurrencySymbol();

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
