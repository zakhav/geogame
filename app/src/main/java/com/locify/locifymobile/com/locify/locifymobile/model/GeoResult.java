package com.locify.locifymobile.com.locify.locifymobile.model;

import java.util.List;

/**
 * Created by vitaliy on 17.03.2016.
 */
public class GeoResult {
    public List<GeoItem> rows;
    public Location searchCenter;
    public long totalCount;

    public GeoResult() {
    }

    public GeoResult(List<GeoItem> rows, Location searchCenter, long totalCount) {
        super();
        this.rows = rows;
        this.searchCenter = searchCenter;
        this.totalCount = totalCount;
    }
}
