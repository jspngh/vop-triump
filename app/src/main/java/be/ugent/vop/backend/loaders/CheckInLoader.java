package be.ugent.vop.backend.loaders;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;

import java.io.IOException;
import java.util.List;

import be.ugent.vop.R;
import be.ugent.vop.backend.BackendAPI;
import be.ugent.vop.backend.myApi.model.RankingBean;

public class CheckInLoader extends AsyncTaskLoader<AsyncResult<List<RankingBean>>> {

    private static final String TAG = "CheckInLoader";
    private final int minGroupSize;
    private final int maxGroupSize;
    private Context context;
    private String venueId;
    private final String groupType;

    public CheckInLoader(Context context, String venueId, int minGroupSize, int maxGroupSize, String groupType) {
        super(context);
        this.context = context.getApplicationContext();
        this.venueId = venueId;
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
    public AsyncResult<List<RankingBean>> loadInBackground() {
        AsyncResult<List<RankingBean>> result = new AsyncResult<>();
        List<RankingBean> rankings;
        Log.d("Checking In", venueId );
        try{
            rankings = BackendAPI.get(context).checkIn(venueId, minGroupSize, maxGroupSize, groupType);
            result.setData(rankings);
            Log.d("Checked In", "Succes");
        } catch(IOException e){
            result.setException(e);
        }
        // Done!
        return result;
    }

    @Override
    public void deliverResult(AsyncResult<List<RankingBean>> response) {
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
    @Override public void onCanceled(AsyncResult<List<RankingBean>> response) {
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
    protected void onReleaseResources(AsyncResult<List<RankingBean>> response) {}
}
