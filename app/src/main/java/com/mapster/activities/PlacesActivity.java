package com.mapster.activities;

import android.app.DatePickerDialog;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
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
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.mapster.R;
import com.mapster.fragment.DatePickerFragment;
import com.mapster.fragment.TimePickerFragment;
import com.mapster.geocode.GeoCode;
import com.mapster.itinerary.UserItem;
import com.mapster.places.autocomplete.PlacesAutoCompleteAdapter;

import org.joda.time.LocalTime;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static junit.framework.Assert.assertTrue;

public class PlacesActivity extends ActionBarActivity implements OnItemClickListener, DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    public static final String START_DATETIME = "START_DATETIME";

    private PlacesAutoCompleteAdapter _autoCompAdapter;
    private LinkedList<AutoCompleteTextView> _autoCompleteTextViewLinkedList;
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

    public enum TravelMode{
        DRIVING("driving"), WALKING("walking"), BIKING("bicycling"), TRANSIT("transit");
        private final String name;
        private TravelMode(String name){
            this.name = name;
        }
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
        _userItemList = new ArrayList<>();
    }

    private void addViewsInLayoutToArrayList(LinearLayout llayout){
        for (int i = 0; i < llayout.getChildCount(); i++) {
            if (llayout.getChildAt(i) instanceof AutoCompleteTextView) {
                _autoCompleteTextViewLinkedList.add((AutoCompleteTextView) llayout.getChildAt(i));
            } else if(llayout.getChildAt(i) instanceof RadioGroup) {
                _transportModeViewList.add((RadioGroup) llayout.getChildAt(i));
            }
        }
    }

    private void initializeAutoCompleteTextViewInArrayList(){
        for (AutoCompleteTextView acTextView : _autoCompleteTextViewLinkedList){
            initializeAutoCompleteTextViews(acTextView);
        }
    }

    private void initializeAutoCompleteTextViews(AutoCompleteTextView autoCompleteTextView) {
        autoCompleteTextView.setAdapter(_autoCompAdapter);
        autoCompleteTextView.setOnItemClickListener(this);
        displayTextFromStart(autoCompleteTextView);
    }

    private void displayTextFromStart(final AutoCompleteTextView acTextView){
        acTextView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus == false) {  // lost focus
                acTextView.setSelection(0,0);
            }else{
                acTextView.setText("");
            }
            }
        });
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
                addAutoCompleteTextViewToLinkedList((AutoCompleteTextView) linearLayout.getChildAt(positionOfAutoCompleteTextView));
                initializeAutoCompleteTextViews((AutoCompleteTextView)linearLayout.getChildAt(positionOfAutoCompleteTextView));
                addRadioGroupToList((RadioGroup)linearLayout.getChildAt(positionOfRadioGroupView));
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

    private void addAutoCompleteTextViewToLinkedList(AutoCompleteTextView autoCompleteTextView){
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
        for (AutoCompleteTextView acTextView : _autoCompleteTextViewLinkedList){
            acTextView.setText("");
        }
    }

    public void ok(View view){
        if(isNotOriginAndDestinationEmpty()){
            addUserCoordinateToArrayList();
            mergeDateAndTimeToDateTime();
            moveToMainActivityWithData();
        } else {
            Toast.makeText(this,"Origin and Destination fields must not be blank",
                           Toast.LENGTH_SHORT).show();
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


    private boolean isNotOriginAndDestinationEmpty(){
        AutoCompleteTextView originInList = _autoCompleteTextViewLinkedList.get(0);
        AutoCompleteTextView destinationInList = _autoCompleteTextViewLinkedList.get(_autoCompleteTextViewLinkedList.size() - 1);
        AutoCompleteTextView originInLayout =
                (AutoCompleteTextView) findViewById(R.id.autocomplete_origin);
        AutoCompleteTextView destinationInLayout =
                (AutoCompleteTextView) findViewById(R.id.autocomplete_destination);
        assertTrue(originInLayout.equals(originInList) && destinationInLayout.equals(destinationInList));
        if(originInList.getText().toString().isEmpty()
                || destinationInLayout.getText().toString().isEmpty()){
            return false;
        } else {
            return true;
        }
    }

    private void addUserCoordinateToArrayList(){
        _userItemList = new ArrayList<>();
        int position = 0;
        for (AutoCompleteTextView acTextView : _autoCompleteTextViewLinkedList){
            try {
                if(!acTextView.getText().toString().isEmpty()) {
                    String text = acTextView.getText().toString();
                    String[] coordinate = new GeoCode().execute(text).get();
                    if (coordinate == null){
                        _userItemList = null;
                        return;
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
                    _userItemList.add(item);
                    System.out.println(item.getName() + " " + item.getTravelMode());
                    position++;
                }
            } catch(InterruptedException e){
                e.printStackTrace();
            } catch(ExecutionException e){
                e.printStackTrace();
            }
        }
    }

    private String getTranposportMode(RadioButton rb){
        switch(rb.getId()) {
            case R.id.bike_mode:
                if (rb.isChecked()) {
                    return TravelMode.BIKING.name;
                }
                return null;
            case R.id.drive_mode:
                if (rb.isChecked()) {
                    return (TravelMode.DRIVING.name);
                }
                return null;
            case R.id.transit_mode:
                if (rb.isChecked()) {
                    return (TravelMode.TRANSIT.name);
                }
                return null;
            case R.id.walk_mode:
                if (rb.isChecked()) {
                    return (TravelMode.WALKING.name);
                }
                return null;
            default:
                return null;
        }
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

    protected void createToast(String name, int duration){
        Toast.makeText(this, name, duration).show();
    }
}