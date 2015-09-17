package com.mapster.api.expedia;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;
import com.mapster.R;
import com.mapster.api.Api;
import com.mapster.api.ApiRequest;
import com.mapster.apppreferences.AppPreferences;
import com.mapster.webutils.Md5Hash;

import java.security.NoSuchAlgorithmException;

/**
 * Provides an interface to Expedia (used for retrieving suggestions of and information about hotels)
 * Might want to look at letting the user set the currency code, or get it from the locale
 */
public class Expedia extends Api {

    // Keys and other API-global data
    private String _cid;
    private String _secretKey;
    private String _apiKey;


    private String _currencyCode;

    // Signature includes timestamp, and hence should be hashed at the time of the request
    private Md5Hash _signatureHasher;

    public Expedia(Context context) {
        _cid = context.getResources().getString(R.string.EXPEDIA_CID);
        _secretKey = context.getResources().getString(R.string.EXPEDIA_SECRET);
        _apiKey = context.getResources().getString(R.string.EXPEDIA_API_KEY);

        try {
            _signatureHasher = new Md5Hash();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        _currencyCode = new AppPreferences(context).getUserCurrency();
    }

    public ApiRequest hotelListRequest(LatLng location, int radius) {
        String sig = _signatureHasher.calculateExpediaSignature(_apiKey, _secretKey);
        ExpediaRequest request = new ExpediaRequest(_cid, _apiKey, sig, _currencyCode, location, radius);
        return getRequest(request);
    }
}