package be.ugent.vop.backend.loaders;

import android.content.Context;
import android.content.AsyncTaskLoader;
import android.util.Log;

import java.io.IOException;

import be.ugent.vop.backend.BackendAPI;
import be.ugent.vop.backend.myApi.model.CloseSessionResponse;
/**
 * Created by jonas on 27-2-2015.
 */
public class EndSessionLoader extends  AsyncTaskLoader<CloseSessionResponse> {

    private Context context;

    public EndSessionLoader(Context context) {
        super(context);
        this.context = context.getApplicationContext();
    }

    /**
     * This is where the bulk of our work is done.  This function is
     * called in a background thread and should generate a new set of
     * data to be published by the loader.
     */
    @Override
    public CloseSessionResponse loadInBackground() {
        CloseSessionResponse result = null;

        try{
            result = BackendAPI.get(context).close();
            Log.d("Endsessionloader", "Connection closed");
            Log.d("Endsessionloader", result.getMessage());
        } catch(IOException e){
            Log.d("Endsessionloader", e.getMessage());
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
    public void deliverResult(CloseSessionResponse response) {
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
    @Override public void onCanceled(CloseSessionResponse response) {
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
    protected void onReleaseResources(CloseSessionResponse response) {}

}
