package be.ugent.vop.backend;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import be.ugent.vop.R;
import be.ugent.vop.backend.myApi.MyApi;
import be.ugent.vop.backend.myApi.model.AllGroupsBean;
import be.ugent.vop.backend.myApi.model.AuthTokenResponse;
import be.ugent.vop.backend.myApi.model.AuthTokenResponseFB;
import be.ugent.vop.backend.myApi.model.CloseSessionResponse;
import be.ugent.vop.backend.myApi.model.GroupBean;
import be.ugent.vop.backend.myApi.model.OverviewBean;
import be.ugent.vop.backend.myApi.model.RankingBean;
import be.ugent.vop.backend.myApi.model.RankingBeanCollection;
import be.ugent.vop.backend.myApi.model.VenueBean;

public class BackendAPI {
    public static BackendAPI instance;

    private static String token;
    private MyApi myApiService = null;

    public static BackendAPI get(Context context){
        if(instance == null){
            SharedPreferences prefs = context.getSharedPreferences(context.getString(R.string.sharedprefs), Context.MODE_PRIVATE);
            token = prefs.getString(context.getString(R.string.backendtoken), "N.A.");
            Log.d("BACKEND TOKEN", token);
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

    public AuthTokenResponse getAuthToken(String fsUserId, String fsToken) throws IOException {
        Log.d("FUCK AUTH TOKEN", fsToken + fsUserId);
        return myApiService.getAuthToken(fsUserId,fsToken).execute();
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

    public VenueBean createVenue(String VenueId, boolean verified) throws IOException{
        return myApiService.createVenue(token, VenueId, verified).execute();
    }

    public VenueBean getVenueInfo(String venueId) throws IOException {
        return myApiService.getVenueInfo(token, venueId).execute();
    }

    public GroupBean registerUserInGroup(long groupId) throws IOException{
         return myApiService.registerUserInGroup(token, groupId).execute();
    }

    public GroupBean getGroupInfo(long groupId) throws IOException{
        return myApiService.getGroupInfo(token, groupId).execute();
    }

    public List<RankingBean> checkIn(String venueId ,String groupSize, String groupType) throws IOException{
        return myApiService.checkInVenue(token, venueId, groupSize,  groupType).execute().getItems();
    }

    public OverviewBean getOverview(ArrayList<String> venues) throws IOException{
        return myApiService.getOverview(token, venues).execute();
    }

    public RankingBeanCollection getLeaderboard(String groupSize, String groupType) throws IOException{
        return myApiService.getLeaderboard(token,groupSize,groupType).execute();
    }

    public List<RankingBean> getRankings(String venueId, String groupSize, String groupType) throws IOException{
        return myApiService.getRankings(groupSize, groupType, token, venueId).execute().getItems();
    }

    public GroupBean createGroup(String name, String description, String type) throws IOException{
        return myApiService.createGroup(token,  name, description, type).execute();
    }
}
