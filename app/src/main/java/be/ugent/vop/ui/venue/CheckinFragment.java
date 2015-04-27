package be.ugent.vop.ui.venue;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import be.ugent.vop.BaseActivity;
import be.ugent.vop.R;
import be.ugent.vop.backend.loaders.SearchVenueLoader;
import be.ugent.vop.backend.loaders.VenueLoader;
import be.ugent.vop.foursquare.FoursquareVenue;


public class CheckinFragment extends Fragment implements LoaderManager.LoaderCallbacks<ArrayList<FoursquareVenue>> {
    private static final String TAG = "CheckinFragment";
    private static final int SCROLL_STATE_IDLE = 0;
    private static final int SCROLL_STATE_DRAGGING = 1;
    private static final String QUERY = "query";

    private View mRootView;
    protected CheckinFragment mFragment;
    protected RecyclerView mRecyclerView;
    protected VenueListAdapter mAdapter;
    private TextView mLoadingMessage;
    protected RecyclerView.LayoutManager mLayoutManager;
    private GoogleMap map;
    private MapFragment fragment;
    private Location mLastLocation;

    private BaseActivity mActivity;

    private BaseActivity.LocationUpdateListener mListener = new BaseActivity.LocationUpdateListener() {
        @Override
        public void locationUpdated(Location newLocation, Date lastUpdated) {
            newLocationAvailable(newLocation, lastUpdated);
        }
    };

    private float mRecyclerviewTranslation;
    private float mRecyclerViewInitialTranslation;
    private float mRecyclerViewMaxTranslation;
    private int mInitialTouchY;

    private boolean mShowMap = true;

    private int mTouchSlop;
    private int mScrollState;
    private int mLastTouchY;
    private int mScrollPointerId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivity = (BaseActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_checkin, container, false);
        mRootView.setTag(TAG);

        setHasOptionsMenu(true);

        mRecyclerView = (RecyclerView) mRootView.findViewById(R.id.venue_list);
        mLoadingMessage = (TextView) mRootView.findViewById(R.id.waiting_message);
        // LinearLayoutManager is used here, this will layout the elements in a similar fashion
        // to the way ListView would layout elements. The RecyclerView.LayoutManager defines how
        // elements are laid out.
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new VenueListAdapter();
        // Set CustomAdapter as the adapter for RecyclerView.
        mRecyclerView.setAdapter(null);
        // END_INCLUDE(initializeRecyclerView);
        mFragment=this;

        ViewConfiguration vc = ViewConfiguration.get(getActivity());
        mTouchSlop = vc.getScaledTouchSlop();

        mRecyclerViewInitialTranslation = mRecyclerViewMaxTranslation = mRecyclerviewTranslation = mRecyclerView.getTranslationY();

        mRecyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        Log.d(TAG, "OnActionDown");
                        mScrollPointerId = MotionEventCompat.getPointerId(event, 0);
                        mInitialTouchY = (int) (event.getRawY() + 0.5f);
                        return false;
                    case MotionEvent.ACTION_UP:
                        Log.d(TAG, "ActionUP");
                        mScrollState = SCROLL_STATE_IDLE;
                        mInitialTouchY = 0;
                        return false;
                    case MotionEvent.ACTION_MOVE:
                        boolean consumed = false;
                        final int index = MotionEventCompat.findPointerIndex(event, mScrollPointerId);
                        if (index < 0) {
                            Log.e(TAG, "Error processing scroll; pointer index for id " +
                                    mScrollPointerId + " not found. Did any MotionEvents get skipped?");
                        }

                        final int y = (int) (event.getRawY() + 0.5f);
                        Log.d(TAG, "mInitialTouchY: " + mInitialTouchY);

                        if (mScrollState != SCROLL_STATE_DRAGGING) {
                            if(mInitialTouchY == 0)
                                mInitialTouchY = (int) (event.getRawY() + 0.5f);
                            final int dy = y - mInitialTouchY;
                            boolean startScroll = false;

                            if (Math.abs(dy) > mTouchSlop) {
                                mLastTouchY = mInitialTouchY ;//+ mTouchSlop * (dy < 0 ? -1 : 1);
                                startScroll = true;
                            }
                            if (startScroll) {
                                mScrollState = SCROLL_STATE_DRAGGING;
                            }
                        }
                        if (mScrollState == SCROLL_STATE_DRAGGING) {
                            final int dy = y - mLastTouchY;
                            Log.d(TAG, "dy: " + dy + ", currentTranslation: " + mRecyclerviewTranslation + ", y: " + y);

                            LinearLayoutManager lm = (LinearLayoutManager) mRecyclerView.getLayoutManager();

                            if(mRecyclerViewMaxTranslation == mRecyclerViewInitialTranslation)
                                mRecyclerViewMaxTranslation = mRootView.getHeight() - TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 72, getActivity().getResources().getDisplayMetrics());

                            if(mShowMap && dy < 0 && mRecyclerView.getTranslationY() != 0){
                                mRecyclerviewTranslation = Math.max(mRecyclerviewTranslation + dy, 0);
                                mRecyclerView.setTranslationY(mRecyclerviewTranslation);
                                consumed = true;
                            } else if (mShowMap && dy > 0 && lm.findFirstCompletelyVisibleItemPosition() == 0){
                                mRecyclerviewTranslation = Math.min(mRecyclerviewTranslation + dy, mRecyclerViewMaxTranslation);
                                mRecyclerView.setTranslationY(mRecyclerviewTranslation);
                                consumed = true;
                            }
                        }
                        mLastTouchY = y;

                        return consumed;
                    default: break;
                }

                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent event) {
                switch(event.getAction()){
                    case MotionEvent.ACTION_UP:
                        Log.d(TAG, "ActionUP");
                        mScrollState = SCROLL_STATE_IDLE;
                        mInitialTouchY = 0;
                    case MotionEvent.ACTION_MOVE:
                        final int index = MotionEventCompat.findPointerIndex(event, mScrollPointerId);
                        if (index < 0) {
                            Log.e(TAG, "Error processing scroll; pointer index for id " +
                                    mScrollPointerId + " not found. Did any MotionEvents get skipped?");
                        }

                        final int y = (int) (event.getRawY() + 0.5f);
                        Log.d(TAG, "mInitialTouchY: " + mInitialTouchY);

                        if (mScrollState != SCROLL_STATE_DRAGGING) {
                            if(mInitialTouchY == 0)
                                mInitialTouchY = (int) (event.getRawY() + 0.5f);
                            final int dy = y - mInitialTouchY;
                            boolean startScroll = false;

                            if (Math.abs(dy) > mTouchSlop) {
                                mLastTouchY = mInitialTouchY ;//+ mTouchSlop * (dy < 0 ? -1 : 1);
                                startScroll = true;
                            }
                            if (startScroll) {
                                mScrollState = SCROLL_STATE_DRAGGING;
                            }
                        }
                        if (mScrollState == SCROLL_STATE_DRAGGING) {
                            final int dy = y - mLastTouchY;
                            Log.d(TAG, "dy: " + dy + ", currentTranslation: " + mRecyclerviewTranslation + ", y: " + y);

                            LinearLayoutManager lm = (LinearLayoutManager) mRecyclerView.getLayoutManager();

                            if(mRecyclerViewMaxTranslation == mRecyclerViewInitialTranslation)
                                mRecyclerViewMaxTranslation = mRootView.getHeight() - TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 72, getActivity().getResources().getDisplayMetrics());

                            if(mShowMap && dy < 0 && mRecyclerView.getTranslationY() != 0){
                                mRecyclerviewTranslation = Math.max(mRecyclerviewTranslation + dy, 0);
                                mRecyclerView.setTranslationY(mRecyclerviewTranslation);
                            } else if (mShowMap && dy > 0 && lm.findFirstCompletelyVisibleItemPosition() == 0){
                                mRecyclerviewTranslation = Math.min(mRecyclerviewTranslation + dy, mRecyclerViewMaxTranslation);
                                mRecyclerView.setTranslationY(mRecyclerviewTranslation);
                            }
                        }
                        mLastTouchY = y;

                }
            }
        });

        return mRootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Needs to call MapsInitializer before doing any CameraUpdateFactory calls
        MapsInitializer.initialize(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();

        mActivity.addLocationUpdateListener(mListener);

        if (fragment == null) {
            fragment = getMapFragment();
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        mActivity.removeLocationUpdateListener(mListener);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.checkin_menu, menu);
        // Associate searchable configuration with the SearchView
        MenuItem searchItem = menu.findItem(R.id.action_search);

        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);

        SearchView searchView = null;
        if (searchItem != null) {
            searchView = (SearchView) searchItem.getActionView();
        }
        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));

            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {
                    Bundle args = new Bundle();
                    args.putString(QUERY, s);
                    getLoaderManager().restartLoader(0, args, mSearchVenueByNameLoaderListener);
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String s) {
                    // Do nothing when not submitted to reduce network activity
                    return true;
                }
            });

            searchView.setOnCloseListener(new SearchView.OnCloseListener() {
                @Override
                public boolean onClose() {
                    Log.d(TAG, "Closing");
                    loadNearbyVenues();
                    return false;
                }
            });
        }

        super.onCreateOptionsMenu(menu, inflater);

    }

    private void newLocationAvailable(Location newLocation, Date lastUpdated){
        Log.d(TAG, "new Location available in fragment");
        if(newLocation.getAccuracy() < BaseActivity.MIN_LOCATION_ACCURACY){
            if(mLastLocation == null || mLastLocation.distanceTo(newLocation) > 100){
                mLastLocation = newLocation;
                loadNearbyVenues();
            }
        }
    }

    private void loadNearbyVenues(){
        mRecyclerView.setTranslationY(mRecyclerViewInitialTranslation);
        mRecyclerviewTranslation = mRecyclerViewInitialTranslation;
        mShowMap = true;
        getLoaderManager().restartLoader(0, null, this);
    }

    private MapFragment getMapFragment() {
        FragmentManager fm = null;

        Log.d(TAG, "sdk: " + Build.VERSION.SDK_INT);
        Log.d(TAG, "release: " + Build.VERSION.RELEASE);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Log.d(TAG, "using getFragmentManager");
            fm = getFragmentManager();
        } else {
            Log.d(TAG, "using getChildFragmentManager");
            fm = getChildFragmentManager();
        }

        return (MapFragment) fm.findFragmentById(R.id.map);
    }

    @Override
    public Loader<ArrayList<FoursquareVenue>> onCreateLoader(int i, Bundle bundle) {
        Log.d(TAG, "onCreateLoader");

        return new VenueLoader(getActivity(), mLastLocation);
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<FoursquareVenue>> arrayListLoader, final ArrayList<FoursquareVenue> venues) {
        Log.d(TAG, "onLoadFinished");
        /**************************************
                 Resultaat kan null zijn
                 Rekening mee houden!
         **************************************/
        mAdapter.setVenues(venues);
        mAdapter.setContext(getActivity());
        mRecyclerView.setAdapter(mAdapter);

        mLoadingMessage.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
        if (fragment == null) {
            fragment = getMapFragment();
        }

        if (map == null) {
            fragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    final HashMap<Marker, String> markerVenue = new HashMap<>();

                    map = googleMap;

                    for(FoursquareVenue v : venues){
                        Marker m = map.addMarker(new MarkerOptions().position(new LatLng(v.getLatitude(), v.getLongitude()))
                                        .title(v.getName()));

                        markerVenue.put(m, v.getId());
                    }

                    map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                        @Override
                        public void onInfoWindowClick(Marker marker) {
                            String venueId = markerVenue.get(marker);

                            FoursquareVenue v = null;

                            for(FoursquareVenue vb : venues){
                                if(vb.getId().equals(venueId)){
                                    v = vb;
                                    break;
                                }
                            }

                            if(v != null){
                                Intent intent = new Intent(getActivity(), VenueActivity.class);
                                intent.putExtra(VenueActivity.VENUE_ID, v.getId());

                                getActivity().startActivity(intent);
                            }

                        }
                    });

                    if(venues.size()!=0) {
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(venues.get(0).getLatitude(),
                                venues.get(0).getLongitude()), 15));
                    }else{
                        //Move to centrum of UGent
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(51.046127,
                                3.727251), 15));
                    }
                    // Zoom in, animating the camera.
                    map.animateCamera(CameraUpdateFactory.zoomTo(17), 2000, null);
                }
            });
        }
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<FoursquareVenue>> arrayListLoader) {
        mRecyclerView.setAdapter(null);
    }

    /**
     * Search venues by name loader
     */

    private LoaderManager.LoaderCallbacks<ArrayList<FoursquareVenue>> mSearchVenueByNameLoaderListener
            = new LoaderManager.LoaderCallbacks<ArrayList<FoursquareVenue>>() {
        @Override
        public void onLoadFinished(Loader<ArrayList<FoursquareVenue>> arrayListLoader, final ArrayList<FoursquareVenue> venues) {
            /**************************************
             Resultaat kan null zijn
             Rekening mee houden!
             **************************************/
            mAdapter.setVenues(venues);
            mAdapter.setContext(getActivity());
            mRecyclerView.setAdapter(mAdapter);

            mLoadingMessage.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);

            mRecyclerView.setTranslationY(0);
            mShowMap = false;
            mRecyclerviewTranslation = 0;
        }

        @Override
        public Loader<ArrayList<FoursquareVenue>> onCreateLoader(int id, Bundle args) {
            return new SearchVenueLoader(mActivity, args.getString(QUERY));
        }

        @Override
        public void onLoaderReset(Loader<ArrayList<FoursquareVenue>> arrayListLoader) {
            mRecyclerView.setAdapter(null);
        }

    };
}
