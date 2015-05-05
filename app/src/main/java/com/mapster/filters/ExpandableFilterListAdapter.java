package com.mapster.filters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.mapster.R;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Created by Harriet on 5/2/2015.
 */
public class ExpandableFilterListAdapter extends BaseExpandableListAdapter {

    private Context context;

    // Headers for the filters (e.g. 'Price level', 'Category')
    private List<String> filterTitles;

    // Text for the filter options (e.g. 'Expensive', 'Accommodation')
    private Map<String, List<String>> filterChildren;

    // References to the child views / filter options that get created within the list
    private Map<String, List<View>> filterChildViews;

    // Names of children that have been instantiated as views already - for convenient/fast access
    private Map<String, HashSet<String>> filterChildViewNames;

    // TODO Consider creating a class to hold child state, or combining filterChildViewNames and filterChildren

    public ExpandableFilterListAdapter(Context context, List<String> filterTitles, HashMap<String,
            List<String>> filterChildren) {
        this.context = context;
        this.filterTitles = filterTitles;
        this.filterChildren = filterChildren;
        this.filterChildViews = new HashMap<>();
        this.filterChildViewNames = new HashMap<>();
    }

    @Override
    public int getGroupCount() {
        return filterTitles.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this.filterChildren.get(this.filterTitles.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return filterTitles.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return this.filterChildren.get(this.filterTitles.get(groupPosition)).get(childPosition);
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

        // Save the group so that we can save its children later
        if (filterChildViews.get(groupTitle) == null) {
            filterChildViews.put(groupTitle, new ArrayList<View>());
            filterChildViewNames.put(groupTitle, new HashSet<String>());
        }

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
        // convertView is reused, so use this variable to keep track of whether this is the reused
        // view or not.
        boolean inflatedView = false;

        // Populate the radio group from the list of filter option text
        final String childText = (String) getChild(groupPosition, childPosition);
        if (convertView == null) {
            inflatedView = true;
            LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.filter_item, null);
        }
        TextView text = (TextView) convertView.findViewById(R.id.filter_option_text);
        text.setText(childText);

        // Save the item view to the group's list of children
        String groupName = (String) getGroup(groupPosition);
        List<View> childViews = filterChildViews.get(groupName);
        // Also check that the child view hasn't already been saved
        HashSet<String> childNames = filterChildViewNames.get(groupName);

        if (inflatedView && !childNames.contains(childText)) {
            childViews.add(convertView);
            childNames.add(childText);
        }

        return convertView;
    }

    /**
     * Returns the children of a group in the ListView
     * @param groupName The name of the group
     * @return List of child items as Views
     */
    public List<View> getGroupChildren(String groupName) {
        return filterChildViews.get(groupName);
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
