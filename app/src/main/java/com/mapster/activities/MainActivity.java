package com.mapster.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.mapster.R;
import com.mapster.json.JSONParser;
import com.mapster.places.GooglePlace;
import com.mapster.places.GooglePlaceJsonParser;

import org.apache.http.HttpConnection;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends FragmentActivity implements GoogleMap.OnMarkerClickListener {
    private static final float UNDEFINED_COLOUR = -1;
    private ArrayList<String> coordinateArrayList;
    ArrayList<LatLng> latLngArrayList;

    private GoogleMap _map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeGoogleMap();
        getDataFromPlaceActivity();
        convertStringArrayListToLatLngArrayList();

        MarkerOptions options = new MarkerOptions();
        for (LatLng position : latLngArrayList){
            options.position(position);
        }
        _map.addMarker(options);
        String url = getMapsApiDirectionsUrl();
        DirectionsTask downloadTask = new DirectionsTask();
        downloadTask.execute(url);

        _map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngArrayList.get(0),
                13));
        addMarkers();
        _map.setOnMarkerClickListener(this);
    }

    private void initializeGoogleMap(){
        SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        _map = fm.getMap();
    }

    private void getDataFromPlaceActivity(){
        Intent i = getIntent();
        coordinateArrayList = i.getStringArrayListExtra("COORDINATE_LIST");
    }

    private void convertStringArrayListToLatLngArrayList(){
        latLngArrayList = new ArrayList<>();
        for (int position = 0; position < coordinateArrayList.size() - 1; position += 2){
            latLngArrayList.add(new LatLng(Double.parseDouble(coordinateArrayList.get(position)),Double.parseDouble(coordinateArrayList.get(position + 1))));
        }
    }

    public String buildPlacesUrl(double lat, double lng, int radius, String[] types) {
        StringBuilder sb = new StringBuilder("https://maps.googleapis.com/maps/api/place"
                                             + "/nearbysearch/json?");
        sb.append("key=" + getResources().getString(R.string.API_KEY));
        sb.append("&location=" + lat + "," + lng);
        sb.append("&radius=" + radius);
        if (types.length > 0) {
            sb.append("&types=");
            String delim = "";
            for (String s: types) {
                sb.append(delim);
                sb.append(s);
                delim = ",";
            }
        }
        return sb.toString();
    }

    public boolean onMarkerClick(Marker marker) {
        LatLng loc = marker.getPosition();
        String url = buildPlacesUrl(loc.latitude, loc.longitude, 2000, new String[0]);
        PlacesTask placesTask = new PlacesTask();
        try {
            String response = placesTask.execute(url).get();
            String x = response;
        } catch (InterruptedException | ExecutionException e) {
            Log.d("Places task", e.toString());
        }
        return true;
    }

    private String getMapsApiDirectionsUrl() {
        int size = latLngArrayList.size();
        LatLng originCoordinate = latLngArrayList.get(0);
        LatLng destinationCoordinate = latLngArrayList.get(size - 1);
        String origin = "?origin=" + originCoordinate.latitude + "," + originCoordinate.longitude;
        String waypoints = "";
        if(size > 2){
            waypoints = "&waypoints=optimize:true";
            for(int position = 1; position < size - 1; position ++){
                LatLng coordinate = latLngArrayList.get(position);
                waypoints += "|" + coordinate.latitude + "," + coordinate.longitude ;
            }
        }
        String destination = "&destination=" + destinationCoordinate.latitude + "," + destinationCoordinate.longitude;
        String output = "/json";
        String url = "https://maps.googleapis.com/maps/api/directions"
                + output + origin + waypoints + destination;
        return url;
    }

    private void addMarkers() {
        if (_map != null) {
            int name = 0;
            for (LatLng position : latLngArrayList){
                name++;
                _map.addMarker(new MarkerOptions().position(position)
                        .title(Integer.toString(name)));
            }
        }
    }

    private String downloadUrl(String url) {
        String data = "";
        try {
            com.mapster.connectivities.HttpConnection http = new com.mapster.connectivities.HttpConnection();
            data = http.readUrl(url);
        } catch (Exception e) {
            Log.d("Background Task", e.toString());
        }
        return data;
    }

    /**
     * Extend this if your task involves getting a response from a URL
     */
    private class ReadTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... url) {
            String response = downloadUrl(url[0]);
            return response;
        }
    }

    private class PlacesTask extends ReadTask {
        @Override
        protected void onPostExecute(String result) {
            PlacesParserTask parserTask = new PlacesParserTask();
            parserTask.execute(result);
        }
    }

    private class DirectionsTask extends ReadTask {
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            new ParserTask().execute(result);
        }
    }

    /**
     * Adapted from http://wptrafficanalyzer.in/blog/showing-nearby-places-with-photos-at-any-location-in-google-maps-android-api-v2/
     */
    private class PlacesParserTask extends AsyncTask<String, Integer, GooglePlace[]> {
        public JSONObject json;

        @Override
        protected GooglePlace[] doInBackground(String... jsonData) {
            GooglePlace[] places = null;
            GooglePlaceJsonParser placeJsonParser = new GooglePlaceJsonParser();

            try {
                json = new JSONObject(jsonData[0]);
                places = placeJsonParser.parse(json);
            } catch (Exception e) {
                Log.d("Exception, oh woe.", e.toString());
            }
            return places;
        }

        /**
         * Creates a marker from each of the places from the parsed JSON
         * @param places = an array of GooglePlaces
         */
        @Override
        protected void onPostExecute(GooglePlace[] places) {
            for (int i=0; i < places.length; i++) {
                GooglePlace place = places[i];
                double lat = Double.parseDouble(place.latitude);
                double lng = Double.parseDouble(place.longitude);
                LatLng latLng = new LatLng(lat, lng);
                Marker m = drawMarker(latLng, BitmapDescriptorFactory.HUE_AZURE);
            }
        }
    }

    private Marker drawMarker(LatLng latLng, float colour) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        if (colour != UNDEFINED_COLOUR) {
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(colour));
        }
        Marker m = _map.addMarker(markerOptions);
        return m;
    }

    private class ParserTask extends
            AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(
                String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                System.out.println(jsonData[0]);
                jObject = new JSONObject(jsonData[0]);
                JSONParser parser = new JSONParser();
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("Mapster " + routes.isEmpty());
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> routes) {
            ArrayList<LatLng> points = null;
            PolylineOptions polyLineOptions = null;
            // traversing through routes
            System.out.println("Mapster " + routes.size());
            for (int i = 0; i < routes.size(); i++) {
                points = new ArrayList<LatLng>();
                polyLineOptions = new PolylineOptions();
                List<HashMap<String, String>> path = routes.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                polyLineOptions.addAll(points);
                polyLineOptions.width(2);
                polyLineOptions.color(Color.BLUE);
            }

            _map.addPolyline(polyLineOptions);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
