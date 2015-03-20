package com.mapster.activities;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.mapster.R;
import com.mapster.json.JSONParser;
import com.mapster.places.GooglePlace;
import com.mapster.places.GooglePlaceJsonParser;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends ActionBarActivity implements GoogleMap.OnMarkerClickListener {

//    private List<Marker> _markers;
    private static final LatLng SKY_CITY = new LatLng(-37.044116, 175.0610719);
    private static final LatLng CHRISTCHURCH = new LatLng(-43.5320544, 172.6362254);
    private static final LatLng TAURANGA = new LatLng(-37.6877974, 176.1651295);
    private static final LatLng ROTURUA = new LatLng(-38.1368478, 176.2497461);
    private static final LatLng UNIVERSITY = new LatLng(-36.852338,174.76910);

    private static final float UNDEFINED_COLOUR = -1;

    private GoogleMap _map;
    private HashMap<String, List<Marker>> _suggestionMarkers; // Markers for suggestions of attractions

    // Contains marker ids and a boolean to indicate whether it has been clicked
    private HashMap<String, Boolean> _userMarkers;

    private MenuItem _filterItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        _map = fm.getMap();

        _userMarkers = new HashMap<>();

//        MarkerOptions options = new MarkerOptions();
//        options.position(SKY_CITY);
//        options.position(CHRISTCHURCH);
//        options.position(TAURANGA);
//        options.position(ROTURUA);
//        options.position(UNIVERSITY);
//        Marker m = _map.addMarker(options);
//        _userMarkers.put(m.getId(), false);
        String url = getMapsApiDirectionsUrl();
        DirectionsTask downloadTask = new DirectionsTask();
        downloadTask.execute(url);

        _map.moveCamera(CameraUpdateFactory.newLatLngZoom(UNIVERSITY,
                13)); // Setting zoom >13 crashes the emulator, see https://code.google.com/p/android/issues/detail?id=82997
        addMarkers();

//        // R.id.map is added automatically when the layout file is built
//        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
//        // Sets this as the callback object for when the GoogleMap instance is ready to use
//        mapFragment.getMapAsync(this);

        _map.setOnMarkerClickListener(this);

        initSuggestionMarkers();

    }

    /**
     * Initialises hashmap for suggestion markers - allows them to be reset when markers are cleared.
     */
    public void initSuggestionMarkers() {
        _suggestionMarkers = new HashMap<>();
        _suggestionMarkers.put("attractions", new ArrayList<Marker>());
        _suggestionMarkers.put("dining", new ArrayList<Marker>());
        _suggestionMarkers.put("accommodation", new ArrayList<Marker>());
    }

    public String buildPlacesUrl(double lat, double lng, int radius, String[] types) {
        StringBuilder sb = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        sb.append("key=" + getResources().getString(R.string.API_KEY));
        sb.append("&location=" + lat + "," + lng);
        sb.append("&radius=" + radius);
        if (types.length > 0) {
            sb.append("&types=");
            String delim = "";
            for (String s: types) {
                sb.append(delim);
                sb.append(s);
                delim = "|";
            }
        }
        String url = sb.toString();
        return url;
    }

    public boolean onMarkerClick(Marker marker) {
        String id  = marker.getId();

        // Record places and add markers if the marker is user-defined and has not been clicked before
        // Check for false to disallow null values (returned if key does not exist in the HashMap)
        if (_userMarkers.get(id) == false) {
            _userMarkers.put(id, true);
            LatLng loc = marker.getPosition();
            String url = buildPlacesUrl(loc.latitude, loc.longitude, 2000, GooglePlace.getAllCategories());
            PlacesTask placesTask = new PlacesTask();
            try {
                placesTask.execute(url).get();
            } catch (InterruptedException | ExecutionException e) {
                Log.d("Places task", e.toString());
            }
            // Make the filters button in the action bar visible
            _filterItem.setVisible(true);
            return true;
        }
        return true; // Maybe return false so default behaviour can occur?
    }

    private String getMapsApiDirectionsUrl() {
        String waypoints = "origin="
                + SKY_CITY.latitude + "," + SKY_CITY.longitude
                + "&waypoints=optimize:true|" + ROTURUA.latitude + "," + ROTURUA.longitude
                + "|" + TAURANGA.latitude + "," + TAURANGA.longitude
                + "&destination=" + CHRISTCHURCH.latitude + "," + CHRISTCHURCH.longitude ;
        String output = "json";
        String sensor = "&sensor=true";
        String url = "https://maps.googleapis.com/maps/api/directions/"
                + output + "?" + waypoints + sensor;
        //url = "https://maps.googleapis.com/maps/api/directions/json?origin=41.3758887,2.1745799999999917&waypoints=41.39097711845494,2.1807326361331434|41.38680260504134,2.188132850805232|41.38458293055814,2.1758925899657697&destination=41.38394800519846,2.166872321048686&sensor=true&mode=walking";
        return url;
    }

    private void addMarkers() {
        if (_map != null) {
            LatLng[] positions = {SKY_CITY, CHRISTCHURCH, TAURANGA, TAURANGA, UNIVERSITY};
            String[] titles = {"First Point", "Second Point", "Third Point", "Fourth Point", "University of Auckland"};

            for (int i=0; i<positions.length; i++) {
                Marker m =_map.addMarker(new MarkerOptions().position(positions[i]).title(titles[i]));
                _userMarkers.put(m.getId(), false);
                m.showInfoWindow();
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
                Marker m;

                // Save the marker in the correct categories
                String[] categories = place.categories;
                String parentCategory;
                int iconId;
                for (String c : categories) {
                    // Not the best way to do this, but ok for 3 categories. I will refactor this.
                    if (GooglePlace.ACCOMMODATION.contains(c)) {
                        parentCategory = "accommodation";
                        iconId = R.drawable.lodging_0star;
                    } else if (GooglePlace.ATTRACTIONS.contains(c)) {
                        parentCategory = "attractions";
                        iconId = R.drawable.flag_export;
                    } else if (GooglePlace.DINING.contains(c)) {
                        parentCategory = "dining";
                        iconId = R.drawable.restaurant;
                    } else {
                        continue;
                    }
                    List<Marker> markers = _suggestionMarkers.get(parentCategory);
                    m = drawMarker(latLng, BitmapDescriptorFactory.fromResource(iconId));
                    markers.add(m);
                }
            }
        }
    }

    private Marker drawMarker(LatLng latLng, BitmapDescriptor bd) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.icon(bd);
        Marker m = _map.addMarker(markerOptions);
        return m;
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
                jObject = new JSONObject(jsonData[0]);
                JSONParser parser = new JSONParser();
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> routes) {
            ArrayList<LatLng> points = null;
            PolylineOptions polyLineOptions = null;
            // traversing through routes
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

//    @Override
//    public void onMapReady(final GoogleMap googleMap) {
//        newMarker(-36.853085, 174.76958, "The University of Auckland", googleMap);
//        newMarker(-36.848448,174.762191, "The Sky Tower", googleMap);
//        newMarker(-36.8273514, 174.811964, "North Head", googleMap);
//
//        googleMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
//            @Override
//            public void onCameraChange(CameraPosition cameraPosition) {
//                // Auto-zoom to fit all markers
//                // This builder creates a minimum bound based on a set of LatLng points
//                LatLngBounds.Builder b = new LatLngBounds.Builder();
//                for (Marker m: _markers) {
//                    b.include(m.getPosition());
//                }
//                LatLngBounds bounds = b.build();
//
//                // The second argument is padding
//                googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));
//                googleMap.setOnCameraChangeListener(null);
//            }
//        });
//
//    }
//
//    /**
//     * Convenience method for adding new markers and keeping track of them in a list.
//     * @param latitude = Latitude value for marker
//     * @param longitude = Longitude value for marker
//     * @param name = Name associated with the marker
//     * @param map = Map that this marker will be added to
//     */
//    private void newMarker(double latitude, double longitude, String name, GoogleMap map) {
//        Marker marker = map.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude))
//                .title(name));
//        _markers.add(marker);
//    }
//
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // Save the filter button so that its visibility can be toggled
        _filterItem = menu.findItem(R.id.filter);
        return true;
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


    public void onFilterItemClick(MenuItem item) {
        item.setChecked(true);
        switch(item.getItemId()) {
            case R.id.all:
                // Display all the markers
                setAllMarkersVisible(true);
                break;
            case R.id.accommodation:
                // Set accommodation markers visible
                setAllMarkersVisible(false);
                List<Marker> accommodationMarkers = _suggestionMarkers.get("accommodation");
                setMarkerListVisible(true, accommodationMarkers);
                break;
            case R.id.attraction:
                // Set attraction markers visible
                setAllMarkersVisible(false);
                List<Marker> attractionMarkers = _suggestionMarkers.get("attractions");
                setMarkerListVisible(true, attractionMarkers);
                break;
            case R.id.dining:
                // Set dining markers visible
                setAllMarkersVisible(false);
                List<Marker> diningMarkers = _suggestionMarkers.get("dining");
                setMarkerListVisible(true, diningMarkers);
                break;
            case R.id.clear:
                // Clear the markers
                setAllMarkersVisible(false);
                // Hide the filter button - no suggestions to filter
                _filterItem.setVisible(false);
                // Reset 'clicked' values for all user-defined markers (all suggestions cleared)
                resetMarkersClicked();
                break;
            default:
                super.onOptionsItemSelected(item);
                break;
        }
    }

    /**
     * Change this to something more generic if reusing it would be useful. Resets all the booleans
     * that track whether a user-defined marker has been clicked to false.
     */
    private void resetMarkersClicked() {
        for (HashMap.Entry<String, Boolean> e: _userMarkers.entrySet())
            _userMarkers.put(e.getKey(), false);
    }

    private void setMarkerListVisible(boolean isVisible, List<Marker> markers) {
        if (markers != null) {
            for (Marker m: markers)
                m.setVisible(isVisible);
        }
    }

    private void setAllMarkersVisible(boolean isVisible) {
        Collection all = _suggestionMarkers.values();
        for (Object o: all) {
            ArrayList<Marker> markerList = (ArrayList) o;
            for (Marker m: markerList)
                m.setVisible(isVisible);
        }
    }

    /**
     * Markers are currently removed instead of hidden, to avoid the computation costs involved in
     * checking whether a marker already exists before creating it and adding it to the HashMap of
     * suggestions markers.
     * TODO: Keep track of the user markers that have been clicked and check that instead
     */
    private void removeAllSuggestionsMarkers() {
        Collection all = _suggestionMarkers.values();
        for (Object o: all) {
            ArrayList<Marker> markerList = (ArrayList) o;
            for (Marker m: markerList)
                m.remove();
        }
        initSuggestionMarkers();
    }
}
