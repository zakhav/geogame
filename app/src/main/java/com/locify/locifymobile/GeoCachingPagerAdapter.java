package com.locify.locifymobile;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by vitaliy on 14.03.2016.
 */
public class GeoCachingPagerAdapter extends FragmentPagerAdapter {
    private PageFragment itemFragments[];
    int tabCount;

    public GeoCachingPagerAdapter(FragmentManager fm, int numberOfTabs) {
        super(fm);
        this.tabCount = numberOfTabs;
        itemFragments = new PageFragment[numberOfTabs];
        itemFragments[0] = new GeoItemsFragment();
        itemFragments[1] = new ItemsMapFragment();
        itemFragments[2] = new GeoSearchFragment();
    }

    @Override
    public Fragment getItem(int position) {
        return itemFragments[position];
    }

    @Override
    public int getCount() {
        return tabCount;
    }

    public PageFragment getFragmentAt(int position) {
        return itemFragments[position];
    }
}

