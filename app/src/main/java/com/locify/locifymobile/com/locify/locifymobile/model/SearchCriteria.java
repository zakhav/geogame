package com.locify.locifymobile.com.locify.locifymobile.model;

import com.loopj.android.http.RequestParams;

/**
 * Created by vitaliy on 18.03.2016.
 */
public class SearchCriteria {
    public static final int DEFAULT_SEARCH_RADIUS_KM = 100;
    public SearchType type = SearchType.CURRENT_LOCATION;
    public int radius;
    public String city;
    public String zip;

    public SearchCriteria() {
        radius = DEFAULT_SEARCH_RADIUS_KM;
        city = "";
        zip = "00000";
    }
}
