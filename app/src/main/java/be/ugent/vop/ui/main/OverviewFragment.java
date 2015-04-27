package be.ugent.vop.ui.main;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Loader;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Date;

import be.ugent.vop.BaseActivity;
import be.ugent.vop.R;
import be.ugent.vop.backend.loaders.OverviewLoader;
import be.ugent.vop.ui.widget.CustomSwipeRefreshLayout;

public class OverviewFragment extends Fragment implements LoaderManager.LoaderCallbacks<OverviewAdapter> {
    private static final String TAG = "OverviewFragment";

    private Location mLastLocation;
    private MainActivity mActivity;
    private LoaderManager.LoaderCallbacks<OverviewAdapter> mFragment;
    protected RecyclerView mRecyclerView;
    protected RecyclerView.LayoutManager mLayoutManager;
    protected SwipeRefreshLayout mSwipeRefreshLayout;


    private BaseActivity.LocationUpdateListener mListener = new BaseActivity.LocationUpdateListener() {
        @Override
        public void locationUpdated(Location newLocation, Date lastUpdated) {
           newLocationAvailable(newLocation, lastUpdated);
        }
    };

    public OverviewFragment(){
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_overview, container, false);

        rootView.setTag(TAG);

        mActivity = (MainActivity) getActivity();
        mFragment = this;
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.overview_refresh);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview_overview);

        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        boolean displayWelcome = mActivity.displayWelcome;
        mRecyclerView.setAdapter(new OverviewAdapter(null, null, mActivity, displayWelcome));
        getLoaderManager().initLoader(0, null, this);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getLoaderManager().restartLoader(1, null, mFragment);
            }
        });
        mSwipeRefreshLayout.setColorSchemeResources(R.color.theme_primary_dark);

        return rootView;
    }

    @Override
    public void onResume(){
        super.onResume();
        mActivity.addLocationUpdateListener(mListener);
    }

    public void onPause(){
        super.onPause();
        mActivity.removeLocationUpdateListener(mListener);
    }

    private void newLocationAvailable(Location newLocation, Date lastUpdated){
        Log.d(TAG, "new Location available in fragment");
        if(newLocation.getAccuracy() < BaseActivity.MIN_LOCATION_ACCURACY){
            mLastLocation = newLocation;
            getLoaderManager().restartLoader(0, null, mFragment);
        }
    }

    @Override
    public Loader<OverviewAdapter> onCreateLoader(int i, Bundle bundle) {
        return (i==0 ?  new OverviewLoader(mActivity, mLastLocation, false) : new OverviewLoader(mActivity, mLastLocation, true));
    }

    @Override
    public void onLoadFinished(Loader<OverviewAdapter> overviewLoader, final OverviewAdapter overviewAdapter) {
        mSwipeRefreshLayout.setRefreshing(false);
        mRecyclerView.setAdapter(overviewAdapter);
    }

    @Override
    public void onLoaderReset(Loader<OverviewAdapter> overviewLoader) {

    }

}
