package com.locify.locifymobile;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.locify.locifymobile.com.locify.locifymobile.model.GeoItemDetails;

import java.util.List;

/**
 * Created by vitaliy on 14.03.2016.
 */
public class ItemDetailsPagerAdapter extends FragmentPagerAdapter {
    List<ItemDetailsFragment> detailsFragments;
    GeoItemDetails itemDetails;

    public ItemDetailsPagerAdapter(FragmentManager fm, List<ItemDetailsFragment> detailsFragments, GeoItemDetails itemDetails) {
        super(fm);
        this.detailsFragments = detailsFragments;
        this.itemDetails = itemDetails;
        for(ItemDetailsFragment fragment: detailsFragments) {
            fragment.setItemDetails(itemDetails);
        }
    }

    @Override
    public Fragment getItem(int position) {
        return detailsFragments.get(position);
    }

    @Override
    public int getCount() {
        return detailsFragments.size();
    }
}

