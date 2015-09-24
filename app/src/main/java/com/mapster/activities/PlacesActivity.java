package com.mapster.activities;

import android.app.DatePickerDialog;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.mapster.R;
import com.mapster.android.gui.util.clearableautocompletetextview.ClearableAutoCompleteTextView;
import com.mapster.android.gui.util.customfonttextview.TypefaceTextView;
import com.mapster.apppreferences.AppPreferences;
import com.mapster.fragment.DatePickerFragment;
import com.mapster.fragment.TimePickerFragment;
import com.mapster.geocode.GeoCode;
import com.mapster.interfaces.GeoCodeListener;
import com.mapster.itinerary.ItineraryItem;
import com.mapster.itinerary.SuggestionItem;
import com.mapster.itinerary.UserItem;
import com.mapster.persistence.ItineraryDataSource;
import com.mapster.persistence.LoadAndSaveHelper;
import com.mapster.places.autocomplete.PlacesAutoCompleteAdapter;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import tourguide.tourguide.Overlay;
import tourguide.tourguide.Pointer;
import tourguide.tourguide.ToolTip;
import tourguide.tourguide.TourGuide;

public class PlacesActivity extends ActionBarActivity implements OnItemClickListener, DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener, GeoCodeListener {

    public static final String START_DATETIME = "START_DATETIME";

    // Persistence stuff
    private ItineraryDataSource _itineraryDataSource;
    private LoadAndSaveHelper _loadAndSaveItineraryHelper;

    private PlacesAutoCompleteAdapter _autoCompAdapter;
    private LinkedList<ClearableAutoCompleteTextView> _autoCompleteTextViewLinkedList;
    private List<RadioGroup> _transportModeViewList;
    private TextView _dateTextView;
    private SimpleDateFormat _dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    private SimpleDateFormat _timeFormat = new SimpleDateFormat("HH:mm");
    private String _dateStartJourney;
    private String _timeStartJourney = _timeFormat.format (new Date());
    private String _dateTimeStartJourney;
    private TextView _timeTextView;
    // List of parcelable user-defined destinations
    private ArrayList<UserItem> _userItemList;
    private TourGuide mTutorialHandler;
    private boolean isAlreadyDoTutorial = false;
    private AppPreferences _preferences;

    @Override
    protected void onDestroy() {
        _itineraryDataSource.deleteUnsavedItineraryItems();
        super.onDestroy();
    }

    @Override
    public void callback(ArrayList<UserItem> userItems) {
        if (userItems != null){
            _userItemList = retainExistingItems(userItems);
            mergeDateAndTimeToDateTime();
            moveToMainActivityWithData();
        } else {
            Toast.makeText(this,"Origin and Destination fields must not be blank",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private ArrayList<UserItem> retainExistingItems(ArrayList<UserItem> items) {
        List<ItineraryItem> existingItems = _itineraryDataSource.getUnsavedAndSavedItems(this);

        // Make a set of all the destination names that already exist in the itinerary
        Map<String, UserItem> existingItemsByName = new HashMap<>();
        for (ItineraryItem item : existingItems)
            existingItemsByName.put(item.getName(), (UserItem) item);

        // Create a new list that retains the old data, if still relevant. Copy times/dates.
        ArrayList<UserItem> updatedItems = new ArrayList<>();
        for (UserItem item : items) {
            UserItem existingItem = existingItemsByName.get(item.getName());
            if (existingItem != null) {
                DateTime time = item.getTime();

                // Copy time
                if (time != null)
                    existingItem.setDateTime(time);

                // Copy transport mode
                String modeTransport = item.getTravelMode();
                if (modeTransport != null && !modeTransport.equals(existingItem.getTravelMode())){
                    existingItem.setTravelMode(modeTransport);
                }

                // Copy country code
                String countryCode = item.getCountryCode();
                if (countryCode != null)
                    existingItem.setCountryCode(countryCode);

                updatedItems.add(existingItem);
            } else {
                updatedItems.add(item);
            }
        }
        return updatedItems;
    }
    
    @Override
    protected void onPause() {
        _itineraryDataSource.close();
        super.onPause();
    }

    @Override
    public void onResume() {
        _itineraryDataSource.open();
        _userItemList = new ArrayList<>(_itineraryDataSource.getUnsavedAndSavedUserItems(this));
        super.onResume();
    }

    public void showDatePickerDialog(View v) {
        _dateTextView = (TextView)v;
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        DialogFragment newFragment = new DatePickerFragment(PlacesActivity.this);
        newFragment.show(ft, "date_dialog");
    }

    public void showTimePickerDialog(View v){
        _timeTextView = (TextView)v;
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        DialogFragment newFragment = new TimePickerFragment(PlacesActivity.this);
        newFragment.show(ft, "time_dialog");
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        Calendar cal = new GregorianCalendar(year, monthOfYear, dayOfMonth);
        _dateStartJourney = _dateFormat.format(cal.getTime());
        _dateTextView.setText(_dateStartJourney);
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        LocalTime lt = new LocalTime(hourOfDay, minute);
        _timeStartJourney = lt.toString("HH:mm");
        _timeTextView.setText(_timeStartJourney);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        _autoCompleteTextViewLinkedList = new LinkedList<>();
        _transportModeViewList = new ArrayList<>();
        _autoCompAdapter = new PlacesAutoCompleteAdapter(this, R.layout.list_item);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places);
        addViewsInLayoutToArrayList((LinearLayout) findViewById(R.id.place_activity_layout));
        initializeAutoCompleteTextViewInArrayList();
        initializeRadioButton(_transportModeViewList.get(0));
        _userItemList = new ArrayList<>();
        _preferences = new AppPreferences(this);

        // Set up database (saving itineraries)
        _itineraryDataSource = new ItineraryDataSource(this);
        _itineraryDataSource.open();
        _loadAndSaveItineraryHelper = new LoadAndSaveHelper(this, getLayoutInflater(),
                _itineraryDataSource, _autoCompleteTextViewLinkedList, _transportModeViewList);

        // Set current itinerary name to null
        writeItineraryNameToSettings(null);
    }

    private void writeItineraryNameToSettings(String itineraryName) {
        String sharedPrefsName = getResources().getString(R.string.shared_prefs);
        String itineraryNamePrefs = getResources().getString(R.string.itinerary_name_prefs);
        SharedPreferences settings = getSharedPreferences(sharedPrefsName, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(itineraryNamePrefs, itineraryName);
        editor.apply();
    }

    private void addViewsInLayoutToArrayList(LinearLayout llayout){
        for (int i = 0; i < llayout.getChildCount(); i++) {
            if (llayout.getChildAt(i) instanceof ClearableAutoCompleteTextView) {
                _autoCompleteTextViewLinkedList.add((ClearableAutoCompleteTextView) llayout.getChildAt(i));
            } else if(llayout.getChildAt(i) instanceof RadioGroup) {
                _transportModeViewList.add((RadioGroup) llayout.getChildAt(i));
            }
        }
    }

    private void initializeAutoCompleteTextViewInArrayList(){
        for (ClearableAutoCompleteTextView acTextView : _autoCompleteTextViewLinkedList){
            initializeAutoCompleteTextViews(acTextView);
        }
    }

    private void initializeAutoCompleteTextViews(ClearableAutoCompleteTextView autoCompleteTextView) {
        autoCompleteTextView.setAdapter(_autoCompAdapter);
        autoCompleteTextView.setOnItemClickListener(this);
        displayTextFromStart(autoCompleteTextView);
    }

    private void displayTextFromStart(final ClearableAutoCompleteTextView acTextView){
        acTextView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus == false) {  // lost focus
                    acTextView.setSelection(0, 0);
                }
            }
        });
    }

    private void initializeRadioButton(RadioGroup radioGroup){
        for(int i = 0; i < radioGroup.getChildCount(); i++){
            RadioButton rb = (RadioButton) radioGroup.getChildAt(i);
            addFontToRadioButton(rb);
        }
    }
    public void addFontToRadioButton(RadioButton view){
        Typeface font = Typeface.createFromAsset(getAssets(), "font/ColabReg.otf");
        view.setTypeface(font);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_places, menu);
        MenuItem menuItem = menu.getItem(0);
        ImageView button = (ImageView) menuItem.getActionView();
        // just adding some padding to look better
        float density = this.getResources().getDisplayMetrics().density;
        int padding = (int)(2 * density);
        button.setScaleX(0.8f);
        button.setScaleY(0.8f);
        button.setPadding(padding, padding, padding, padding);
        button.setImageDrawable(this.getResources().getDrawable(R.drawable.map_marker_green));
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setUpActionBarMenu();
            }
        });
        if (!_preferences.isDonePlacesTutorial()) {
            setUpTourGuide(button);
        }
        return true;
    }

    private void setUpActionBarMenu(){

        final int positionOfRadioGroupView = 2;
        final int positionOfAutoCompleteTextView = 1;
        LinearLayout linearLayout = addStopPoints();
        addAutoCompleteTextViewToLinkedList((ClearableAutoCompleteTextView) linearLayout.getChildAt(positionOfAutoCompleteTextView));
        initializeAutoCompleteTextViews((ClearableAutoCompleteTextView) linearLayout.getChildAt(positionOfAutoCompleteTextView));
        addRadioGroupToList((RadioGroup) linearLayout.getChildAt(positionOfRadioGroupView));
        initializeRadioButton((RadioGroup) linearLayout.getChildAt(positionOfRadioGroupView));
    }

    private void setUpTourGuide(final ImageView imageView){
        TypefaceTextView origin = (TypefaceTextView)findViewById(R.id.origin);
        final TypefaceTextView destination = (TypefaceTextView)findViewById(R.id.destination);
        final RadioButton driveMode = (RadioButton)findViewById(R.id.drive_mode);
        final RadioButton walkMode = (RadioButton)findViewById(R.id.walk_mode);
        final RadioButton bikeMode = (RadioButton)findViewById(R.id.bike_mode);
        final RadioButton transitMode = (RadioButton)findViewById(R.id.transit_mode);

        mTutorialHandler = TourGuide.init(this).with(TourGuide.Technique.Click)
                .setPointer(new Pointer())
                .setToolTip(new ToolTip()
                        .setTitle("Origin")
                        .setDescription("Give us your starting place")
                        .setShadow(true)
                        .setGravity(Gravity.BOTTOM | Gravity.RIGHT)
                        .setBackgroundColor(getResources().getColor(R.color.indigo_600)))
                .setOverlay(new Overlay().setEnterAnimation(getEnterAnimation()).setExitAnimation(getExitAnimation()))
                .playOn(origin);
        origin.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                mTutorialHandler.cleanUp();
                mTutorialHandler
                        .setToolTip(new ToolTip()
                                .setTitle("Transport Mode")
                                .setDescription("Give us your preferred mode\nof transport from origin to\nthe next location. Either Drive")
                                .setGravity(Gravity.RIGHT|Gravity.BOTTOM)
                                .setShadow(true)
                                .setBackgroundColor(getResources().getColor(R.color.indigo_600)))
                        .setOverlay(new Overlay()
                                .setEnterAnimation(getEnterAnimation())
                                .setExitAnimation(getExitAnimation())
                                .setStyle(Overlay.Style.Rectangle))
                        .playOn(driveMode);
                view.setOnClickListener(null);
            }
        });

        driveMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTutorialHandler.cleanUp();
                mTutorialHandler.setToolTip(new ToolTip()
                        .setShadow(true)
                        .setTitle("Transport Mode")
                        .setDescription("Or walk")
                        .setBackgroundColor(getResources().getColor(R.color.indigo_600)))
                        .setOverlay(new Overlay()
                                .setEnterAnimation(getEnterAnimation())
                                .setExitAnimation(getExitAnimation())
                                .setStyle(Overlay.Style.Rectangle))
                        .playOn(walkMode);

                v.setOnClickListener(null);
            }
        });

        walkMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTutorialHandler.cleanUp();
                mTutorialHandler.motionType(TourGuide.MotionType.ClickOnly).setToolTip(new ToolTip()
                        .setShadow(true)
                        .setTitle("Transport Mode")
                        .setDescription("Or cycling")
                        .setBackgroundColor(getResources().getColor(R.color.indigo_600)))
                        .setOverlay(new Overlay()
                                .setEnterAnimation(getEnterAnimation())
                                .setExitAnimation(getExitAnimation())
                                .setStyle(Overlay.Style.Rectangle))
                        .playOn(bikeMode);

                v.setOnClickListener(null);
            }
        });

        bikeMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTutorialHandler.cleanUp();
                mTutorialHandler.motionType(TourGuide.MotionType.ClickOnly).setToolTip(new ToolTip()
                        .setShadow(true)
                        .setTitle("Transport Mode")
                        .setGravity(Gravity.LEFT)
                        .setDescription("Or transit.\n Either train, bus or ferry.\n" +
                                " We will find the earliest transit so\nyou would have minimal waiting time")
                        .setBackgroundColor(getResources().getColor(R.color.indigo_600)))
                        .setOverlay(new Overlay()
                                .setEnterAnimation(getEnterAnimation())
                                .setExitAnimation(getExitAnimation())
                                .setStyle(Overlay.Style.Rectangle))
                        .playOn(transitMode);

                v.setOnClickListener(null);
            }
        });

        transitMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTutorialHandler.cleanUp();
                mTutorialHandler.motionType(TourGuide.MotionType.ClickOnly)
                        .setToolTip(new ToolTip()
                                .setShadow(true)
                                .setTitle("Destination")
                                .setDescription("Give us your last stop of your journey")
                                .setGravity(Gravity.RIGHT)
                                .setBackgroundColor(getResources().getColor(R.color.indigo_600)))
                        .setOverlay(new Overlay()
                                .setEnterAnimation(getEnterAnimation())
                                .setExitAnimation(getExitAnimation())
                                .setStyle(Overlay.Style.Rectangle))
                        .playOn(destination);

                v.setOnClickListener(null);
            }
        });

        destination.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mTutorialHandler.cleanUp();
                mTutorialHandler.motionType(TourGuide.MotionType.ClickOnly)
                        .setToolTip(new ToolTip()
                                .setShadow(true)
                                .setTitle("Stop Point")
                                .setDescription("Click to add locations between origin and destination")
                                .setGravity(Gravity.BOTTOM)
                                .setBackgroundColor(getResources().getColor(R.color.indigo_600)))
                        .setOverlay(new Overlay()
                                .setEnterAnimation(getEnterAnimation())
                                .setExitAnimation(getExitAnimation()))
                        .playOn(imageView);
                view.setOnClickListener(null);
            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mTutorialHandler.cleanUp();
                setUpActionBarMenu();
                TypefaceTextView stopPoint = (TypefaceTextView) findViewById(R.id.add_point);
                if (!isAlreadyDoTutorial()) {
                    mTutorialHandler.setToolTip(new ToolTip()
                            .setTitle("Stop Point")
                            .setDescription("Give us locations between origin and destination")
                            .setGravity(Gravity.RIGHT)
                            .setBackgroundColor(getResources().getColor(R.color.indigo_600)))
                            .setOverlay(new Overlay()
                                    .setEnterAnimation(getEnterAnimation())
                                    .setExitAnimation(getExitAnimation())
                                    .setStyle(Overlay.Style.Rectangle))
                            .playOn(stopPoint);
                    stopPoint.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mTutorialHandler.cleanUp();
                            v.setOnClickListener(null);
                            checkAlreadyDoTutorial();
                        }
                    });
                }
            }
        });
        _preferences.setDonePlacesTutorial();
    }

    private boolean isAlreadyDoTutorial(){
        return isAlreadyDoTutorial;
    }

    private void checkAlreadyDoTutorial() {
        isAlreadyDoTutorial = true;
    }

    private Animation getEnterAnimation(){
        Animation enterAnimation = new AlphaAnimation(0f, 0.5f);
        enterAnimation.setDuration(600);
        enterAnimation.setFillAfter(true);
        return enterAnimation;
    }

    private Animation getExitAnimation(){
        Animation exitAnimation = new AlphaAnimation(0.5f, 0f);
        exitAnimation.setDuration(600);
        exitAnimation.setFillAfter(true);
        return exitAnimation;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_settings:
                return true;
            case R.id.action_add_stops:
                return true;
            case R.id.save:
                // May be a race condition here
                _loadAndSaveItineraryHelper.showSaveDialogue();
                return true;
            case R.id.load:
                _loadAndSaveItineraryHelper.showLoadDialogue();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private LinearLayout addStopPoints(){
        LinearLayout layoutToAddPoint = (LinearLayout) findViewById(R.id.add_stop_points);
        LinearLayout inflateLayout = (LinearLayout)View.inflate(
                                      this, R.layout.add_stop_points, null);
        layoutToAddPoint.addView(inflateLayout);
        layoutToAddPoint.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        return inflateLayout;
    }

    private void addAutoCompleteTextViewToLinkedList(ClearableAutoCompleteTextView autoCompleteTextView){
        _autoCompleteTextViewLinkedList.add(_autoCompleteTextViewLinkedList.size() - 1, autoCompleteTextView);
    }

    private void addRadioGroupToList(RadioGroup radioGroup){
        _transportModeViewList.add(radioGroup);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        String result = (String) adapterView.getItemAtPosition(position);
        Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
        hideSoftKeyboard(view);
    }

    private void hideSoftKeyboard(View view){
        InputMethodManager inputManager = (InputMethodManager) this.getSystemService(
                Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
        inputManager.hideSoftInputFromInputMethod(view.getApplicationWindowToken(), 0);
    }

    public void clearAll(View view){
        for (ClearableAutoCompleteTextView acTextView : _autoCompleteTextViewLinkedList){
            acTextView.setText("");
        }
    }

    public void ok(View view){
        if(isNotOriginAndDestinationEmpty()) {
            addUserCoordinateToArrayList();
        } else {
            createToast("Origin and destination cannot be empty.", Toast.LENGTH_SHORT);
        }
    }

    private boolean isNotOriginAndDestinationEmpty(){
        ClearableAutoCompleteTextView originInList = _autoCompleteTextViewLinkedList.get(0);
        ClearableAutoCompleteTextView destinationInList = _autoCompleteTextViewLinkedList.get(_autoCompleteTextViewLinkedList.size() - 1);
        ClearableAutoCompleteTextView originInLayout =
                (ClearableAutoCompleteTextView) findViewById(R.id.autocomplete_origin);
        ClearableAutoCompleteTextView destinationInLayout =
                (ClearableAutoCompleteTextView) findViewById(R.id.autocomplete_destination);
        if(originInList.getText().toString().isEmpty()
                || destinationInLayout.getText().toString().isEmpty()){
            return false;
        } else {
            return true;
        }
    }

    public void addDate(View view){
        LinearLayout ll = addDateToStopPoint(view);
        ViewGroup vg = (ViewGroup)(view.getParent());
        vg.removeView(view);
    }

    private LinearLayout addDateToStopPoint(View view){
        LinearLayout layoutAddPoint = (LinearLayout) findViewById(R.id.add_stop_points);
        LinearLayout layoutToAddDate = null;
        for (int i = 0; i < layoutAddPoint.getChildCount(); i++) {
            LinearLayout ll = (LinearLayout)layoutAddPoint.getChildAt(i);
            for(int j = 0; j < ll.getChildCount(); j++){
                if (view.equals(ll.getChildAt(j))) {
                    layoutToAddDate = (LinearLayout) ll.getChildAt(j + 1);
                    break;
                }
            }
        }
        LinearLayout inflateLayout = (LinearLayout)View.inflate(
                this, R.layout.add_date, null);
        layoutToAddDate.addView(inflateLayout);
        layoutToAddDate.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        return inflateLayout;
    }

    private void addUserCoordinateToArrayList(){
        new GeoCode(_autoCompleteTextViewLinkedList, _transportModeViewList, this).execute();
    }

    public List<UserItem> getUserItems() {
        return _userItemList;
    }

    private void mergeDateAndTimeToDateTime(){
        if (_dateStartJourney == null)
            _dateStartJourney = _dateFormat.format(new Date());
        if (_timeStartJourney == null)
            _timeStartJourney = _timeFormat.format(new Date());
        _dateTimeStartJourney = _dateStartJourney + " " + _timeStartJourney;
    }

    private void moveToMainActivityWithData(){
        if (_userItemList != null) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra(START_DATETIME, _dateTimeStartJourney);
            intent.putExtra("MOVED_FROM_PLACES", true);
            updateDatabaseWithItems();
            startActivity(intent);
        } else {
            createToast("One of the addresses or addresses you passed is non-exist. Please select from the suggested list", Toast.LENGTH_LONG);
        }
    }

    public void updateDatabaseWithItems() {
        List<UserItem> items = _itineraryDataSource.getUnsavedAndSavedUserItems(this);
        List<UserItem> updatedItems = combineItemsWithUserItems(_userItemList, items);
        _itineraryDataSource.deleteUnsavedItineraryItems();
        _itineraryDataSource.insertMultipleItineraryItems(updatedItems);
    }

    public List<UserItem> combineItemsWithUserItems(List<UserItem> items, List<UserItem> itemsToUpdateFrom) {
        // Key user items by name
        Map<String, UserItem> itemsByName = new HashMap<>();
        for (ItineraryItem item : itemsToUpdateFrom)
            itemsByName.put(item.getName(), (UserItem) item);

        // Swap out suggestions in the user item list for those from the database. Keep the times.
        List<UserItem> updatedItems = new ArrayList<>();
        for (UserItem item : items) {
            UserItem oldItem = itemsByName.get(item.getName());
            if (oldItem != null && oldItem.equals(item)) { // Compares location and name
                // Item was already in the database - update with new time / transport mode
                if (item.getTime() != null)
                    oldItem.setDateTime(item.getTime());
                oldItem.setTravelMode(item.getTravelMode());
                updatedItems.add(oldItem);
            } else {
                // Itinerary item is new
                updatedItems.add(item);
            }
        }

        return updatedItems;
    }

    public void removeStopPoint(View view){
        LinearLayout ll = ((LinearLayout)view.getParent().getParent());
        LinearLayout llParent = (LinearLayout)ll.getParent();
        for (int i = 0; i < llParent.getChildCount(); i++){
            if (ll == llParent.getChildAt(i)){
                _autoCompleteTextViewLinkedList.remove(i + 1);
                _transportModeViewList.remove(i + 1);

            }
        }
        llParent.removeView(ll);
    }

    protected void createToast(String name, int duration){
        Toast.makeText(this, name, duration).show();
    }

    /**
     * Called when itinerary is loaded so that the text fields reflect the stop points.
     */
    public void updateFieldsFromItinerary(List<UserItem> items) {
        LinearLayout ll = (LinearLayout)findViewById(R.id.add_stop_points);
        final int positionOfRadioGroupView = 2;
        for (int i = 0; i < ll.getChildCount(); i ++){
            View child = ll.getChildAt(i);
            ll.removeView(child);
        }
        _autoCompleteTextViewLinkedList  = new LinkedList<>();
        _transportModeViewList = new ArrayList<>();
        View mainActivityLayout = findViewById(R.id.place_activity_layout); // Will this be null?
        addViewsInLayoutToArrayList((LinearLayout) findViewById(R.id.place_activity_layout));
        initializeAutoCompleteTextViewInArrayList();
        initializeRadioButton(_transportModeViewList.get(0));
        ClearableAutoCompleteTextView originTextField =
                (ClearableAutoCompleteTextView) mainActivityLayout.findViewById(R.id.autocomplete_origin);
        ClearableAutoCompleteTextView destinationTextField =
                (ClearableAutoCompleteTextView) mainActivityLayout.findViewById(R.id.autocomplete_destination);

        // Set origin and destination text from the first and last items in the list
        int nItems = items.size();
        UserItem origin = items.get(0);
        UserItem destination = items.get(nItems - 1);
        originTextField.setText(origin.getFullAddress());
        destinationTextField.setText(destination.getFullAddress());
        String modeTransport = origin.getTravelMode();
        if (modeTransport.equals(GeoCode.TravelMode.DRIVING.name().toLowerCase())){
            RadioButton rb = (RadioButton) _transportModeViewList.get(0).getChildAt(0);
            rb.setChecked(true);
        } else if (modeTransport.equals(GeoCode.TravelMode.WALKING.name().toLowerCase())){
            RadioButton rb = (RadioButton) _transportModeViewList.get(0).getChildAt(1);
            rb.setChecked(true);
        }else if (modeTransport.equals(GeoCode.TravelMode.BICYCLING.name().toLowerCase())){
            RadioButton rb = (RadioButton) _transportModeViewList.get(0).getChildAt(2);
            rb.setChecked(true);
        }
        else if (modeTransport.equals(GeoCode.TravelMode.TRANSIT.name().toLowerCase())){
            RadioButton rb = (RadioButton) _transportModeViewList.get(0).getChildAt(3);
            rb.setChecked(true);
        }

        // Go through the rest of the list, adding 'stop point' views for each item
        for (int i=1; i<nItems-1; i++) {
            LinearLayout stopPointLayout = addStopPoints(); // Layout with an empty text field
            ClearableAutoCompleteTextView stopPointNameView = (ClearableAutoCompleteTextView)
                    stopPointLayout.findViewById(R.id.stop_point_name);
            addAutoCompleteTextViewToLinkedList((ClearableAutoCompleteTextView) stopPointLayout.getChildAt(1));
            initializeAutoCompleteTextViews((ClearableAutoCompleteTextView) stopPointLayout.getChildAt(1));
            addRadioGroupToList((RadioGroup) stopPointLayout.getChildAt(positionOfRadioGroupView));
            initializeRadioButton((RadioGroup) stopPointLayout.getChildAt(positionOfRadioGroupView));
            stopPointNameView.setText(items.get(i).getFullAddress());
            modeTransport = items.get(i).getTravelMode();

            if (GeoCode.TravelMode.DRIVING.name().toLowerCase().equals(modeTransport)){
                RadioButton rb = (RadioButton) _transportModeViewList.get(0).getChildAt(0);
                rb.setChecked(true);
            } else if (GeoCode.TravelMode.WALKING.name().toLowerCase().equals(modeTransport)){
                RadioButton rb = (RadioButton) _transportModeViewList.get(0).getChildAt(1);
                rb.setChecked(true);
            }else if (GeoCode.TravelMode.BICYCLING.name().toLowerCase().equals(modeTransport)){
                RadioButton rb = (RadioButton) _transportModeViewList.get(0).getChildAt(2);
                rb.setChecked(true);
            }
            else if (GeoCode.TravelMode.TRANSIT.name().toLowerCase().equals(modeTransport)){
                RadioButton rb = (RadioButton) _transportModeViewList.get(0).getChildAt(3);
                rb.setChecked(true);
            }
        }
    }

//    final int positionOfAutoCompleteTextView = 1;
//    final int positionOfRadioGroupView = 2;
//    LinearLayout linearLayout = addStopPoints();
//    addAutoCompleteTextViewToLinkedList((ClearableAutoCompleteTextView) linearLayout.getChildAt(positionOfAutoCompleteTextView));
//    initializeAutoCompleteTextViews((ClearableAutoCompleteTextView)linearLayout.getChildAt(positionOfAutoCompleteTextView));
//    addRadioGroupToList((RadioGroup)linearLayout.getChildAt(positionOfRadioGroupView));
//    initializeRadioButton((RadioGroup)linearLayout.getChildAt(positionOfRadioGroupView));
}