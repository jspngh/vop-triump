package be.ugent.vop.loaders;

/**
 * Created by siebe on 23/02/15.
 */

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import java.io.IOException;

import be.ugent.vop.backend.BackendAPI;
import be.ugent.vop.backend.myApi.MyApi;
import be.ugent.vop.backend.myApi.model.AllGroupsBean;

/**
 * A custom Loader that loads all of the installed applications.
 */
public class AllGroupsLoader extends AsyncTaskLoader<AllGroupsBean> {

    AllGroupsBean mAllGroupsBean;
    private static MyApi myApiService = null;
    private Context context;

    public AllGroupsLoader(Context context) {
        super(context);
        this.context = context.getApplicationContext();
    }

    /**
     * This is where the bulk of our work is done.  This function is
     * called in a background thread and should generate a new set of
     * data to be published by the loader.
     */
    @Override public AllGroupsBean loadInBackground() {
        AllGroupsBean result = null;

        try{
            result = BackendAPI.get(context).getAllGroups();
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
    @Override public void deliverResult(AllGroupsBean allGroupsBean) {
        if (isReset()) {
            // An async query came in while the loader is stopped.  We
            // don't need the result.
            if (allGroupsBean != null) {
                onReleaseResources(allGroupsBean);
            }
        }
        AllGroupsBean oldAllGroupsBean = allGroupsBean;
        mAllGroupsBean = allGroupsBean;

        if (isStarted()) {
            // If the Loader is currently started, we can immediately
            // deliver its results.
            super.deliverResult(allGroupsBean);
        }

        // At this point we can release the resources associated with
        // 'oldApps' if needed; now that the new result is delivered we
        // know that it is no longer in use.
        if (oldAllGroupsBean != null) {
            onReleaseResources(oldAllGroupsBean);
        }
    }

    /**
     * Handles a request to start the Loader.
     */
    @Override protected void onStartLoading() {
        if (mAllGroupsBean != null) {
            // If we currently have a result available, deliver it
            // immediately.
            deliverResult(mAllGroupsBean);
        }

        if (takeContentChanged() || mAllGroupsBean == null) {
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
    @Override public void onCanceled(AllGroupsBean allGroupsBean) {
        super.onCanceled(allGroupsBean);

        // At this point we can release the resources associated with 'apps'
        // if needed.
        onReleaseResources(allGroupsBean);
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
        if (mAllGroupsBean != null) {
            onReleaseResources(mAllGroupsBean);
            mAllGroupsBean = null;
        }
    }

    /**
     * Helper function to take care of releasing resources associated
     * with an actively loaded data set.
     */
    protected void onReleaseResources(AllGroupsBean allGroupsBean) {
        // For a simple List<> there is nothing to do.  For something
        // like a Cursor, we would close it here.
    }
}