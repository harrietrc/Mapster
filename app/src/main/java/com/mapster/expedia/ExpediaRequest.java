package com.mapster.expedia;

import android.content.Context;

import com.mapster.R;
import com.mapster.connectivities.HttpConnection;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Currently only used for hotel info requests
 * Might want to look at letting the user set the currency code, or get it from the locale
 */
public class ExpediaRequest {

    private String _service = "http://api.ean.com/ean-services/rs/hotel/";
    private String _version = "v3/";
    private String _method = "info";
    private String _otherElementsStr = "&cid=487641&minorRev=99&locale=en_US&options=HOTEL_SUMMARY"
            + "&xml=%3CHotelInformationRequest%3E%3C%2FHotelInformationRequest%3E";

    private String _secret;
    private String _apiKey;
    private String _cid;
    private String _currencyCode;

    public ExpediaRequest(Context context) {
        _secret = context.getResources().getString(R.string.EXPEDIA_SECRET);
        _apiKey = context.getResources().getString(R.string.EXPEDIA_API_KEY);
        _cid = context.getResources().getString(R.string.EXPEDIA_CID);
        _currencyCode = "NZD"; // Should be set based on location or user preference
    }

    /**
     * Does authentication stuff (hashes the secret with the API Key) and makes a request to
     * Expedia
     * @return The response as a String
     * @throws NoSuchAlgorithmException
     */
    public String request(int hotelId) throws NoSuchAlgorithmException {
        // Generate the MD5 hash
        MessageDigest md = MessageDigest.getInstance("MD5");
        long timeInSeconds = (System.currentTimeMillis() / 1000);
        String input = _apiKey + _secret + timeInSeconds;
        md.update(input.getBytes());
        String sig = String.format("%032x", new BigInteger(1, md.digest()));

        String url = _service + _version + _method + "?apikey=" + _apiKey
                + "&sig=" + sig + _otherElementsStr + "&cid=" + _cid + "&hotelId=" + hotelId
                + "&currencyCode=" + _currencyCode;

        // Make the request and return the response as a string
        HttpConnection conn = new HttpConnection();
        String response = null;
        try {
            response = conn.readUrl(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("RESPONSE: " + response + "\nEXPEDIA URL: " + url);
        return response;
    }
}