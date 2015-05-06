package com.mapster.places.autocomplete;

import android.content.Context;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import com.mapster.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by tommyngo on 19/03/15.
 */
public class PlacesAutoCompleteAdapter extends ArrayAdapter<String> implements Filterable{
    private ArrayList<String> resultList;
    private Context _context;

    public PlacesAutoCompleteAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        this._context = context;
    }

    @Override
    public int getCount() {
        return resultList.size();
    }

    @Override
    public String getItem(int index) {
        return resultList.get(index);
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                String apiKey = _context.getString(R.string.API_KEY);
                AutoCompletePlaces autoCompletePlaces = new AutoCompletePlaces(apiKey);
                if (constraint != null) {
                    // Retrieve the autocomplete results.
                    resultList = autoCompletePlaces.autocomplete(constraint.toString());

                    // Assign the data to the FilterResults
                    filterResults.values = resultList;
                    filterResults.count = resultList.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    notifyDataSetChanged();
                }
                else {
                    notifyDataSetInvalidated();
                }
            }};
        return filter;
    }
}
