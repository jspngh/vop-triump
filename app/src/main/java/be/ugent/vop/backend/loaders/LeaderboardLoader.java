package be.ugent.vop.backend.loaders;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.util.List;

import be.ugent.vop.backend.BackendAPI;
import be.ugent.vop.backend.myApi.model.RankingBean;


public class LeaderboardLoader extends AsyncTaskLoader<List<RankingBean>> {

    private final Context context;
    private final int minGroupSize;
    private final int maxGroupSize;
    private final String groupType;

    public LeaderboardLoader(Context context, int minGroupSize, int maxGroupSize, String groupType) {
        super(context);
        this.context = context.getApplicationContext();
        this.minGroupSize = minGroupSize;
        this.maxGroupSize = maxGroupSize;
        this.groupType=groupType;
    }

    /**
     * This is where the bulk of our work is done.  This function is
     * called in a background thread and should generate a new set of
     * data to be published by the loader.
     */
    @Override
    public List<RankingBean> loadInBackground() {
        Log.d("LeaderboardLoader", "");
        List<RankingBean> result = null;
        try{
            result = (BackendAPI.get(context).getLeaderboard(minGroupSize, maxGroupSize, groupType));
        } catch(IOException e){
            Log.d("LeaderboardLoader", e.getMessage());
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
    public void deliverResult(List<RankingBean> response) {
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
    @Override public void onCanceled(List<RankingBean> response) {
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
    protected void onReleaseResources(List<RankingBean> response) {}

}