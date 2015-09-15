package com.mapster.webutils;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Harriet on 7/26/2015. Used to create a standard query string (appended to the end of
 * a URL)
 */
public class QueryString {

    private Map<String, String> _fieldsAndValues;

    public QueryString() {
        _fieldsAndValues = new HashMap<>();
    }

    public QueryString(Map<String, String> fieldsAndValues) {
        _fieldsAndValues = fieldsAndValues;
    }

    @Override
    public String toString() {
        String queryStringPrefix = "?";
        String fieldsAndValues = allFieldsToString();
        String queryString = "";

        if (fieldsAndValues.length() != 0)
            queryString = queryStringPrefix + fieldsAndValues;

        return queryString;
    }

    /**
     * @return Field/value pairs joined by "&". If there were no pairs, an empty string is returned.
     */
    public String allFieldsToString() {
        // Get a list of field/value pairs for fields with values that are non-null
        List<String> formattedFieldValuePairs = new ArrayList<>();
        for (String fieldName : _fieldsAndValues.keySet()) {
            String formattedPair = fieldToString(fieldName);
            formattedFieldValuePairs.add(formattedPair);
        }

        // Format the field/value pairs for use in a query string
        String fieldsAndValues = "";
        if (formattedFieldValuePairs.size() != 0)
            fieldsAndValues = StringUtils.join(formattedFieldValuePairs, "&");

        return fieldsAndValues;
    }

    /**
     * @return Field/value pair in "field=value" format. Fields with no values are represented as
     * "field=".
     */
    public String fieldToString(String fieldName) {
        String value = _fieldsAndValues.get(fieldName);
        if (value == null)
            value = "";
        return fieldName + "=" + value;
    }
}
