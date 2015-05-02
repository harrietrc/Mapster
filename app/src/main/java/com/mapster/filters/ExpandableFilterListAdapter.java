package com.mapster.filters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.mapster.R;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Harriet on 5/2/2015.
 */
public class ExpandableFilterListAdapter extends BaseExpandableListAdapter {

    private Context context;

    // Filter group names - e.g. 'Price level', 'Category'
    private static final String[] filterGroupNames = {"Category", "Price level"};

    // TODO Currently each new filter group corresponds with a separate layout file. Change this
    // (i.e. share a layout file and populate with text dynamically) if this gets unwieldy.
    private Map<String, Integer> layoutIdsByName = new HashMap<String, Integer>(){{
        put("Category", R.layout.category_filter_options);
        put("Price level", R.layout.price_level_filter_options);
    }};

    // Alternative implementation:
    // Names of options within the filter groups (e.g. 'attractions' inside 'Catgeory')
//    private Map<String, List<String>> filterOptions;

    public ExpandableFilterListAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getGroupCount() {
        return filterGroupNames.length;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        // TODO
        return 0;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return filterGroupNames[groupPosition];
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        // TODO
        return null;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String groupTitle = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.filter_group, null);
        }
        TextView viewTitle = (TextView) convertView.findViewById(R.id.filter_title);
        viewTitle.setText(groupTitle);
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if (convertView == null) {
            String groupName = (String) getGroup(groupPosition);
            int layoutId = layoutIdsByName.get(groupName);
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(layoutId, null);
        }
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }
}
