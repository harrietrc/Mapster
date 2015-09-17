package com.mapster.suggestions;

import android.content.Context;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.mapster.api.expedia.ExpediaHotel;
import com.mapster.api.expedia.ExpediaHotelInfoTask;
import com.mapster.api.fixerio.FixerIoRangeTask;
import com.mapster.api.fixerio.FixerIoRateTask;
import com.mapster.apppreferences.AppPreferences;

import java.util.Currency;
import java.util.Locale;

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
        String countryCode = "NZ";
        if (getItem() != null)
            countryCode = getItem().getCountryCode();
        Locale locale = new Locale("", countryCode);
        Currency currency = Currency.getInstance(locale);
        String currencyCode = currency.getCurrencyCode();
        return currencyCode == null ? "NZD" : currencyCode;
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
    public String getPriceString(Context context) {
        return priceRangeToString(context);
    }

    @Override
    public void convertCost(String userCurrencyCode, String localCurrencyCode,
                            TextView conversionView, Marker markerToRefresh) {
        // Want to convert from user to local, as user currency is specified in the request
        FixerIoRangeTask task = new FixerIoRangeTask(_hotel.getLowRate(), _hotel.getHighRate(),
                userCurrencyCode, localCurrencyCode, conversionView, markerToRefresh);
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
    public String priceRangeToString(Context context) {
        StringBuilder sb = new StringBuilder();
        Double lowRate = _hotel.getLowRate();
        Double highRate = _hotel.getHighRate();

        AppPreferences prefs = new AppPreferences(context);
        String currencyCode = prefs.getUserCurrency();
        String currencySymbol = "$";
        try {
            currencySymbol = Currency.getInstance(currencyCode).getSymbol();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

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
