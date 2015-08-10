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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.mapster.R;
import com.mapster.android.gui.util.clearableautocompletetextview.ClearableAutoCompleteTextView;
import com.mapster.fragment.DatePickerFragment;
import com.mapster.fragment.TimePickerFragment;
import com.mapster.geocode.GeoCode;
import com.mapster.interfaces.GeoCodeListener;
import com.mapster.itinerary.UserItem;
import com.mapster.persistence.ItineraryDataSource;
import com.mapster.persistence.LoadAndSaveHelper;
import com.mapster.places.autocomplete.PlacesAutoCompleteAdapter;

import org.joda.time.LocalTime;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;

import static junit.framework.Assert.assertTrue;

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

    @Override
    public void callback(ArrayList<UserItem> userItems) {
        if (userItems != null){
            _userItemList = userItems;
            mergeDateAndTimeToDateTime();
            moveToMainActivityWithData();
        } else {
            Toast.makeText(this,"Origin and Destination fields must not be blank",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        _itineraryDataSource.close();
        super.onPause();
    }

    @Override
    public void onResume() {
        _itineraryDataSource.open();
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
                acTextView.setSelection(0,0);
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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_settings:
                return true;
            case R.id.action_add_stops:
                final int positionOfAutoCompleteTextView = 1;
                final int positionOfRadioGroupView = 2;
                LinearLayout linearLayout = addStopPoints();
                addAutoCompleteTextViewToLinkedList((ClearableAutoCompleteTextView) linearLayout.getChildAt(positionOfAutoCompleteTextView));
                initializeAutoCompleteTextViews((ClearableAutoCompleteTextView)linearLayout.getChildAt(positionOfAutoCompleteTextView));
                addRadioGroupToList((RadioGroup)linearLayout.getChildAt(positionOfRadioGroupView));
                initializeRadioButton((RadioGroup)linearLayout.getChildAt(positionOfRadioGroupView));
                return true;
            case R.id.save:
                // May be a race condition here
                _loadAndSaveItineraryHelper.showSaveDialogue();
                return true;
            case R.id.load:
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
                LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT));
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
        assertTrue(originInLayout.equals(originInList) && destinationInLayout.equals(destinationInList));
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
                LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT));
        return inflateLayout;
    }

    private void addUserCoordinateToArrayList(){
        new GeoCode(_autoCompleteTextViewLinkedList, _transportModeViewList, this).execute();
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
            intent.putParcelableArrayListExtra("USER_ITEM_LIST", _userItemList);
            startActivity(intent);
        } else {
            createToast("One of the addresses or addresses you passed is non-exist. Please select from the suggested list", Toast.LENGTH_LONG);
        }
    }

    public void removeStopPoint(View view){
        LinearLayout ll = ((LinearLayout)view.getParent().getParent());
        LinearLayout llParent = (LinearLayout)ll.getParent();
        for (int i = 0; i < llParent.getChildCount(); i++){
            if (ll == llParent.getChildAt(i)){
                _autoCompleteTextViewLinkedList.remove(i + 1);
            }
        }
        llParent.removeView(ll);
    }

    protected void createToast(String name, int duration){
        Toast.makeText(this, name, duration).show();
    }
}