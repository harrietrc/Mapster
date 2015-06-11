package com.mapster.foursquare;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;
import com.mapster.R;
import com.mapster.connectivities.HttpConnection;

import java.io.IOException;

/**
 * Created by Harriet on 6/10/2015.
 */
public class FoursquareApi {

    private static final String API_HOST = "api.foursquare.com";
    private static final String EXPLORE_PATH = "/v2/venues/explore";

    // Currently Foursquare is only used for restaurants. If this changes, make section a parameter
    // of the method that constructs the request.
    private static final String EXTRA_PARAMS = "?section=food&venuePhotos=1&v=20150610";

    private Context _context;

    public FoursquareApi(Context context) {
        _context = context;
    }

    /**
     * Makes a request for restaurants near the given location
     * @param latLng Latitude and longitude of location near which to search
     * @param radius Radius in metres
     * @param numberOfResults Number of restaurants to return
     * @return The response as a String
     */
    public String exploreRestaurantsNearLocation(LatLng latLng, int radius, int numberOfResults) {
        String lat = String.format("%.2f", latLng.latitude);
        String lng = String.format("%.2f", latLng.longitude);

        StringBuilder url = new StringBuilder("https://" + API_HOST + EXPLORE_PATH +
                EXTRA_PARAMS);
        url.append("&radius=" + radius);
        url.append("&limit=" + numberOfResults);
        url.append("&client_id=" + _context.getString(R.string.FOURSQUARE_CLIENT_ID));
        url.append("&client_secret=" + _context.getString(R.string.FOURSQUARE_CLIENT_SECRET));
        url.append("&ll=" + lat + "," + lng);

        return downloadUrl(url.toString());
    }

    private String downloadUrl(String url) {
        HttpConnection conn = new HttpConnection();
        String response = null;
        try {
            response = conn.readUrl(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

}
