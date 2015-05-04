package com.mapster.filters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.mapster.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Harriet on 5/2/2015.
 */
public class ExpandableFilterListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<String> filterTitles;
    private Map<String, List<String>> filterChildren;

    public ExpandableFilterListAdapter(Context context, List<String> filterTitles, HashMap<String,
            List<String>> filterChildren) {
        this.context = context;
        this.filterTitles = filterTitles;
        this.filterChildren = filterChildren;
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
        // Populate the radio group from the list of filter option text
        final String filterText = (String) getChild(groupPosition, childPosition);
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.test_item, null);
        }
        TextView text = (TextView) convertView.findViewById(R.id.test_item);
        text.setText(filterText);
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
