package com.mapster.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
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
    private LinearLayout _activityLinearLayout;
    private ArrayList<AutoCompleteTextView> autoCompleteTextViewList;
    private ArrayList<String> coordinateArrayList;

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        String result = (String) adapterView.getItemAtPosition(position);
        Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
        InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
        inputManager.hideSoftInputFromInputMethod(view.getApplicationWindowToken(), 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        _activityLinearLayout = (LinearLayout) findViewById(R.id.place_activity_layout);
        getAllAutoCompleteTextViewChildren();
        initializeAutoCompleteTextViews();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_places, menu);
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
        }

        return super.onOptionsItemSelected(item);
    }

    private void initializeAutoCompleteTextViews() {
        for (AutoCompleteTextView autoCompTextView : autoCompleteTextViewList){
            autoCompTextView.setAdapter(_autoCompAdapder);
            autoCompTextView.setOnItemClickListener(this);
        }
//        AutoCompleteTextView originAutoCompView = (AutoCompleteTextView) findViewById(R.id.autocomplete_origin);
//        originAutoCompView.setAdapter(_autoCompAdapder);
//        originAutoCompView.setOnItemClickListener(this);
//
//        AutoCompleteTextView destinationAutoCompView = (AutoCompleteTextView) findViewById(R.id.autocomplete_destination);
//        destinationAutoCompView.setAdapter(_autoCompAdapder);
//        destinationAutoCompView.setOnItemClickListener(this);
    }

    private void getAllAutoCompleteTextViewChildren(){
        autoCompleteTextViewList = new ArrayList<AutoCompleteTextView>();
        _autoCompAdapder = new PlacesAutoCompleteAdapter(this, R.layout.list_item);
        for (int i = 0; i < _activityLinearLayout.getChildCount(); i++) {
            if (_activityLinearLayout.getChildAt(i) instanceof AutoCompleteTextView) {
                autoCompleteTextViewList.add((AutoCompleteTextView) _activityLinearLayout.getChildAt(i));
            }
        }
    }

    public void clearAll(View view){
        for (AutoCompleteTextView acTextView : autoCompleteTextViewList){
            acTextView.setText("");
        }
    }

    public void ok(View view){
        if(checkForOriginAndDestination()){
            coordinateArrayList = new ArrayList<>();
            for (AutoCompleteTextView acTextView : autoCompleteTextViewList){
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
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("CO-ORDINATE_LIST", coordinateArrayList);
            startActivity(intent);
        } else {
            Toast.makeText(this,"Origin and Destination fields can not be blank",Toast.LENGTH_SHORT).show();
        }

       }

    private boolean checkForOriginAndDestination(){
        AutoCompleteTextView originInList = autoCompleteTextViewList.get(0);
        AutoCompleteTextView destinationInList = autoCompleteTextViewList.get(autoCompleteTextViewList.size() - 1);
        AutoCompleteTextView originInLayout = (AutoCompleteTextView) findViewById(R.id.autocomplete_origin);
        AutoCompleteTextView destinationInLayout = (AutoCompleteTextView) findViewById(R.id.autocomplete_destination);
        if(originInLayout.equals(originInList) && destinationInLayout.equals(destinationInList)){
            if(!originInList.getText().toString().isEmpty() && !destinationInLayout.getText().toString().isEmpty()){
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

//    public void addStopPoint(View view){
//        createTextViewToThisLayout("Stop Point");
//    }
//
//    private TextView createTextViewToThisLayout(String text){
//        final LinearLayout.LayoutParams lparams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//        final TextView textView = new TextView(this);
////        textView.setLayoutParams(lparams);
//        textView.setText(text);
//        activityLayout.addView(textView);
//        return textView;
//    }
//
//    private AutoCompleteTextView createAutoCompleteTextView(){
//        final LinearLayout.LayoutParams lparams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//        final AutoCompleteTextView autoCompleteTextView = new AutoCompleteTextView(this);
//        return autoCompleteTextView;
//    }

}
