package com.locify.locifymobile;

import android.Manifest;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.locify.locifymobile.com.locify.locifymobile.model.GeoItemDetails;
import com.locify.locifymobile.com.locify.locifymobile.model.SearchResultBuffer;

import java.util.ArrayList;
import java.util.List;


public class GeoItemDetailsActivity extends FragmentActivity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "GeoItemDetailsActivity";
    public static final String IDEM_DETAILS = "item.details";
    public static final String IDEM_DISTANCE = "item.distance";
    private static final String IDEM_EXTRA = "item_buffer";

    private static final int LOGS_TAB_INDEX = 0;
    private static final int MAP_TAB_INDEX = 1;

    private TabLayout tabLayout;
    private ItemDetailsPagerAdapter pagerAdapter;
    private LocationManager locationManager;
    private static final long LOCATION_REFRESH_TIME = 5000L;
    private static final float LOCATION_REFRESH_DISTANCE = 10.0f;
    private GoogleApiClient googleApiClient;

    private static final String STORE_FRAGMENT_LOGS = "fragment.logs";
    private static final String STORE_FRAGMENT_MAP = "fragment.detailsMap";

    private GeoItemDetails itemDetails;
    private ItemLogsFragment logsFragment;
    private ItemDetailsMapFragment mapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_details);

        List<ItemDetailsFragment> pageFragments = new ArrayList<ItemDetailsFragment>();

        FragmentManager fm = getSupportFragmentManager();

        if(savedInstanceState != null) {
            String logsFragmentTag = savedInstanceState.getString(STORE_FRAGMENT_LOGS);
            logsFragment = (ItemLogsFragment) fm.findFragmentByTag(logsFragmentTag);
            String mapFragmentTag = savedInstanceState.getString(STORE_FRAGMENT_MAP);
            mapFragment = (ItemDetailsMapFragment) fm.findFragmentByTag(mapFragmentTag);
        } else {
            logsFragment = new ItemLogsFragment();
            mapFragment = new ItemDetailsMapFragment();
        }
        pageFragments.add(logsFragment);
        pageFragments.add(mapFragment);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        tabLayout = (TabLayout) findViewById(R.id.tab_item_deatils);
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.geo_items));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.geo_marker));

        final ViewPager viewPager = (ViewPager) findViewById(R.id.details_pager);

        String detailsJson = getIntent().getStringExtra(IDEM_DETAILS);
        float distance = getIntent().getFloatExtra(IDEM_DISTANCE, 0.0f);
        itemDetails = null;

        if(detailsJson != null) {
            Gson gson = new Gson();
            itemDetails = gson.fromJson(detailsJson, GeoItemDetails.class);
        }
        if(savedInstanceState != null) {
            restoreItemFromBundle(savedInstanceState);
        }

        pagerAdapter = new ItemDetailsPagerAdapter(
                getSupportFragmentManager(), pageFragments, itemDetails);
        viewPager.setAdapter(pagerAdapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "Location services connected.");
        int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ;
        Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if (location == null) {
            Log.e(TAG, "Couldn't obtain current location");
            location = new Location((String)null);
            location.setLatitude(37);
            location.setLongitude(-122);
            LocifyClient.getInstance().sendCurrentLocation(this, location);
        } else {
            handleNewLocation(location);
        };
    }

    private void handleNewLocation(Location location) {
        LocifyClient.getInstance().sendCurrentLocation(this, location);
        Log.d(TAG, location.toString());
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Location services suspended. Please reconnect.");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // Display the error code on failure
        Log.e(TAG, "Connection Failure : " + connectionResult.getErrorCode());
    }

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        googleApiClient.disconnect();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        persistItemToBundle(outState);

        if (logsFragment != null) {
            outState.putString(STORE_FRAGMENT_LOGS, logsFragment.getTag());
        }
        if (mapFragment != null) {
            outState.putString(STORE_FRAGMENT_MAP, mapFragment.getTag());
        }

    }

    private void persistItemToBundle(Bundle bundle) {
        Gson gson = new GsonBuilder().create();
        bundle.putString(IDEM_EXTRA, gson.toJson(itemDetails));
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        restoreItemFromBundle(savedInstanceState);
    }

    private void restoreItemFromBundle(Bundle bundle) {
        Gson gson = new GsonBuilder().create();
        itemDetails = gson.fromJson(bundle.getString(IDEM_EXTRA), GeoItemDetails.class);
    }
}
