package be.ugent.vop.backend.loaders;

import android.content.Context;
import android.content.AsyncTaskLoader;
import android.util.Log;

import java.io.IOException;

import be.ugent.vop.backend.BackendAPI;
import be.ugent.vop.backend.myApi.model.VenueBean;

/**
 * Created by vincent on 03/03/15.
 */

/**
 * A custom Loader that loads all of the installed applications.
 */
public class RankingLoader extends AsyncTaskLoader<VenueBean> {
    private final String TAG = "RankingLoader";

    private VenueBean venue;
    private Context context;
    private String venueId;


    public RankingLoader(Context context, String venueId) {
        super(context);
        this.context = context.getApplicationContext();
        this.venueId = venueId;
    }

    /**
     * This is where the bulk of our work is done.  This function is
     * called in a background"éé thread and should generate a new set of
     * data to be published by the loader.
     */
    @Override
    public VenueBean loadInBackground() {
        Log.d(TAG,"loadInBackground");
        VenueBean result = null;

        try{
            result = BackendAPI.get(context).getVenueInfo(venueId);
        } catch(IOException e){
            Log.d("AllGroupsLoader", e.getMessage());
        }

        // Done!
        return result;
    }

    /**
     * Called when there is new data to deliver to the client.  The
     * super class will take care of delivering it; the implementation
     * here just adds a little more logic.
     */
    @Override public void deliverResult(VenueBean venue) {
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
    @Override public void onCanceled(VenueBean venue) {
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