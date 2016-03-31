package com.locify.locifymobile;


import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.locify.locifymobile.com.locify.locifymobile.model.GeoItem;
import com.locify.locifymobile.com.locify.locifymobile.model.GeoResult;
import com.locify.locifymobile.com.locify.locifymobile.model.SearchResultBuffer;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class GeoItemsFragment extends PageFragment implements RetriveItemsListener {
    private static final String TAG = "GeoItemsFragment";
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    GeoItemAdapter adapter;
    protected Handler handler;
    private boolean showProgress;

    public GeoItemsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.v(TAG, "GeoItemsFragment onCreateView: " + this);
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_geo_items, container, false);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.geo_items_view);

        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new GeoItemAdapter(getActivity(), searchResult, recyclerView);
        adapter.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void loadMore() {
                //add null, so the adapter will check view_type and show progress bar at bottom
                List<GeoItem> items = searchResult.getItems();
                items.add(null);
                showProgress = true;
                adapter.notifyItemInserted(items.size() - 1);
                LocifyClient client = LocifyClient.getInstance();
                client.retrieveItemList(getActivity(), GeoItemsFragment.this, searchResult);
            }

            @Override
            public boolean hasMore() {
                return !searchResult.isFull();
            }
        });
        recyclerView.setAdapter(adapter);
        return rootView;
    }

    public void refreshItemList() {
        searchResult.resetResult();
        LocifyClient client = LocifyClient.getInstance();
        client.retrieveItemList(getActivity(), this, searchResult);
    }

    @Override
    public void itemsRetrieved(GeoResult result) {
        Log.v(TAG, "Get Items succeeded ");
        List<GeoItem> items = searchResult.getItems();
        removeProgress();
        for (GeoItem row: result.rows) {
            items.add(row);
            adapter.notifyItemInserted(items.size());
        }
        searchResult.setCenter(result.searchCenter.getPoint());
        searchResult.setTotal(result.totalCount);
        if(adapter != null) {
            adapter.setLoaded();
        }
    }

    @Override
    public void requestFailed(int statusCode) {
        Log.e(TAG, "Get Items failed with status code: " + statusCode);
        removeProgress();
        if(adapter != null) {
            adapter.setLoaded();
        }
    }

    private void removeProgress() {
        if(showProgress) {
            List<GeoItem> items = searchResult.getItems();
            items.remove(items.size() - 1);
            adapter.notifyItemRemoved(items.size());
            showProgress = false;
        }
    }
}
