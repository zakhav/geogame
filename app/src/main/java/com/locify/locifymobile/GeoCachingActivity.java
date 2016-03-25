package com.locify.locifymobile;

import android.Manifest;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.design.widget.TabLayout;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.locify.locifymobile.com.locify.locifymobile.model.SearchCriteria;
import com.locify.locifymobile.com.locify.locifymobile.model.SearchResultBuffer;
import com.locify.locifymobile.com.locify.locifymobile.model.SearchType;


public class GeoCachingActivity extends FragmentActivity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "GeoCachingActivity";
    private static final String SEARCH_TYPE_PROP = "search_type";
    private static final String SEARCH_RADIUS_PROP = "search_radius";
    private static final String SEARCH_CITY_PROP = "search_city";
    private static final String SEARCH_ZIP_PROP = "search_zip";

    private static final int ITEMS_TAB_INDEX = 0;
    private static final int MAP_TAB_INDEX = 1;
    private static final int LOCATION_REQUEST_CODE = 101;
    public static final int RESULT_DETAILS = 137;

    private TabLayout tabLayout;
    private GeoCachingPagerAdapter pagerAdapter;
    private LocationManager locationManager;
    private static final long LOCATION_REFRESH_TIME = 5000L;
    private static final float LOCATION_REFRESH_DISTANCE = 10.0f;
    private GoogleApiClient googleApiClient;

    private SearchResultBuffer searchBuffer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        searchBuffer = new SearchResultBuffer();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geo_caching);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

//        requestPermission(Manifest.permission.ACCESS_FINE_LOCATION,
//                LOCATION_REQUEST_CODE);

        tabLayout = (TabLayout) findViewById(R.id.tab_geocaching);
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.geo_items));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.geo_marker));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.geo_find));

        final ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        pagerAdapter = new GeoCachingPagerAdapter(
                getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(pagerAdapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                PageFragment activatedFragment = pagerAdapter.getFragmentAt(tab.getPosition());
                if (activatedFragment != null) {
                    activatedFragment.pageActivated();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                PageFragment deactivatedFragment = pagerAdapter.getFragmentAt(tab.getPosition());
                if (deactivatedFragment != null) {
                    deactivatedFragment.pageDectivated();
                }
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

    private void loadSearchCriteria(Bundle instanceState, SearchCriteria searchCriteria) {
        String typeProp = instanceState.getString(SEARCH_TYPE_PROP, SearchType.CURRENT_LOCATION.name());
        searchCriteria.type = SearchType.valueOf(typeProp);
        searchCriteria.radius = instanceState.getInt(SEARCH_RADIUS_PROP, 100);
        searchCriteria.zip = instanceState.getString(SEARCH_ZIP_PROP, "00000");
        searchCriteria.city = instanceState.getString(SEARCH_CITY_PROP, "");
    }

    public SearchResultBuffer getSearchBuffer() {
        return searchBuffer;
    }

    public void persistSearchCriteria(Bundle instanceState, SearchCriteria searchCriteria) {
        instanceState.putString(SEARCH_TYPE_PROP, searchCriteria.type.name());
        instanceState.putInt(SEARCH_RADIUS_PROP, searchCriteria.radius);
        instanceState.putString(SEARCH_ZIP_PROP, searchCriteria.zip);
        instanceState.putString(SEARCH_CITY_PROP, searchCriteria.city);
    }

    public void doSearch() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        GeoItemsFragment itemsFragment = (GeoItemsFragment)pagerAdapter.getFragmentAt(ITEMS_TAB_INDEX);
        ItemsMapFragment mapFragment = (ItemsMapFragment)pagerAdapter.getFragmentAt(MAP_TAB_INDEX);
        if(itemsFragment != null) {
            TabLayout.Tab itemsTab = tabLayout.getTabAt(ITEMS_TAB_INDEX);
            if(itemsTab != null) {
                itemsTab.select();
            }
            itemsFragment.refreshItemList();
            if(mapFragment != null) {
                mapFragment.resetSearch();
            }
        }
    }
}
