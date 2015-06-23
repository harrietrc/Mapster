package com.mapster.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.mapster.R;
import com.mapster.connectivities.tasks.ExpediaHotelListTask;
import com.mapster.connectivities.tasks.FoursquareExploreTask;
import com.mapster.connectivities.tasks.GooglePlacesTask;
import com.mapster.connectivities.tasks.ReadTask;
import com.mapster.filters.Filters;
import com.mapster.itinerary.SuggestionItem;
import com.mapster.itinerary.UserItem;
import com.mapster.itinerary.persistence.ItineraryDataSource;
import com.mapster.itinerary.persistence.UpdateMainFromItineraryTask;
import com.mapster.json.JSONParser;
import com.mapster.map.information.MapInformation;
import com.mapster.suggestions.Suggestion;
import com.mapster.suggestions.SuggestionInfoAdapter;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends ActionBarActivity implements GoogleMap.OnMarkerClickListener, OnMapReadyCallback {
    private static final float UNDEFINED_COLOUR = -1;
    private static final String COORDINATE_LIST = "COORDINATE_LIST";
    private static final String TRANSPORT_MODE = "TRANSPORT_MODE";
    private static final String USER_ITEM_LIST = "USER_ITEM_LIST";

    // Flag that says whether to update the itinerary database (at the moment this is a heavy-handed
    // drop-table and insertion of all the user-defined destinations and chosen suggestions.
    private boolean _itineraryUpdateRequired;

    // Lists from previous activity - should refactor as there is redundancy here
    private ArrayList<String> _coordinateArrayList;
    private ArrayList<List<LatLng>> _latLngArrayList;
    private ArrayList<String> _transportMode;

    // Only used to get data from PlacesActivity. Use _userItemsByMarkerId to keep track of UserItems.
    private ArrayList<UserItem> _userItemList;

    private GoogleMap _map;
    private ArrayList<List<String>> _sortedCoordinateArrayList;
    private ArrayList<String> _sortedTransportMode;
    // Markers divided into categories (to make enumeration of categories faster)
    private HashMap<String, List<Marker>> _markersByCategory;

    // All suggestions, keyed by marker ID
    private HashMap<String, SuggestionItem> _suggestionItemsByMarkerId;
    // User items, keyed by marker ID
    private HashMap<String, UserItem> _userItemsByMarkerId;

    // Contains marker ids and a boolean to indicate whether it has been clicked
    private HashMap<String, Boolean> _userMarkers;

    private MenuItem _filterItem; // Filters button

    // The layout for this activity - used to listen for drawer state.
    private DrawerLayout _drawerLayout;

    // TODO Issue with these is that with each new filter a new field will need to be added -
    // consider changing to a HashMap of different filter values if enough filters are added for
    // fields to be unwieldy
    private String _currentCategory;
    private Integer _priceLevel;

    // Interacts with itinerary database
    private ItineraryDataSource _itineraryDataSource;

    // Controls the state of the filters
    private Filters _filters;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //TODO refactor
        super.onCreate(savedInstanceState);

        _itineraryDataSource = new ItineraryDataSource(this);
        _itineraryDataSource.open();

        // If we change activity, save the existing user-defined destinations to the database
        _itineraryUpdateRequired = true;

        setContentView(R.layout.activity_main);
        initializeGoogleMap();
        getDataFromPlaceActivity();
        sortCoordinateArrayList();
        convertStringArrayListToLatLngArrayList();

        _userMarkers = new HashMap<>();
        _userItemsByMarkerId = new HashMap<>();
        _suggestionItemsByMarkerId = new HashMap<>();

        // Populate the filters drawer/list
        ExpandableListView filters = (ExpandableListView) findViewById(R.id.filter_list);
        _filters = new Filters(filters);
        _filters.populateFilterList(this);

        // Set up a listener for the drawer
        _drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        DrawerLayout.DrawerListener drawerListener = new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {}
            @Override
            public void onDrawerOpened(View drawerView) {
                _filters.refreshFilterRadioButtons();
            }
            @Override
            public void onDrawerClosed(View drawerView) {
                _filters.refreshFilterRadioButtons();
            }
            @Override
            public void onDrawerStateChanged(int newState) {}
        };
        _drawerLayout.setDrawerListener(drawerListener);
    }

    @Override
    public void onResume() {
        if (!_suggestionItemsByMarkerId.isEmpty()) {
            // Don't want this to run straight after the first onCreate() call
            UpdateMainFromItineraryTask updateTask = new UpdateMainFromItineraryTask(
                    _suggestionItemsByMarkerId, _userItemsByMarkerId, _itineraryDataSource);
            updateTask.execute();
        }
        super.onResume();
    }

    public void setItineraryUpdateRequired() {
        _itineraryUpdateRequired = true;
    }

    public SuggestionItem getSuggestionItemByMarker(Marker marker) {
        String id = marker.getId();
        return  _suggestionItemsByMarkerId.get(id);
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
        fm.getMapAsync(this);
    }

    private void getDataFromPlaceActivity(){
        Intent i = getIntent();
        _coordinateArrayList = i.getStringArrayListExtra(COORDINATE_LIST);
        _transportMode = i.getStringArrayListExtra(TRANSPORT_MODE);
        _userItemList = i.getParcelableArrayListExtra(USER_ITEM_LIST);
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

    /**
     * Triggered when the button in the actionbar that opens the filters menu is clicked
     */
    public void onFilterButtonClick() {
        // If there are issues with timing, use isDrawerVisible() instead
        // This is the actual drawer (within the DrawerLayout)
        ExpandableListView filtersList = _filters.getFilterList();

        if (_drawerLayout.isDrawerOpen(filtersList)) {
            _drawerLayout.closeDrawer(filtersList);
        } else {
            _drawerLayout.openDrawer(filtersList);
        }
    }

    public boolean onMarkerClick(Marker marker) {
        String id  = marker.getId();
        Boolean isClicked = _userMarkers.get(id);

        // Centre on the marker that was clicked
        int zoom = (int) _map.getCameraPosition().zoom;
        CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(new LatLng(marker.getPosition().latitude + (double) 90 / Math.pow(2, zoom), marker.getPosition().longitude), zoom);
        _map.animateCamera(cu);

        if (isClicked == null) {
            // Marker is not recorded as user-defined, so assume it's a suggestion. Request place
            // detail and show the info window.
            Suggestion s = _suggestionItemsByMarkerId.get(id).getSuggestion();

            if (marker.getSnippet() == null) {
                // Make requests to the web API's, populating suggestion information. Internet access required.
                s.requestSuggestionInfo(this.getApplicationContext());

                // Use the detail to set the information displayed in the popup and save it to the place.
                String infoWindowString = s.getInfoWindowString();
                marker.setSnippet(infoWindowString);
            }
            marker.showInfoWindow();
            return false; // Marker toolbar will be shown (returning false allows default behaviour)
        } else if (!isClicked) {
            // Marker is user-defined and has not been clicked before. Record places and add markers.
            _userMarkers.put(id, true);
            UserItem item = _userItemsByMarkerId.get(marker.getId());

            LatLng loc = marker.getPosition();

            // Get suggestions from Google Places. 1st arg: search radius in m. 2nd arg: number of results
            GooglePlacesTask placesTask = new GooglePlacesTask(this, 3000, 15, item);
            placesTask.execute(loc);

            // Get suggestions from Expedia. Unfortunately this one takes longer than the Google task
            ExpediaHotelListTask expediaTask = new ExpediaHotelListTask(this, 3000, 15, item);
            expediaTask.execute(loc);

            // Get suggestions from Foursquare
            FoursquareExploreTask foursquareTask = new FoursquareExploreTask(this, 3000, 15, item);
            foursquareTask.execute(loc);

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
        int size = latLngArrayList.size();
        LatLng originCoordinate = latLngArrayList.get(0);
        LatLng destinationCoordinate = latLngArrayList.get(size - 1);
        StringBuilder url = new StringBuilder("https://maps.googleapis.com/maps/api/directions");
        url.append("/json");
        url.append("?origin=" + originCoordinate.latitude + "," + originCoordinate.longitude);
        StringBuilder waypoints = new StringBuilder("");
        if(size > 2){
            waypoints.append("&waypoints=optimize:true");
            for(int position = 1; position < size - 1; position ++){
                LatLng coordinate = latLngArrayList.get(position);
                waypoints.append("|" + coordinate.latitude + "," + coordinate.longitude);
            }
        }
        url.append(waypoints);
        url.append("&destination=" + destinationCoordinate.latitude + "," + destinationCoordinate.longitude);
        url.append("&mode=" + transportMode);
        System.out.println(url);
        return url.toString();
    }

    private void addMarkers() {
        if (_map != null) {
            for(List<LatLng> latLng : _latLngArrayList){
                for (int i=0; i<latLng.size(); i++){
                    LatLng position = latLng.get(i);
                    UserItem item = _userItemList.get(i);
                    String name = item.getName();
                    Marker m = _map.addMarker(new MarkerOptions().position(position).title(name));
                    _userMarkers.put(m.getId(), false);
                    _userItemsByMarkerId.put(m.getId(), item);
                }
            }
        }
    }

    /**
     * Adds a marker for a suggestion, updates the suggestion with the new marker, and saves the
     * suggestion.
     * @param item A suggestion of a destination for the user
     * @param icon An icon to represent the suggestion on the map
     * @param title Name of the destination
     */
    public void addSuggestionItem(SuggestionItem item, BitmapDescriptor icon, String title) {
        Suggestion suggestion = item.getSuggestion();
        Marker marker = drawMarker(suggestion.getLocation(), icon);
        marker.setTitle(title);
        suggestion.setMarker(marker);
        _suggestionItemsByMarkerId.put(marker.getId(), item);
        String category = suggestion.getCategory();
        List<Marker> cat = _markersByCategory.get(category);
        cat.add(marker);

        // Refilter markers
        setVisibilityByFilters();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        _map = googleMap;
        _map.moveCamera(CameraUpdateFactory.newLatLngZoom(_latLngArrayList.get(0).get(0),
                13));
        addMarkers();
        _map.setOnMarkerClickListener(this);

        // SuggestionInfoAdapter listens for and adapts all infowindow-related activity
        SuggestionInfoAdapter infoAdapter = new SuggestionInfoAdapter(getLayoutInflater(), this);
        _map.setInfoWindowAdapter(infoAdapter);
        _map.setOnInfoWindowClickListener(infoAdapter);

        _map.setMyLocationEnabled(true);
        initSuggestionMarkers();

        for(int i = 0; i < _latLngArrayList.size(); i++){
            DirectionsTask downloadTask = new DirectionsTask();
            String url = getMapsApiDirectionsUrl(_latLngArrayList.get(i), _sortedTransportMode.get(i));
            downloadTask.execute(url);
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
            Log.d("SIZE", String.valueOf(mapInformation.getRoutes().size()));
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
                System.out.println(points);
                polyLineOptions.addAll(points);
                polyLineOptions.width(7f);
//                polyLineOptions.color(Color.BLUE);
                polyLineOptions.color(mapInformation.getRouteColor().get(i));
                _map.addPolyline(polyLineOptions);
            }
            drawInstructions(mapInformation);
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
        } else if (id == R.id.budget_button) {
            startBudgetActivity();
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

            for (SuggestionItem suggestionItem : _suggestionItemsByMarkerId.values()) {
                Suggestion s = suggestionItem.getSuggestion();
                Marker m = s.getMarker();

                Integer priceLevel = s.getParsedPriceLevel();
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
        _filters.setFilterOptionChecked(filterName, filterOptionName);
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
        _filters.clearFilterRadioButtons(filterTitleString);

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
        _filters.clearFilterRadioButtons(filterTitleString);
    }

    /**
     * Called when the 'Clear suggestions' button (which clears all the markers) is clicked.
     */
    public void onClearClick(View v) {
        // Clear the markers
        setAllMarkersVisible(false);

        // Reset the RadioButtons
        _filters.clearAllFilterRadioButtons();

        // Hide the filter button - no suggestions to filter
        _filterItem.setVisible(false);

        // Hide the filters fragment
        ExpandableListView filtersList = _filters.getFilterList();
        _drawerLayout.closeDrawer(filtersList);

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

    private void startBudgetActivity() {
        // TODO Maybe make the database access a task?
        if (_itineraryUpdateRequired) {
            // Update the database if the itinerary has changed
            Collection<UserItem> userItems = _userItemsByMarkerId.values();
            _itineraryDataSource.recreateItinerary();
            _itineraryDataSource.insertMultipleItineraryItems(userItems);
            _itineraryUpdateRequired = false;
        }
        Intent intent = new Intent(this, ItineraryActivity.class);
        startActivity(intent);
    }
}
