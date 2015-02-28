package be.ugent.vop.loaders;

import android.app.LoaderManager;
import android.content.Context;
import android.content.AsyncTaskLoader;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import be.ugent.vop.Event;
import be.ugent.vop.EventBroker;
import be.ugent.vop.EventListener;
import be.ugent.vop.R;
import be.ugent.vop.backend.BackendAPI;
import be.ugent.vop.backend.myApi.MyApi;
import be.ugent.vop.backend.myApi.model.AllGroupsBean;
import be.ugent.vop.foursquare.FoursquareAPI;
import be.ugent.vop.foursquare.FoursquareVenue;

/**
 * A custom Loader that loads all of the installed applications.
 */
public class VenueLoader extends AsyncTaskLoader<ArrayList<FoursquareVenue>> implements EventListener {
    private final String TAG = "VenueLoader";

    private int i = 0;
    ArrayList<FoursquareVenue> mVenueList;
    private static MyApi myApiService = null;
    private Context context;

    public VenueLoader(Context context) {
        super(context);
        this.context = context.getApplicationContext();
        EventBroker.get().addListener(this);
    }

    /**
     * This is where the bulk of our work is done.  This function is
     * called in a background"éé thread and should generate a new set of
     * data to be published by the loader.
     */
    @Override
    public ArrayList<FoursquareVenue> loadInBackground() {
        ArrayList<FoursquareVenue> result = null;
        Log.d("VenueLoader", " "+i );
        result = FoursquareAPI.get(context).getNearbyVenues();
        // Done!
        return result;
    }

    /**
     * Called when there is new data to deliver to the client.  The
     * super class will take care of delivering it; the implementation
     * here just adds a little more logic.
     */
    @Override public void deliverResult( ArrayList<FoursquareVenue> venueList) {
        mVenueList = venueList;
        if (isStarted()) {
            // If the Loader is currently started, we can immediately
            // deliver its results.
            super.deliverResult(venueList);
        }
    }

    /**
     * Handles a request to start the Loader.
     */
    @Override protected void onStartLoading() {
        if (mVenueList != null) {
            // If we currently have a result available, deliver it
            // immediately.
            deliverResult(mVenueList);
        }

        if (takeContentChanged() || mVenueList == null) {
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
    @Override public void onCanceled(ArrayList<FoursquareVenue> venueList) {
        super.onCanceled(venueList);
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
        if (mVenueList != null) {
            mVenueList = null;
        }
    }

    /**************************************
     *
     * Override methodes interface EventListener
     *
     ***************************************/

    @Override
    public void handleEvent(Event e){
        Log.d(TAG,"handleEvent");
        onContentChanged();
    }


}