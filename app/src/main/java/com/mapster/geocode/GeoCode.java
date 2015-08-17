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
import com.mapster.itinerary.UserItem;
import com.mapster.json.StatusCode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
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
    private HttpURLConnection conn;
    private LinkedList<ClearableAutoCompleteTextView> _autoCompleteTextViewLinkedList;
    private List<RadioGroup> _transportModeViewList;
    private PlacesActivity _activity;
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


    private String[] convertAddressToLatLng(String address){
        StringBuilder jsonResults;
        URL url = null;
        InputStreamReader in = null;
        String[] coordinate = null;
        try {
            StringBuilder sb = formURLString(address);
            try {
                url = new URL(sb.toString());
                System.out.println(url.toString());
                conn = (HttpURLConnection) url.openConnection();
                in = new InputStreamReader(conn.getInputStream());
            } catch (MalformedURLException e){
                e.printStackTrace();
            } catch(IOException e){
                e.printStackTrace();
            }
            jsonResults = convertJsonResultsToStringBuilder(in);
            coordinate = extractLatLngFromResults(jsonResults);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error processing GeoCode API URL", e);
            return coordinate;
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error connecting to GeoCode API", e);
            return coordinate;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return coordinate;
    }

    private StringBuilder formURLString(String input){
        StringBuilder sb = new StringBuilder(GEOCODE_API_BASE + OUT_JSON);
        try {
            sb.append("?address=" + URLEncoder.encode(input, "utf8"));
            sb.append("&key="+ _activity.getString(R.string.API_KEY));
            System.out.println(sb.toString());
        } catch (UnsupportedEncodingException e){
            e.printStackTrace();
        }
        sb.append("&key=" + _activity.getString(R.string.API_KEY));
        return sb;
    }

    private StringBuilder convertJsonResultsToStringBuilder(InputStreamReader in) throws IOException {
        StringBuilder jsonResults = new StringBuilder();
        int read;
        char[] buff = new char[1024];
        while ((read = in.read(buff)) != -1) {
            jsonResults.append(buff, 0, read);
        }
        return jsonResults;
    }

    private String[] extractLatLngFromResults(StringBuilder jsonResults) {
        String[] coordinate = new String[2];
        try {
            // Create a JSON object hierarchy from the results
            JSONObject jsonObj = new JSONObject(jsonResults.toString());
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
                String text = acTextView.getText().toString();
                String[] coordinate = convertAddressToLatLng(text);
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
                UserItem item = new UserItem(placeName, location, transportMode);
                userItemList.add(item);
                System.out.println(item.getName() + " " + item.getTravelMode());
                position++;
            }
        }
        return userItemList;
    }

    @Override
    public void onPostExecute(ArrayList<UserItem> userItems){
        callback(userItems);
        _dialog.dismiss();
        _activity.callback(userItems);
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
