package com.mapster.expedia;

import android.content.Context;

import com.mapster.R;
import com.mapster.connectivities.HttpConnection;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides an interface to Expedia (used for retrieving suggestions of and information about hotels)
 * Might want to look at letting the user set the currency code, or get it from the locale
 */
public class Expedia {

    private String _service = "http://api.ean.com/ean-services/rs/hotel/";
    private String _version = "v3/";

    // Change minorRev if a newer stable version of the API becomes available.
    private String _otherElementsStr =
            "&cid=487641&minorRev=29&locale=en_US&apiExperience=PARTNER_MOBILE_APP&_type=json";

    // XML - different for each sort of request. Just an empty set of tags.
    private String _infoXml = "%3CHotelInformationRequest%3E%3C%2FHotelInformationRequest%3E";
    private String _listXml = "%3CHotelListRequest%3E%3C%2FHotelListRequest%3E";

    private String _secret;
    private String _apiKey;
    private String _cid;
    private String _currencyCode;
    private String _radiusUnit;

    public Expedia(Context context) {
        _secret = context.getResources().getString(R.string.EXPEDIA_SECRET);
        _apiKey = context.getResources().getString(R.string.EXPEDIA_API_KEY);
        _cid = context.getResources().getString(R.string.EXPEDIA_CID);
        _currencyCode = "NZD"; // Should be set based on location or user preference
        _radiusUnit = "KM"; // User should be able to set to MI or KM
    }

    /**
     * Does authentication stuff (hashes the secret with the API Key) and makes a request to
     * Expedia
     * @return The response as a String
     * @throws NoSuchAlgorithmException
     */
    public String hotelInfoRequest(int hotelId) {
        String sig = null;
        try {
            sig = getSignature();
        } catch (NoSuchAlgorithmException e) {
            // TODO better error handling
            e.printStackTrace();
        }

        String url = _service + _version + "info" + "?apikey=" + _apiKey
                + "&sig=" + sig + _otherElementsStr + "&cid=" + _cid + "&hotelId=" + hotelId
                + "&currencyCode=" + _currencyCode + "&xml=" + _infoXml;

        // Make the request and return the response as a string
        return downloadUrl(url);
    }

    /**
     * Creates the hash required for authentication with Expedia.
     * @return The signature for use in the request to Expedia (sig parameter)
     * @throws NoSuchAlgorithmException Thrown by MessageDigest.getInstance() (MD5 hash)
     */
    private String getSignature() throws NoSuchAlgorithmException {
        // Generate the MD5 hash
        MessageDigest md = MessageDigest.getInstance("MD5");
        long timeInSeconds = (System.currentTimeMillis() / 1000);
        String input = _apiKey + _secret + timeInSeconds;
        md.update(input.getBytes());
        String sig = String.format("%032x", new BigInteger(1, md.digest()));
        return sig;
    }

    /**
     * Returns a list of hotels in a specified area. Also returns a summary of information about the
     * hotels, which includes a price range.
     * @param latitude Format is DD.MMmmm
     * @param longitude Format is DDD.MMmmm
     * @param radius Search radius - unit is specified through constructor (KM or MI). The documented
     *               minimum value is 2, but I would recommend a higher value; 2 resulted in errors.
     * @return A string representation of the response.
     */
    public String hotelListRequest(float latitude, float longitude, int radius, int numberOfResults) {
        String sig = null;
        try {
            sig = getSignature();
        } catch (NoSuchAlgorithmException e) {
            // TODO better error handling
            e.printStackTrace();
        }

        // Format latitude and longitude into the correct format for the request
        // Note that it seems to work with 6dp, but the docs don't indicate that it should.
        String latString = String.format("%.5f", latitude);
        String longString = String.format("%.5f", longitude);

        String url = _service + _version + "list" + "?apikey=" + _apiKey
                + "&sig=" + sig + _otherElementsStr + "&cid=" + _cid + "&currencyCode="
                + _currencyCode + "&latitude=" + latString + "&longitude=" + longString +
                "&searchRadius=" + radius + "&searchRadiusUnit=" + _radiusUnit + "&numberOfResults="
                + numberOfResults + "&xml=" + _listXml;
        System.out.print("URL: " + url);
        // Make the request and return the response as a string
        return downloadUrl(url);
    }

    public String downloadUrl(String url) {
        HttpConnection conn = new HttpConnection();
        String response = null;
        try {
            response = conn.readUrl(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.print("EXP RESP: " + response);
        return response;
    }
}