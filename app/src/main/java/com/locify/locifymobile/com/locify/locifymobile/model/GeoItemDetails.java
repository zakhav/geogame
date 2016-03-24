package com.locify.locifymobile.com.locify.locifymobile.model;

import java.util.List;

/**
 * Created by vitaliy on 24.03.2016.
 */
public class GeoItemDetails {
    public GeoItem item;
    public List<LogItem> logs;

    public GeoItemDetails(GeoItem item, List<LogItem> logs) {
        this.item = item;
        this.logs = logs;
    }
}
