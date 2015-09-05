package com.mapster.api.fixerio;

import com.mapster.api.ApiRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Harriet on 23/08/2015.
 */
public class FixerIoRequest extends ApiRequest {

    private static final String SERVICE = "https://api.fixer.io/latest";

    private String _fromCurrencyCode;
    private String _toCurrencyCode;

    public FixerIoRequest(String fromCurrencyCode, String toCurrencyCode) {
        _fromCurrencyCode = fromCurrencyCode;
        _toCurrencyCode = toCurrencyCode;
    }

    @Override
    protected String constructBaseUrl() {
        return SERVICE;
    }

    @Override
    protected Map<String, String> queryFieldsAsMap() {
        Map<String, String> queryFields = new HashMap<>();
        queryFields.put("symbols", _fromCurrencyCode + "," + _toCurrencyCode);
        return queryFields;
    }
}
