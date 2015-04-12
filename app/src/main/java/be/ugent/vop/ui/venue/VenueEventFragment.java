package be.ugent.vop.ui.venue;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Loader;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import be.ugent.vop.R;
import be.ugent.vop.backend.loaders.NewEventLoader;
import be.ugent.vop.backend.loaders.VenueEventsLoader;
import be.ugent.vop.backend.myApi.model.EventBean;
import be.ugent.vop.ui.event.EventAdapter;

public class VenueEventFragment extends Fragment implements VenueActivity.VenueActivityCallback {
    private static final String TAG = "VenueEventFragment";
    private String fsVenueId;
    private Activity context;

    private ArrayList<EventBean> eventsForVenue;
    private EventAdapter adapterEvents;
    private ListView eventListView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_venue_event, container, false);
        eventListView = (ListView) rootView.findViewById(R.id.ListViewEvents);

        context = getActivity();

        if(getArguments().containsKey(VenueActivity.VENUE_ID))
            fsVenueId = getArguments().getString(VenueActivity.VENUE_ID);

        getLoaderManager().initLoader(0,null,eventForVenueLoader);

        return rootView;
    }

    /**
     *
     * new event loader
     */


    private LoaderManager.LoaderCallbacks<List<EventBean>> eventForVenueLoader
            = new LoaderManager.LoaderCallbacks<List<EventBean>>() {

        @Override
        public void onLoadFinished(Loader<List<EventBean>> loader, List<EventBean> events) {
            Log.d(TAG, "onLoadFinished, VenueEventLoader");


            if (events != null && events.size()>0){
                    eventListView.setVisibility(View.VISIBLE);
                    Log.d(TAG, "size of events " + events.size());
                    eventsForVenue = new ArrayList<EventBean>();
                    for (EventBean r : events) eventsForVenue.add(r);
                    adapterEvents = new EventAdapter(context, eventsForVenue);
                    eventListView.setAdapter(adapterEvents);
                }
        }

        @Override
        public Loader<List<EventBean>> onCreateLoader(int id, Bundle args) {
            Log.d(TAG, "onCreateLoader");
            VenueEventsLoader loader = new VenueEventsLoader(context,fsVenueId);
            return loader;
        }

        @Override
        public void onLoaderReset(Loader<List<EventBean>> loader) {
            //rankingListView.setAdapter(null);
        }
    };

    @Override
    public void setColorPalette(Palette p) {

    }
}
