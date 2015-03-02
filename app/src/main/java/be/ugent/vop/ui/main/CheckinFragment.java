package be.ugent.vop.ui.main;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.content.Loader;
import android.location.Location;
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

import be.ugent.vop.R;
import be.ugent.vop.VenueFragment;
import be.ugent.vop.foursquare.FoursquareVenue;
import be.ugent.vop.loaders.VenueLoader;


/**
 * Created by siebe on 28/02/15.
 */
public class CheckinFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LoaderManager.LoaderCallbacks<ArrayList<FoursquareVenue>> {
    private static final String TAG = "CheckinFragment";
    private static final int SPAN_COUNT = 2;
    private static final int DATASET_COUNT = 60;

    static final LatLng HAMBURG = new LatLng(53.558, 9.927);
    static final LatLng KIEL = new LatLng(53.551, 9.993);


    protected RecyclerView mRecyclerView;
    protected VenueListAdapter mAdapter;
    private TextView mLoadingMessage;
    protected RecyclerView.LayoutManager mLayoutManager;
    protected String[] mDataset;
    private GoogleMap map;
    private MapFragment fragment;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    private VenueFragment venueFragment;

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
//        mLayoutManager = new LinearLayoutManager(getActivity());
//        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new VenueListAdapter();
        // Set CustomAdapter as the adapter for RecyclerView.
        mRecyclerView.setAdapter(null);

        // END_INCLUDE(initializeRecyclerView);


        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Needs to call MapsInitializer before doing any CameraUpdateFactory calls
        MapsInitializer.initialize(getActivity());

        FragmentManager fm = getFragmentManager();
        fragment = (MapFragment) fm.findFragmentById(R.id.map);
        if (fragment == null) {
            fragment = MapFragment.newInstance();
            //fm.beginTransaction().replace(R.id.map, fragment).commit();
        }
        buildGoogleApiClient();

        mGoogleApiClient.connect();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "Google API onConnected");
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            getLoaderManager().initLoader(0, null, this);
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public Loader<ArrayList<FoursquareVenue>> onCreateLoader(int i, Bundle bundle) {
        Loader loader = new VenueLoader(getActivity(), mLastLocation);
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<FoursquareVenue>> arrayListLoader, ArrayList<FoursquareVenue> venues) {
        Log.d(TAG, "onLoadFinished");
        mAdapter.setVenues(venues);
        mRecyclerView.setAdapter(mAdapter);

        mLoadingMessage.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);

        FragmentManager fm = getFragmentManager();
        if (fragment == null) {
            fragment = MapFragment.newInstance();
            fm.beginTransaction().replace(R.id.map, fragment).commit();
        }

        if (map == null) {
            fragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    map = googleMap;
                    Marker hamburg = map.addMarker(new MarkerOptions().position(HAMBURG)
                            .title("Hamburg"));

                    // Move the camera instantly to hamburg with a zoom of 15.
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(HAMBURG, 15));

                    // Zoom in, animating the camera.
                    map.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
                }
            });
        }
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<FoursquareVenue>> arrayListLoader) {
        mRecyclerView.setAdapter(null);
    }
}
