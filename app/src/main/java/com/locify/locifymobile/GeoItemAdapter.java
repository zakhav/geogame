package com.locify.locifymobile;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.location.Location;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.locify.locifymobile.com.locify.locifymobile.model.GeoItem;
import com.locify.locifymobile.com.locify.locifymobile.model.GeoItemDetails;
import com.locify.locifymobile.com.locify.locifymobile.model.LogItem;
import com.locify.locifymobile.com.locify.locifymobile.model.SearchResultBuffer;

import java.util.List;

/**
 * Created by vitaliy on 16.03.2016.
 */
public class GeoItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "GeoItemAdapter";
    private final int VIEW_PROGRESS = 0;
    private final int VIEW_ITEM = 1;

    private static final int METERS_IN_KM = 1000;

    private Activity activity;
    private SearchResultBuffer searchResult;
    // endless scrolling data
    private int visibleThreshold = 5;
    private int lastVisibleItem, totalItemCount;
    private boolean loading;
    private OnLoadMoreListener onLoadMoreListener;

    public GeoItemAdapter(Activity activity, SearchResultBuffer searchResult, RecyclerView recyclerView) {
        this.activity = activity;
        this.searchResult = searchResult;
        if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {
            final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    if(dy > 0) {
                        totalItemCount = linearLayoutManager.getItemCount();
                        lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                        if (!loading
                                && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                            // End has been reached
                            // Do something
                            if (onLoadMoreListener != null) {
                                if(onLoadMoreListener.hasMore()) {
                                    loading = true;
                                    onLoadMoreListener.loadMore();
                                }
                            }
                        }
                    }
                }
            });
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        RecyclerView.ViewHolder vh;
        if (viewType == VIEW_ITEM) {
            View v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.geo_item_layout, viewGroup, false);
            vh = new ItemViewHolder(v, activity);
        } else {
            View v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.progressbar_item, viewGroup, false);

            vh = new ProgressViewHolder(v);
        }
        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int i) {
        if (holder instanceof ItemViewHolder) {
            ItemViewHolder viewHolder = (ItemViewHolder)holder;
            List<GeoItem> items = searchResult.getItems();

            GeoItem item = items.get(i);
            viewHolder.setItem(item);

            Location itemLocation = item.data.location.getPoint();
            Location center = itemLocation;

            Location searchCenter = searchResult.getCenter();
            if (searchCenter != null) {
                center = searchCenter;
            }

            Resources res = viewHolder.itemView.getResources();
            float distanceInKm = itemLocation.distanceTo(center) / METERS_IN_KM;
            viewHolder.setDistance(distanceInKm);

            viewHolder.itemTitle.setText(item.data.getName());
            viewHolder.itemDistance.setText(Html.fromHtml(res.getString(R.string.item_distance, distanceInKm)));
            viewHolder.itemUser.setText(Html.fromHtml(res.getString(R.string.item_owner, item.data.owner.username)));
            viewHolder.itemDifficulty.setRating((float) item.data.difficulty);
        } else {
            ((ProgressViewHolder)holder).progressBar.setIndeterminate(true);
        }
    }

    @Override
    public int getItemViewType(int position) {
        List<GeoItem> items = searchResult.getItems();
        GeoItem item = items.get(position);

        return item != null ? VIEW_ITEM : VIEW_PROGRESS;
    }

    @Override
    public int getItemCount() {
        return searchResult.getItems().size();
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    public void setLoaded() {
        loading = false;
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder implements RetriveItemLogsListener {
        private Activity activity;
        public GeoItem item;
        public TextView itemTitle;
        public TextView itemDistance;
        public TextView itemUser;
        public RatingBar itemDifficulty;
        private float distance;

        public ItemViewHolder(View itemView, Activity activity) {
            super(itemView);
            this.activity = activity;
            itemTitle = (TextView)itemView.findViewById(R.id.item_title);
            itemDistance = (TextView)itemView.findViewById(R.id.item_distance);
            itemUser = (TextView)itemView.findViewById(R.id.item_user);
            itemDifficulty = (RatingBar)itemView.findViewById(R.id.difficultyRating);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    itemClick(v);
                }
            });
        }

        public void setItem(GeoItem item) {
            this.item = item;
        }
        public void setDistance(float distance) {
            this.distance = distance;
        }

        private void itemClick(View v) {
            Log.d("GeoItemAdapter", "Item click. Code: " + item.data.code);
            LocifyClient client = LocifyClient.getInstance();
            client.retrieveItemLogs(v.getContext(), this, item.data.code);
        }

        @Override
        public void itemLogsRetrieved(List<LogItem> result) {
            GeoItemDetails itemDetails = new GeoItemDetails(item, result);
            Intent intent = new Intent(activity, GeoItemDetailsActivity.class);

            Gson gson = new Gson();
            String detailsJson = gson.toJson(itemDetails);
            intent.putExtra(GeoItemDetailsActivity.IDEM_DETAILS, detailsJson);
            intent.putExtra(GeoItemDetailsActivity.IDEM_DISTANCE, distance);
            activity.startActivityForResult(intent, GeoCachingActivity.RESULT_DETAILS);
//            Log.e("GeoItemAdapter", "Get item logs suceeded: " + result.size());
        }

        @Override
        public void requestFailed(int statusCode) {
            Log.e("GeoItemAdapter", "Get item logs failed: " + statusCode);
        }
    }

    public static class ProgressViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;

        public ProgressViewHolder(View v) {
            super(v);
            progressBar = (ProgressBar) v.findViewById(R.id.endlessProgressBar);
        }
    }
}