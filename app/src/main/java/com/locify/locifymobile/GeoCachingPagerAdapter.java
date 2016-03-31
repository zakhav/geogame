package com.locify.locifymobile;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import com.locify.locifymobile.com.locify.locifymobile.model.SearchResultBuffer;

/**
 * Created by vitaliy on 14.03.2016.
 */
public class GeoCachingPagerAdapter extends FragmentPagerAdapter {
    private SearchResultBuffer searchResult;
    int tabCount;
    private Fragment currentFragment;

    public Fragment getCurrentFragment() {
        return currentFragment;
    }
    public GeoCachingPagerAdapter(FragmentManager fm, int numberOfTabs, SearchResultBuffer searchResult) {
        super(fm);
        this.tabCount = numberOfTabs;
        this.searchResult = searchResult;
    }

    @Override
    public Fragment getItem(int position) {
        PageFragment pageFragment = null;
        switch (position) {
            case 0:
                pageFragment = new GeoItemsFragment();
                break;
            case 1:
                pageFragment = new ItemsMapFragment();
                break;
            case 2:
                pageFragment = new GeoSearchFragment();
                break;
            default:
                break;
        }
        if(pageFragment != null) {
            pageFragment.setSearchResult(searchResult);
        }

        return pageFragment;
    }

    @Override
    public int getCount() {
        return tabCount;
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        if (getCurrentFragment() != object) {
            currentFragment = ((Fragment) object);
        }
        super.setPrimaryItem(container, position, object);
    }
}

