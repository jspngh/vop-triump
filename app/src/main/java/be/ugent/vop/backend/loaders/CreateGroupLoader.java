package be.ugent.vop.backend.loaders;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import java.io.IOException;

import be.ugent.vop.backend.BackendAPI;
import be.ugent.vop.backend.myApi.model.GroupBean;

/**
 * Created by Lars on 3/3/15.
 */
public class CreateGroupLoader extends AsyncTaskLoader<GroupBean> {

    private Context context;
    String name;
    String description;
    String type;

    public CreateGroupLoader(Context context, String name, String description ,String type) {
        super(context);
        this.context = context.getApplicationContext();
        this.name = name;
        this.description = description;
        this.type = type;
    }

    /**
     * This is where the bulk of our work is done.  This function is
     * called in a background thread and should generate a new set of
     * data to be published by the loader.
     */
    @Override
    public GroupBean loadInBackground() {
        GroupBean result = null;
        Log.d("CreateGroupLoader", ""+name);
        try{
            result = BackendAPI.get(context).createGroup( name,  description, type);
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
    public void deliverResult(GroupBean response) {
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
    @Override public void onCanceled(GroupBean response) {
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
    protected void onReleaseResources(GroupBean response) {}

}