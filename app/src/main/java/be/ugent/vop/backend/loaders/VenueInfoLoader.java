package be.ugent.vop.backend.loaders;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;

import java.util.LinkedList;

import be.ugent.vop.backend.myApi.model.EventBean;
import be.ugent.vop.foursquare.FoursquareAPI;
import be.ugent.vop.foursquare.FoursquareVenue;
import be.ugent.vop.ui.reward.RewardListViewFragment;

/**
 * A custom Loader that loads all of the installed applications.
 */
public class VenueInfoLoader extends AsyncTaskLoader<FoursquareVenue> {
    private final String TAG = "VenueInfoLoader";

    private FoursquareVenue venue;
    private final Context context;
    private final String fsVenueId;

    public VenueInfoLoader(Context context, String fsVenueId) {
        super(context);
        this.context = context.getApplicationContext();
        this.fsVenueId = fsVenueId;
    }

    /**
     * This is where the bulk of our work is done.  This function is
     * called in a background"éé thread and should generate a new set of
     * data to be published by the loader.
     */
    @Override
    public FoursquareVenue loadInBackground() {
        Log.d(TAG, "loadInBackground");

        FoursquareVenue result = FoursquareAPI.get(context).getVenueInfo(fsVenueId);

        // Done!
        return result;
    }

    /**
     * Called when there is new data to deliver to the client.  The
     * super class will take care of delivering it; the implementation
     * here just adds a little more logic.
     */
    @Override public void deliverResult(FoursquareVenue venue) {
        this.venue = venue;
        if (isStarted()) {
            // If the Loader is currently started, we can immediately
            // deliver its results.
            super.deliverResult(venue);
        }
    }

    /**
     * Handles a request to start the Loader.
     */
    @Override protected void onStartLoading() {
        if (venue != null) {
            // If we currently have a result available, deliver it
            // immediately.
            deliverResult(venue);
        }

        if (takeContentChanged() || venue == null) {
            // If the data has changed since the last time it was loaded
            // or is not currently available, start a load.
            Log.d("RankingLoader", "testing forceLoad");
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
    @Override public void onCanceled(FoursquareVenue venue) {
        super.onCanceled(venue);
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
        if (venue != null) {
            venue = null;
        }
    }

}