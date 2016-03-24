package com.locify.locifymobile;

import android.support.v4.app.Fragment;

import com.locify.locifymobile.com.locify.locifymobile.model.GeoItemDetails;

/**
 * Created by vitaliy on 18.03.2016.
 */
public class ItemDetailsFragment extends Fragment {
    private GeoItemDetails itemDetails;

    public GeoItemDetails getItemDetails() {
        return itemDetails;
    }

    public void setItemDetails(GeoItemDetails itemDetails) {
        this.itemDetails = itemDetails;
    }
}
