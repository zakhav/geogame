package com.locify.locifymobile;


import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import com.locify.locifymobile.com.locify.locifymobile.model.GeoItem;
import com.locify.locifymobile.com.locify.locifymobile.model.GeoItemDetails;

import java.text.SimpleDateFormat;

/**
 * A simple {@link Fragment} subclass.
 */
public class ItemLogsFragment extends ItemDetailsFragment {
    SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    private static final String TAG = "ItemLogsFragment";
    RecyclerView recyclerView;
    TextView itemTitle;
    TextView itemDistance;
    TextView itemUser;
    RatingBar itemDifficulty;

    TextView itemDateHidden;
    TextView itemLastFound;
    TextView itemDescription;

    RecyclerView.LayoutManager layoutManager;
    ItemLogAdapter adapter;

    public ItemLogsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_item_details, container, false);
        itemTitle = (TextView)rootView.findViewById(R.id.item_title);
        itemDistance = (TextView)rootView.findViewById(R.id.item_distance);
        itemUser = (TextView)rootView.findViewById(R.id.item_user);
        itemDifficulty = (RatingBar)rootView.findViewById(R.id.difficultyRating);

        itemDateHidden = (TextView)rootView.findViewById(R.id.date_hidden);
        itemLastFound = (TextView)rootView.findViewById(R.id.last_found);
        itemDescription = (TextView)rootView.findViewById(R.id.item_description);

        recyclerView = (RecyclerView) rootView.findViewById(R.id.item_logs_view);

        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new ItemLogAdapter(getItemDetails());
        recyclerView.setAdapter(adapter);

        syncControls();

        return rootView;
    }

    private void syncControls() {
        GeoItemDetails itemDetails = getItemDetails();
        GeoItem item = itemDetails.item;
        Resources res = recyclerView.getResources();

        float distanceInKm = getActivity().getIntent().getFloatExtra(GeoItemDetailsActivity.IDEM_DISTANCE, 0.0f);
        itemTitle.setText(item.data.getName());
        itemDistance.setText(Html.fromHtml(res.getString(R.string.item_distance, distanceInKm)));
        itemUser.setText(Html.fromHtml(res.getString(R.string.item_owner, item.data.owner.username)));
        itemDifficulty.setRating((float) item.data.difficulty);

        itemDescription.setText(Html.fromHtml(item.data.getDescription()));

        itemDateHidden.setText(Html.fromHtml(res.getString(R.string.date_hidden, dateFormatter.format(item.data.dateHidden))));
        itemLastFound.setText(Html.fromHtml(res.getString(R.string.last_found, dateFormatter.format(item.data.lastModified))));
    }
}
