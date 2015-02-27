package be.ugent.vop.foursquare;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.app.ProgressDialog;

import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Message;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import be.ugent.vop.R;

public class FoursquareAPI implements GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private String FSQToken;
    private final String API_URL = "https://api.foursquare.com/v2";
    private final String VERSION = "20150101";
    private final String MODE = "foursquare";

    private Context context;

    // Google Location services
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location mLastLocation;
    private double mLatitude;
    private double mLongitude;

    private static FoursquareAPI instance;

    private boolean DEBUG = true;

    private FoursquareAPI(Context context){
        SharedPreferences prefs = context.getSharedPreferences(context.getString(R.string.sharedprefs), Context.MODE_PRIVATE);
        this.FSQToken = prefs.getString(context.getString(R.string.foursquaretoken), "N.A.");
        this.context = context;
        buildGoogleApiClient();
        mGoogleApiClient.connect();

    }


    public static FoursquareAPI get(Context context){
        if(instance==null){
            instance= new FoursquareAPI(context);
        }
        return instance;
    }


    public ArrayList<FoursquareVenue> getNearbyVenues()  {
        //default coordinates (Brussels) in case GPS Provider is disabled
        double longitude=50.8467104;
        double latitude=4.3526391;

        if(mLastLocation!=null){
            Log.d("Foursquare API", "lastLocation not null");
        }

        String url =API_URL + "/venues/search?ll=" + longitude +","+ latitude+ "&oauth_token=" 		+ FSQToken + "&v=" + VERSION+ "&m=" + MODE;
        Log.d("FoursquareAPI", url);
        ArrayList<FoursquareVenue> venueList = new ArrayList<>();

        try {
            String response = request(url);
            JSONArray venues = new JSONObject(response).getJSONObject("response").getJSONArray("venues");

            int numVenues = venues.length();
                for (int i = 0; i < numVenues; i++) {
                    JSONObject venue = venues.getJSONObject(i);
                    String id = "no id available";
                    String city="no city info available";
                    String name="no name info available";
                    String address="no address available";
                    String country="no country info available";
                    double lon=-1;
                    double lat=-1;

                    if(venue.has("id")) id = venue.getString("id");
                    if(venue.has("name")) name = venue.getString("name");

                    JSONObject location = venue.getJSONObject("location");

                    if(location.has("lng")) lon = location.getDouble("lng");
                    if(location.has("lat")) lat = location.getDouble("lat");


                    if(location.has("address"))  address = location.getString("address");
                    if(location.has("city"))  city = location.getString("city");
                    if(location.has("country"))  country = location.getString("country");

                    venueList.add(new FoursquareVenue(id,name,address,city,country,lon,lat));
                }
        } catch(IOException e){
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(DEBUG){
        String s = "size: "+venueList.size()+" ";
        for(FoursquareVenue v:venueList){
          s+=v.toString();
        }
        Log.d("FoursquareAPI",s);
        }
        return venueList;
    }

    public String getPhotoURL(FoursquareVenue venue){
        return "";
    }


    /**
     * Helpfunctions
     *
     */

    private String request(String URL) throws IOException {
        String response = "";
        try {
            URL url 	= new URL(URL);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoInput(true);
            urlConnection.connect();
            Log.d("request.fsqapi", "Response Code: " + urlConnection.getResponseCode());
            response = streamToString(urlConnection.getInputStream());

        } catch (Exception ex) {
            throw ex;
        }
        return response;
    }


    private String streamToString(InputStream is) throws IOException {
        String str  = "";

        if (is != null) {
            StringBuilder sb = new StringBuilder();
            String line;

            try {
                BufferedReader reader 	= new BufferedReader(new InputStreamReader(is));

                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }

                reader.close();
            } finally {
                is.close();
            }

            str = sb.toString();
        }

        return str;
    }

    /**
     *  Google API builder
     *
     *************************************/


    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();

    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest()
                .setInterval(5000)
                .setFastestInterval(5000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }
    /**
    *
    *   Google location services override methodes
    *
     ***********************************/

    protected void startLocationUpdates() {
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
       mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        Log.d("Foursquare API","Hello");
        if (mLastLocation != null) {
           mLatitude =  mLastLocation.getLatitude();
           mLongitude = mLastLocation.getLongitude();
            Log.d("Foursquare API","lat: "+mLatitude+" long:"+mLongitude);
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
        Log.i("Foursquare API", "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("Foursquare API", "Location changed");
        mLastLocation = location;
        getNearbyVenues();
    }
}





