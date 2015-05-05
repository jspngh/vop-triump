package be.ugent.vop.backend;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;
import com.google.api.client.util.DateTime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import be.ugent.vop.backend.myApi.MyApi;
import be.ugent.vop.backend.myApi.model.AuthTokenResponse;
import be.ugent.vop.backend.myApi.model.CloseSessionResponse;
import be.ugent.vop.backend.myApi.model.EventBean;
import be.ugent.vop.backend.myApi.model.EventRewardBean;
import be.ugent.vop.backend.myApi.model.GroupBean;
import be.ugent.vop.backend.myApi.model.GroupsBean;
import be.ugent.vop.backend.myApi.model.OverviewBean;
import be.ugent.vop.backend.myApi.model.RankingBean;
import be.ugent.vop.backend.myApi.model.UserBean;
import be.ugent.vop.backend.myApi.model.VenueBean;
import be.ugent.vop.utils.PrefUtils;

public class BackendAPI {
    public static BackendAPI instance;

    private static String token;
    private Context context;
    private MyApi myApiService = null;

    private SharedPreferences.OnSharedPreferenceChangeListener listener =
            new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                    if(PrefUtils.BACKEND_TOKEN.equals(key)) {
                        String token = PrefUtils.getBackendToken(context);
                        if (token != null) setToken(PrefUtils.getBackendToken(context));
                    }
                }
            };

    public static BackendAPI get(Context context){
        if(instance == null){
            token = PrefUtils.getBackendToken(context);
            if(token != null)
                Log.d("BACKEND TOKEN", token);
            instance = new BackendAPI();
            instance.context = context.getApplicationContext();
            PrefUtils.registerOnSharedPreferenceChangeListener(instance.context, instance.listener);
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
                    .setRootUrl("https://mystic-impulse-87918.appspot.com/_ah/api/")
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
        Log.d("AUTH TOKEN", fsToken +"   " +fsUserId);
        return myApiService.getAuthToken(fsUserId,fsToken).execute();
    }

    public CloseSessionResponse close() throws IOException {
        return myApiService.closeSession(token).execute();
    }

    public List<GroupBean> getAllGroups() throws IOException{
       return myApiService.getAllGroups(token).execute().getItems();
    }

    public UserBean getUserInfo() throws IOException{
        return myApiService.getUserInfo(token).execute();
    }

    public UserBean getUserInfoForId(String userId) throws IOException{
        return myApiService.getUserInfoForId(token, userId).execute();
    }

    public GroupsBean getGroupsForUser() throws IOException{
        return myApiService.getGroupsForUser(token).execute();
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

    public void removeUserFromGroup(String userId, long groupId) throws IOException{
        myApiService.removeUserFromGroup(token, userId,  groupId).execute();
    }

    public GroupBean getGroupInfo(long groupId) throws IOException{
        return myApiService.getGroupInfo(token, groupId).execute();
    }


    public List<RankingBean> checkIn(String venueId, int minGroupSize, int maxGroupSize, String groupType) throws IOException{
        Log.d("BackendAPI",token);
        return myApiService.checkInVenue(token, venueId, minGroupSize, maxGroupSize, groupType).execute().getItems();
    }

    public OverviewBean getOverview(ArrayList<String> venues) throws IOException{
        return myApiService.getOverview(token).setVenueIds(venues).execute();
    }


    public List<RankingBean> getLeaderboard(int minGroupSize, int maxGroupSize, String groupType) throws IOException{
        return myApiService.getLeaderboard(token, minGroupSize, maxGroupSize, groupType).execute().getItems();
    }

    public List<RankingBean> getRankings(String venueId, int minGroupSize, int maxGroupSize, String groupType) throws IOException{
        return myApiService.getRankings(groupType, maxGroupSize, minGroupSize, token, venueId).execute().getItems();
    }

    public GroupBean createGroup(String name, String description, String type) throws IOException{
        return myApiService.createGroup(token,  name, description, type).execute();
    }

    public EventBean createEvent(String venueId, List<Long> groupIds, DateTime start, DateTime end, String description,String reward, int minParticipants,int maxParticipants,boolean verified) throws IOException{
        MyApi.CreateEventFinal e = myApiService.createEventFinal(token, venueId,  start,end, description, reward, minParticipants, maxParticipants,verified);
        if(groupIds!=null){
        e.setGroupIds(groupIds);
        }
        return e.execute();

    }

    public EventRewardBean getEventsForUser() throws IOException{
        return myApiService.getEventsForUser(token).execute();
    }

    public List<EventBean> getEventsForVenue(String venueId) throws IOException{
            return myApiService.getEventsForVenue(token, venueId).execute().getItems();
    }

    public List<UserBean> getPendingRequests(long groupId) throws IOException{
        return myApiService.getPendingRequests(groupId, token).execute().getItems();
    }

    public void denyUserInGroup(String userId, long groupId) throws IOException{
        myApiService.denyUserInGroup(groupId, token, userId).execute();
    }

    public void acceptUserInGroup(String userId, long groupId) throws IOException{
        myApiService.acceptUserInGroup(groupId, token, userId).execute();
    }

    public void claimReward( long eventId,long groupId) throws IOException {
        myApiService.claimReward(token,eventId,groupId).execute();
    }


    public void registerGcmId(String gcmId) throws IOException{
        myApiService.registerGcmId(token, gcmId).execute();
    }
}
