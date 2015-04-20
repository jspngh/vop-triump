package be.ugent.vop.ui.venue;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
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
import be.ugent.vop.backend.loaders.VenueLoader;
import be.ugent.vop.foursquare.FoursquareVenue;
import be.ugent.vop.ui.widget.CustomSwipeRefreshLayout;


public class CheckinFragment extends Fragment implements LoaderManager.LoaderCallbacks<ArrayList<FoursquareVenue>> {
    private static final String TAG = "CheckinFragment";

    protected CheckinFragment mFragment;
    protected CustomSwipeRefreshLayout mSwipeRefreshLayout;
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivity = (BaseActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_checkin, container, false);
        rootView.setTag(TAG);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.venue_list);
        mLoadingMessage = (TextView) rootView.findViewById(R.id.waiting_message);
        mSwipeRefreshLayout = (CustomSwipeRefreshLayout) rootView.findViewById(R.id.fragment_checkin_swipe_refresh);
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
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getLoaderManager().restartLoader(0,null,mFragment);
            }
        });
        mSwipeRefreshLayout.setColorSchemeResources(R.color.theme_primary_dark);

        return rootView;
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

    private void newLocationAvailable(Location newLocation, Date lastUpdated){
        Log.d(TAG, "new Location available in fragment");
        if(newLocation.getAccuracy() < BaseActivity.MIN_LOCATION_ACCURACY){
            if(mLastLocation == null || mLastLocation.distanceTo(newLocation) > 100){
                mLastLocation = newLocation;
                getLoaderManager().restartLoader(0, null, this);
            }
        }
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
        mSwipeRefreshLayout.setRefreshing(false);
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
}
