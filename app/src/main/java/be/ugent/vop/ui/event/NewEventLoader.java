package be.ugent.vop.ui.event;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import com.google.api.client.util.DateTime;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import be.ugent.vop.backend.BackendAPI;
import be.ugent.vop.backend.myApi.model.EventBean;
import be.ugent.vop.backend.myApi.model.GroupBean;

/**
 * Created by vincent on 24/03/15.
 */
public class NewEventLoader extends AsyncTaskLoader<EventBean> {
    private Context context;
    private String fsVenueId;
    private List<Long> groupIds;
    private DateTime start;
    private DateTime end;
    private String description;
    private String reward;
    private int min;
    private int max;
    private boolean verified;


    public NewEventLoader(Context context) {
        super(context);
    }

    public void setParams(String fsVenueId,List<Long> groupIds,DateTime start,DateTime end,String description,String reward,int min, int max,boolean verified){
        this.fsVenueId = fsVenueId;
        this.groupIds = groupIds;
        this.start = start;
        this.end = end;
        this.description = description;
        this.reward = reward;
        this.min = min;
        this.max = max;
        this.verified = verified;
    }

    /**
     * This is where the bulk of our work is done.  This function is
     * called in a background thread and should generate a new set of
     * data to be published by the loader.
     */
    @Override
    public EventBean loadInBackground() {
        EventBean result = null;
        try{
            result = BackendAPI.get(context).createEvent(fsVenueId, groupIds,start, end, description, reward, min, max, verified);
        } catch(IOException e){
            Log.d("CreateGroupLoader", e.getMessage());
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
    public void deliverResult(EventBean response) {
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
    @Override public void onCanceled(EventBean response) {
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
    protected void onReleaseResources(EventBean response) {}

}
