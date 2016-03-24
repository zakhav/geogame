package com.locify.locifymobile;

import com.locify.locifymobile.com.locify.locifymobile.model.GeoResult;
import com.locify.locifymobile.com.locify.locifymobile.model.LogItem;

import java.util.List;

/**
 * Created by vitaliy on 15.03.2016.
 */
public interface RetriveItemLogsListener {
    public void itemLogsRetrieved(List<LogItem> result);
    public void requestFailed(int statusCode);
}
