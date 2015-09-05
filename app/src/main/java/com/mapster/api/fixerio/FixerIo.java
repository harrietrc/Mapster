package com.mapster.api.fixerio;

import com.mapster.api.Api;
import com.mapster.api.ApiRequest;

/**
 * Created by Harriet on 23/08/2015. Retrieves currency rates from https://github.com/hakanensari/fixer-io
 * (which gives results as JSON)
 */
public class FixerIo extends Api {

    public ApiRequest getConversionRate(String fromCurrencyCode, String toCurrencyCode) {
        FixerIoRequest request = new FixerIoRequest(fromCurrencyCode, toCurrencyCode);
        return getRequest(request);
    }
}
