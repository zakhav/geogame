package com.locify.locifymobile;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import com.locify.locifymobile.com.locify.locifymobile.model.SearchResultBuffer;

import java.util.List;

/**
 * Created by vitaliy on 14.03.2016.
 */
public class GeoCachingPagerAdapter extends FragmentPagerAdapter {
    private SearchResultBuffer searchResult;
    List<PageFragment> pageFragments;
    private Fragment currentFragment;

    public GeoCachingPagerAdapter(FragmentManager fm, List<PageFragment> pageFragments, SearchResultBuffer searchResult) {
        super(fm);
        this.pageFragments = pageFragments;
        this.searchResult = searchResult;
        for(PageFragment fragment: pageFragments) {
            fragment.setSearchResult(searchResult);
        }
    }

    @Override
    public Fragment getItem(int position) {
        return pageFragments.get(position);
    }

    @Override
    public int getCount() {
        return pageFragments.size();
    }
}

