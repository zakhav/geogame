package com.locify.locifymobile;

import android.support.v4.app.Fragment;

import com.locify.locifymobile.com.locify.locifymobile.model.SearchResultBuffer;

/**
 * Created by vitaliy on 18.03.2016.
 */
public class PageFragment extends Fragment {
    private SearchResultBuffer searchResult;

    public void setSearchResult(SearchResultBuffer searchResult) {
        this.searchResult = searchResult;
    }

    public SearchResultBuffer getSearchResult() {
        return searchResult;
    }
}
