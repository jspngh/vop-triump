package be.ugent.vop.ui.main;

import android.app.Fragment;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.MapFragment;

import java.io.IOException;
import java.util.ArrayList;

import be.ugent.vop.R;
import be.ugent.vop.backend.BackendAPI;
import be.ugent.vop.backend.myApi.model.OverviewBean;
import be.ugent.vop.backend.myApi.model.VenueBean;
import be.ugent.vop.foursquare.FoursquareAPI;
import be.ugent.vop.foursquare.FoursquareVenue;

public class OverviewFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "OverviewFragment";
    private static final int SPAN_COUNT = 2;
    private static final int DATASET_COUNT = 60;

    private GoogleApiClient mGoogleApiClient;
    private MapFragment fragment;
    protected RecyclerView mRecyclerView;
    protected OverviewAdapter mAdapter;
    protected RecyclerView.LayoutManager mLayoutManager;
    protected String[] mDataset;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildGoogleApiClient();
        mGoogleApiClient.connect();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_overview, container, false);
        rootView.setTag(TAG);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview_overview);

        // LinearLayoutManager is used here, this will layout the elements in a similar fashion
        // to the way ListView would layout elements. The RecyclerView.LayoutManager defines how
        // elements are laid out.
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(new OverviewAdapter(null, null));

        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation == null) {
            mLastLocation = new Location("");
            mLastLocation.setLatitude(51.115789);
            mLastLocation.setLongitude(4.002567);
        }

        new AsyncTask<Location, Void, OverviewBean>() {
            private Location mLocation;
            private ArrayList<FoursquareVenue> fsVenues;

            @Override
            protected OverviewBean doInBackground(Location... params) {
                mLocation = params[0];
                fsVenues = FoursquareAPI.get(getActivity()).getNearbyVenues(mLocation);
                ArrayList<String> venues = new ArrayList<>();
                for(FoursquareVenue v : fsVenues){
                    venues.add(v.getId());
                }
                OverviewBean overview = null;
                try {
                    overview = BackendAPI.get(getActivity()).getOverview(venues);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ArrayList<FoursquareVenue> venuesInOverview = new ArrayList<>();
                if(overview != null && overview.getVenues() != null) {
                    for (VenueBean venue : overview.getVenues()) {
                        for(FoursquareVenue fsVenue : fsVenues) {
                            if (venue.getVenueId().equals(fsVenue.getId())) {
                                venuesInOverview.add(fsVenue);
                            }
                        }
                    }
                }
                if(venuesInOverview.size() < 3){
                    for(int i = 0; i < fsVenues.size() && venuesInOverview.size() < 3; i++){
                        if(!venuesInOverview.contains(fsVenues.get(i)))
                            venuesInOverview.add(fsVenues.get(i));
                    }
                }
                fsVenues = venuesInOverview;
                return overview;
            }

            @Override
            protected void onPostExecute(OverviewBean result) {
                Log.d("overview", "" + result);
                if(result != null && (result.getVenues() == null || result.getVenues().size() < 3))
                    mRecyclerView.setAdapter(new OverviewAdapter(result, fsVenues));
                else
                    mRecyclerView.setAdapter(new OverviewAdapter(result, null));
                super.onPostExecute(result);
            }
        }.execute(mLastLocation);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        buildGoogleApiClient();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "Google API onConnected");
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

}
