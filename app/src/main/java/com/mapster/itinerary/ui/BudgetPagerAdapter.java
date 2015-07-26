//package com.mapster.itinerary.ui;
//
//import android.support.v4.app.Fragment;
//import android.support.v4.app.FragmentManager;
//import android.support.v4.app.FragmentPagerAdapter;
//
///**
// * Created by Harriet on 6/20/2015.
// */
//public class BudgetPagerAdapter extends FragmentPagerAdapter {
//
//    // Would prefer to replace these with icons (clock, dollar sign)
//    private final String[] TAB_TITLES = {"Schedule", "Budget"};
//
//    // Tags used to get references to the two fragments
//    private String _budgetTag;
//    private String _scheduleTag;
//
//    public enum FragmentIdentifier { BUDGET, SCHEDULE }
//
//    private FragmentManager _manager;
//
//    public BudgetPagerAdapter(FragmentManager manager) {
//        super(manager);
//        _manager = manager;
//    }
//
//    /**
//     * Assumes getItem() has been called
//     */
//    public Fragment getFragment(FragmentIdentifier identifier) {
//        if (identifier == FragmentIdentifier.BUDGET) {
//            return _manager.findFragmentByTag(_budgetTag);
//        } else if (identifier == FragmentIdentifier.SCHEDULE) {
//            return _manager.findFragmentByTag(_scheduleTag);
//        }
//        return null;
//    }
//
//    @Override
//    public CharSequence getPageTitle(int position) {
//        return TAB_TITLES[position];
//    }
//
//    @Override
//    public Fragment getItem(int position) {
//        switch (position) {
//            case 0:
//                ScheduleFragment schedule = new ScheduleFragment();
//                _scheduleTag = schedule.getTag();
//                return schedule;
//            case 1:
//                BudgetFragment budget = new BudgetFragment();
//                _budgetTag = budget.getTag();
//                return budget;
//        }
//        return null;
//    }
//
//    @Override
//    public int getCount() {
//        return TAB_TITLES.length;
//    }
//}
