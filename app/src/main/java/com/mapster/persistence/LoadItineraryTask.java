package com.mapster.persistence;

import android.app.ProgressDialog;
import android.os.AsyncTask;

import com.mapster.activities.PlacesActivity;
import com.mapster.itinerary.UserItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Harriet on 10/08/2015. Similar to UpdateMainFromItineraryTask, which needs tidying
 * up I think.
 */
public class LoadItineraryTask extends AsyncTask<Void, Void, List<UserItem>> {

    private ProgressDialog _dialog;
    private PlacesActivity _activity;
    private String _itineraryName;
    private ItineraryDataSource _datasource;

    public LoadItineraryTask(PlacesActivity activity, ItineraryDataSource datasource, String itineraryName) {
        _activity = activity;
        _datasource = datasource;
        _itineraryName = itineraryName;
    }

    @Override
    protected void onPreExecute() {
        _dialog = new ProgressDialog(_activity);
        _dialog.setMessage("Please wait...");
        _dialog.show();
    }

    @Override
    protected List<UserItem> doInBackground(Void... params) {
        List<UserItem> items = _datasource.getItemsByItineraryName(_itineraryName);
        return items;
    }

    @Override
    protected void onPostExecute(List<UserItem> items) {
        ArrayList itemsArrayList = new ArrayList();
        itemsArrayList.addAll(items);
        _activity.callback(itemsArrayList);
        _dialog.dismiss();
    }
}
