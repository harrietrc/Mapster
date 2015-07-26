package com.mapster.api;

import com.mapster.webutils.QueryString;

import java.util.Map;

/**
 * Created by Harriet on 7/26/2015. Represents a webservice made available through a request-based
 * API. Assumes that parameters are passed as a query string.
 */
public abstract class ApiRequest {

    public String constructUrl() {
        String baseUrl = constructBaseUrl();
        String queryString = generateQueryString();
        return baseUrl + queryString;
    }

    protected abstract String constructBaseUrl();

    protected String generateQueryString() {
        Map<String, String> queryFields = queryFieldsAsMap();
        QueryString qs = new QueryString(queryFields);
        return qs.toString();
    }

    protected abstract Map<String, String> queryFieldsAsMap();

}
