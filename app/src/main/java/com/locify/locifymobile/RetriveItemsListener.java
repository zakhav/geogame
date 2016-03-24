package com.locify.locifymobile;

import android.location.Location;

import com.locify.locifymobile.com.locify.locifymobile.model.GeoItem;
import com.locify.locifymobile.com.locify.locifymobile.model.GeoResult;

import java.util.List;

/**
 * Created by vitaliy on 15.03.2016.
 */
public interface RetriveItemsListener {
    public void itemsRetrieved(GeoResult result);
    public void requestFailed(int statusCode);
}
