package com.mapster.activities;

import android.app.FragmentTransaction;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.content.Intent;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
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
import com.mapster.connectivities.tasks.ReadTask;
import com.mapster.filters.FiltersFragment;
import com.mapster.json.JSONParser;
import com.mapster.map.information.MapInformation;
import com.mapster.places.GooglePlace;
import com.mapster.places.GooglePlaceDetail;
import com.mapster.places.GooglePlaceJsonParser;
import com.mapster.connectivities.tasks.PlaceDetailTask;
import com.mapster.suggestions.Suggestion;
import com.mapster.suggestions.SuggestionInfoAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class MainActivity extends ActionBarActivity implements GoogleMap.OnMarkerClickListener {
    private static final float UNDEFINED_COLOUR = -1;
    private static final String COORDINATE_LIST = "COORDINATE_LIST";
    private static final String TRANSPORT_MODE = "TRANSPORT_MODE";
    private ArrayList<String> _coordinateArrayList;
    private ArrayList<List<LatLng>> _latLngArrayList;
    private ArrayList<String> _transportMode;
    private List<String> nameList;
    private GoogleMap _map;
    private ArrayList<List<String>> _sortedCoordinateArrayList;
    private ArrayList<String> _sortedTransportMode;
    // Markers divided into categories (to make enumeration of categories faster)
    private HashMap<String, List<Marker>> _markersByCategory;
    // All suggestions, keyed by marker ID
    private HashMap<String, Suggestion> _suggestionsByMarkerId;

    // Contains marker ids and a boolean to indicate whether it has been clicked
    private HashMap<String, Boolean> _userMarkers;

    private MenuItem _filterItem; // Filters button

    // TODO Issue with these is that with each new filter a new field will need to be added -
    // consider changing to a HashMap of different filter values if enough filters are added for
    // fields to be unwieldy
    private String _currentCategory;
    private Integer _priceLevel;

    // The fragment with the list of filters
    FiltersFragment _filtersFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //TODO refactor
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeGoogleMap();
        getDataFromPlaceActivity();
        sortCoordinateArrayList();
        convertStringArrayListToLatLngArrayList();
        _userMarkers = new HashMap<>();
        for(int i = 0; i < _latLngArrayList.size(); i++){
            DirectionsTask downloadTask = new DirectionsTask();
            String url = getMapsApiDirectionsUrl(_latLngArrayList.get(i), _sortedTransportMode.get(i));
            downloadTask.execute(url);
        }

        _map.moveCamera(CameraUpdateFactory.newLatLngZoom(_latLngArrayList.get(0).get(0),
                13));
        addMarkers();
        _map.setOnMarkerClickListener(this);
        _map.setInfoWindowAdapter(new SuggestionInfoAdapter(getLayoutInflater(),
                this));
        _map.setMyLocationEnabled(true);
        initSuggestionMarkers();
        _suggestionsByMarkerId = new HashMap<>();

        _filtersFragment = (FiltersFragment) getFragmentManager().findFragmentById(R.id.filters);
        // Setting the visibility in the XML doesn't have effect, so hide it here
        getFragmentManager().beginTransaction().hide(_filtersFragment).commit();
    }

    public Suggestion getSuggestionByMarker(Marker marker) {
        String id = marker.getId();
        return _suggestionsByMarkerId.get(id);
    }

    /**
     * Initialises hashmap for suggestion markers - allows them to be reset when markers are cleared.
     */
    public void initSuggestionMarkers() {
        _markersByCategory = new HashMap<>();
        _markersByCategory.put("attractions", new ArrayList<Marker>());
        _markersByCategory.put("dining", new ArrayList<Marker>());
        _markersByCategory.put("accommodation", new ArrayList<Marker>());
    }

    private void initializeGoogleMap(){
        SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        _map = fm.getMap();
    }

    private void getDataFromPlaceActivity(){
        Intent i = getIntent();
        _coordinateArrayList = i.getStringArrayListExtra(COORDINATE_LIST);
        _transportMode = i.getStringArrayListExtra(TRANSPORT_MODE);
        nameList = i.getStringArrayListExtra("NAME_LIST");
    }

    private void convertStringArrayListToLatLngArrayList(){
        _latLngArrayList = new ArrayList<>();
        List<LatLng> helper = null;
        for( List<String> coordinate : _sortedCoordinateArrayList) {
            helper = new ArrayList<>();
            for (int position = 0; position < coordinate.size() - 1; position += 2) {
                helper.add(new LatLng(Double.parseDouble(coordinate.get(position)), Double.parseDouble(coordinate.get(position + 1))));
            }
            _latLngArrayList.add(helper);
        }
    }

    private void sortCoordinateArrayList(){
        //TODO Refactor
        _sortedTransportMode = new ArrayList<>();
        _sortedCoordinateArrayList = new ArrayList<>();
        int posInCoordinateArrayList = 0;
        List<String> helper = null;
        for(int i = 0; i <_transportMode.size(); i++){
            _sortedTransportMode.add(_transportMode.get(i));
            if (i >= _transportMode.size() - 1) {
                helper = addPointToList(i, posInCoordinateArrayList);
                _sortedCoordinateArrayList.add(helper);
                break;
            }
            if(_transportMode.get(i).equals(_transportMode.get(i + 1))) {
                System.out.println(_transportMode.get(i)  + " " + _transportMode.get(i + 1));
                while (_transportMode.get(i).toString().equals(_transportMode.get(i + 1).toString())) {
                    i++;
                    if (i >= _transportMode.size() - 1) {
                        break;
                    }
                }
            }
            helper = addPointToList(i, posInCoordinateArrayList);
            _sortedCoordinateArrayList.add(helper);
            posInCoordinateArrayList = i + 1;
        }
    }

    private List<String> addPointToList(int position, int posInCoordinateArray){
        List<String> helper = new ArrayList<>();
        int posCoordinateArrayList = (position + 1) * 2 + 1;// position of the _coordinateArrayList
        for(int j = posInCoordinateArray * 2; j <= posCoordinateArrayList; j++){
            helper.add(_coordinateArrayList.get(j));
        }
        return helper;
    }

    public String buildPlacesUrl(double lat, double lng, int radius, String[] types) {
        StringBuilder sb = new StringBuilder("https://maps.googleapis.com/maps/api/place"
                                             + "/nearbysearch/json?");
        sb.append("key=" + getResources().getString(R.string.API_KEY));
        sb.append("&location=" + lat + "," + lng);
        sb.append("&radius=" + radius);
        sb.append("&rankby=prominence");
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

    /**
     * Triggered when the button in the actionbar that opens the filters menu is clicked
     */
    public void onFilterButtonClick() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        if (_filtersFragment.isVisible()) {
            ft.hide(_filtersFragment);
        } else {
            ft.show(_filtersFragment);
        }
        ft.commit();
    }

    public boolean onMarkerClick(Marker marker) {
        String id  = marker.getId();
        Boolean isClicked = _userMarkers.get(id);

        // Centre on the marker that was clicked
        int zoom = (int) _map.getCameraPosition().zoom;
        CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(new LatLng(marker.getPosition().latitude + (double)90/Math.pow(2, zoom), marker.getPosition().longitude), zoom);
        _map.animateCamera(cu);

        if (isClicked == null) {
            // Marker is not recorded as user-defined, so assume it's a suggestion. Request place
            // detail and show the info window.
            Suggestion s = _suggestionsByMarkerId.get(id);

            if (marker.getSnippet() == null) {
                PlaceDetailTask detailTask = new PlaceDetailTask(this.getApplicationContext());
                GooglePlaceDetail detail = null;
                String infoWindowString;

                // Query the Places API for detail on place corresponding to marker
                try {
                    detail = detailTask.execute(s.getPlaceId()).get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
                // Use the detail to set the information displayed in the popup and save it to the place.
                infoWindowString = detail == null? "" : detail.toString();
                marker.setSnippet(infoWindowString);
                s.setPlaceDetail(detail);
                // Filter suggestions - all suggestions for this marker should be loaded, but not all
                // of them need to be visible
            }
            marker.showInfoWindow();
            return false; // Marker toolbar will be shown (returning false allows default behaviour)
        } else if (!isClicked) {
            // Marker is user-defined and has not been clicked before. Record places and add markers.
            _userMarkers.put(id, true);
            LatLng loc = marker.getPosition();
            PlacesTask placesTask = new PlacesTask();
            placesTask.execute(loc);
            // Make the filters button in the action bar visible
            _filterItem.setVisible(true);
            return false;
        } else {
            // User-defined marker has been clicked before. Display suggestions that aren't visible
            setVisibilityByFilters(); // TODO fix so this takes multiple markers into account
            marker.showInfoWindow();
            _filterItem.setVisible(true);
            return false;
        }
    }

    private String getMapsApiDirectionsUrl(List<LatLng> latLngArrayList, String transportMode) {
        //TODO refactor String --> StringBuilder
        int size = latLngArrayList.size();
        LatLng originCoordinate = latLngArrayList.get(0);
        LatLng destinationCoordinate = latLngArrayList.get(size - 1);
        StringBuilder origin = new StringBuilder("?origin=" + originCoordinate.latitude + "," + originCoordinate.longitude);
        StringBuilder destination = new StringBuilder("&destination=" + destinationCoordinate.latitude + "," + destinationCoordinate.longitude);
        StringBuilder waypoints = new StringBuilder("");
        StringBuilder mode = new StringBuilder("&mode=" + transportMode);
        if(size > 2){
            waypoints.append("&waypoints=optimize:true");
            for(int position = 1; position < size - 1; position ++){
                LatLng coordinate = latLngArrayList.get(position);
                waypoints.append("|" + coordinate.latitude + "," + coordinate.longitude);
            }
        }

        String output = "/json";
        StringBuilder url = new StringBuilder( "https://maps.googleapis.com/maps/api/directions"
                + output + origin + waypoints + destination + mode);
        System.out.println(url);
        return url.toString();
    }

    private void addMarkers() {
        if (_map != null) {
            for(List<LatLng> latLng : _latLngArrayList){
                for (int i=0; i<latLng.size(); i++){
                    LatLng position = latLng.get(i);
                    String name = nameList.get(i);
                    Marker m = _map.addMarker(new MarkerOptions().position(position).title(name));
                    _userMarkers.put(m.getId(), false);
                }
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
     * Ensures that the suggestion is categorised and keyed by marker id (so that it can be easily
     * retrieved) when it is created.
     * @param marker
     * @param place
     * @param category
     */
    private void addSuggestion(Marker marker, GooglePlace place, String category) {
        Suggestion suggestion = new Suggestion(marker, place, category);
        _suggestionsByMarkerId.put(marker.getId(), suggestion);
        List<Marker> cat = _markersByCategory.get(category);
        cat.add(marker);
    }

    private class PlacesTask extends AsyncTask<LatLng, Void, List<GooglePlace>> {
        /**
         * Fetches and parses suggestions near a point of interest.
         * @return = The places found
         */
        @Override
        protected List<GooglePlace> doInBackground(LatLng... locs) {
            GooglePlaceJsonParser placeJsonParser = new GooglePlaceJsonParser();
            List<GooglePlace> places = new ArrayList<>();
            int radius = 2000;

            List<String> urls = new ArrayList<>();

            urls.add(buildPlacesUrl(locs[0].latitude, locs[0].longitude, radius,
                    GooglePlace.getAccommodationCategories()));
            urls.add(buildPlacesUrl(locs[0].latitude, locs[0].longitude, radius,
                    GooglePlace.getDiningCategories()));
            urls.add(buildPlacesUrl(locs[0].latitude, locs[0].longitude, radius,
                    GooglePlace.getAttractionCategories()));

            for (String url: urls) {
                // Query the Google Places API to get nearby places
                String response = downloadUrl(url);

                // Parse JSON response into GooglePlaces
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    List<GooglePlace> parsed = placeJsonParser.parse(jsonResponse);

                    // Limit the number of suggestions per category
                    List<GooglePlace> shortList = parsed;
                    int maxSuggestions = 10;
                    if (places.size() > maxSuggestions)
                        shortList = parsed.subList(0, maxSuggestions);
                    places.addAll(shortList);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return places;
        }

        /**
         * Creates a marker from each of the places from the parsed JSON, adding it to the UI
         * @param places = an array of GooglePlaces
         */
        @Override
        protected void onPostExecute(List<GooglePlace> places) {
            for (GooglePlace place: places) {
                double lat = Double.parseDouble(place.latitude);
                double lng = Double.parseDouble(place.longitude);
                LatLng latLng = new LatLng(lat, lng);
                Marker m;

                // Save the marker in the correct categories
                String[] categories = place.categories;
                String parentCategory = null;
                int iconId = 0;
                // Find the category for the marker. May find multiple categories - icon will
                // represent the last one found.
                for (String c : categories) {
                    // Not the best way to do this, but ok for 3 categories.
                    if (GooglePlace.ACCOMMODATION.contains(c)) {
                        parentCategory = "accommodation";
                        iconId = R.drawable.lodging_0star;
                    } else if (GooglePlace.ATTRACTIONS.contains(c)|GooglePlace.EXTRA_ATTRACTIONS.contains(c)) {
                        parentCategory = "attractions";
                        iconId = R.drawable.flag_export;
                    } else if (GooglePlace.DINING.contains(c)) {
                        parentCategory = "dining";
                        iconId = R.drawable.restaurant;
                    }
                }
                // 'establishment' type is allowed only if it also matches one of the
                // EXTRA_ATTRACTIONS types - if not, discard the Place.
                if (parentCategory != null) {
                    m = drawMarker(latLng, BitmapDescriptorFactory.fromResource(iconId));
                    m.setTitle(place.name);
                    addSuggestion(m, place, parentCategory);
                }
            }
            // Filter suggestions
            setVisibilityByFilters();
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
     * @param latLng = The latitude and longitude of the new marker
     * @param bd = An icon for the marker
     * @return = The new marker
     */
    private Marker drawMarker(LatLng latLng, BitmapDescriptor bd) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.icon(bd);
        Marker m = _map.addMarker(markerOptions);
        return m;
    }

    private class ParserTask extends
            AsyncTask<String, Integer, MapInformation> {

        @Override
        protected MapInformation doInBackground(
                String... jsonData) {

            JSONObject jObject;
            MapInformation mapInformation = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                JSONParser parser = new JSONParser();
                mapInformation = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return mapInformation;
        }

        @Override
        protected void onPostExecute(MapInformation mapInformation) {
            if(mapInformation == null){
                createToast("Routes not found", Toast.LENGTH_SHORT);
                return;
            }
            ArrayList<LatLng> points;
            PolylineOptions polyLineOptions = null;
            // traversing through routes
            for (int i = 0; i < mapInformation.getRoutes().size(); i++) {
                points = new ArrayList<LatLng>();
                polyLineOptions = new PolylineOptions();
                List<HashMap<String, String>> path = mapInformation.getRoutes().get(i);

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
            drawInstructions(mapInformation);
            _map.addPolyline(polyLineOptions);
        }

        private void drawInstructions(MapInformation mapInformation){
            LinearLayout ll = (LinearLayout)findViewById(R.id.instructions);
            addChildToLayout(ll, "Total Duration: " + mapInformation.getTotalDuration().getName() + " Total Distance: " + mapInformation.getTotalDistance().getName(), 18);
            for(int i = 0; i < mapInformation.getInstructions().size(); i++){
                if(!mapInformation.getInstructions().get(i).isEmpty()) {
                    String name = new String();
                    if (!mapInformation.getDuration().get(i).getName().isEmpty()) {
                        name += mapInformation.getDuration().get(i).getName();
                        name += " ";
                        name += mapInformation.getDistance().get(i).getName();
                        addChildToLayout(ll, name, 16);
                    }
                    addChildToLayout(ll, mapInformation.getInstructions().get(i), 16);
                }
            }
            System.out.println(mapInformation.getInstructions());
        }

        private void addChildToLayout(LinearLayout ll, String name, int size){
            ll.addView(createTextView(name, size));
        }
    }

    protected TextView createTextView(String name, int size){
        TextView valueTV = new TextView(this);
        valueTV.setText(Html.fromHtml(name));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT
                ,LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 10, 0, 10);
        valueTV.setTextSize(size);
        valueTV.setLayoutParams(params);
        return valueTV;
    }

    protected void createToast(String name, int duration){
        Toast.makeText(this, name, duration).show();
    }
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
        } else if (id == R.id.filter) {
            onFilterButtonClick();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Sets the visibility of markers, taking into account all applicable filters
     * TODO Currently each filter needs to take into account previous ones - change this so it's
     * less complicated and error prone.
     */
    public void setVisibilityByFilters() {
        setVisibilityByCategory();
        setVisibilityByPriceLevel();
    }

    /**
     * Set all the markers in the current category to be visible. If the current category is null,
     * set all markers to be visible.
     */
    public void setVisibilityByCategory() {
        // All the markers in the current category (accommodation, attractions, or dining)
        List<Marker> markers = _markersByCategory.get(_currentCategory);

        if (_currentCategory == null) {
            setAllMarkersVisible(true);
        } else {
            // Hide everything else
            setAllMarkersVisible(false);
            setMarkerListVisible(true, markers);
        }
    }

    /**
     * Sets visibility of markers based on a price level (retrieved from the _priceLevel field)
     */
    public void setVisibilityByPriceLevel() {
        // All the markers in the current category (accommodation, attractions, or dining)
        List<Marker> markers = _markersByCategory.get(_currentCategory);

        // A 'null' level should be interpreted as no filter
        if (_priceLevel == null) {
            setVisibilityByCategory();
        } else {
            // Set to speed up accesses - null if markers aren't categorised.
            Set<Marker> categorySet = null;
            if (_currentCategory != null)
                categorySet = new HashSet<>(markers);

            for (Suggestion s : _suggestionsByMarkerId.values()) {
                Marker m = s.getMarker();

                Integer priceLevel = s.getPriceLevel();
                boolean markerIsVisible = m.isVisible();

                // getPriceLevel will return null if there was no price level provided
                if (priceLevel == null || ((priceLevel > _priceLevel) && markerIsVisible)) {
                    m.setVisible(false);
                } else {
                    if (!markerIsVisible && (categorySet == null || categorySet.contains(m))) {
                        // Marker was turned off because it didn't meet a lower price bracket
                        m.setVisible(true);
                    }
                }
            }
        }
    }

    /**
     * onClick listener for an item in a list of filter options. Filters markers, displaying those
     * that match that filter.
     * @param layout Contains a checkbox/radiobutton and option text.
     */
    public void onFilterItemClick(View layout) {
        // TODO Error checking - assumes a certain structure
        TextView filterOption = (TextView) layout.findViewById(R.id.filter_option_text);
        String filterOptionName = filterOption.getText().toString();
        String filterName = null;

        // TODO If necessary, check which filter the item belongs to. Worth refactoring.
        // TODO Refactor - too dependent on filter names.
        // The giant switch statement isn't ideal.

        switch (filterOptionName) {
            // Categories
            case "Accommodation":
                _currentCategory = "accommodation";
                filterName = "Category";
                break;
            case "Attractions":
                _currentCategory = "attractions";
                filterName = "Category";
                break;
            case "Dining":
                _currentCategory = "dining";
                filterName = "Category";
                break;

            // Price levels
            case "Cheap or free":
                _priceLevel = 1;
                filterName = "Price level";
                break;
            case "Moderately priced":
                _priceLevel = 2;
                filterName = "Price level";
                break;
            case "Expensive":
                _priceLevel = 3;
                filterName = "Price level";
                break;

            default:
                break;
        }
        // Uncheck everything before checking this option's checkbox
        _filtersFragment.setFilterOptionChecked(filterName, filterOptionName);
        setVisibilityByFilters();
    }

    /**
     * Called when the 'clear' button on a single filter is clicked. Clears/resets that filter.
     * @param view
     */
    public void onFilterClearClick(View view) {
        // TODO Makes a bunch of assumptions that need to be checked.
        RelativeLayout parent = (RelativeLayout) view.getParent();
        TextView filterTitle = (TextView) parent.findViewById(R.id.filter_title);
        String filterTitleString = filterTitle.getText().toString();

        // Uncheck all the checkboxes in the group
        _filtersFragment.clearFilterRadioButtons(filterTitleString);

        switch (filterTitleString) {
            case "Category":
                _currentCategory = null;
                break;
            case "Price level":
                _priceLevel = null;
                break;
            default:
                break;
        }
        setVisibilityByFilters();
        _filtersFragment.clearFilterRadioButtons(filterTitleString);
    }

    /**
     * Called when the 'Clear suggestions' button (which clears all the markers) is clicked.
     */
    public void onClearClick(View v) {
        // Clear the markers
        setAllMarkersVisible(false);

        // Reset the RadioButtons
        _filtersFragment.clearAllFilterRadioButtons();

        System.out.println("CLEAR");

        // Hide the filter button - no suggestions to filter
        _filterItem.setVisible(false);

        // Hide the filters fragment
        getFragmentManager().beginTransaction().hide(_filtersFragment).commit();

        // Set the filters back to null
        _currentCategory = null;
        _priceLevel = null;
    }

    private void setMarkerListVisible(boolean isVisible, List<Marker> markers) {
        if (markers != null) {
            for (Marker m: markers)
                m.setVisible(isVisible);
        }
    }

    private void setAllMarkersVisible(boolean isVisible) {
        Collection all = _markersByCategory.values();
        for (Object o: all) {
            ArrayList<Marker> markerList = (ArrayList) o;
            for (Marker m: markerList)
                m.setVisible(isVisible);
        }
    }
}
