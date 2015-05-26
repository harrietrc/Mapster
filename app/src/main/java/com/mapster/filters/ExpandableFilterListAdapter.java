package com.mapster.filters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.mapster.R;

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
    private Map<String, HashSet<View>> filterChildViews;

    // Names of children that have been instantiated as views already - for convenient/fast access
    private Map<String, HashSet<String>> filterChildViewNames;

    // Because filter item views sometimes get reinstantiated, keep track of which options are checked
    private Map<String, String> checkedFilterOptions;

    // TODO Consider creating a class to hold child state, or combining filterChildViewNames and filterChildren

    public ExpandableFilterListAdapter(Context context, List<String> filterTitles, HashMap<String,
            List<String>> filterChildren) {
        this.context = context;
        this.filterTitles = filterTitles;
        this.filterChildren = filterChildren;
        this.filterChildViews = new HashMap<>();
        this.filterChildViewNames = new HashMap<>();

        // Record of which options in the list are checked/selected
        checkedFilterOptions = new HashMap<>();
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
        // E.g. 'Category', 'Price level', 'Clear suggestions'
        String groupTitle = (String) getGroup(groupPosition);

        // As opposed to the 'Clear suggestions' item
        boolean isFilterOption = false;
        if (convertView != null)
            isFilterOption = convertView.findViewById(R.id.clear) != null;

        // Save the group so that we can save its children later
        if (filterChildViews.get(groupTitle) == null) {
            filterChildViews.put(groupTitle, new HashSet<View>());
            filterChildViewNames.put(groupTitle, new HashSet<String>());
        }

        // Inflate the correct layout - either a filter option or the clear suggestions option
        // TODO There are unnecessary inflations here
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (groupPosition == (getGroupCount()-1)) {
            // The last group is always 'Clear suggestions'
            convertView = inflater.inflate(R.layout.filter_group_clear, null);
        } else if (convertView == null || !isFilterOption) {
            // Inflate filter option layout if the view is null or the 'Clear suggestions' item
            convertView = inflater.inflate(R.layout.filter_group, null);
        }

        TextView viewTitle = (TextView) convertView.findViewById(R.id.filter_title);
        viewTitle.setText(groupTitle);

        refreshRadioButtons();

        return convertView;
    }

    public void refreshRadioButtons() {
        for (String s: filterTitles) {
            setFilterGroupChecked(s);
        }
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        // Populate the radio group from the list of filter option text
        final String childText = (String) getChild(groupPosition, childPosition);
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.filter_item, null);
        }
        TextView text = (TextView) convertView.findViewById(R.id.filter_option_text);
        text.setText(childText);

        // Save/update the item view in the group's list of children
        String groupName = (String) getGroup(groupPosition);
        if (filterChildren.get(groupName).contains(childText)) {
            filterChildViews.get(groupName).add(convertView);
            filterChildViewNames.get(groupName).add(childText);
        }

        // TODO Not a fan of this implementation. Checks list of children to ensure that they are children of this group.
        // As such maintaining a list of children is meaningless - top of my list of things to fix.
        purgeFilterViews(groupName);

        // Check whether the child should be checked or not
        String checkedOption = checkedFilterOptions.get(groupName);
        if (checkedOption != null && checkedOption.equals(childText)) {
            ((CheckableLinearLayout) convertView).setChecked(true);
        } else {
            ((CheckableLinearLayout) convertView).setChecked(false);
        }

        return convertView;
    }

    /**
     * Gets rid of references to children that no longer refer t children - i.e. the view has
     * been reused for another group
     * TODO This isn't great - try to think of an alternative. Perhaps keep all views in a single list.
     * The parent/group name is being checked anyway so this is probably a better idea.
     */
    private void purgeFilterViews(String groupName) {
        HashSet<View> children = filterChildViews.get(groupName);
        List<String> childNames = filterChildren.get(groupName);
        HashSet<View> updated = new HashSet<>();

        if (children != null) {
            for (View v : children) {
                String name = ((TextView) v.findViewById(R.id.filter_option_text)).getText().toString();
                if (childNames.contains(name))
                    updated.add(v);
            }
        }

        // Update the list of children
        filterChildViews.put(groupName, updated);
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    /**
     * Unchecks all the filter options in a filter group. Called when 'Clear' is clicked
     * @param filterName The name of the filter group
     */
    public void clearFilterRadioButtons(String filterName) {
        HashSet<View> children = filterChildViews.get(filterName);
        if (children != null) {
            for (View v : children)
                ((CheckableLinearLayout) v).setChecked(false);
        }
        checkedFilterOptions.put(filterName, null);
    }

    public void clearAllFilterRadioButtons() {
        for (String s: filterTitles) {
            clearFilterRadioButtons(s);
        }
    }

    /**
     * Call this when setting a filter option's radio button to checked. Sets the correct checked
     * state for all
     * @param group
     * @param checkedChild
     */
    public void setFilterOptionChecked(String group, String checkedChild) {
        // Record option as checked
        checkedFilterOptions.put(group, checkedChild);
        // Set the checked state for all the options in the group
        setFilterGroupChecked(group);
    }

    /**
     * Sets the correct state for the radiobuttons in a group based on the saved checked option.
     * @param group Name of a filter group
     */
    public void  setFilterGroupChecked(String group) {
        HashSet<View> children = filterChildViews.get(group);
        String checkedChild = checkedFilterOptions.get(group);

        if (checkedChild == null) {
            // Clear all the radio buttons - no item is checked
            clearFilterRadioButtons(group);
        } else {
            // Check whether the view's name matches the checked option's name, and set checked
            for (View v : children) {
                String optionName = ((TextView) v.findViewById(R.id.filter_option_text)).getText().toString();
                if (optionName.equals(checkedChild)) {
                    ((CheckableLinearLayout) v).setChecked(true);
                } else {
                    ((CheckableLinearLayout) v).setChecked(false);
                }
            }
        }
    }
}
