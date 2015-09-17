package com.mapster.api.fixerio;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.model.Marker;
import com.mapster.apppreferences.AppPreferences;
import com.mapster.json.FixerIoRateParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Currency;

/**
 * Created by Harriet on 25/08/2015. Takes a value to convert, a base currency, and a currency to
 * convert to. Finds the converted value and populates a text view with the formatted conversion.
 */
public class FixerIoRateTask extends AsyncTask<Void, Void, Double> {

    private static double DEFAULT_CURRENCY_RATE = 1.0;
    private static String DEFAULT_CURRENCY_SYMBOL = "$";

    private String _fromCurrencyCode;
    protected String _toCurrencyCode;
    protected TextView _conversionView;
    private double _valueToConvert;
    protected Marker _markerToRefresh;

    protected double _rate;

    public FixerIoRateTask(double valueToConvert, Context context, String toCurrencyCode,
                           TextView viewToPopulate, Marker markerToRefresh) {
        _fromCurrencyCode = new AppPreferences(context).getUserCurrency();
        _toCurrencyCode = toCurrencyCode;
        _conversionView = viewToPopulate;
        _valueToConvert = valueToConvert;
        _markerToRefresh = markerToRefresh;
    }

    public FixerIoRateTask(double valueToConvert, String fromCurrencyCode, String toCurrencyCode,
                           TextView viewToPopulate, Marker markerToRefresh) {
        _fromCurrencyCode = fromCurrencyCode;
        _toCurrencyCode = toCurrencyCode;
        _conversionView = viewToPopulate;
        _valueToConvert = valueToConvert;
        _markerToRefresh = markerToRefresh;
    }

    @Override
    protected Double doInBackground(Void... params) {
        // Make a request to Fixer.io for the conversion rates as JSON
        FixerIo api = new FixerIo();
        String response = api.getConversionRate(_fromCurrencyCode, _toCurrencyCode).getResponse();
        FixerIoRateParser parser = new FixerIoRateParser();

        // Extract the conversion rate from the JSON
        try {
            JSONObject jsonResponse = new JSONObject(response);
            _rate = parser.getRate(jsonResponse, _fromCurrencyCode, _toCurrencyCode);
        } catch (JSONException e) {
            _rate = DEFAULT_CURRENCY_RATE;
        }

        // Apply the conversion rate to the value supplied for conversion
        return _rate * _valueToConvert;
    }

    @Override
    protected void onPostExecute(Double convertedValue) {
        // Don't have to populate a view - might just want the result
        if (_conversionView != null) {
            String formattedValue = formatMoney(convertedValue);
            _conversionView.setText(formattedValue);
            _conversionView.setVisibility(View.VISIBLE);
            if (_markerToRefresh.isInfoWindowShown())
                _markerToRefresh.showInfoWindow();
        }
    }

    protected String formatMoney(double convertedValue) {
        String formattedValue = String.format("%.2f)", convertedValue);
        String currencySymbol = getCurrencySymbolFromCode(_toCurrencyCode);
        return "(" + currencySymbol + formattedValue;
    }

    protected String getCurrencySymbolFromCode(String currencyCode) {
        String currencySymbol;

        try {
            Currency c = Currency.getInstance(currencyCode);
            currencySymbol = c.getSymbol();
        } catch (IllegalArgumentException e) {
            // Currency was invalid - fall through to default
            currencySymbol = DEFAULT_CURRENCY_SYMBOL;
        }

        return currencySymbol;
    }
}
