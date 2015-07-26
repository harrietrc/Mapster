package com.mapster.api.expedia;

import android.app.Activity;
import android.os.AsyncTask;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.mapster.R;
import com.mapster.activities.MainActivity;
import com.mapster.itinerary.SuggestionItem;
import com.mapster.itinerary.UserItem;
import com.mapster.json.ExpediaHotelListJsonParser;
import com.mapster.suggestions.ExpediaSuggestion;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by Harriet on 5/25/2015. Alternative to retrieving hotels from Google Places; uses
 * Expedia's hotel listing and suggestions. If this is executed, then suggestions of accommodation
 * from Google Places need not be retrieved or displayed.
 */
public class ExpediaHotelListTask extends AsyncTask<LatLng, Void, List<ExpediaHotel>> {

    // Unfortunately a reference to the main activity is required in order to draw the marker and
    // save the suggestion asynchronously
    private Activity _activity;

    private int _radius; // The radius in which to search for hotels (currently in km)
    private int _numberOfResults;
    private UserItem _item; // The user-defined destination that these hotels are suggestions for

    public ExpediaHotelListTask(Activity activity, int searchRadius, int numberOfResults,
                                UserItem item) {
        _activity = activity;
        _radius = searchRadius/1000; // Expects metres, for consistency with the GooglePlaces task
        _numberOfResults = numberOfResults;
        _item = item;
    }

    @Override
    protected List<ExpediaHotel> doInBackground(LatLng... locations) {
        Expedia exp = new Expedia(_activity);
        List<ExpediaHotel> hotels = null;
        ExpediaHotelListJsonParser parser = new ExpediaHotelListJsonParser();

        // Make a request to the Expedia API in order to retrieve hotel info
        String response = exp.hotelListRequest(locations[0], _radius);

        // Parse the response to JSON, then retrieve a list of hotels
        try {
            JSONObject jsonResponse = new JSONObject(response);
            hotels = parser.getHotels(jsonResponse);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return hotels;
    }

    /**
     * Creates a suggestion and marker for each hotel returned in the response.
     * @param hotels Objects containing hotel data
     *               // TODO Change PlacesTask to a similar implementation to this
     */
    @Override
    protected void onPostExecute(List<ExpediaHotel> hotels) {
        MainActivity mainActivity = (MainActivity) _activity;

        int min = Math.min(_numberOfResults, hotels.size());

        // Unfortunately the number of results can't be limited without providing an arrival and
        // departure date, so we have to artificially limit it here for now.
        for (int i=0; i<min; i++) {
            ExpediaHotel hotel = hotels.get(i);
            ExpediaSuggestion suggestion = new ExpediaSuggestion(hotel);
            BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.lodging_0star);
            SuggestionItem item = new SuggestionItem(suggestion, _item);
            mainActivity.addSuggestionItem(item, icon, hotel.getName());
        }
    }
}
