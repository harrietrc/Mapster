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

import static junit.framework.Assert.assertTrue;

public class PlacesActivity extends ActionBarActivity implements OnItemClickListener{
    private PlacesAutoCompleteAdapter _autoCompAdapder;
    private ArrayList<AutoCompleteTextView> autoCompleteTextViewArrayList;
    private ArrayList<String> coordinateArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        autoCompleteTextViewArrayList = new ArrayList<AutoCompleteTextView>();
        _autoCompAdapder = new PlacesAutoCompleteAdapter(this, R.layout.list_item);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places);
        addAutoCompleteTextViewInLayoutToArrayList((LinearLayout)findViewById(R.id.place_activity_layout));
        initializeAutoCompleteTextViewInArrayList();
    }

    private void addAutoCompleteTextViewInLayoutToArrayList(LinearLayout llayout){
        for (int i = 0; i < llayout.getChildCount(); i++) {
            if (llayout.getChildAt(i) instanceof AutoCompleteTextView) {
                autoCompleteTextViewArrayList.add((AutoCompleteTextView) llayout.getChildAt(i));
            }
        }
    }

    private void initializeAutoCompleteTextViewInArrayList(){
        for (AutoCompleteTextView acTextView : autoCompleteTextViewArrayList){
            initializeAutoCompleteTextViews(acTextView);
        }
    }

    private void initializeAutoCompleteTextViews(AutoCompleteTextView autoCompleteTextView) {
        autoCompleteTextView.setAdapter(_autoCompAdapder);
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
        // Inflate the menu; this adds items to the action bar if it is present.
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
                LinearLayout linearLayout = addStopPoints();
                addAutoCompleteTextViewToArrayList((AutoCompleteTextView)linearLayout.getChildAt(positionOfAutoCompleteTextView));
                initializeAutoCompleteTextViews((AutoCompleteTextView)linearLayout.getChildAt(positionOfAutoCompleteTextView));
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
        autoCompleteTextViewArrayList.add(autoCompleteTextView);
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
        for (AutoCompleteTextView acTextView : autoCompleteTextViewArrayList){
            acTextView.setText("");
        }
    }

    public void ok(View view){
        if(isNotOriginAndDestinationEmpty()){
            addUserCoordinateToArrayList();
            moveToMainActivityWithData();
        } else {
            Toast.makeText(this,"Origin and Destination fields must not be blank",
                           Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isNotOriginAndDestinationEmpty(){
        AutoCompleteTextView originInList = autoCompleteTextViewArrayList.get(0);
        AutoCompleteTextView destinationInList = autoCompleteTextViewArrayList.get(1);
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
        coordinateArrayList = new ArrayList<>();
        for (AutoCompleteTextView acTextView : autoCompleteTextViewArrayList){
            try {
                if(acTextView.getText().toString().length() > 1) {
                    String[] coordinate = new GeoCode().execute(
                                                        acTextView.getText().toString()).get();
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

    private void moveToMainActivityWithData(){
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("COORDINATE_LIST", coordinateArrayList);
        startActivity(intent);
    }
}
