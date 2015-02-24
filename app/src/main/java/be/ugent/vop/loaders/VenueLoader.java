package be.ugent.vop.loaders;


import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import java.util.ArrayList;

import be.ugent.vop.foursquare.FoursquareAPI;
import be.ugent.vop.foursquare.FoursquareVenue;


/**
 * Created by vincent on 24/02/15.
 */

public class VenueLoader extends AsyncTaskLoader<ArrayList<FoursquareVenue>> {

    ArrayList<FoursquareVenue> mVenueList;

    public VenueLoader(Context context) {
        super(context);
    }

    /**
     * This is where the bulk of our work is done.  This function is
     * called in a background thread and should generate a new set of
     * data to be published by the loader.
     */
    @Override public  ArrayList<FoursquareVenue> loadInBackground() {
        ArrayList<FoursquareVenue> venueList;
        // nog kunnen doorgeven van argumenten longitude en latitude!
        venueList = FoursquareAPI.get(getContext()).getNearby(50.0,4.0);

        return venueList;
    }

    /**
     * Called when there is new data to deliver to the client.  The
     * super class will take care of delivering it; the implementation
     * here just adds a little more logic.
     */
    @Override public void deliverResult(ArrayList<FoursquareVenue> venueList) {
        if (isReset()) {
            // An async query came in while the loader is stopped.  We
            // don't need the result.
            if (venueList != null) {
                //  onReleaseResources(venueList);
            }
        }
        ArrayList<FoursquareVenue> oldVenueList = venueList;
        mVenueList = venueList;

        if (isStarted()) {
            // If the Loader is currently started, we can immediately
            // deliver its results.
            super.deliverResult(venueList);
        }

        // At this point we can release the resources associated with
        // 'oldApps' if needed; now that the new result is delivered we
        // know that it is no longer in use.
        if (oldVenueList != null) {
            // onReleaseResources(oldVenueList);
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
    @Override public void onCanceled(ArrayList<FoursquareVenue> venueList) {
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



}
