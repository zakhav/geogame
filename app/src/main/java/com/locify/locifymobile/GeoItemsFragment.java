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
    SearchResultBuffer searchBuffer;
    protected Handler handler;
    private boolean showProgress;

    public GeoItemsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_geo_items, container, false);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.geo_items_view);

        GeoCachingActivity geoCachingActivity = (GeoCachingActivity)getActivity();
        searchBuffer = geoCachingActivity.getSearchBuffer();

        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new GeoItemAdapter(getActivity(), searchBuffer, recyclerView);
        adapter.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void loadMore() {
                //add null, so the adapter will check view_type and show progress bar at bottom
                List<GeoItem> items = searchBuffer.getItems();
                items.add(null);
                showProgress = true;
                adapter.notifyItemInserted(items.size() - 1);
                LocifyClient client = LocifyClient.getInstance();
                client.retrieveItemList(getActivity(), GeoItemsFragment.this, searchBuffer);
            }

            @Override
            public boolean hasMore() {
                return !searchBuffer.isFull();
            }
        });
        recyclerView.setAdapter(adapter);
        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GeoCachingActivity geoCachingActivity = (GeoCachingActivity)getActivity();
        searchBuffer = geoCachingActivity.getSearchBuffer();
        refreshItemList();
    }

    public void refreshItemList() {
        searchBuffer.resetResult();
        LocifyClient client = LocifyClient.getInstance();
        client.retrieveItemList(getActivity(), this, searchBuffer);
    }

    @Override
    public void itemsRetrieved(GeoResult result) {
        Log.v(TAG, "Get Items succeeded ");
        List<GeoItem> items = searchBuffer.getItems();
        removeProgress();
        for (GeoItem row: result.rows) {
            items.add(row);
            adapter.notifyItemInserted(items.size());
        }
        searchBuffer.setCenter(result.searchCenter.getPoint());
        searchBuffer.setTotal(result.totalCount);
        adapter.setLoaded();
    }

    @Override
    public void requestFailed(int statusCode) {
        Log.e(TAG, "Get Items failed with status code: " + statusCode);
        removeProgress();
        adapter.setLoaded();
    }

    private void removeProgress() {
        if(showProgress) {
            List<GeoItem> items = searchBuffer.getItems();
            items.remove(items.size() - 1);
            adapter.notifyItemRemoved(items.size());
            showProgress = false;
        }
    }
}
