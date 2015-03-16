package be.ugent.vop.ui.venue;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;

import be.ugent.vop.R;
import be.ugent.vop.backend.myApi.model.VenueBean;
import be.ugent.vop.backend.loaders.VenueLoader;
import be.ugent.vop.ui.venue.VenueActivity;
import be.ugent.vop.ui.venue.VenueListAdapter;

/**
 * Created by siebe on 28/02/15.
 */
public class CheckinFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LoaderManager.LoaderCallbacks<ArrayList<VenueBean>> {
    private static final String TAG = "CheckinFragment";

    protected RecyclerView mRecyclerView;
    protected VenueListAdapter mAdapter;
    private TextView mLoadingMessage;
    protected RecyclerView.LayoutManager mLayoutManager;
    protected String[] mDataset;
    private GoogleMap map;
    private MapFragment fragment;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //buildGoogleApiClient();
    }

    private void buildGoogleApiClient() {
        Log.d(TAG, "buildGoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_checkin, container, false);
        rootView.setTag(TAG);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.venue_list);
        mLoadingMessage = (TextView) rootView.findViewById(R.id.waiting_message);

        // LinearLayoutManager is used here, this will layout the elements in a similar fashion
        // to the way ListView would layout elements. The RecyclerView.LayoutManager defines how
        // elements are laid out.
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new VenueListAdapter();
        // Set CustomAdapter as the adapter for RecyclerView.
        mRecyclerView.setAdapter(null);
        // END_INCLUDE(initializeRecyclerView);

        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            int state;

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                state = newState;
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy){
                //if(state == RecyclerView.SCROLL_STATE_DRAGGING){
                Log.d(TAG, "Vertical scroll: " + dy);
                    mRecyclerView.animate().translationY(dy);
                //}
            }
        });


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
        if (fragment == null) {
            fragment = getMapFragment();
        }
        buildGoogleApiClient();

        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "Google API onConnected");

        // Indien er geen locatie gevonden is zal er nooit een loader aangemaakt worden,
        // hierdoor zullen er ook geen venues geladen worden wanneer er later wel een locatie gevonden wordt.

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public Loader<ArrayList<VenueBean>> onCreateLoader(int i, Bundle bundle) {
        Log.d(TAG, "onCreateLoader");
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            return new VenueLoader(getActivity(), mLastLocation);
        } else {
            Location defaultLocation = new Location("");
            defaultLocation.setLatitude(50);
            defaultLocation.setLongitude(4);
            return new VenueLoader(getActivity(),defaultLocation );
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
    public void onLoadFinished(Loader<ArrayList<VenueBean>> arrayListLoader, final ArrayList<VenueBean> venues) {
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
                    final HashMap<Marker, Long> markerVenue = new HashMap<Marker, Long>();

                    map = googleMap;

                    for(VenueBean v : venues){
                        Marker m = map.addMarker(new MarkerOptions().position(new LatLng(v.getLatitude(), v.getLongitude()))
                                        .title(v.getDescription()));

                        markerVenue.put(m, v.getVenueId());
                    }

                    map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                        @Override
                        public void onInfoWindowClick(Marker marker) {
                            long venueId = markerVenue.get(marker);

                            VenueBean v = null;

                            for(VenueBean vb : venues){
                                if(vb.getVenueId() == venueId){
                                    v = vb;
                                    break;
                                }
                            }

                            if(v != null){
                                Intent intent = new Intent(getActivity(), VenueActivity.class);
                                intent.putExtra(VenueActivity.VENUE_ID, v.getVenueId());

                                getActivity().startActivity(intent);
                            }

                        }
                    });

                    // Zoom in, animating the camera.
                    map.animateCamera(CameraUpdateFactory.zoomTo(17), 2000, null);
                }
            });
        }
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<VenueBean>> arrayListLoader) {
        mRecyclerView.setAdapter(null);
    }
}
