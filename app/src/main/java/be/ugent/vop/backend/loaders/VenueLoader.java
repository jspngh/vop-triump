package be.ugent.vop.backend.loaders;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.location.Location;
import android.util.Log;

import java.util.ArrayList;

import be.ugent.vop.backend.myApi.MyApi;
import be.ugent.vop.foursquare.FoursquareAPI;
import be.ugent.vop.foursquare.FoursquareVenue;

/**
 * A custom Loader that loads all of the installed applications.
 */
public class VenueLoader extends AsyncTaskLoader<ArrayList<FoursquareVenue>> {
    private final String TAG = "VenueLoader";

    private int i = 0;
    ArrayList<FoursquareVenue> mVenueList;
    private static MyApi myApiService = null;
    private Context context;
    private Location location;

    public VenueLoader(Context context, Location loc) {
        super(context);
        this.context = context.getApplicationContext();
        this.location = loc;
    }

    /**
     * This is where the bulk of our work is done.  This function is
     * called in a background thread and should generate a new set of
     * data to be published by the loader.
     */
    @Override
    public ArrayList<FoursquareVenue> loadInBackground() {
        Log.d(TAG,"loadInBackground");
        ArrayList<FoursquareVenue> result = null;
        Log.d("VenueLoader", " "+i );
        result = FoursquareAPI.get(context).getNearbyVenues(location);
        Log.d("VenueLoader", "size of venues found: "+ result.size() );
        // Done!
        //  result = new ArrayList<>();
        // result.add(new FoursquareVenue("666","Naam","","","",30,30));
        return result;
    }

    /**
     * Called when there is new data to deliver to the client.  The
     * super class will take care of delivering it; the implementation
     * here just adds a little more logic.
     */
    @Override public void deliverResult( ArrayList<FoursquareVenue> venueList) {
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