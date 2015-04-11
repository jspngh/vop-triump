package be.ugent.vop.ui.main;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Loader;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import be.ugent.vop.R;
import be.ugent.vop.backend.loaders.OverviewLoader;

public class OverviewFragment extends Fragment implements LoaderManager.LoaderCallbacks<OverviewAdapter> {
    private static final String TAG = "OverviewFragment";

    private Location mLastLocation;
    private Activity context;
    protected RecyclerView mRecyclerView;
    protected RecyclerView.LayoutManager mLayoutManager;

    public OverviewFragment(){
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_overview, container, false);

        rootView.setTag(TAG);
        SharedPreferences prefs = getActivity().getSharedPreferences(getString(R.string.sharedprefs), MainActivity.MODE_PRIVATE);
        context = getActivity();

        mLastLocation = new Location("");
        mLastLocation.setLatitude(prefs.getFloat(getString(R.string.locationLatitude), (float)51.046127));
        mLastLocation.setLongitude(prefs.getFloat(getString(R.string.locationLongitude), (float)3.727251));

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview_overview);

        // LinearLayoutManager is used here, this will layout the elements in a similar fashion
        // to the way ListView would layout elements. The RecyclerView.LayoutManager defines how
        // elements are laid out.
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        boolean displayWelcome = ((MainActivity)context).displayWelcome;
        if(displayWelcome){
            mRecyclerView.setAdapter(new OverviewAdapter(null, null, context, displayWelcome));
        } else{
            mRecyclerView.setAdapter(new OverviewAdapter(null, null, context, displayWelcome));
            getLoaderManager().initLoader(0, null, this);
        }
        return rootView;
    }

    @Override
    public Loader<OverviewAdapter> onCreateLoader(int i, Bundle bundle) {
        return new OverviewLoader(context, mLastLocation);
    }

    @Override
    public void onLoadFinished(Loader<OverviewAdapter> overviewLoader, final OverviewAdapter overviewAdapter) {
        mRecyclerView.setAdapter(overviewAdapter);
    }
    @Override
    public void onLoaderReset(Loader<OverviewAdapter> overviewLoader) {
    }

/*    public void setOverview(){

        if(mLocationService != null) {
            location = mLocationService.getLocation();
        }
        if(location == null){
            location = mLastLocation;
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
                mRecyclerView.setAdapter(new OverviewAdapter(result, fsVenues, getActivity()));
                super.onPostExecute(result);
            }
        }.execute(location);
    }*/
}
