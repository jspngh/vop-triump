package be.ugent.vop.ui.event;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

import be.ugent.vop.R;
import be.ugent.vop.backend.loaders.EventLoader;
import be.ugent.vop.backend.myApi.model.EventBean;

/**
 * Created by Lars on 24/03/15.
 */
public class EventFragment extends Fragment{

    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private Context context;
    private static Activity activity;
    private ListView eventListView;
    private TextView noEventTextView;
    private EventAdapter adapter;
    private ArrayList<EventBean> events;
    public EventFragment(){}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_event, container, false);
        eventListView = (ListView) rootView.findViewById(R.id.event_list);
        noEventTextView = (TextView) rootView.findViewById(R.id.noEventTextView);
        context = getActivity();
        activity = getActivity();
        getLoaderManager().initLoader(0,null,mEventLoaderListener);
        return rootView;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    private LoaderManager.LoaderCallbacks<List<EventBean>> mEventLoaderListener
            = new LoaderManager.LoaderCallbacks<List<EventBean>>() {
        @Override
        public Loader<List<EventBean>> onCreateLoader(int id, Bundle args) {
            Log.d("EventFragment", "onCreateLoader");
            EventLoader loader = new EventLoader(context);
            return loader;
        }

        @Override
        public void onLoadFinished(Loader<List<EventBean>> loader, List<EventBean> data) {
            Log.d("EventFragment", "onLoadFinished");
            if (data!=null &&data.size() != 0) {
                noEventTextView.setVisibility(View.GONE);
                eventListView.setVisibility(View.VISIBLE);
                Log.d("EventFragment", "size of data " + data.size());
                events = new ArrayList<EventBean>();
                for (EventBean r : data) events.add(r);
                adapter = new EventAdapter(context, events);
                eventListView.setAdapter(adapter);
            } else {
                noEventTextView.setText(R.string.no_event);
            }

        }


        @Override
        public void onLoaderReset(Loader<List<EventBean>> loader) {

        }
    };
}
