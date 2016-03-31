package com.locify.locifymobile;


import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.locify.locifymobile.com.locify.locifymobile.model.GeoItem;
import com.locify.locifymobile.com.locify.locifymobile.model.SearchResultBuffer;


/**
 * A simple {@link Fragment} subclass.
 */
public class ItemsMapFragment extends PageFragment implements OnMapReadyCallback {
    private GoogleMap itemsMap;
    private boolean deferMapReinit;
    private boolean mapCentered;

    public ItemsMapFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_items_map, container, false);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        FragmentManager fm = getChildFragmentManager();
        SupportMapFragment supportMapFragment = (SupportMapFragment) fm.findFragmentById(R.id.map);
        if(supportMapFragment != null) {
            supportMapFragment.getMapAsync(this);
        }
    }

    public void pageActivated() {
        if(itemsMap == null) {
            deferMapReinit = true;
        } else {
            syncMapItems();
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        this.itemsMap = map;
        itemsMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        centerMap();
        if(deferMapReinit) {
            deferMapReinit = false;
            syncMapItems();
        }
    }

    private void centerMap() {
        Location searchCenter = searchResult.getCenter();
        if(searchCenter != null) {
            CameraUpdate center = CameraUpdateFactory.newLatLngZoom(
                    new LatLng(searchCenter.getLatitude(), searchCenter.getLongitude()),
                    6);
            itemsMap.moveCamera(center);
            mapCentered = true;
        }
    }

    private void syncMapItems() {
        itemsMap.clear();
        Location searchCenter = searchResult.getCenter();
        if(searchCenter != null) {
            Circle radiusCircle = itemsMap.addCircle(new CircleOptions()
                    .center(new LatLng(searchCenter.getLatitude(), searchCenter.getLongitude()))
                    .radius(searchResult.getCriteria().radius * 1000)
                    .strokeColor(getResources().getColor(R.color.searchCircleStroke))
                    .fillColor(getResources().getColor(R.color.searchCircleFill)));
        }
        for(GeoItem item: searchResult.getItems()) {
            com.locify.locifymobile.com.locify.locifymobile.model.Location itemLocatipon = item.data.location;
            Marker marker = itemsMap.addMarker(new MarkerOptions()
                    .position(new LatLng(itemLocatipon.latitude, itemLocatipon.longitude))
                    .title(item.data.getName()));
        }
        if(!mapCentered) {
            centerMap();
        }
    }

    public void resetSearch() {
        mapCentered = false;
    }
}
