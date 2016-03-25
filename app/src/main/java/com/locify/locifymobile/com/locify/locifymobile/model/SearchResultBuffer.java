package com.locify.locifymobile.com.locify.locifymobile.model;

import com.loopj.android.http.RequestParams;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vitaliy on 17.03.2016.
 */
public class SearchResultBuffer {
    private static final int UNDEFINED = -1;
    private List<GeoItem> items;
    private android.location.Location center;
    private SearchCriteria criteria;
    private int pageSize;
    private long total;

    public SearchResultBuffer() {
        items = new ArrayList<GeoItem>();
        criteria = new SearchCriteria();
        pageSize = 10;
        total = UNDEFINED;
    }

    public List<GeoItem> getItems() {
        return items;
    }

    public void setItems(List<GeoItem> items) {
        this.items = items;
    }

    public android.location.Location getCenter() {
        return center;
    }

    public void setCenter(android.location.Location center) {
        this.center = center;
    }

    public void resetResult() {
        items.clear();
        center = null;
        total = UNDEFINED;
    }

    public SearchCriteria getCriteria() {
        return criteria;
    }

    public void setCriteria(SearchCriteria criteria) {
        this.criteria = criteria;
    }

    public RequestParams createRequestParams() {
        RequestParams params = new RequestParams();
        String type = "currentLocation";
        String filter = "";
        if(criteria.type == SearchType.CITY) {
            type = "city";
            filter = criteria.city;
        } else if(criteria.type == SearchType.ZIP) {
            type = "zip";
            filter = criteria.zip;
        }
        params.put("searchBy", type);
        params.put("searchFilter", filter);
        params.put("searchRadius", criteria.radius);
        int offset = items.size();
        if(offset > 0) {
            GeoItem item = items.get(offset - 1);
            if(item == null) {
                offset--;
            }
        }
        params.put("offset", offset);
        params.put("pageSize", pageSize);
        return params;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public boolean isFull() {
        return (items.size() == total);
    }
}
