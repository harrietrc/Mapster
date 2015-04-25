package com.mapster.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.mapster.R;
import com.mapster.geocode.GeoCode;
import com.mapster.places.autocomplete.PlacesAutoCompleteAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static junit.framework.Assert.assertTrue;

public class PlacesActivity extends ActionBarActivity implements OnItemClickListener{
    private PlacesAutoCompleteAdapter _autoCompAdapter;
    private ArrayList<AutoCompleteTextView> _autoCompleteTextViewArrayList;
    private ArrayList<String> _coordinateArrayList;
    private List<RadioGroup> _transportModeViewList;
    private List<String> _transportModeList;
    public enum TravelMode{
        DRIVING("driving"), WALKING("walking"), BIKING("bicycling"), TRANSIT("transit");
        private final String name;
        private TravelMode(String name){
            this.name = name;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        _autoCompleteTextViewArrayList = new ArrayList<>();
        _transportModeViewList = new ArrayList<>();
        _autoCompAdapter = new PlacesAutoCompleteAdapter(this, R.layout.list_item);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places);
        addViewsInLayoutToArrayList((LinearLayout) findViewById(R.id.place_activity_layout));
        initializeAutoCompleteTextViewInArrayList();
    }

    private void addViewsInLayoutToArrayList(LinearLayout llayout){
        for (int i = 0; i < llayout.getChildCount(); i++) {
            if (llayout.getChildAt(i) instanceof AutoCompleteTextView) {
                _autoCompleteTextViewArrayList.add((AutoCompleteTextView) llayout.getChildAt(i));
            } else if(llayout.getChildAt(i) instanceof RadioGroup) {
                _transportModeViewList.add((RadioGroup) llayout.getChildAt(i));
            }
        }
    }

    private void initializeAutoCompleteTextViewInArrayList(){
        for (AutoCompleteTextView acTextView : _autoCompleteTextViewArrayList){
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
                addAutoCompleteTextViewToArrayList((AutoCompleteTextView)linearLayout.getChildAt(positionOfAutoCompleteTextView));
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

    private void addAutoCompleteTextViewToArrayList(AutoCompleteTextView autoCompleteTextView){
        _autoCompleteTextViewArrayList.add(autoCompleteTextView);
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
        for (AutoCompleteTextView acTextView : _autoCompleteTextViewArrayList){
            acTextView.setText("");
        }
    }

    public void ok(View view){
        if(isNotOriginAndDestinationEmpty()){
            addUserCoordinateToArrayList();
            addTransportModeToList();
            moveToMainActivityWithData();
        } else {
            Toast.makeText(this,"Origin and Destination fields must not be blank",
                           Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isNotOriginAndDestinationEmpty(){
        AutoCompleteTextView originInList = _autoCompleteTextViewArrayList.get(0);
        AutoCompleteTextView destinationInList = _autoCompleteTextViewArrayList.get(1);
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
        _coordinateArrayList = new ArrayList<>();
        for (AutoCompleteTextView acTextView : _autoCompleteTextViewArrayList){
            try {
                if(!acTextView.getText().toString().isEmpty()) {
                    String[] coordinate = new GeoCode().execute(
                                                        acTextView.getText().toString()).get();
                    _coordinateArrayList.add(coordinate[0]);
                    _coordinateArrayList.add(coordinate[1]);
                }
            } catch(InterruptedException e){
                e.printStackTrace();
            } catch(ExecutionException e){
                e.printStackTrace();
            }
        }
    }

    private void addTransportModeToList(){
        _transportModeList = new ArrayList<>();
        for(int i = 0; i < _transportModeViewList.size(); i++){
            if(_autoCompleteTextViewArrayList.get(i).getId() != R.id.autocomplete_destination) {
                if (!_autoCompleteTextViewArrayList.get(i + 1).getText().toString().isEmpty()) {
                    for (int j = 0; j < _transportModeViewList.get(i).getChildCount(); j++) {
                        RadioButton rb = (RadioButton) _transportModeViewList.get(i).getChildAt(j);
                        addToTranposportModeList(rb);
                    }
                }
            }
        }
    }

    private void addToTranposportModeList(RadioButton rb){
        switch(rb.getId()) {
            case R.id.bike_mode:
                if (rb.isChecked()) {
                    _transportModeList.add(TravelMode.BIKING.name);
                }
                break;
            case R.id.drive_mode:
                if (rb.isChecked()) {
                    _transportModeList.add(TravelMode.DRIVING.name);
                }
                break;
            case R.id.transit_mode:
                if (rb.isChecked()) {
                    _transportModeList.add(TravelMode.TRANSIT.name);
                }
                break;
            case R.id.walk_mode:
                if (rb.isChecked()) {
                    _transportModeList.add(TravelMode.WALKING.name);
                }
                break;
            default:
                break;
        }

    }

    private void moveToMainActivityWithData(){
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("COORDINATE_LIST", _coordinateArrayList);
        startActivity(intent);
    }
}
