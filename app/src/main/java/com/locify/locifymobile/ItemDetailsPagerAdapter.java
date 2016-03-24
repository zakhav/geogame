package com.locify.locifymobile;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.locify.locifymobile.com.locify.locifymobile.model.GeoItemDetails;

/**
 * Created by vitaliy on 14.03.2016.
 */
public class ItemDetailsPagerAdapter extends FragmentPagerAdapter {
    int tabCount;
    GeoItemDetails itemDetails;

    public ItemDetailsPagerAdapter(FragmentManager fm, int numberOfTabs, GeoItemDetails itemDetails) {
        super(fm);
        this.tabCount = numberOfTabs;
        this.itemDetails = itemDetails;
    }

    @Override
    public Fragment getItem(int position) {
        ItemDetailsFragment itemFragment = null;
        switch (position) {
            case 0:
                itemFragment = new ItemLogsFragment();
                break;
            case 1:
                itemFragment = new ItemDetailsMapFragment();
                break;
            default:
                break;
        }
        itemFragment.setItemDetails(itemDetails);
        return itemFragment;
    }

    @Override
    public int getCount() {
        return tabCount;
    }
}

