package be.ugent.vop.backend.loaders;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.location.Location;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;

import be.ugent.vop.Event;
import be.ugent.vop.EventBroker;
import be.ugent.vop.EventListener;
import be.ugent.vop.backend.BackendAPI;
import be.ugent.vop.backend.myApi.MyApi;
import be.ugent.vop.backend.myApi.model.VenueBean;
import be.ugent.vop.foursquare.FoursquareAPI;
import be.ugent.vop.foursquare.FoursquareVenue;

/**
 * A custom Loader that loads all of the installed applications.
 */
public class VenueLoader extends AsyncTaskLoader<ArrayList<VenueBean>> implements EventListener {
    private final String TAG = "VenueLoader";

    private int i = 0;
    ArrayList<VenueBean> mVenueList;
    private static MyApi myApiService = null;
    private Context context;
    private Location location;

    public VenueLoader(Context context, Location loc) {
        super(context);
        this.context = context.getApplicationContext();
        this.location = loc;
        EventBroker.get().addListener(this);
    }

    /**
     * This is where the bulk of our work is done.  This function is
     * called in a background thread and should generate a new set of
     * data to be published by the loader.
     */
    @Override
    public ArrayList<VenueBean> loadInBackground() {
        Log.d(TAG,"loadInBackground");
        ArrayList<VenueBean> result = new ArrayList<VenueBean>();
        Log.d("VenueLoader", " "+i );
        try {

            result = (ArrayList<VenueBean>) BackendAPI.get(context).getNearbyVenues(location).getVenues(); // TODO: is cast here OK?
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(result==null) return new ArrayList<VenueBean>();
        return result;
    }

    /**
     * Called when there is new data to deliver to the client.  The
     * super class will take care of delivering it; the implementation
     * here just adds a little more logic.
     */
    @Override public void deliverResult( ArrayList<VenueBean> venueList) {
        mVenueList = venueList;
        if (isStarted()) {
            // If the Loader is currently started, we can immediately
            // deliver its results.
            super.deliverResult(venueList);
        }
    }

    /**
     * Handles a request to start the Loader.
     */
    @Override protected void onStartLoading() {
        if (mVenueList != null) {
            // If we currently have a result available, deliver it
            // immediately.
            deliverResult(mVenueList);
        }

        if (takeContentChanged() || mVenueList == null) {
            // If the data has changed since the last time it was loaded
            // or is not currently available, start a load.
            forceLoad();
        }
    }

    /**
     * Handles a request to stop the Loader.
     */
    @Override protected void onStopLoading() {
        // Attempt to cancel the current load task if possible.
        cancelLoad();
    }

    /**
     * Handles a request to cancel a load.
     */
    @Override public void onCanceled(ArrayList<VenueBean> venueList) {
        super.onCanceled(venueList);
    }

    /**
     * Handles a request to completely reset the Loader.
     */
    @Override protected void onReset() {
        super.onReset();

        // Ensure the loader is stopped
        onStopLoading();

        // At this point we can release the resources associated with 'apps'
        // if needed.
        if (mVenueList != null) {
            mVenueList = null;
        }
    }

    /**************************************
     *
     * Override methodes interface EventListener
     *
     ***************************************/

    @Override
    public void handleEvent(Event e){
        Log.d(TAG,"handleEvent");
        onContentChanged();
    }
}