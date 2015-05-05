package com.mapster.activities;

import android.app.FragmentTransaction;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.content.Intent;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
import com.mapster.filters.CheckableLinearLayout;
import com.mapster.filters.FiltersFragment;
import com.mapster.json.JSONParser;
import com.mapster.places.GooglePlace;
import com.mapster.places.GooglePlaceDetail;
import com.mapster.places.GooglePlaceDetailJsonParser;
import com.mapster.places.GooglePlaceJsonParser;
import com.mapster.suggestions.Suggestion;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class MainActivity extends ActionBarActivity implements GoogleMap.OnMarkerClickListener {
    private ArrayList<String> coordinateArrayList;
    private List<String> nameList;
    private ArrayList<LatLng> latLngArrayList;
    private GoogleMap _map;

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
    FiltersFragment filtersFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //TODO refactor
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeGoogleMap();
        getDataFromPlaceActivity();
        convertStringArrayListToLatLngArrayList();
        _userMarkers = new HashMap<>();
        String url = getMapsApiDirectionsUrl();
        DirectionsTask downloadTask = new DirectionsTask();
        downloadTask.execute(url);

        _map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngArrayList.get(0),
                13));
        addMarkers();
        _map.setOnMarkerClickListener(this);
        _map.setInfoWindowAdapter(new SuggestionInfoAdapter(getLayoutInflater()));

        initSuggestionMarkers();
        _suggestionsByMarkerId = new HashMap<>();

        filtersFragment = (FiltersFragment) getFragmentManager().findFragmentById(R.id.filters);
        // Setting the visibility in the XML doesn't seem to have effect, so hide it here
        getFragmentManager().beginTransaction().hide(filtersFragment).commit();
    }

    /**
     * Does what it says on the tin. Required by SuggestionInfoAdapter, as Marker can't be
     * extended.
     * @param markerId = ID of a marker
     * @return = Suggestion object corresponding to that Marker id.
     */
    public Suggestion getSuggestionByMarkerId(String markerId) {
        return _suggestionsByMarkerId.get(markerId);
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
        coordinateArrayList = i.getStringArrayListExtra("COORDINATE_LIST");
        nameList = i.getStringArrayListExtra("NAME_LIST");
    }

    private void convertStringArrayListToLatLngArrayList(){
        // TODO Error checking
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

    public String buildDetailUrl(String placeId) {
        StringBuilder sb = new StringBuilder("https://maps.googleapis.com/maps/api/place/details/json?");
        sb.append("key=" + getResources().getString(R.string.API_KEY));
        sb.append("&placeid=" + placeId);
        String url = sb.toString();
        return url;
    }

    /**
     * Triggered when the button in the actionbar that opens the filters menu is clicked
     */
    public void onFilterButtonClick() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        if (filtersFragment.isVisible()) {
            ft.hide(filtersFragment);
        } else {
            ft.show(filtersFragment);
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
                PlaceDetailTask detailTask = new PlaceDetailTask();
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

    private String getMapsApiDirectionsUrl() {
        //TODO refactor String --> StringBuilder
        int size = latLngArrayList.size();
        LatLng originCoordinate = latLngArrayList.get(0);
        LatLng destinationCoordinate = latLngArrayList.get(1);
        String origin = "?origin=" + originCoordinate.latitude + "," + originCoordinate.longitude;
        String destination = "&destination=" + destinationCoordinate.latitude + "," + destinationCoordinate.longitude;
        String waypoints = "";
        if(size > 2){
            waypoints = "&waypoints=optimize:true";
            for(int position = 2; position < size; position ++){
                LatLng coordinate = latLngArrayList.get(position);
                waypoints += "|" + coordinate.latitude + "," + coordinate.longitude ;
            }
        }

        String output = "/json";
        String url = "https://maps.googleapis.com/maps/api/directions"
                + output + origin + waypoints + destination;
        return url;
    }

    private void addMarkers() {
        if (_map != null) {
            for (int i=0; i<latLngArrayList.size(); i++){
                LatLng position = latLngArrayList.get(i);
                String name = nameList.get(i);
                Marker m = _map.addMarker(new MarkerOptions().position(position).title(name));
                _userMarkers.put(m.getId(), false);
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

    /**
     * Extend this if your task involves getting a response from a URL
     */
    private class ReadTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... url) {
            return downloadUrl(url[0]);
        }
    }

    private class PlaceDetailTask extends AsyncTask<String, Void, GooglePlaceDetail> {

        @Override
        protected GooglePlaceDetail doInBackground(String... placeIds) {
            GooglePlaceDetailJsonParser detailJsonParser = new GooglePlaceDetailJsonParser();
            String url = buildDetailUrl(placeIds[0]);
            String response = downloadUrl(url);
            GooglePlaceDetail detail = null;

            try {
                JSONObject jsonResponse = new JSONObject(response);
                detail = detailJsonParser.parse(jsonResponse);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return detail;
        }
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
            ArrayList<LatLng> points;
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
     * TODO Decide whether this method is preferable to maintaining a separate data structure like _markersByCategory
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
        filtersFragment.clearFilterRadioButtons(filterName);
        ((CheckableLinearLayout)layout).setChecked(true);
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
        filtersFragment.clearFilterRadioButtons(filterTitleString);

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
    }

    /**
     * Called when the 'clear' button (which clears all the markers) is clicked.
     * @param item
     */
    public void onClearClicked(MenuItem item) {
        // Clear the markers
        setAllMarkersVisible(false);

        // Reset the menu TODO nope
        Menu filters = _filterItem.getSubMenu();
//        filters.findItem(R.id.all).setChecked(true);
//        filters.findItem(R.id.priceLevelNone).setChecked(true);

        // Hide the filter button - no suggestions to filter
        _filterItem.setVisible(false);

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

    private String buildPhotoUrl(String photoReference) {
        int maxWidth = 200;
        int maxHeight = 200;

        StringBuilder sb = new StringBuilder("https://maps.googleapis.com/maps/api/place/photo?");
        sb.append("key=" + getResources().getString(R.string.API_KEY));
        sb.append("&photoreference=" + photoReference);
        sb.append("&maxwidth=" + maxWidth);
        sb.append("&maxheight=" + maxHeight);

        return sb.toString();
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        private ImageView _image;

        public DownloadImageTask(ImageView image) {
            _image = image;
        }

        @Override
        protected Bitmap doInBackground(String... urls) {
            String url = urls[0];
            Bitmap icon = null;
            try {
                InputStream in = new URL(url).openStream();
                icon = BitmapFactory.decodeStream(in);
            } catch (IOException e) {
                Log.e("DownloadImageTask", e.getMessage());
                e.printStackTrace();
            }
            return icon;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            _image.setImageBitmap(result);
        }
    }


    public class SuggestionInfoAdapter implements GoogleMap.InfoWindowAdapter {

        private LayoutInflater _inflater;

        // This field prevents the ImageView from being garbage collected before its drawable can be
        // set, and the image returned by Picasso displayed.
        private ImageView _currentInfoWindowImage;

        public SuggestionInfoAdapter(LayoutInflater inflater) {
            _inflater = inflater;
        }

        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }

        @Override
        /**
         * This is only called if getInfoWindow() returns null. If this returns null, the default info
         * window will be displayed.
         */
        public View getInfoContents(Marker marker) {
            String markerId = marker.getId();
            Suggestion s = _suggestionsByMarkerId.get(markerId);
            View info = _inflater.inflate(R.layout.suggestion_info_window, null);

            TextView title = (TextView) info.findViewById(R.id.title);
            title.setText(marker.getTitle());

            TextView snippet = (TextView) info.findViewById(R.id.snippet);
            snippet.setText(marker.getSnippet());

            // Set the rating of the place, if the place is not user-defined
            RatingBar ratingBar = (RatingBar) info.findViewById(R.id.rating_bar);
            float rating;
            if (s != null) {
                rating = s.getRating();
            } else {
                rating = 0;
            }

            // Hide rating if none if given
            if (rating == 0) {
                ratingBar.setVisibility(View.GONE);
            } else {
                ratingBar.setRating(rating);
            }

            ImageView image = (ImageView) info.findViewById(R.id.image);

            // Load the icon into the ImageView of the InfoWindow
            if (s != null) {
                String photoReference = s.getPhotoReference();
                if (photoReference != null) {
                    String imageUrl = buildPhotoUrl(photoReference);
                    if (s.isClicked()) {
                        // Marker has been clicked before - don't need to call the callback to load icon
                        // Picasso has a fit() method for fitting to an ImageView, but it doesn't seem to work.
                        Picasso.with(MainActivity.this).load(imageUrl).resize(150,150).centerCrop().into(image);
                    } else {
                        // Marker clicked for first time - download the icon and load it into the view
                        s.setClicked(true);
                        _currentInfoWindowImage = image;
                        Picasso.with(MainActivity.this).load(imageUrl).resize(150, 150).centerCrop()
                                .into(_currentInfoWindowImage, new InfoWindowRefresher(marker));
                    }
                }
            }

            // Hide the ImageView if it has no image
            if (image.getDrawable() == null) {
                image.setVisibility(View.GONE);
            }

            return info;
        }
    }

    private class InfoWindowRefresher implements Callback {
        private Marker _markerToRefresh;

        private InfoWindowRefresher(Marker markerToRefresh) {
            _markerToRefresh = markerToRefresh;
        }

        @Override
        public void onSuccess() {
            _markerToRefresh.showInfoWindow();
        }

        @Override
        public void onError() {

        }
    }
}
