package be.ugent.vop.backend.loaders;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import be.ugent.vop.backend.BackendAPI;
import be.ugent.vop.backend.myApi.model.EventBean;
import be.ugent.vop.backend.myApi.model.EventRewardBean;
import be.ugent.vop.backend.myApi.model.GroupBean;
import be.ugent.vop.backend.myApi.model.RankingBean;
import be.ugent.vop.backend.myApi.model.RankingBeanCollection;
import be.ugent.vop.backend.myApi.model.VenueBean;
import be.ugent.vop.foursquare.FoursquareAPI;
import be.ugent.vop.foursquare.FoursquareVenue;
import be.ugent.vop.ui.reward.RewardListViewFragment;

/**
 * Created by jonas on 3/3/15.
 */
public class EventLoader extends AsyncTaskLoader<EventRewardBean> {

    private Context context;

    public EventLoader(Context context) {
        super(context);
        this.context = context.getApplicationContext();
    }

    /**
     * This is where the bulk of our work is done.  This function is
     * called in a background thread and should generate a new set of
     * data to be published by the loader.
     */
    @Override
    public EventRewardBean loadInBackground() {
        Log.d("EventLoader", "");
        EventRewardBean result = null;
        try{
            result = (BackendAPI.get(context).getEventsForUser());
            HashMap<EventBean,FoursquareVenue> event_venue = new HashMap<>();
            List<EventBean> mRewards = result.getRewards();
            List<EventBean> mEvents = result.getEvents();
            if(mRewards!=null) {
                for (int j = 0; j < mRewards.size(); j++) {
                    event_venue.put(mRewards.get(j),FoursquareAPI.get(context).getVenueInfo(mRewards.get(j).getVenueId()));
                }
            }
            if(mEvents!=null) {
                for (int j = 0; j < mEvents.size(); j++) {
                    event_venue.put(mEvents.get(j),FoursquareAPI.get(context).getVenueInfo(mEvents.get(j).getVenueId()));
                }
            }
        } catch(IOException e){
            Log.d("EventLoader", e.getMessage());
        }
        // Done!
        return result;
    }

    /**
     * Called when there is new data to deliver to the client.  The
     * super class will take care of delivering it; the implementation
     * here just adds a little more logic.
     */
    @Override
    public void deliverResult(EventRewardBean response) {
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
    @Override public void onCanceled(EventRewardBean response) {
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
    protected void onReleaseResources(EventRewardBean response) {}

}