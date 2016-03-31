package com.locify.locifymobile;

import android.Manifest;
import android.location.Location;
import android.location.LocationManager;
import android.os.PersistableBundle;
import android.support.v4.app.Fragment;
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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.locify.locifymobile.com.locify.locifymobile.model.SearchCriteria;
import com.locify.locifymobile.com.locify.locifymobile.model.SearchResultBuffer;
import com.locify.locifymobile.com.locify.locifymobile.model.SearchType;

import java.util.Date;


public class GeoCachingActivity extends FragmentActivity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "GeoCachingActivity";
    private static final String SEARCH_TYPE_PROP = "search_type";
    private static final String SEARCH_RADIUS_PROP = "search_radius";
    private static final String SEARCH_CITY_PROP = "search_city";
    private static final String SEARCH_ZIP_PROP = "search_zip";

    private static final String SEARCH_EXTRA = "search_buffer";

    private static final int ITEMS_TAB_INDEX = 0;
    private static final int MAP_TAB_INDEX = 1;
    private static final int LOCATION_REQUEST_CODE = 101;
    public static final int RESULT_DETAILS = 137;

    private TabLayout tabLayout;
    private GeoCachingPagerAdapter pagerAdapter;
    private ViewPager viewPager;
    private LocationManager locationManager;
    private static final long LOCATION_REFRESH_TIME = 5000L;
    private static final float LOCATION_REFRESH_DISTANCE = 10.0f;
    private GoogleApiClient googleApiClient;

    private SearchResultBuffer searchBuffer;

    public GeoCachingActivity() {
        searchBuffer = new SearchResultBuffer();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "OnCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geo_caching);

        if(savedInstanceState != null) {
            restoreSearchFromBundle(savedInstanceState);
        }

        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        tabLayout = (TabLayout) findViewById(R.id.tab_geocaching);
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.geo_items));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.geo_marker));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.geo_find));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        viewPager = (ViewPager) findViewById(R.id.pager);
        pagerAdapter = new GeoCachingPagerAdapter(
                getSupportFragmentManager(), tabLayout.getTabCount(), searchBuffer);
        viewPager.setAdapter(pagerAdapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                Fragment pageFragment = pagerAdapter.getCurrentFragment();
                if(tab.getPosition() == MAP_TAB_INDEX) {
                    ItemsMapFragment mapFragment = (ItemsMapFragment)pageFragment;
                    mapFragment.pageActivated();
                }
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
        Log.i(TAG, "OnStart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        googleApiClient.disconnect();
        Log.i(TAG, "OnStop");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(TAG, "OnRestart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "OnResume");
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
        TabLayout.Tab itemsTab = tabLayout.getTabAt(ITEMS_TAB_INDEX);
        if(itemsTab != null) {
            itemsTab.select();
            GeoItemsFragment itemsFragment = (GeoItemsFragment)pagerAdapter.getCurrentFragment();
            itemsFragment.refreshItemList();
        }
//            if(mapFragment != null) {
//                mapFragment.resetSearch();
//            }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        persistSearchToBundle(outState);
        Log.i(TAG, "onSaveInstanceState: " + outState);
    }

    private void persistSearchToBundle(Bundle bundle) {
        Gson gson = new GsonBuilder().create();
        bundle.putString(SEARCH_EXTRA, gson.toJson(searchBuffer));
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        restoreSearchFromBundle(savedInstanceState);
        Log.i(TAG, "onRestoreInstanceState: " + savedInstanceState);
    }

    private void restoreSearchFromBundle(Bundle bundle) {
        Gson gson = new GsonBuilder().create();
        searchBuffer = gson.fromJson(bundle.getString(SEARCH_EXTRA), SearchResultBuffer.class);
    }
}
