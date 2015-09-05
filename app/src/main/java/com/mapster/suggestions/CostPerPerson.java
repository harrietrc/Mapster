package com.mapster.suggestions;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.TextView;

import com.mapster.R;
import com.mapster.api.fixerio.FixerIoRateTask;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Created by Harriet on 26/08/2015.
 */
public class CostPerPerson {

    private static final String DEFAULT_HOME_CURRENCY_CODE = "NZD";

    // TODO Should be set by the user to a preferred/home currency
    private String _homeCurrencyCode;

    // Associates currency codes with known conversion rates (to home currency)
    private Map<String, Double> _conversionRates;

    private Money _costPerPerson;

    public CostPerPerson(double costPerPerson, String currencyCode) {
        _homeCurrencyCode = currencyCode;
        CurrencyUnit unit = CurrencyUnit.of(_homeCurrencyCode);
        _costPerPerson = Money.of(unit, costPerPerson);
        _conversionRates = new HashMap<>();
    }

    public CostPerPerson(double costPerPerson, Context context) {
        _conversionRates = new HashMap<>();
        _homeCurrencyCode = getHomeCurrencyCodeFromSettings(context);
        CurrencyUnit unit = CurrencyUnit.of(_homeCurrencyCode);
        _costPerPerson = Money.of(unit, costPerPerson);
    }

    public CostPerPerson(double costPerPerson, String currencyCode, Context context) {
        _conversionRates = new HashMap<>();
        CurrencyUnit unit = CurrencyUnit.of(currencyCode);
        _costPerPerson = Money.of(unit, costPerPerson);
        _homeCurrencyCode = getHomeCurrencyCodeFromSettings(context);
    }

    public Double inHomeCurrency() {
        return  _conversionRates.get(_homeCurrencyCode);
    }

    public Double inCurrency(String currencyCode) {
        double cost = _costPerPerson.getAmount().doubleValue();
        double conversion = cost;

        FixerIoRateTask conversionTask = new FixerIoRateTask(cost, _homeCurrencyCode, currencyCode,
                null);
        try {
            conversion = conversionTask.execute().get(); // Waits for the thread
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return conversion;
    }

    /**
     * Preferable to inCurrency() because it doesn't wait for the conversion result.
     */
    public void populateViewWithConversion(TextView viewToPopulate, String currencyCode) {
        double cost = _costPerPerson.getAmount().doubleValue();
        FixerIoRateTask conversionTask = new FixerIoRateTask(cost, _homeCurrencyCode, currencyCode,
                viewToPopulate);
        conversionTask.execute();
    }

    private String getHomeCurrencyCodeFromSettings(Context context) {
        String sharedPrefsName = context.getResources().getString(R.string.shared_prefs);
        SharedPreferences prefs = context.getSharedPreferences(sharedPrefsName, 0);
        String homeCurrencyCode = prefs.getString("HomeCurrencyCode", null);
        return homeCurrencyCode == null ? DEFAULT_HOME_CURRENCY_CODE : homeCurrencyCode;
    }

}
