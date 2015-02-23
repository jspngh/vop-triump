package be.ugent.vop.loaders;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import java.io.IOException;

import be.ugent.vop.backend.BackendAPI;
import be.ugent.vop.backend.myApi.MyApi;
import be.ugent.vop.backend.myApi.model.AuthTokenResponse;

/**
 * A custom Loader that loads all of the installed applications.
 */
public class AuthTokenLoader extends AsyncTaskLoader<AuthTokenResponse> {

    AuthTokenResponse mToken;
    private static MyApi myApiService = null;
    private Context context;
    private long userId = 0;
    private String fsToken = null;

    public AuthTokenLoader(Context context, long userId, String fsToken) {
        super(context);
        this.context = context.getApplicationContext();
        this.userId = userId;
        this.fsToken = fsToken;
    }

    /**
     * This is where the bulk of our work is done.  This function is
     * called in a background thread and should generate a new set of
     * data to be published by the loader.
     */
    @Override
    public AuthTokenResponse loadInBackground() {
        AuthTokenResponse result = null;

        try{
            result = BackendAPI.get(context).getAuthToken(userId, fsToken);
        } catch(IOException e){
            Log.d("OpenSessionLoader", e.getMessage());
        }

        // Done!
        return result;
    }

    /**
     * Called when there is new data to deliver to the client.  The
     * super class will take care of delivering it; the implementation
     * here just adds a little more logic.
     */
    @Override public void deliverResult(AuthTokenResponse token) {
        if (isReset()) {
            // An async query came in while the loader is stopped.  We
            // don't need the result.
            if (token != null) {
                onReleaseResources(token);
            }
        }
        AuthTokenResponse oldToken = token;
        mToken = token;

        if (isStarted()) {
            // If the Loader is currently started, we can immediately
            // deliver its results.
            super.deliverResult(token);
        }

        // At this point we can release the resources associated with
        // 'oldApps' if needed; now that the new result is delivered we
        // know that it is no longer in use.
        if (oldToken != null) {
            onReleaseResources(oldToken);
        }
    }

    /**
     * Handles a request to start the Loader.
     */
    @Override protected void onStartLoading() {
        if (mToken != null) {
            // If we currently have a result available, deliver it
            // immediately.
            deliverResult(mToken);
        }

        if (takeContentChanged() || mToken == null) {
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
    @Override public void onCanceled(AuthTokenResponse token) {
        super.onCanceled(token);

        // At this point we can release the resources associated with 'apps'
        // if needed.
        onReleaseResources(token);
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
        if (mToken != null) {
            onReleaseResources(mToken);
            mToken = null;
        }
    }

    /**
     * Helper function to take care of releasing resources associated
     * with an actively loaded data set.
     */
    protected void onReleaseResources(AuthTokenResponse token) {

    }
}