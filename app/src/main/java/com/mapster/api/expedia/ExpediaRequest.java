package com.mapster.api.expedia;

import com.google.android.gms.maps.model.LatLng;
import com.mapster.api.ApiRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Harriet on 7/26/2015. Mostly a data structure - don't add too many functions as its
 * job is to expose that data. Represents an entry point to the Expedia API.
 */
public class ExpediaRequest extends ApiRequest {

    private static final String SERVICE = "http://api.ean.com/ean-services/rs/hotel/";
    private static final String VERSION = "v3/";
    private static final String LIST_PATH = "list";

    private static final int MINOR_REV = 29;
    private static final String LOCALE = "en_US";
    private static final String API_EXPERIENCE = "PARTNER_MOBILE_APP";

    private static final String TYPE = "json"; // TYPE field (note underscore)

    private String _currencyCode;
    private String _radiusUnit;

    // API keys and other data
    private String _cid;
    private String _apiKey;

    // XML - different for each sort of request. Just an empty set of tags.
    private String _listXml = "%3CHotelListRequest%3E%3C%2FHotelListRequest%3E";

    // Vary with requests
    private LatLng _location;
    private String _signature;
    private int _radius;

    public ExpediaRequest(String cid, String apiKey, String signature, String currencyCode, LatLng location, int radius) {
        _apiKey = apiKey;
        _cid = cid;
        _signature = signature;
        _radius = radius;
        _location = location;
        _currencyCode = currencyCode;
        _radiusUnit = "KM"; // TODO User should be able to set to MI or KM
    }

    public String constructBaseUrl() {
        return SERVICE + VERSION + LIST_PATH;
    }

    protected Map<String, String> queryFieldsAsMap() {
        Map<String, String> queryFields = new HashMap<>();

        queryFields.put("apiKey", _apiKey);
        queryFields.put("cid", _cid);
        queryFields.put("currencyCode", _currencyCode);
        queryFields.put("searchRadiusUnit", _radiusUnit);
        queryFields.put("xml", _listXml);
        queryFields.put("minorRev", Integer.toString(MINOR_REV));
        queryFields.put("locale", LOCALE);
        queryFields.put("apiExperience", API_EXPERIENCE);
        queryFields.put("_type", TYPE);
        queryFields.put("latitude", String.format("%.5f", _location.latitude));
        queryFields.put("longitude", String.format("%.5f", _location.longitude));
        queryFields.put("searchRadius", Integer.toString(_radius));
        queryFields.put("sig", _signature);

        return queryFields;
    }
}
