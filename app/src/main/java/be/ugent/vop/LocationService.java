package be.ugent.vop;

/**
 * Created by vincent on 28/02/15.
 */
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class LocationService extends Service implements GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private String TAG = "LocationService";

    // Google Location services
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location mLastLocation;
    private double mLatitude;
    private double mLongitude;

    private boolean mCurrentlyProcessingLocation = false;

    private boolean DEBUG = false;



    /*************************************
     *
     *  Override methods for extending Service
     *
     *************************************/



    @Override
    public void onCreate() {
        Log.d(TAG,"onCreate");
        buildGoogleApiClient();
        mGoogleApiClient.connect();
        mCurrentlyProcessingLocation = true;
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG,"onStartCommand");
        // if we are currently trying to get a location and the alarm manager has called this again,
        // no need to start processing a new location.
        if (!mCurrentlyProcessingLocation) {
            mCurrentlyProcessingLocation = true;
            buildGoogleApiClient();
            mGoogleApiClient.connect();
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /*************************************
     *
     *  Google API builder
     *
     *************************************/


    protected synchronized void buildGoogleApiClient() {
        Log.d(TAG, "buildGoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();

    }

    protected void createLocationRequest() {
        Log.d(TAG, "createLocationRequest");
        mLocationRequest = new LocationRequest()
                .setInterval(5000)
                .setFastestInterval(5000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }
    /**********************************
     *
     *   Google location services override methodes
     *
     ***********************************/

    protected void startLocationUpdates() {
        Log.d(TAG, "startLocationUpdates");
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient , mLocationRequest, this);
    }

    protected void stopLocationUpdates() {
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }


    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d(TAG,"onConnected");
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            mLatitude =  mLastLocation.getLatitude();
            mLongitude = mLastLocation.getLongitude();
            Log.d(TAG,"lat: "+mLatitude+" long:"+mLongitude);
        }

        startLocationUpdates();
    }


    @Override
    public void onConnectionSuspended(int nr){
        mLastLocation = null;
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "Location changed, lat: "+location.getLatitude()+ "| long: "+location.getLongitude());
        mLastLocation = location;
      //  Toast.makeText(this,"lat: "+location.getLatitude()+ "| long: "+location.getLongitude(), Toast.LENGTH_SHORT).show();
      //  onContentChanged();
    }

}