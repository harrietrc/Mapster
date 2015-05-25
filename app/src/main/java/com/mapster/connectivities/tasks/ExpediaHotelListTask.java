package com.mapster.connectivities.tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.mapster.expedia.Expedia;
import com.mapster.expedia.ExpediaHotel;
import com.mapster.json.ExpediaHotelListJsonParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by Harriet on 5/25/2015. Alternative to retrieving hotels from Google Places; uses
 * Expedia's hotel listing and suggestions. If this is executed, then suggestions of accommodation
 * from Google Places need not be retrieved or displayed.
 */
public class ExpediaHotelListTask extends AsyncTask<Void, Void, Void> {

    private Context _context;
    private float _latitude;
    private float _longitude;
    private int _radius; // The radius in which to search for hotels (currently in km)
    private int _numberOfResults;

    public ExpediaHotelListTask(Context context, float latitude, float longitude, int searchRadius,
                                int numberOfResults) {
        _context = context;
        _latitude = latitude;
        _longitude = longitude;
        _radius = searchRadius;
        _numberOfResults = numberOfResults;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Expedia exp = new Expedia(_context);
        List<ExpediaHotel> hotels = null;
        ExpediaHotelListJsonParser parser = new ExpediaHotelListJsonParser();

        // Make a request to the Expedia API in order to retrieve hotel info
        String response = exp.hotelListRequest(_latitude, _longitude, _radius, _numberOfResults);

        // Parse the response to JSON, then retrieve a list of hotels
        try {
            JSONObject jsonResponse = new JSONObject(response);
            hotels = parser.getHotels(jsonResponse);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }
}
