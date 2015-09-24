package com.mapster.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.mapster.R;
import com.mapster.apppreferences.AppPreferences;
import com.mapster.itinerary.ItineraryItem;
import com.mapster.map.models.SortedCoordinate;
import com.mapster.tutorial.Tutorial;
import com.mapster.api.expedia.ExpediaHotelListTask;
import com.mapster.api.foursquare.FoursquareExploreTask;
import com.mapster.api.googleplaces.GooglePlacesTask;
import com.mapster.date.CustomDate;
import com.mapster.filters.Filters;
import com.mapster.geocode.GeoCode;
import com.mapster.infowindow.SuggestionInfoWindowAdapter;
import com.mapster.itinerary.SuggestionItem;
import com.mapster.itinerary.UserItem;
import com.mapster.persistence.ItineraryDataSource;
import com.mapster.itinerary.UpdateMainFromItineraryTask;
import com.mapster.json.JSONParser;
import com.mapster.json.StatusCode;
import com.mapster.map.models.MapInformation;
import com.mapster.map.models.Path;
import com.mapster.map.models.Routes;
import com.mapster.suggestions.Suggestion;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.joda.time.DateTime;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainActivity extends ActionBarActivity implements GoogleMap.OnMarkerClickListener, OnMapReadyCallback {
    private static final String TAG = "MainActivity";
    private static final String USER_ITEM_LIST = "USER_ITEM_LIST";

    // Only used to get data from PlacesActivity. Use _userItemsByMarkerId to keep track of UserItems.
    private ArrayList<UserItem> _userItemList;

    // Required to manually trigger onInfoWindowClick() for the tutorial
    private SuggestionInfoWindowAdapter _infoWindowAdapter;

    private GoogleMap _map;
    private List<SortedCoordinate> _sortedCoordinateList;
    private String _startDateTime;
    private CustomDate _customDate;
    private SlidingUpPanelLayout _layout;

    // Markers divided into categories (to make enumeration of categories faster)
    private HashMap<String, List<Marker>> _markersByCategory;

    private ParserTask _parserTask; // Must wait for this before switching to ItineraryActivity

    // All suggestions, keyed by marker ID
    private HashMap<String, SuggestionItem> _suggestionItemsByMarkerId;
    // User items, keyed by marker ID
    private HashMap<String, UserItem> _userItemsByMarkerId;
    // All markers by marker ID
    private HashMap<String, Marker> _markersByMarkerId;

    // Contains marker ids and a boolean to indicate whether it has been clicked
    private HashMap<String, Boolean> _userMarkers;

    private ImageButton _filterItem; // Filters button

    // The layout for this activity - used to listen for drawer state.
    private DrawerLayout _drawerLayout;

    private String _currentCategory;
    private Integer _priceLevel;

    // Interacts with itinerary database
    private ItineraryDataSource _itineraryDataSource;

    // Controls the state of the filters
    private Filters _filters;

    // For tutorial
    private Marker _firstMarker;
    private Marker _suggestedMarker;
    private Tutorial _tutorial;
    private AppPreferences _preferences;
    private ImageButton _itineraryItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _preferences = new AppPreferences(this);
        _itineraryDataSource = new ItineraryDataSource(this);
        _itineraryDataSource.open();

        setContentView(R.layout.activity_main);

        _layout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        _tutorial = new Tutorial(this);
        initializeGoogleMap();
        _userMarkers = new HashMap<>();

        _userItemsByMarkerId = new HashMap<>();
        _suggestionItemsByMarkerId = new HashMap<>();
        _markersByMarkerId = new HashMap<>();

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
    protected void onPause() {
        updateItineraryDatabase();
        _itineraryDataSource.close();
        super.onPause();
    }

    @Override
    public void onResume() {
        _itineraryDataSource.open();
        UpdateMainFromItineraryTask updateTask = new UpdateMainFromItineraryTask(this);
        updateTask.execute();
        super.onResume();
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

    public void getDataFromPlaceActivity(){
        Intent i = getIntent();
        _startDateTime = i.getStringExtra(PlacesActivity.START_DATETIME);
        _userItemList = new ArrayList<>(_itineraryDataSource.getUnsavedAndSavedUserItems(this));
        sortCoordinateArrayList();
        _customDate = new CustomDate(_startDateTime);
    }

    private void sortCoordinateArrayList(){
        _sortedCoordinateList = new ArrayList<>();
        int posInCoordinateArrayList = 0;
        List<LatLng> helper = null;
        int i = 0;
        String modeTransport = null;
        while( i <_userItemList.size() - 1){
            modeTransport = _userItemList.get(i).getTravelMode();
            if(_userItemList.get(i).getTravelMode().equals(_userItemList.get(i + 1).getTravelMode())) {
                while (_userItemList.get(i).getTravelMode().equals(_userItemList.get(i + 1).getTravelMode())) {
                    i++;
                    if (i >= _userItemList.size() - 1) {
                        break;
                    }
                }
            }
            i++;
            helper = addPointToList(i, posInCoordinateArrayList);
            if (helper.size() > 2 && modeTransport.equals(GeoCode.TravelMode.TRANSIT.name().toLowerCase())) {
                List<LatLng> transitHelper = null;
                for (int j = 0; j < helper.size() - 1; j++) {
                    transitHelper = new ArrayList<>();
                    for (int k = j; k < j + 2; k++) {
                        transitHelper.add(helper.get(k));
                    }
                    _sortedCoordinateList.add(new SortedCoordinate(modeTransport, transitHelper));
                }
            } else if (helper.size() > 8) {
                int position = 1;
                for (int j = 8; j < helper.size(); j = j + 8){
                    _sortedCoordinateList.add(new SortedCoordinate(modeTransport, helper.subList(position - 1, j)));
                    position = j ;
                    if (j + 8 >= helper.size() && j != helper.size()){
                        _sortedCoordinateList.add(new SortedCoordinate(modeTransport, helper.subList(position - 1, helper.size())));
                    }
                }
            } else {
                _sortedCoordinateList.add(new SortedCoordinate(modeTransport, helper));
            }
            posInCoordinateArrayList = i;
        }
    }

    private List<LatLng> addPointToList(int position, int posInCoordinateArray){
        List<LatLng> helper = new ArrayList<>();
        for(int j = posInCoordinateArray; j <= position; j++){
            helper.add(_userItemList.get(j).getLocation());
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
                s.requestSuggestionInfo(this);

                // Use the detail to set the information displayed in the popup and save it to the place.
                String infoWindowString = s.getInfoWindowString();
                marker.setSnippet(infoWindowString);
                System.out.println(infoWindowString);
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
            _filterItem.setVisibility(View.VISIBLE);
            return false;
        } else {
            // User-defined marker has been clicked before. Display suggestions that aren't visible
            setVisibilityByFilters();
            marker.showInfoWindow();
            _filterItem.setVisibility(View.VISIBLE);
            return false;
        }
    }

    private String getMapsApiDirectionsUrl(List<LatLng> latLngArrayList, String transportMode, MapInformation mapInformation) {
        int size = latLngArrayList.size();
        LatLng originCoordinate = latLngArrayList.get(0);
        LatLng destinationCoordinate = latLngArrayList.get(size - 1);
        StringBuilder url = new StringBuilder("https://maps.googleapis.com/maps/api/directions");
        url.append("/json");
        url.append("?origin=" + originCoordinate.latitude + "," + originCoordinate.longitude);
        StringBuilder waypoints = new StringBuilder("");
        if(size > 2){
            waypoints.append("&waypoints=optimize:false");
            for(int position = 1; position < size - 1; position ++){
                LatLng coordinate = latLngArrayList.get(position);
                waypoints.append("|" + coordinate.latitude + "," + coordinate.longitude);
            }
        }
        url.append(waypoints);
        url.append("&destination=" + destinationCoordinate.latitude + "," + destinationCoordinate.longitude);
        if (mapInformation == null)
            url.append("&departure_time=" + _customDate.secondsBetween());
        else {
            url.append("&departure_time=" + mapInformation.getDate().secondsBetween());
        }
        url.append("&mode=" + transportMode);
//        url.append("&key=" + getString(R.string.API_KEY));
        System.out.println(url);
        return url.toString();
    }

    private void addMarkers() {
        Set<String> names = new HashSet<>();

        if (_map != null) {
            int pos = 0;
            for (int j = 0; j < _sortedCoordinateList.size(); j++) {
                List<LatLng> latLng = _sortedCoordinateList.get(j).getSortedCoordinateList();
                for (int i = 0; i < latLng.size(); i++) {
                    LatLng position = latLng.get(i);
                    UserItem item = null;
                    String name = null;
                    if (pos == 0 || i != 0) {
                        item = _userItemList.get(pos);
                        name = item.getName();
                        pos++;
                    }

                    if (name != null && !names.contains(name)) {
                        Marker m = _map.addMarker(new MarkerOptions().position(position).title(name));
                        String markerId = m.getId();
                        _markersByMarkerId.put(markerId, m);
                        if (pos <= 1) {
                            _firstMarker = m;
                        }
                        _userMarkers.put(markerId, false);
                        item.setMarkerId(markerId);
                        _userItemsByMarkerId.put(markerId, item);
                        names.add(name);
                    }
                }
            }
        }

        updateItineraryDatabase();
    }

    private void updateItems() {
        if (_map != null) {
            Map<String, UserItem> itemsByName = new HashMap<>();
            for (UserItem item : _userItemList)
                itemsByName.put(item.getName(), item);

            for (UserItem itemToUpdate : _userItemsByMarkerId.values()) {
                String itemName = itemToUpdate.getName();
                DateTime updatedTime = itemsByName.get(itemName).getTime();
                itemToUpdate.setDateTime(updatedTime);
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
        String markerId = marker.getId();
        marker.setTitle(title);
        item.setMarkerId(markerId);
        _suggestionItemsByMarkerId.put(markerId, item);
        String category = suggestion.getCategory();
        List<Marker> cat = _markersByCategory.get(category);
        cat.add(marker);
        _suggestedMarker = marker;
        // Refilter markers
        setVisibilityByFilters();
    }

    public void moveCamera() {
        _map.moveCamera(CameraUpdateFactory.newLatLngZoom(_sortedCoordinateList.get(0).getSortedCoordinateList().get(0),
                15));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        _map = googleMap;
        _map.setOnMarkerClickListener(this);

        // SuggestionInfoAdapter listens for and adapts all infowindow-related activity
        _infoWindowAdapter = new SuggestionInfoWindowAdapter(getLayoutInflater(), this);
        _map.setInfoWindowAdapter(_infoWindowAdapter);
        _map.setOnInfoWindowClickListener(_infoWindowAdapter);

        _map.setMyLocationEnabled(true);
        initSuggestionMarkers();
    }

    public void setupMapFromItinerary() {
        addMarkers();
        final View mapView =  getSupportFragmentManager()
                .findFragmentById(R.id.map).getView();
        if (mapView.getViewTreeObserver().isAlive()) {
            mapView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    // remove the listener
                    // ! before Jelly Bean:
                    mapView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    // ! for Jelly Bean and later:
                    //mapView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    // set map viewport
                    // CENTER is LatLng object with the center of the map
                    _map.moveCamera(CameraUpdateFactory.newLatLngZoom(_firstMarker.getPosition(), 15));
                    // ! you can query Projection object here
                    doTutorialInstruction();
                }
            });
        }
        _parserTask = new ParserTask(this); // Saved so we can wait for it before going to the next activity
        _parserTask.execute();
    }

    private void doTutorialInstruction(){
        if (!_preferences.isDoneInstructionTutorial()) {
            final View totalTextView = findViewById(R.id.total_distance_duration);
            final LinearLayout instructionLayout = (LinearLayout) findViewById(R.id.instructions);
            TextView tv = setUpTutorial(new Point((int) totalTextView.getX(), (int) totalTextView.getY()), totalTextView.getWidth(), totalTextView.getHeight(), 0, 0);
            FrameLayout fl = (FrameLayout) totalTextView.getParent();
            fl.addView(tv);
            _tutorial.setToolTip("Turn-by-turn Instructions", "Tap for more detailed instructions. Tap again to close",
                    Gravity.TOP | Gravity.CENTER, getResources().getColor(R.color.indigo_600));
            _tutorial.setOverlayRectangular();
            _tutorial.setTutorialByClick(tv);
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    _tutorial.cleanUp();
                    SlidingUpPanelLayout layout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
                    layout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
                    v.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    TextView instruction = (TextView) instructionLayout.getChildAt(0);
                                    _tutorial.setToolTip("Instruction", "Tap each instruction to indicate that you have" +
                                                    " done this instruction.", Gravity.BOTTOM | Gravity.CENTER,
                                            getResources().getColor(R.color.indigo_600));
                                    _tutorial.setOverlayRectangular();
                                    _tutorial.setTutorialByClick(instruction);
                                }
                            });

                        }
                    }, 100);
                    v.setOnClickListener(null);
                    v.setVisibility(View.GONE);
                    TextView instruction = (TextView) instructionLayout.getChildAt(0);
                    if (instruction != null) {
                        instruction.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                _tutorial.cleanUp();
                                ((TextView) v).setPaintFlags(((TextView) v).getPaintFlags() ^ Paint.STRIKE_THRU_TEXT_FLAG);

                                doTutorialForCollapseInstruction();
                            }
                        });
                    }
                }
            });
        }
    }
    private TextView setUpTutorial(Point markerScreenPosition,int width, int height, float offsetX, float offsetY){
        TextView tv = new TextView(this);
        FrameLayout.LayoutParams flp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,FrameLayout.LayoutParams.WRAP_CONTENT);
        tv.setWidth(width);
        tv.setHeight(height);
        tv.setLayoutParams(flp);
        tv.setX(markerScreenPosition.x - offsetX);
        tv.setY(markerScreenPosition.y - offsetY);
        return tv;
    }

    private void doTutorialForCollapseInstruction(){
        if (!_preferences.isDoneInstructionTutorial()) {
            final View totalTextView = findViewById(R.id.total_distance_duration);
            TextView tv = setUpTutorial(new Point((int) totalTextView.getX(), (int) totalTextView.getY()), totalTextView.getWidth(), totalTextView.getHeight(), 0, 0);
            FrameLayout fl = (FrameLayout) totalTextView.getParent();
            fl.addView(tv);
            _tutorial.setToolTip("Turn-by-turn Instructions", "Tap again to close",
                    Gravity.BOTTOM | Gravity.CENTER, getResources().getColor(R.color.indigo_600));
            _tutorial.setOverlayRectangular();
            _tutorial.setTutorialByClick(totalTextView);
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    _tutorial.cleanUp();
                    SlidingUpPanelLayout layout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
                    layout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                    v.setOnClickListener(null);
                    v.setVisibility(View.GONE);
                    doTutorialSuggestion();
                }
            });
            _preferences.setDoneInstructionTutorial();
        }
    }

    private void doTutorialSuggestion(){
        if (!_preferences.isDoneSuggestionTutorial()) {
            _map.moveCamera(CameraUpdateFactory.newLatLngZoom(_firstMarker.getPosition(), 15));
            Point markerScreenPosition = _map.getProjection().toScreenLocation(_firstMarker.getPosition());
            TextView marker = setUpTutorial(markerScreenPosition, 120, 120, 50, 90);
            FrameLayout fl = (FrameLayout) findViewById(R.id.map_layout);
            fl.addView(marker);
            final MainActivity activity = this;
            _tutorial.setToolTip("Map Marker",
                    "Tap the red map marker\nto give you points of interest\nin the surrounding area.",
                    Gravity.BOTTOM | Gravity.CENTER, getResources().getColor(R.color.indigo_600));
            _tutorial.setOverlayCircle();
            _tutorial.setTutorialByClick(marker);
            marker.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    _tutorial.cleanUp();
                    activity.onMarkerClick(_firstMarker);
                    v.setOnClickListener(null);
                    v.setVisibility(View.GONE);
                }
            });
        }
    }

    public void doTutorialAfterSuggestionAppear(){
        if (!_preferences.isDoneSuggestionTutorial()) {
            final MainActivity activity = this;
            _map.moveCamera(CameraUpdateFactory.newLatLngZoom(_suggestedMarker.getPosition(), 15));
            final Point markerScreenPosition = _map.getProjection().toScreenLocation(_suggestedMarker.getPosition());
            TextView tv = setUpTutorial(markerScreenPosition, 120, 120, 50, 90);
            FrameLayout fl = (FrameLayout) findViewById(R.id.map_layout);
            fl.addView(tv);
            _tutorial.setToolTip("Suggested Marker",
                    "Tap this suggested marker to see\n" +
                            "more information about this hotel.",
                    Gravity.BOTTOM | Gravity.CENTER, getResources().getColor(R.color.indigo_600));
            _tutorial.setTutorialByClick(tv);
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    _tutorial.cleanUp();
                    activity.onMarkerClick(_suggestedMarker);
                    v.setOnClickListener(null);
                    v.setVisibility(View.GONE);
                    TextView tv1 = new TextView(activity);
                    FrameLayout.LayoutParams flp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
                    tv1.setWidth(120);
                    tv1.setHeight(120);
                    tv1.setLayoutParams(flp);

                    tv1.setX(markerScreenPosition.x - 50);
                    tv1.setY(markerScreenPosition.y - 150);
                    FrameLayout fl = (FrameLayout) findViewById(R.id.map_layout);
                    fl.addView(tv1);
                    _tutorial.setToolTip("Add To Itinerary", "Tap this window to add this location to itinerarry", Gravity.BOTTOM | Gravity.CENTER, activity.getResources().getColor(R.color.indigo_600));
                    _tutorial.setTutorialByClick(tv1);
                    tv1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            _tutorial.cleanUp();
                            v.setOnClickListener(null);
                            v.setVisibility(View.GONE);
                            activity._infoWindowAdapter.onInfoWindowClick(_suggestedMarker);
                            _preferences.setDoneSuggestionTutorial();
                        }
                    });
                }
            });
        }
    }

    public void doTutorialActionBar() {
        if (!_preferences.isDoneActionBarMainTutorial()){
            _tutorial.setToolTip("Filter", "Tap this icon to filter the suggested POIs.",
                    Gravity.BOTTOM | Gravity.CENTER, getResources().getColor(R.color.indigo_600));
            _tutorial.setOverlayCircle();
            _tutorial.setTutorialByClick(_filterItem);

            _filterItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    _tutorial.cleanUp();
                    onFilterButtonClick();
                    if (!_preferences.isDoneActionBarMainTutorial()) {
                        _tutorial.setToolTip("Itinerary/Budget", "Tap this icon to go to the itinerary/budget screen.",
                                Gravity.BOTTOM | Gravity.CENTER, getResources().getColor(R.color.indigo_600));
                        _tutorial.setOverlayCircle();
                        _tutorial.setTutorialByClick(_itineraryItem);
                        _itineraryItem.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                _tutorial.cleanUp();
                                startItineraryActivity();
                            }
                        });
                        _preferences.setDoneActionBarMainTutorial();
                    }
                }
            });
        }

    }

    public void doTutorialAddingToItitinerary(final Button yes, Button no){
        if(!_preferences.isDoneSuggestionTutorial()){
            no.setEnabled(false);
            _tutorial.setToolTip("Add To Itinerary", "Add this location to itinerary list", Gravity.CENTER | Gravity.BOTTOM, getResources().getColor(R.color.indigo_600));
            _tutorial.setNoOverlay();
            _tutorial.setTutorialByClick(yes);
            _preferences.setDoneSuggestionTutorial();
        }
    }

    public void cleanUpTutorial(){
        try {
            _tutorial.cleanUp();
        } catch (NullPointerException e){
            e.printStackTrace();
        }
    }

    public ItineraryDataSource getItineraryDatasource() {
        return _itineraryDataSource;
    }

    public Map<String, UserItem> getUserItemsByMarkerId() {
        return _userItemsByMarkerId;
    }

    public Map<String, SuggestionItem> getSuggestionItemsByMarkerId() {
        return _suggestionItemsByMarkerId;
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
        _markersByMarkerId.put(m.getId(), m);
        return m;
    }

    private class ParserTask extends
            AsyncTask<Void, Integer, MapInformation> {
        private ProgressDialog _dialog;
        private MainActivity _mainMainActivity;
        private LinearLayout _linearLayout;
        ParserTask(MainActivity mainActivity){
            _mainMainActivity = mainActivity;
            _linearLayout = (LinearLayout)findViewById(R.id.instructions);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            _dialog = new ProgressDialog(_mainMainActivity);
            _dialog.setMessage("Please wait...");
            _dialog.show();
        }
        @Override
        protected MapInformation doInBackground(Void... params) {
            MapInformation mapInformation = null;
            JSONObject jObject;
            for (SortedCoordinate sortedCoordinate : _sortedCoordinateList){
                String url = getMapsApiDirectionsUrl(sortedCoordinate.getSortedCoordinateList(), sortedCoordinate.getModeTransport(),mapInformation);
                if (mapInformation != null) {
                    _customDate = mapInformation.getDate();
                    System.out.println(mapInformation.getDate().toString());
                }
                String jsonData = readTask(url);
                try {
                    jObject = new JSONObject(jsonData);
                    JSONParser parser = new JSONParser(_customDate);
                    mapInformation = parser.parse(jObject, mapInformation);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // Save the dates/times for each waypoint to the corresponding itinerary items
            if (mapInformation != null && mapInformation.getDates() != null) {
                List<CustomDate> waypointDates = mapInformation.getDates();
                if (_userItemList.size() == waypointDates.size()) {
                    // The dates saved in the MapInformation are expected to correspond with the user-defined waypoints
                    for (int i=0; i<waypointDates.size(); i++)
                        _userItemList.get(i).setDateTime(waypointDates.get(i).getDateTime());
                } else {
                    throw new AssertionError("Expected the number of dates to be the same as the number of waypoints");
                }
            }

            return mapInformation;
        }

        private String readTask(String url) {
            String data = "";
            com.mapster.connectivities.HttpConnection http = new com.mapster.connectivities.HttpConnection();
            try {
                 data = http.readUrl(url);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return data;
        }

        @Override
        protected void onPostExecute(MapInformation mapInformation) {

            _dialog.dismiss();
            if(mapInformation.getStatus().equals(StatusCode.NOT_FOUND)){
                createToast("At least one of the locations specified in the request's origin, " +
                        "destination, or stop points could not be geocoded.", Toast.LENGTH_SHORT);
                return;
            } else if (mapInformation.getStatus().equals(StatusCode.ZERO_RESULTS)){
                createToast("No route could be found", Toast.LENGTH_SHORT);
                return;
            } else if (mapInformation.getStatus().equals(StatusCode.INVALID_REQUEST)){
                createToast("Please select one of the suggested locations in previous screen", Toast.LENGTH_SHORT);
                return;
            } else if (mapInformation.getStatus().equals(StatusCode.OVER_QUERY_LIMIT)){
                createToast("Please contact developers for a new API key", Toast.LENGTH_SHORT);
                return;
            } else if (mapInformation.getStatus().equals(StatusCode.REQUEST_DENIED)){
                createToast("The service denied use of the directions service by your application.", Toast.LENGTH_SHORT);
                return;
            } else if (mapInformation.getStatus().equals(StatusCode.UNKNOWN_ERROR)){
                createToast("A directions request could not be processed due to a server error. The request may succeed if you try again.", Toast.LENGTH_SHORT);
                return;
            }

            ArrayList<LatLng> points;
            PolylineOptions polyLineOptions = null;
            // traversing through routes
            for (int i = 0; i < mapInformation.getRoutes().size(); i++) {
                points = new ArrayList<LatLng>();
                polyLineOptions = new PolylineOptions();
                Routes route = mapInformation.getRoutes().get(i);
                for (int j = 0; j < route.getRoutePoints().size(); j++) {
                    HashMap<String, String> point = route.getRoutePoints().get(j);
                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);
                    points.add(position);
                }
                polyLineOptions.addAll(points);
                polyLineOptions.width(10f);
                polyLineOptions.color(route.getColor());
                _map.addPolyline(polyLineOptions);
            }
            drawInstructions(mapInformation);
        }

        private void drawInstructions(MapInformation mapInformation){
            CustomDate startDate = new CustomDate(_startDateTime);
            TextView total = (TextView) findViewById(R.id.total_distance_duration);
            addFontToTextView(total);
            StringBuilder output = new StringBuilder("Total Duration: ");
            output.append(CustomDate.convertSecondsToHours(CustomDate.secondsBetween(
                    mapInformation.getPaths().get(mapInformation.getPaths().size() - 1)
                            .getDate().getDateTime(), startDate.getDateTime())));
            output.append("<br/> Total Distance: ");
            DecimalFormat df = new DecimalFormat("#.#");
            output.append(df.format(mapInformation.getTotalDistance().getValue()/1000.0) + " km");
            output.append("<br/> Start Date: <b>" + startDate.toString() + "<b>");

            total.setText(Html.fromHtml(output.toString()));

            List<Path> paths = mapInformation.getPaths();
            Path path = null;
            StringBuilder name = null;
            for(int i = 0; i < paths.size(); i++){
                path = paths.get(i);
                name = new StringBuilder();
                name.append(path.getInstruction().getInstruction().replaceAll("<(/)?div(.+?(?=>))?>", ". "));
                if (!paths.get(i).getDuration().getName().isEmpty()) {
                    name.append("<br/>");
                    name.append(path.getMode() + ": ");
                    name.append("For ");
                    name.append(path.getDistance().getName());
                    name.append(", ");
                    name.append(path.getDuration().getName());
                    name.append("<br/>Arrival Time: ");
                    name.append(path.getDate().toString());
                }
                addChildToLayout(name.toString(), 16);
            }
        }

        private void addChildToLayout(String name, int size){
            _linearLayout.addView(createTextView(name, size));
        }
    }

    public void addFontToTextView(TextView view){
        Typeface font = Typeface.createFromAsset(getAssets(), "font/ColabReg.otf");
        view.setTypeface(font);
    }

    protected TextView createTextView(String name, int size){
        TextView valueTV = new TextView(this);
        addFontToTextView(valueTV);
        valueTV.setText(Html.fromHtml(name));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT
                ,LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 30, 0, 0);
        valueTV.setTextSize(size);
        valueTV.setLayoutParams(params);
        valueTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((TextView) v).setPaintFlags(((TextView) v).getPaintFlags() ^ Paint.STRIKE_THRU_TEXT_FLAG);
            }
        });
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
        MenuItem menuItem = menu.findItem(R.id.menu_custom);
        LinearLayout ll = (LinearLayout)menuItem.getActionView();
        _filterItem = (ImageButton)ll.getChildAt(0);
        _filterItem.setVisibility(View.INVISIBLE);
        _filterItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFilterButtonClick();
            }
        });

        _itineraryItem  = (ImageButton)ll.getChildAt(2);
        _itineraryItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startItineraryActivity();
            }
        });
        ImageButton clear = (ImageButton)ll.getChildAt(1);
        MenuItem item = menu.findItem(R.id.action_toggle);
        if (_layout != null) {
            if (_layout.getPanelState() == SlidingUpPanelLayout.PanelState.HIDDEN) {
                item.setTitle(R.string.action_show);
            } else {
                item.setTitle(R.string.action_hide);
            }
        }

        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClearClick(v);
            }
        });

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()){
            case R.id.action_toggle: {
                if (_layout != null) {
                    if (_layout.getPanelState() != SlidingUpPanelLayout.PanelState.HIDDEN) {
                        _layout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
                        item.setTitle(R.string.action_show);
                    } else {
                        _layout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                        item.setTitle(R.string.action_hide);
                    }
                }
                return true;
            }
            case R.id.action_anchor: {
                if (_layout != null) {
                    if (_layout.getAnchorPoint() == 1.0f) {
                        _layout.setAnchorPoint(0.6f);
                        _layout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
                        item.setTitle(R.string.action_anchor_disable);
                    } else {
                        _layout.setAnchorPoint(1.0f);
                        _layout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                        item.setTitle(R.string.action_anchor_enable);
                    }
                }
                return true;
            }

            case R.id.action_settings:{
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onBackPressed() {
        if (_layout != null &&
                (_layout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED || _layout.getPanelState() == SlidingUpPanelLayout.PanelState.ANCHORED)) {
            _layout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        } else {
            super.onBackPressed();
        }
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
            setSuggestionMarkersVisible(true);
        } else {
            // Hide everything else
            setSuggestionMarkersVisible(false);
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
                String mId = suggestionItem.getMarkerId();
                Marker m = _markersByMarkerId.get(mId);

                Integer priceLevel = s.getParsedPriceLevel();

                if (m != null) {
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
    }

    public void updateSuggestionItem(SuggestionItem item) {
        Suggestion suggestion = item.getSuggestion();
        String markerId = item.getMarkerId();
        Marker marker = _markersByMarkerId.get(markerId);
        int iconId = 0;
        BitmapDescriptor icon;
        boolean isInItinerary = item.isInItinerary();

        switch (suggestion.getCategory()) {
            case "dining":
                iconId = isInItinerary ? R.drawable.restaurant_red : R.drawable.restaurant;
                break;
            case "accommodation":
                iconId = isInItinerary ? R.drawable.lodging_0star_red : R.drawable.lodging_0star;
                break;
            case "attractions":
                iconId = isInItinerary ? R.drawable.flag_export_red : R.drawable.flag_export;
                break;
        }
        icon = BitmapDescriptorFactory.fromResource(iconId);
        marker.setIcon(icon);

        // Remove the SuggestionItem from its UserItem if it has been deleted
        if (!isInItinerary) {
            UserItem userItem = item.getUserItem();
            userItem.removeSuggestionItem(item);
        }
    }

    /**
     * onClick listener for an item in a list of filter options. Filters markers, displaying those
     * that match that filter.
     * @param layout Contains a checkbox/radiobutton and option text.
     */
    public void onFilterItemClick(View layout) {
        TextView filterOption = (TextView) layout.findViewById(R.id.filter_option_text);
        String filterOptionName = filterOption.getText().toString();
        String filterName = null;

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
     * TODO can use this
     */
    public void onFilterClearClick(View view) {

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
        setSuggestionMarkersVisible(false);

        // Reset the RadioButtons
        _filters.clearAllFilterRadioButtons();

        // Hide the filter button - no suggestions to filter
        _filterItem.setVisibility(View.VISIBLE);

        // Hide the filters fragment
        ExpandableListView filtersList = _filters.getFilterList();
        _drawerLayout.closeDrawer(filtersList);

        // Set the filters back to null
        _currentCategory = null;
        _priceLevel = null;
    }

    public Marker getMarkerById(String markerId) {
        return _markersByMarkerId.get(markerId);
    }

    private void setMarkerListVisible(boolean isVisible, List<Marker> markers) {
        if (markers != null)
            for (Marker m: markers)
                m.setVisible(isVisible);
    }

    /**
     * Sets visibility of all the suggestion markers except the ones in the itinerary, which should
     * always be displayed.
     * @param isVisible Visibility for the markers
     */
    private void setSuggestionMarkersVisible(boolean isVisible) {
        for (SuggestionItem item: _suggestionItemsByMarkerId.values())
            if (!item.isInItinerary()) {
                String markerId = item.getMarkerId();
                Marker marker = _markersByMarkerId.get(markerId);
                marker.setVisible(isVisible);
            }
    }

    private void startItineraryActivity() {
        updateItems();
        updateItineraryDatabase();
        Intent intent = new Intent(this, ItineraryActivity.class);
        intent.putExtra("MOVED_FROM_PLACES", false);
        startActivity(intent);
    }

    public void updateItineraryDatabase() {
        Collection<UserItem> userItems = _userItemsByMarkerId.values();
        _itineraryDataSource.deleteUnsavedItineraryItems();
        _itineraryDataSource.insertMultipleItineraryItems(userItems);
    }
}
