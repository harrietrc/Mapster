package com.mapster.persistence;

import android.app.ProgressDialog;
import android.os.AsyncTask;

import com.mapster.activities.PlacesActivity;
import com.mapster.itinerary.ItineraryItem;
import com.mapster.itinerary.UserItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Harriet on 10/08/2015. Similar to UpdateMainFromItineraryTask, which needs tidying
 * up I think.
 */
public class LoadItineraryTask extends AsyncTask<Void, Void, List<ItineraryItem>> {

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
    protected List<ItineraryItem> doInBackground(Void... params) {
        return _datasource.getItemsByItineraryName(_itineraryName);
    }

    @Override
    protected void onPostExecute(List<ItineraryItem> items) {
        ArrayList<UserItem> userItems = new ArrayList<>();

        // TODO That cast/instanceof check shouldn't be necessary - fiddle with generics?
        for (ItineraryItem item : items)
            if (item instanceof UserItem) // Inelegant
                userItems.add((UserItem) item);

        _activity.callback(userItems);
        _dialog.dismiss();
    }
}
