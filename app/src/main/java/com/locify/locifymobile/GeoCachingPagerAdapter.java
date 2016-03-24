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
    }

    @Override
    public Fragment getItem(int position) {
        PageFragment itemFragment = null;
        switch (position) {
            case 0:
                itemFragment = new GeoItemsFragment();
                break;
            case 1:
                itemFragment = new ItemsMapFragment();
                break;
            case 2:
                itemFragment = new GeoSearchFragment();
                break;
            default:
                break;
        }
        if(itemFragment != null) {
            itemFragments[position] = itemFragment;
        }
        return itemFragment;
    }

    @Override
    public int getCount() {
        return tabCount;
    }

    public PageFragment getFragmentAt(int position) {
        return itemFragments[position];
    }

}

