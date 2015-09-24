package com.mapster.api;

import com.mapster.webutils.QueryString;

import java.util.Map;

/**
 * Created by Harriet on 7/26/2015. Represents a webservice made available through a request-based
 * API. Assumes that parameters are passed as a query string.
 */
public abstract class ApiRequest {

    private String _url;
    private String _response;

    public void setResponse(String response) {
        _response = response;
    }

    public String getUrl() {
        if (_url == null) {
            String baseUrl = constructBaseUrl();
            String queryString = generateQueryString();
            _url = baseUrl + queryString;
        }
        return _url;
    }

    public String getResponse() {
        if (_response == null) {
            throw new InvalidResponseException("No response saved. Has a request been made?");
        } else {
            return _response;
        }
    }

    protected abstract String constructBaseUrl();

    protected String generateQueryString() {
        Map<String, String> queryFields = queryFieldsAsMap();
        QueryString qs = new QueryString(queryFields);
        return qs.toString();
    }

    protected abstract Map<String, String> queryFieldsAsMap();

}
