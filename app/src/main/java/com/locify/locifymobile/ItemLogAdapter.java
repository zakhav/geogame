package com.locify.locifymobile;

import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.locify.locifymobile.com.locify.locifymobile.model.GeoItemDetails;
import com.locify.locifymobile.com.locify.locifymobile.model.LogItem;


/**
 * Created by vitaliy on 24.03.2016.
 */
public class ItemLogAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "ItemLogAdapter";
    private final int VIEW_ITEM_LOG = 1;
    private GeoItemDetails itemDetails;

    public ItemLogAdapter(GeoItemDetails itemDetails) {
        this.itemDetails = itemDetails;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        RecyclerView.ViewHolder vh = null;
        if (viewType == VIEW_ITEM_LOG) {
            View v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.log_item_layout, viewGroup, false);
            vh = new ItemLogViewHolder(v);
        }
        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int i) {
        if (holder instanceof ItemLogViewHolder) {
            ItemLogViewHolder viewHolder = (ItemLogViewHolder)holder;

            LogItem item = itemDetails.logs.get(i);
            viewHolder.setItem(item);
            viewHolder.itemDate.setText(item.data.date.toString());

            Resources res = viewHolder.itemView.getResources();
            viewHolder.itemUser.setText(Html.fromHtml(res.getString(R.string.log_user, item.data.user.username)));
            viewHolder.itemComment.setText(Html.fromHtml(res.getString(R.string.log_comment, item.data.comment)));
        }
    }

    @Override
    public int getItemViewType(int position) {
        return VIEW_ITEM_LOG;
    }

    @Override
    public int getItemCount() {
        return itemDetails.logs.size();
    }

    public static class ItemLogViewHolder extends RecyclerView.ViewHolder {
        public LogItem item;
        public TextView itemDate;
        public TextView itemUser;
        public TextView itemComment;

        public ItemLogViewHolder(View itemView) {
            super(itemView);
            itemDate = (TextView)itemView.findViewById(R.id.log_date);
            itemUser = (TextView)itemView.findViewById(R.id.log_user);
            itemComment = (TextView)itemView.findViewById(R.id.log_comment);
        }

        public void setItem(LogItem item) {
            this.item = item;
        }
    }
}