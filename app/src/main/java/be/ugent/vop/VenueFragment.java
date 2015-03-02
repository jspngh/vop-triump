package be.ugent.vop;

import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import be.ugent.vop.foursquare.FoursquareAPI;
import be.ugent.vop.foursquare.FoursquareVenue;
import be.ugent.vop.loaders.VenueLoader;
import be.ugent.vop.ui.main.MainActivity;


public class VenueFragment extends Fragment implements LoaderManager.LoaderCallbacks<ArrayList<FoursquareVenue>> {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";
    private ListView venueListView;
    private ArrayAdapter arrayAdapter;
    private String[] venueArray;


    private MainActivity mainActivity = null;

    public VenueFragment()
    {

    }

    public static VenueFragment newInstance(int sectionNumber) {
        VenueFragment fragment = new VenueFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_group, container, false);
        venueListView = (ListView) rootView.findViewById(R.id.group_list_view);

        getLoaderManager().initLoader(0, null, this);

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // ((MainActivity) activity).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));
    }

    @Override
    public Loader<ArrayList<FoursquareVenue>> onCreateLoader(int id, Bundle bundle) {
        VenueLoader loader = new VenueLoader(getActivity(), null);
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<FoursquareVenue>> objectLoader, ArrayList<FoursquareVenue> venueList) {
        Log.d("","Number of venues: " + venueList.size());

        venueArray = new String[venueList.size()];
        for(int i = 0; i < venueList.size(); i++){
            venueArray[i] = venueList.get(i).getName();
        }

        Log.d("Venue ID: ", venueList.get(0).getId());

        arrayAdapter = new ArrayAdapter(this.getActivity(), android.R.layout.simple_list_item_1, venueArray);
        venueListView.setAdapter(arrayAdapter);
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<FoursquareVenue>> objectLoader) {

    }

}
