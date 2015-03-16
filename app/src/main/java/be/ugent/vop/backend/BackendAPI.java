package be.ugent.vop.backend;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;

import java.io.IOException;
import java.util.List;

import be.ugent.vop.R;
import be.ugent.vop.backend.myApi.MyApi;
import be.ugent.vop.backend.myApi.model.AllGroupsBean;
import be.ugent.vop.backend.myApi.model.AuthTokenResponseFB;
import be.ugent.vop.backend.myApi.model.CloseSessionResponse;
import be.ugent.vop.backend.myApi.model.GroupBean;
import be.ugent.vop.backend.myApi.model.RankingBean;
import be.ugent.vop.backend.myApi.model.RankingBeanCollection;
import be.ugent.vop.backend.myApi.model.VenueBean;
import be.ugent.vop.backend.myApi.model.VenuesBean;

public class BackendAPI {
    public static BackendAPI instance;

    private static String token;
    private MyApi myApiService = null;

    public static BackendAPI get(Context context){
        if(instance == null){
            SharedPreferences prefs = context.getSharedPreferences(context.getString(R.string.sharedprefs), Context.MODE_PRIVATE);
            token = prefs.getString(context.getString(R.string.backendtoken), "N.A.");

            Log.d("FUCK YOU", token);

            instance = new BackendAPI();
        }

        return instance;
    }

    public void setToken(String token){
        this.token = token;
    }

    private BackendAPI(){
        if(myApiService == null) {  // Only do this once
            MyApi.Builder builder = new MyApi.Builder(AndroidHttp.newCompatibleTransport(),
                    new AndroidJsonFactory(), null)
                    // options for running against local devappserver
                    // - 10.0.2.2 is localhost's IP address in Android emulator
                    // - turn off compression when running against local devappserver
                    .setRootUrl("https://triumph-app.appspot.com/_ah/api/")
                    .setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
                        @Override
                        public void initialize(AbstractGoogleClientRequest<?> abstractGoogleClientRequest) throws IOException {
                            abstractGoogleClientRequest.setDisableGZipContent(true);
                        }
                    });
            // end options for devappserver

            myApiService = builder.build();
        }
    }

    public AuthTokenResponseFB getAuthTokenFB(String fbUserId, String fbToken) throws IOException {
        Log.d("FUCK AUTH TOKEN", fbToken + fbUserId);
        return myApiService.getAuthTokenFB(fbUserId,fbToken).execute();
    }

    public CloseSessionResponse close() throws IOException {
        return myApiService.closeSession(token).execute();
    }

    public AllGroupsBean getAllGroups() throws IOException{
        return myApiService.getAllGroups(token).execute();
    }


    public VenueBean getVenueInfo(long venueId) throws IOException {
        return myApiService.getVenueInfo(token, venueId).execute();
    }

    public GroupBean registerUserInGroup( String token, long groupId) throws IOException{
         return myApiService.registerUserInGroup(token, groupId).execute();
    }

    public GroupBean getGroupInfo(long groupId) throws IOException{
        return myApiService.getGroupInfo(token, groupId).execute();
    }

    public VenueBean checkIn(long venueId, long groupId) throws IOException{
        return myApiService.checkInVenue(token, venueId, groupId).execute();
    }

    public VenuesBean getNearbyVenues(Location loc) throws IOException{
        return myApiService.getNearbyVenues(token, loc.getLatitude(), loc.getLongitude()).execute();
    }

    public RankingBeanCollection getLeaderboard() throws IOException{
        return myApiService.getLeaderboard(token).execute();
    }
}
