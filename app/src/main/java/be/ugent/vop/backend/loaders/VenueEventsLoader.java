package be.ugent.vop.backend.loaders;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import be.ugent.vop.backend.BackendAPI;
import be.ugent.vop.backend.myApi.model.EventBean;
import be.ugent.vop.backend.myApi.model.EventRewardBean;
import be.ugent.vop.foursquare.FoursquareAPI;
import be.ugent.vop.foursquare.FoursquareVenue;

public class VenueEventsLoader extends AsyncTaskLoader<HashMap<EventBean,FoursquareVenue>> {
    private Context context;
    private final String venueId;

    public VenueEventsLoader(Context context, String venueId) {
        super(context);
        this.venueId = venueId;
        this.context = context.getApplicationContext();
    }

    /**
     * This is where the bulk of our work is done.  This function is
     * called in a background thread and should generate a new set of
     * data to be published by the loader.
     */
    @Override
    public HashMap<EventBean,FoursquareVenue> loadInBackground() {
        Log.d("EventLoader", "");
        HashMap<EventBean,FoursquareVenue> event_venue = null;
        List<EventBean> mEvents;
        try{
            mEvents = (BackendAPI.get(context).getEventsForVenue(this.venueId));
            event_venue = new HashMap<>();
            if(mEvents!=null) {
                for (int j = 0; j < mEvents.size(); j++) {
                    event_venue.put(mEvents.get(j), FoursquareAPI.get(context).getVenueInfo(mEvents.get(j).getVenueId()));
                }
            }
        } catch(IOException e){
            Log.d("EventLoader", e.getMessage());
        }
        // Done!
        return event_venue;
    }

    /**
     * Called when there is new data to deliver to the client.  The
     * super class will take care of delivering it; the implementation
     * here just adds a little more logic.
     */
    @Override
    public void deliverResult(HashMap<EventBean,FoursquareVenue> response) {
        if (isReset()) {
            onReleaseResources(response);
        }
        if (isStarted()) {
            // If the Loader is currently started, we can immediately
            // deliver its results.
            super.deliverResult(response);
        }
    }

    /**
     * Handles a request to start the Loader.
     */
    @Override protected void onStartLoading() {
        forceLoad();
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
    @Override public void onCanceled(HashMap<EventBean,FoursquareVenue> response) {
        super.onCanceled(response);

        // At this point we can release the resources associated with 'apps'
        // if needed.
        onReleaseResources(response);
    }

    /**
     * Handles a request to completely reset the Loader.
     */
    @Override protected void onReset() {
        super.onReset();

        // Ensure the loader is stopped
        onStopLoading();
    }

    /**
     * Helper function to take care of releasing resources associated
     * with an actively loaded data set.
     */
    protected void onReleaseResources(HashMap<EventBean,FoursquareVenue> response) {}

}
