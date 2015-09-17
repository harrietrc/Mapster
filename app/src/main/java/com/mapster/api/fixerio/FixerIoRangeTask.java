package com.mapster.api.fixerio;

import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.model.Marker;

/**
 * Created by Tom on 18/09/2015.
 */
public class FixerIoRangeTask extends FixerIoRateTask {

    private double _highValue;

    public FixerIoRangeTask(double lowValue, double highValue, String fromCurrencyCode, String toCurrencyCode, TextView viewToPopulate, Marker markerToRefresh) {
        super(lowValue, fromCurrencyCode, toCurrencyCode, viewToPopulate, markerToRefresh);
        _highValue = highValue;
    }

    @Override
    protected String formatMoney(double convertedValue) {
        String currencySymbol = getCurrencySymbolFromCode(_toCurrencyCode);
        return String.format("(%s%.2f - %s%.2f)", currencySymbol, convertedValue, currencySymbol, _highValue*_rate);
    }

    @Override
    protected void onPostExecute(Double convertedValue) {
        super.onPostExecute(convertedValue);
        if (_conversionView != null) {
            String formattedValue = formatMoney(convertedValue);
            _conversionView.setText(formattedValue);
            _conversionView.setVisibility(View.VISIBLE);
            if (_markerToRefresh.isInfoWindowShown())
                _markerToRefresh.showInfoWindow();
        }
    }
}
