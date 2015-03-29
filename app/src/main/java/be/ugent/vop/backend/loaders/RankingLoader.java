package be.ugent.vop.backend.loaders;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.util.List;

import be.ugent.vop.backend.BackendAPI;
import be.ugent.vop.backend.myApi.model.RankingBean;

/**
 * Created by vincent on 03/03/15.
 */

/**
 * A custom Loader that loads all of the installed applications.
 */
public class RankingLoader extends AsyncTaskLoader<List<RankingBean>>  {
    private final String TAG = "RankingLoader";

    private List<RankingBean> rankings;
    private Context context;
    private String VenueId;
    private static int min;
    private static int max;


    public RankingLoader(Context context, String venueId,int min, int max) {
        super(context);
        this.context = context.getApplicationContext();
        this.VenueId = venueId;
        this.min=min;
        this.max=max;

    }

    public static void setMin(int m){
        min = m;
    }

    public static void setMax(int m){
        max = m;
    }

    /**
     * This is where the bulk of our work is done.  This function is
     * called in a background"éé thread and should generate a new set of
     * data to be published by the loader.
     */
    @Override
    public List<RankingBean> loadInBackground() {
        Log.d(TAG,"loadInBackground");
        List<RankingBean> result = null;

        try{
            Log.d(TAG, "looking up ranking w/ specs: "+min+", "+max);
            result = BackendAPI.get(context).getRankings(VenueId,min,max);
        } catch(IOException e){
            Log.d(TAG, e.getMessage());
        }
        // Done!
        return result;
    }

    /**
     * Called when there is new data to deliver to the client.  The
     * super class will take care of delivering it; the implementation
     * here just adds a little more logic.
     */
    @Override public void deliverResult(List<RankingBean> rankings) {
        this.rankings = rankings;
        if (isStarted()) {
            // If the Loader is currently started, we can immediately
            // deliver its results.
            super.deliverResult(rankings);
        }
    }

    /**
     * Handles a request to start the Loader.
     */
    @Override protected void onStartLoading() {
        if (rankings != null) {
            // If we currently have a result available, deliver it
            // immediately.
            deliverResult(rankings);
        }

        if (takeContentChanged() || rankings == null) {
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
    @Override public void onCanceled(List<RankingBean> rankings) {
        super.onCanceled(rankings);
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
        if (rankings != null) {
            rankings = null;
        }
    }


}