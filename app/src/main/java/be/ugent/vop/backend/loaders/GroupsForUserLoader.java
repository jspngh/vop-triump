package be.ugent.vop.backend.loaders;

import android.content.Context;
import android.content.AsyncTaskLoader;
import android.util.Log;

import java.io.IOException;

import be.ugent.vop.backend.BackendAPI;
import be.ugent.vop.backend.myApi.model.GroupsBean;

/**
 * Created by vincent on 22/03/15.
 */
public class GroupsForUserLoader extends AsyncTaskLoader<GroupsBean> {
    private final String TAG = "GroupsForUserLoader";


    GroupsBean mAllGroupsBean;
    private Context context;

    public GroupsForUserLoader(Context context) {
        super(context);
        this.context = context.getApplicationContext();
    }

    /**
     * This is where the bulk of our work is done.  This function is
     * called in a background thread and should generate a new set of
     * data to be published by the loader.
     */
    @Override public GroupsBean loadInBackground() {
        GroupsBean result = null;
        try{
            result = BackendAPI.get(context).getGroupsForUser();
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
    @Override public void deliverResult(GroupsBean allGroupsBean) {
        if (isReset()) {
            // An async query came in while the loader is stopped.  We
            // don't need the result.
            if (allGroupsBean != null) {
                onReleaseResources(allGroupsBean);
            }
        }
        GroupsBean oldAllGroupsBean = allGroupsBean;
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
    @Override public void onCanceled(GroupsBean allGroupsBean) {
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
    protected void onReleaseResources(GroupsBean allGroupsBean) {
        // For a simple List<> there is nothing to do.  For something
        // like a Cursor, we would close it here.
    }
}