package com.mapster.yelp;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;
import com.mapster.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.scribe.builder.ServiceBuilder;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;

/**
 * Code sample for accessing the Yelp API V2.
 *
 * This program demonstrates the capability of the Yelp API version 2.0 by using the Search API to
 * query for businesses by a search term and location, and the Business API to query additional
 * information about the top result from the search query.
 *
 * <p>
 * See <a href="http://www.yelp.com/developers/documentation">Yelp Documentation</a> for more info.
 *
 */
public class YelpApi {

    private static final String API_HOST = "api.yelp.com";
    private static final int SEARCH_LIMIT = 10;
    private static final String SEARCH_PATH = "/v2/search";
    private static final String BUSINESS_PATH = "/v2/business";

    OAuthService service;
    Token accessToken;

    /**
     * Setup the Yelp API OAuth credentials.
     * @param context Required for access to keys in strings.xml
     */
    public YelpApi(Context context) {
        // TODO Alternatively, in both this class and the ExpediaApi class, pass in the keys rather
        // than the context.

        // Set the various keys
        String consumerKey = context.getString(R.string.YELP_CONSUMER_KEY);
        String consumerSecret = context.getString(R.string.YELP_COMSUMER_KEY_SECRET);
        String token = context.getString(R.string.YELP_TOKEN);
        String tokenSecret = context.getString(R.string.YELP_TOKEN_SECRET);

        service =
                new ServiceBuilder().provider(TwoStepOAuth.class).apiKey(consumerKey)
                        .apiSecret(consumerSecret).build();
        accessToken = new Token(token, tokenSecret);
    }

    /**
     * Searches for businesses near a location
     * @param term The type of establishment (e.g. 'bar')
     * @param latLng Location to search near
     * @param radius Search radius in metres
     * @return
     */
    public String searchForBusinessesByLocation(String term, LatLng latLng, int radius) {
        String lat = Double.toString(latLng.latitude);
        String lng = Double.toString(latLng.longitude);

        // TODO Deals filter would be nice to have (see deals_filter param)
        // TODO May also want to allow the user to set the 'sort' param (best rated vs. best match)

        OAuthRequest request = createOAuthRequest(SEARCH_PATH);
        request.addQuerystringParameter("term", term);
        request.addQuerystringParameter("ll", lat + "," + lng);
        request.addQuerystringParameter("limit", String.valueOf(SEARCH_LIMIT));
        request.addQuerystringParameter("radius_filter", String.valueOf(radius));
        return sendRequestAndGetResponse(request);
    }

    /**
     * Creates and sends a request to the Business API by business ID.
     * <p>
     * See <a href="http://www.yelp.com/developers/documentation/v2/business">Yelp Business API V2</a>
     * for more info.
     *
     * @param businessID <tt>String</tt> business ID of the requested business
     * @return <tt>String</tt> JSON Response
     */
    public String searchByBusinessId(String businessID) {
        OAuthRequest request = createOAuthRequest(BUSINESS_PATH + "/" + businessID);
        return sendRequestAndGetResponse(request);
    }

    /**
     * Creates and returns an {@link OAuthRequest} based on the API endpoint specified.
     *
     * @param path API endpoint to be queried
     * @return <tt>OAuthRequest</tt>
     */
    private OAuthRequest createOAuthRequest(String path) {
        OAuthRequest request = new OAuthRequest(Verb.GET, "http://" + API_HOST + path);
        return request;
    }

    /**
     * Sends an {@link OAuthRequest} and returns the {@link Response} body.
     *
     * @param request {@link OAuthRequest} corresponding to the API request
     * @return <tt>String</tt> body of API response
     */
    private String sendRequestAndGetResponse(OAuthRequest request) {
        System.out.println("Querying " + request.getCompleteUrl() + " ...");
        this.service.signRequest(this.accessToken, request);
        Response response = request.send();
        return response.getBody();
    }

    /**
     * Queries the Search API based on the command line arguments and takes the first result to query
     * the Business API.
     *
     * @param term The type of establishment to search for (e.g. 'bar')
     * @return businesses A JSONArray of businesses found
     */
    public JSONArray queryAPI(String term, LatLng latLng, int radius) {
        String searchResponseJSON = searchForBusinessesByLocation(term, latLng, radius);

        JSONObject response = null;
        try {
            response = new JSONObject(searchResponseJSON);
        } catch (JSONException e) {
            System.out.println("Error: could not parse JSON response:");
            System.out.println(searchResponseJSON);
            e.printStackTrace();
        }

        String firstBusinessID = null;
        JSONArray businesses = null;

        try {
            businesses = (JSONArray) response.get("businesses");
            JSONObject firstBusiness = (JSONObject) businesses.get(0);
            firstBusinessID = firstBusiness.get("id").toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        System.out.println(String.format(
                "%s businesses found, querying business info for the top result \"%s\" ...",
                businesses.length(), firstBusinessID));

        // Select the first business and display business details
        String businessResponseJSON = searchByBusinessId(firstBusinessID.toString());
        System.out.println(String.format("Result for business \"%s\" found:", firstBusinessID));
        System.out.println(businessResponseJSON);

        return businesses;
    }
}