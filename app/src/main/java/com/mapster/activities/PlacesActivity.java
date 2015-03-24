package com.mapster.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.mapster.R;
import com.mapster.geocode.GeoCode;
import com.mapster.places.autocomplete.PlacesAutoCompleteAdapter;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class PlacesActivity extends ActionBarActivity implements OnItemClickListener{
    private PlacesAutoCompleteAdapter _autoCompAdapder;
    private ArrayList<AutoCompleteTextView> autoCompleteTextViewArrayList;
    private ArrayList<String> coordinateArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_places, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch(item.getItemId()){
            case R.id.action_settings:
                return true;
            case R.id.action_add_stops:
                addStopPoints();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void addStopPoints(){
        LinearLayout layoutToAddPoint = (LinearLayout) findViewById(R.id.add_stop_points);
        LinearLayout inflateLayout = (LinearLayout)View.inflate(
                                      this, R.layout.add_stop_points, null);
        layoutToAddPoint.addView(inflateLayout);
        layoutToAddPoint.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT));
    }

    private void initializeAutoCompleteTextViews() {
        for (final AutoCompleteTextView autoCompTextView : autoCompleteTextViewArrayList){
            autoCompTextView.setAdapter(_autoCompAdapder);
            autoCompTextView.setOnItemClickListener(this);
            displayTextFromStart(autoCompTextView);
        }
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

    private void displayTextFromStart(final AutoCompleteTextView acTextView){
        acTextView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus == false) {  // lost focus
                    acTextView.setSelection(0,0);
                }
            }
        });
    }

    private void getAllAutoCompleteTextViewChildren(){
        LinearLayout addPoint = (LinearLayout) findViewById(R.id.add_stop_points);
        LinearLayout activityLinearLayout = (LinearLayout) findViewById(R.id.place_activity_layout);
        autoCompleteTextViewArrayList = new ArrayList<AutoCompleteTextView>();
        _autoCompAdapder = new PlacesAutoCompleteAdapter(this, R.layout.list_item);
        // Must be in this order because of the order of origin and destination
        addAutoCompleteTextViewInLayoutToList(addPoint);
        addAutoCompleteTextViewInLayoutToList(activityLinearLayout);
    }

    private void addAutoCompleteTextViewInLayoutToList(LinearLayout llayout){
        for (int i = 0; i < llayout.getChildCount(); i++) {
            if (llayout.getChildAt(i) instanceof AutoCompleteTextView) {
                autoCompleteTextViewArrayList.add((AutoCompleteTextView) llayout.getChildAt(i));
            }
        }
    }

    public void clearAll(View view){
        for (AutoCompleteTextView acTextView : autoCompleteTextViewArrayList){
            acTextView.setText("");
        }
    }

    public void ok(View view){
        getAllAutoCompleteTextViewChildren();
        initializeAutoCompleteTextViews();
        if(isNotOriginAndDestinationEmpty()){
            addUserCoordinateToArrayList();
            transferDataToMainActivity();
        } else {
            Toast.makeText(this,"Origin and Destination fields can not be blank",
                           Toast.LENGTH_SHORT).show();
        }
    }

    private void addUserCoordinateToArrayList(){
        coordinateArrayList = new ArrayList<>();
        for (AutoCompleteTextView acTextView : autoCompleteTextViewArrayList){
            try {
                if(acTextView.getText().toString().length() > 1) {
                    String[] coordinate = new GeoCode().execute(acTextView.getText().toString()).get();
                    coordinateArrayList.add(coordinate[0]);
                    coordinateArrayList.add(coordinate[1]);
                }
            } catch(InterruptedException e){
                e.printStackTrace();
            } catch(ExecutionException e){
                e.printStackTrace();
            }
        }
    }

    private void transferDataToMainActivity(){
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("COORDINATE_LIST", coordinateArrayList);
        startActivity(intent);
    }

    private boolean isNotOriginAndDestinationEmpty(){
        AutoCompleteTextView originInList = autoCompleteTextViewArrayList.get(0);
        AutoCompleteTextView destinationInList = autoCompleteTextViewArrayList.get(
                                                 autoCompleteTextViewArrayList.size() - 1);
        AutoCompleteTextView originInLayout =
                            (AutoCompleteTextView) findViewById(R.id.autocomplete_origin);
        AutoCompleteTextView destinationInLayout =
                            (AutoCompleteTextView) findViewById(R.id.autocomplete_destination);
        if(originInLayout.equals(originInList) && destinationInLayout.equals(destinationInList)){
            if(originInList.getText().toString().isEmpty()
            || destinationInLayout.getText().toString().isEmpty()){
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }
}
