package com.mapster.geocode;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.google.android.gms.maps.model.LatLng;
import com.mapster.R;
import com.mapster.activities.PlacesActivity;
import com.mapster.android.gui.util.clearableautocompletetextview.ClearableAutoCompleteTextView;
import com.mapster.connectivities.HttpConnection;
import com.mapster.itinerary.UserItem;
import com.mapster.json.GeocodeParser;
import com.mapster.json.StatusCode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by tommyngo on 20/03/15.
 */
public class GeoCode extends AsyncTask<Void, Void, ArrayList<UserItem>> {
    private static final String GEOCODE_API_BASE = "https://maps.google.com/maps/api/geocode";
    private static final String LOG_TAG = "Mapster";
    private static final String OUT_JSON = "/json";
    private LinkedList<ClearableAutoCompleteTextView> _autoCompleteTextViewLinkedList;
    private List<RadioGroup> _transportModeViewList;
    protected PlacesActivity _activity;
    private ProgressDialog _dialog;

    public GeoCode(LinkedList<ClearableAutoCompleteTextView> autoCompleteTextViewLinkedList,
                   List<RadioGroup> transportModeViewList, PlacesActivity activity){
        _autoCompleteTextViewLinkedList = autoCompleteTextViewLinkedList;
        _transportModeViewList = transportModeViewList;
        _activity = activity;
    }

    public enum TravelMode{
        DRIVING("driving"), WALKING("walking"), BICYCLING("bicycling"), TRANSIT("transit");
        private final String name;
        private TravelMode(String name){
            this.name = name;
        }
    }

    private String getRequestWithAddress(String address) {
        String response = null;
        StringBuilder sb = formURLString(address);
        try {
            response = new HttpConnection().readUrl(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    private StringBuilder formURLString(String input){
        StringBuilder sb = new StringBuilder(GEOCODE_API_BASE + OUT_JSON);
        try {
            sb.append("?address=" + URLEncoder.encode(input, "utf8"));
            System.out.println(sb.toString());
        } catch (UnsupportedEncodingException e){
            e.printStackTrace();
        }
        sb.append("&key=" + _activity.getString(R.string.API_KEY));
        return sb;
    }

    private String[] extractLatLngFromResults(String response) {
        String[] coordinate = new String[2];
        try {
            // Create a JSON object hierarchy from the results
            JSONObject jsonObj = new JSONObject(response);
            if (!StatusCode.valueOf(jsonObj.getString("status")).equals(StatusCode.OK)){
                Log.d("GEOCODE", "not ok");
                return null;
            }
            JSONArray jsonArray = jsonObj.getJSONArray("results");
            for (int i = 0; i < jsonArray.length(); i++) {
                coordinate[0] = jsonArray.getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getString("lat");
                coordinate[1] = jsonArray.getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getString("lng");
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Cannot process JSON results", e);
        }
        return coordinate;
    }

    @Override
    public ArrayList<UserItem> doInBackground(Void... input) {
        ArrayList<UserItem> userItemList = new ArrayList<>();;
        int position = 0;
        for (ClearableAutoCompleteTextView acTextView : _autoCompleteTextViewLinkedList){
            if(!acTextView.getText().toString().isEmpty()) {
                String countryCode;
                String text = acTextView.getText().toString();

                String response = getRequestWithAddress(text);
                JSONObject jsonResponse = null;
                try {
                    jsonResponse = new JSONObject(response);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // Get the country code from the results
                GeocodeParser geocodeParser = new GeocodeParser();
                countryCode = geocodeParser.getCountryCode(jsonResponse);

                String[] coordinate = extractLatLngFromResults(response);
                if (coordinate == null){
                    userItemList = null;
                    return userItemList;
                }
                String placeName = text.split(",")[0];
                placeName = placeName == null? text : placeName;

                Double lat = Double.parseDouble(coordinate[0]);
                Double lng = Double.parseDouble(coordinate[1]);
                LatLng location = new LatLng(lat, lng);
                String transportMode = null;
                if(acTextView.getId() != R.id.autocomplete_destination) {
                    for (int j = 0; j < _transportModeViewList.get(position).getChildCount(); j++) {
                        RadioButton rb = (RadioButton) _transportModeViewList.get(position).getChildAt(j);
                        transportMode = getTranposportMode(rb);
                        if (transportMode != null){
                            break;
                        }
                    }
                }

                // Create a parcelable representation of the user-defined destination
                UserItem item = new UserItem(placeName, location, transportMode, countryCode, text);
                userItemList.add(item);
                position++;
            }
        }
        return userItemList;
    }

    @Override
    public void onPostExecute(ArrayList<UserItem> userItems){
        callback(userItems);
        _dialog.dismiss();
    }

    protected void callback(ArrayList<UserItem> userItems) {
        _activity.callback(userItems);
    }

    @Override
    public void onPreExecute(){
        _dialog = new ProgressDialog(_activity);
        _dialog.setMessage("Please wait...");
        _dialog.show();
    }

    private String getTranposportMode(RadioButton rb){
        switch(rb.getId()) {
            case R.id.bike_mode:
                if (rb.isChecked()) {
                    return TravelMode.BICYCLING.name;
                }
                return null;
            case R.id.drive_mode:
                if (rb.isChecked()) {
                    return TravelMode.DRIVING.name;
                }
                return null;
            case R.id.transit_mode:
                if (rb.isChecked()) {
                    return (TravelMode.TRANSIT.name);
                }
                return null;
            case R.id.walk_mode:
                if (rb.isChecked()) {
                    return TravelMode.WALKING.name;
                }
                return null;
            default:
                return null;
        }
    }

}
