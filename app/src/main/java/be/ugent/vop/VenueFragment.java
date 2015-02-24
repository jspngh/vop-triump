package be.ugent.vop;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import be.ugent.vop.foursquare.FoursquareVenue;
import be.ugent.vop.loaders.VenueLoader;


public class VenueFragment extends ListFragment implements
        LoaderManager.LoaderCallbacks<ArrayList<FoursquareVenue>> {
    private static final String TAG = "VenueFragment";
    private static final boolean DEBUG = true;

    private ListView venueListView;
    private ArrayAdapter arrayAdapter;
    private String[] venueArray;

    // The Loader's id (this id is specific to the ListFragment's LoaderManager)
    // just 1 loader so ID not important...
    private static final int LOADER_ID = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_group, container, false);
        venueListView = (ListView) rootView.findViewById(R.id.groups_list);

        getLoaderManager().initLoader(0, null, this);

        return rootView;
    }


    /**********************/
    /** LOADER CALLBACKS **/
    /**********************/

    @Override
    public Loader<ArrayList<FoursquareVenue>> onCreateLoader(int id, Bundle args) {
        if (DEBUG) Log.i(TAG, "+++ onCreateLoader() called! +++");
        return new VenueLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<FoursquareVenue>> loader, ArrayList<FoursquareVenue> data) {
        if (DEBUG) Log.i(TAG, "+++ onLoadFinished() called! +++");
        Log.d("","Number of groups: " + data.size());
        venueArray = new String[data.size()];

        for(int i=0;i<data.size();i++){
            venueArray[i]=data.get(i).name;
        }

        arrayAdapter = new ArrayAdapter(this.getActivity(), android.R.layout.simple_list_item_1, venueArray);
        venueListView.setAdapter(arrayAdapter);
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<FoursquareVenue>> loader) {
        if (DEBUG) Log.i(TAG, "+++ onLoadReset() called! +++");
       // arrayAdapter.setData(null);
    }

}
