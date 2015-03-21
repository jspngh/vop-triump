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

import be.ugent.vop.R;
import be.ugent.vop.backend.BackendAPI;
import be.ugent.vop.backend.myApi.model.OverviewBean;

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

        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation == null) {
            mLastLocation = new Location("");
            mLastLocation.setLatitude(51.115789);
            mLastLocation.setLongitude(4.002567);
        }

        new AsyncTask<Location, Void, OverviewBean>() {
            private Location mLocation;

            @Override
            protected OverviewBean doInBackground(Location... params) {
                mLocation = params[0];
                OverviewBean result = null;
                try {
                    result = BackendAPI.get(getActivity()).getOverview(mLocation);
                    Log.d("overview", "" + result);
                } catch (IOException e) {
                    Log.d("overview", "receiving ERROR");
                    e.printStackTrace();
                }
                return result;
            }

            @Override
            protected void onPostExecute(OverviewBean result) {
                Log.d("overview", ""+result);
                super.onPostExecute(result);
                mAdapter = new OverviewAdapter(result);
                mRecyclerView.setAdapter(mAdapter);
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
