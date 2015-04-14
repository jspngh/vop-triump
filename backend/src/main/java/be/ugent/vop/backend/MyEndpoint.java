/*
   For step-by-step instructions on connecting your Android application to this backend module,
   see "App Engine Java Endpoints Module" template documentation at
   https://github.com/GoogleCloudPlatform/gradle-appengine-templates/tree/master/HelloEndpoints
*/

package be.ugent.vop.backend;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.InternalServerErrorException;
import com.google.api.server.spi.response.UnauthorizedException;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Named;

//TODO: Pictures for group, users and venues

/**
 * An endpoint class we are exposing
 */
@Api(name = "myApi", version = "v1", namespace = @ApiNamespace(ownerDomain = "backend.vop.ugent.be", ownerName = "backend.vop.ugent.be", packagePath = ""))
public class MyEndpoint {

    private static final String SESSION_ENTITY = "Session";
    private static final String SESSION_USER_ID = "userId";
    private static final String SESSION_TOKEN = "sessionToken";

    private static final String GCM_ENTITY = "Gcm";
    private static final String GCM_USER_ID = "userId";
    private static final String GCM_GCM_ID = "gcmId";

    private static final String GCM_API_KEY = "AIzaSyBRyeAM5-ozbgltDeJebQEHvVoX42YVY10";
    private static final Sender sender = new Sender(GCM_API_KEY);

    private static final String USER_ENTITY = "User";
    private static final String USER_ID = "fsUserId";
    private static final String USER_FIRST_NAME = "firstName";
    private static final String USER_LAST_NAME = "lastName";
    private static final String USER_EMAIL = "email";
    private static final String USER_JOINED = "joined";
    private static final String USER_PICTURE = "profilePicture";

    private static final String GROUP_ENTITY = "Group";
    private static final String GROUP_NAME = "name";
    private static final String GROUP_DESCRIPTION = "description";
    private static final String GROUP_TYPE = "type";
    private static final String GROUP_ADMIN_ID = "adminId";
    private static final String GROUP_CREATED = "created";
    private static final String GROUP_NUM_MEMBERS = "numMembers";

    private static final String CHECKIN_ENTITY = "Checkin";
    private static final String CHECKIN_DATE = "date";
    private static final String CHECKIN_POINTS = "points";
    private static final String CHECKIN_USER_ID = "userId";
    private static final String CHECKIN_VENUE_ID = "venueId";
    private static final String CHECKIN_GROUP_ID = "groupId";

    private static final String VENUE_ENTITY = "Venue";
    private static final String VENUE_FIRST_CHECKIN= "firstCheckin";
    private static final String VENUE_ADMIN = "adminId";
    private static final String VENUE_VERIFIED = "verfied";
    private static final String VENUE_ID = "venueId";

    private static final String USERGROUP_ENTITY = "userGroup";
    private static final String USERGROUP_USER_ID = "userId";
    private static final String USERGROUP_GROUP_ID = "groupId";
    private static final String USERGROUP_JOINED = "joined";
    private static final String USERGROUP_ACCEPTED = "accepted";
    private static final String USERGROUP_IS_ADMIN = "isAdmin";

    private static final String EVENT_ENTITY = "Event";
    private static final String EVENT_USER_ID = "userId";
    private static final String EVENT_VENUE_ID = "venueId";
    private static final String EVENT_START = "start";
    private static final String EVENT_END = "end";
    private static final String EVENT_DESCRIPTION = "description";
    private static final String EVENT_REWARD = "reward";
    private static final String EVENT_REQUIREMENT = "requirement";
    private static final String EVENT_MIN_PARTICIPANTS = "minParticipants";
    private static final String EVENT_MAX_PARTICIPANTS = "maxParticipants";
    private static final String EVENT_VERIFIED = "verified";
    private static final String EVENT_PROCESSED = "processed";

    private static final String GROUPEVENT_ENTITY = "groupEvent";
    private static final String GROUPEVENT_GROUP_ID = "groupId";
    private static final String GROUPEVENT_EVENT_ID = "eventId";


    private static final String USEREVENT_ENTITY = "userEvent";
    private static final String USEREVENT_EVENT_ID = "eventId";
    private static final String USEREVENT_USER_ID = "userId";
    private static final String USEREVENT_GROUP_ID = "groupId";
    private static final String USEREVENT_RECEIVED = "received";

    private static final String OVERVIEW_ENTITY = "Overview";

    private SecureRandom random = new SecureRandom();

    /**
       Cron jobs
     */

    @ApiMethod(name = "generateRewards")
    public void generateRewards() {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Query.Filter dateFilter =
                new Query.FilterPredicate(EVENT_END,
                        Query.FilterOperator.LESS_THAN_OR_EQUAL,
                        new Date());

        Query.Filter processedFilter =
                new Query.FilterPredicate(EVENT_PROCESSED,
                        Query.FilterOperator.EQUAL,
                        0);

        Query.Filter filter = Query.CompositeFilterOperator.and(dateFilter, processedFilter);
        Query q_event = new Query(EVENT_ENTITY).setFilter(filter);
        PreparedQuery pq_event = datastore.prepare(q_event);
        List<Entity> rewards = new ArrayList<>();

        List<String> userIds = new ArrayList<>();
        for (Entity r : pq_event.asIterable()){
            r.setProperty(EVENT_PROCESSED,1);
            DatastoreServiceFactory.getDatastoreService().put(r);

            List<RankingBean> ranking = _getRankingsforEvent(r.getKey().getId());
            for(int i=0;i<Math.min((long)ranking.size(),(long)r.getProperty(EVENT_REQUIREMENT));i++){
                RankingBean winner = ranking.get(i);
                for(UserBean b :winner.getGroupBean().getMembers()){
                    String userId = b.getUserId();
                    userIds.add(userId);

                    Entity reward = new Entity(USEREVENT_ENTITY);
                    reward.setProperty(USEREVENT_EVENT_ID, r.getKey().getId());
                    reward.setProperty(USEREVENT_GROUP_ID, winner.getGroupBean().getGroupId());
                    reward.setProperty(USEREVENT_USER_ID, userId);
                    reward.setProperty(USEREVENT_RECEIVED, 0);
                    rewards.add(reward);
                }
            }
    }

        _notifyUsersOfEventEndingInTopRanking(userIds);
        DatastoreServiceFactory.getDatastoreService().put(rewards);
    }

    @ApiMethod(name = "askForUserFeedback")
    public void askForUserFeedback() {
        _askForUserFeedback();
    }

    @ApiMethod(name = "claimReward")
    public void claimReward(@Named("token") String token,
                            @Named("eventId") long eventId,
                            @Named("groupId") long groupId) throws UnauthorizedException{
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        String userId = _getUserIdForToken(token);
        Query.Filter userFilter =
                new Query.FilterPredicate(USEREVENT_USER_ID,
                        Query.FilterOperator.EQUAL,
                        userId);
        Query.Filter eventFilter =
                new Query.FilterPredicate(USEREVENT_EVENT_ID,
                        Query.FilterOperator.EQUAL,
                        eventId);
        Query.Filter groupFilter =
                new Query.FilterPredicate(USEREVENT_GROUP_ID,
                        Query.FilterOperator.EQUAL,
                        groupId);


        Query.Filter filter = Query.CompositeFilterOperator.and(userFilter, eventFilter,groupFilter);
        Query r_q = new Query(USEREVENT_ENTITY).setFilter(filter);
        PreparedQuery r_pq = datastore.prepare(r_q);
        Entity reward=null;
        try {
            reward = r_pq.asSingleEntity();
            if(reward!=null) {
                reward.setProperty(USEREVENT_RECEIVED, 1);
                datastore.put(reward);
            }
        }catch(PreparedQuery.TooManyResultsException e){
        }
}

    @ApiMethod(name = "getUserInfo")
    public UserBean getUserInfo(@Named("token") String token) throws UnauthorizedException {
        String userId = _getUserIdForToken(token);
        UserBean response = null;
        try {
            response = _getUserBeanForId(userId);
        } catch (EntityNotFoundException e) {
            e.printStackTrace();
        }

        return response;
    }

    @ApiMethod(name = "getUserInfoForId")
    public UserBean getUserInfoForId(@Named("token") String token,
                                     @Named("userId") String userId) throws UnauthorizedException {
        _getUserIdForToken(token);
        UserBean response = null;
        try {
            response = _getUserBeanForId(userId);
        } catch (EntityNotFoundException e) {
            e.printStackTrace();
        }
        return response;
    }

    @ApiMethod(name = "createGroup")
    public GroupBean createGroup(@Named("token") String token,
                                 @Named("groupName") String groupName,
                                 @Named("description") String description,
                                 @Named("type") String type) throws UnauthorizedException {
        String userId = _getUserIdForToken(token);
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Entity group = new Entity(GROUP_ENTITY);
        group.setProperty(GROUP_NAME, groupName);
        group.setProperty(GROUP_TYPE, type);
        group.setProperty(GROUP_DESCRIPTION, description);
        group.setProperty(GROUP_ADMIN_ID, userId);
        group.setProperty(GROUP_NUM_MEMBERS, 0);

        Date created = new Date();
        group.setProperty(GROUP_CREATED, created);
        datastore.put(group);

        long groupId = group.getKey().getId();

        GroupBean groupBean = null;

        try{
            _registerUserInGroup(userId, groupId);
            _acceptUserInGroup(userId, groupId, true);
            groupBean = _getGroupBean(groupId);
        }
        catch(EntityNotFoundException e){
            e.printStackTrace();
        }

        return groupBean;
    }

    @ApiMethod(name = "createVenue")
    public VenueBean createVenue( @Named("token") String token,
                                  @Named("VenueId") String VenueId,
                                  @Named("verified") boolean verified) throws UnauthorizedException{

        String adminId = _getUserIdForToken(token);
        Entity venue = new Entity(VENUE_ENTITY, VenueId);
        venue.setProperty(VENUE_ID, VenueId);
        venue.setProperty(VENUE_ADMIN, adminId);
        venue.setProperty(VENUE_FIRST_CHECKIN, new Date());
        venue.setProperty(VENUE_VERIFIED, verified);

        //TODO: check if venue is valid before adding in db

        DatastoreServiceFactory.getDatastoreService().put(venue);

        return _getVenueBean(venue);
    }


    @ApiMethod(name = "getGroupInfo")
    public GroupBean getGroupInfo(@Named("token") String token,
                                  @Named("groupId") long groupId) throws UnauthorizedException {
        String userId = _getUserIdForToken(token); // Try to authenticate the user
        GroupBean response = null;
        try {
            response = _getGroupBean(groupId);
        } catch (EntityNotFoundException e) {
            e.printStackTrace();
        }
        boolean userIsAdmin = response.getAdminId().equals(userId);
        response.setUserAdmin(userIsAdmin);
        return response;
    }

    @ApiMethod(name = "getVenueInfo")
    public VenueBean getVenueInfo(@Named("token") String token,
                                  @Named("venueId") String venueId) throws UnauthorizedException, EntityNotFoundException {
        _getUserIdForToken(token); // Try to authenticate the user
        VenueBean response = null;
        response = _getVenueBean(venueId);
        return response;
    }

    @ApiMethod(name = "createEventFinal")
    public EventBean createEventFinal(@Named("token") String token,
                                      @Named("venueId") String venueId,
                                      @Named("start") Date start,
                                      @Named("end") Date end,
                                      @Named ("description") String description,
                                      @Named ("reward")String reward,
                                      @Named ("minParticipants") int minParticipants,
                                      @Named ("maxParticipants") int maxParticipants,
                                      @Named ("verified") boolean verified,
                                      @Nullable @Named("groupIds") List<Long> groupIds)
            throws UnauthorizedException, EntityNotFoundException {
        String userId = _getUserIdForToken(token); // Try to authenticate the user
        Entity event = new Entity(EVENT_ENTITY);
        event.setProperty(EVENT_DESCRIPTION, description);
        event.setProperty(EVENT_END, end);
        event.setProperty(EVENT_START, start);
        event.setProperty(EVENT_REWARD, reward);
        event.setProperty(EVENT_USER_ID,userId);
        event.setProperty(EVENT_VENUE_ID,venueId);
        event.setProperty(EVENT_VERIFIED,((verified)?(long)1:(long)0));
        event.setProperty(EVENT_PROCESSED,0);
        event.setProperty(EVENT_REQUIREMENT,(long)1);
        if(verified){
            event.setProperty(EVENT_MIN_PARTICIPANTS,(long)minParticipants);
            event.setProperty(EVENT_MAX_PARTICIPANTS,(long)maxParticipants);
            DatastoreServiceFactory.getDatastoreService().put(event);
        }else{
//when event is not verified the min and max number of participants is not importent
            event.setProperty(EVENT_MIN_PARTICIPANTS,(long)-1);
            event.setProperty(EVENT_MAX_PARTICIPANTS,(long)-1);
//insert event to retrieve key in "groupEvent.setProperty(GROUPEVENT_EVENT_ID, event.getKey().getId());"
            DatastoreServiceFactory.getDatastoreService().put(event);
            if (groupIds != null) {
                for (Long s : groupIds) {
                    Entity groupEvent = new Entity(GROUPEVENT_ENTITY);
                    groupEvent.setProperty(GROUPEVENT_GROUP_ID, s.longValue());
                    groupEvent.setProperty(GROUPEVENT_EVENT_ID, event.getKey().getId());
                    DatastoreServiceFactory.getDatastoreService().put(groupEvent);
                }
            }
        }
//TODO: check if venue is valid before adding in db
        EventBean e = _getEventBean(event);
        return e;
    }

    @ApiMethod(name = "getEventsForUser")
    public EventRewardBean getEventsForUser(@Named("token") String token) throws UnauthorizedException, EntityNotFoundException, InternalServerErrorException {
        String userId = _getUserIdForToken(token);
        EventRewardBean eventreward = new EventRewardBean();
        eventreward.setEvents(_getEventsForUser(userId));
        eventreward.setRewards(_getRewardsForUser(userId));
        return eventreward;
    }


    @ApiMethod(name = "getEventsForVenue")
    public List<EventBean> getEventsForVenue(@Named("token") String token,@Named("venueId") String venueId) throws UnauthorizedException, EntityNotFoundException, InternalServerErrorException {
        _getUserIdForToken(token);
        return _getEventsForVenue(venueId);
    }

    @ApiMethod(name = "getGroupsForUser")
    public GroupsBean getGroupsForUser(@Named("token") String token) throws UnauthorizedException, InternalServerErrorException {
        String userId = _getUserIdForToken(token);
        GroupsBean groupsBean;
        try {
            groupsBean = _getGroupsForUser(userId);
        } catch (Exception e) {
            e.printStackTrace();
            throw new InternalServerErrorException("Sorry, we screwed something up... getGroupsForUser");
        }
        return groupsBean;

    }

    @ApiMethod(name = "registerGcmId")
    public void registerGcmId(@Named("token") String token,
                              @Named("gcmId") String gcmId) throws UnauthorizedException, InternalServerErrorException {
        String userId = _getUserIdForToken(token); // Try to authenticate the user
        _registerGcmId(userId,gcmId);
    }


    @ApiMethod(name = "getAllGroups")
    public List<GroupBean> getAllGroups(@Named("token") String token) throws UnauthorizedException, InternalServerErrorException {
        _getUserIdForToken(token); // Try to authenticate the user
        try {
            return _getAllGroups();
        } catch (EntityNotFoundException e) {
            e.printStackTrace();
            throw new InternalServerErrorException("Sorry, we could not find any groups");
        }
    }

    @ApiMethod(name = "registerUserInGroup")
    public GroupBean registerUserInGroup(@Named("token") String token,
                                         @Named("groupId") long groupId) throws UnauthorizedException, InternalServerErrorException {
        String userId = _getUserIdForToken(token);
        GroupBean response = null;
        try {
            _registerUserInGroup(userId, groupId);
            response = _getGroupBean(groupId);
        } catch (EntityNotFoundException e) {
            throw new InternalServerErrorException("Requested group does not exist!");
        }

        return response;
    }

    @ApiMethod(name = "sendToUser")
    public void sendToUser(@Named("token") String token) throws UnauthorizedException, InternalServerErrorException, IOException {
        String userId = _getUserIdForToken(token);
        String gcmId = _getGcmIdForUserId(userId);

        Message message = new Message.Builder()
                .addData("message", "this is the message")
                .addData("other-parameter", "some value")
                .build();

        sender.send(message, gcmId, 1);
    }

    @ApiMethod(name = "notifyUsersOfEventEndingInTopRanking")
    public void notifyUsersOfEventEndingInTopRanking(@Named("userId") List<String> userIds ) throws UnauthorizedException, InternalServerErrorException, IOException {
        _notifyUsersOfEventEndingInTopRanking(userIds);
    }

    @ApiMethod(name = "checkInVenue")
    public List<RankingBean> checkInVenue(@Named("token") String token,
                                          @Named("venueId") String venueId,
                                          @Named("minGroupSize") int minGroupSize,
                                          @Named("maxGroupSize") int maxGroupSize,
                                          @Named("groupType") String groupType) throws UnauthorizedException, InternalServerErrorException, EntityNotFoundException {
        String userId = _getUserIdForToken(token);
        GroupsBean groups = _getGroupsForUser(userId);
        VenueBean venue;
        try {
            venue = _getVenueBean(venueId);
        } catch(EntityNotFoundException e){
            venue = createVenue(token, venueId, false);
        }
        if(groups.getNumGroups()==0) {
            return null;
            //needs to be catched
        }
        for(GroupBean g : groups.getGroups()){
            _checkInVenue(userId, g.getGroupId(), venueId);
        }
        return _getRankings(venueId, (long)minGroupSize, (long)maxGroupSize, groupType);
    }

    @ApiMethod(name = "getAuthToken")
    public AuthTokenResponse getAuthToken(@Named("fsUserID") String fsUserId,
                                          @Named("fsToken") String fsToken) throws UnauthorizedException, InternalServerErrorException{
        AuthTokenResponse response = new AuthTokenResponse();
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        response.setAuthToken("Siebe");
        try {
            URL url = new URL("https://api.foursquare.com/v2/users/self?oauth_token=" + fsToken + "&v=20140806&m=foursquare");
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuilder jsonResponseBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonResponseBuilder.append(line);
            }
            String jsonResponse = jsonResponseBuilder.toString();
            JSONObject fsResponse = new JSONObject(jsonResponse);
            JSONObject meta = fsResponse.getJSONObject("meta");
            int code = meta.getInt("code");
            if(code == 401)
                throw new UnauthorizedException("Invalid Foursquare login");
            JSONObject r = fsResponse.getJSONObject("response");
            JSONObject user = r.getJSONObject("user");
            String returnedUserId = user.getString("id");
            String firstName = user.getString("firstName");
            String lastName = "";
            if(user.has("lastName"))
                lastName = user.getString("lastName");
            JSONObject photo = user.getJSONObject("photo");
            String profilePicture = "";
            if(photo.has("prefix") && photo.has("suffix"))
                profilePicture = photo.getString("prefix") + "original" + photo.getString("suffix");
            JSONObject contact = user.getJSONObject("contact");
            String email = contact.getString("email");
            response.setUserId(returnedUserId);
            if(returnedUserId.equals(fsUserId)){
// User is who he claims to be
// Check if we already saved his info
                try{
                    _getUserBeanForId(returnedUserId);
                }catch (EntityNotFoundException e){
// User not in our database
                    Entity userEntity = new Entity(USER_ENTITY, returnedUserId);
                    userEntity.setProperty(USER_ID, returnedUserId);
                    userEntity.setProperty(USER_FIRST_NAME, firstName);
                    userEntity.setProperty(USER_LAST_NAME, lastName);
                    userEntity.setProperty(USER_EMAIL, email);
                    userEntity.setProperty(USER_JOINED, new Date());
                    userEntity.setProperty(USER_PICTURE, profilePicture);
                    datastore.put(userEntity);
                }
// Create session for user and send back auth token
// Create random token
                String sessionToken = new BigInteger(256, random).toString(32);
// Store session information in datastore
                Entity session = new Entity(SESSION_ENTITY, sessionToken);
                session.setProperty(SESSION_USER_ID, returnedUserId);
                session.setProperty(SESSION_TOKEN, sessionToken);
                datastore.put(session);
// set token in response
                response.setAuthToken(sessionToken);
            }
            reader.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new InternalServerErrorException("Whoops, we screwed something up :( MalformedURLException \n" + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            throw new InternalServerErrorException("Whoops, we screwed something up :( IOException \n" + e.getMessage());
        } catch (JSONException e) {
            e.printStackTrace();
            throw new InternalServerErrorException("Whoops, we screwed something up :( JSONException \n" + e.getMessage());
        }
        return response;
    }

    @ApiMethod(name = "closeSession")
    public CloseSessionResponse closeSession( @Named("token") String token){
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        Key userKey = KeyFactory.createKey(SESSION_ENTITY, token);
        datastore.delete(userKey);

        CloseSessionResponse response = new CloseSessionResponse();
        response.setMessage("Success");
        return response;
    }

    @ApiMethod(name = "getLeaderboard")
    public List<RankingBean> getLeaderboard(@Named("token") String token,
                                            @Named("minGroupSize") int minGroupSize,
                                            @Named("maxGroupSize") int maxGroupSize,
                                            @Named("groupType") String groupType) throws UnauthorizedException, EntityNotFoundException {
        _getUserIdForToken(token); // Try to authenticate the user
        List<RankingBean> leaderboard = new ArrayList<>();
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Query q_groups = new Query(GROUP_ENTITY);
        PreparedQuery pq_groups = datastore.prepare(q_groups);
        Query q_checkins;
        PreparedQuery pq_checkins;
        CheckinBean checkin;
        for (Entity r : pq_groups.asIterable()){
            RankingBean rank = new RankingBean();
            long groupId = r.getKey().getId();
            int points = 0;
            GroupBean group = _getGroupBean(groupId);
            //TODO: check for groupsize
            if(isCorrectGroupSize(group.getNumMembers(), minGroupSize, maxGroupSize)) {
                if (group.getType().equals(groupType) || groupType.equals(GroupBean.GROUP_TYPE_ALL)) {
                    rank.setGroupBean(group);
                    Query.Filter propertyFilter =
                            new Query.FilterPredicate(CHECKIN_GROUP_ID,
                                    Query.FilterOperator.EQUAL,
                                    groupId);
                    q_checkins = new Query(CHECKIN_ENTITY).setFilter(propertyFilter);
                    pq_checkins = datastore.prepare(q_checkins);

                    for (Entity s : pq_checkins.asIterable()) {
                        checkin = _getCheckinBean(s);
                        points += checkin.getPoints();

                    }
                    rank.setPoints(points);
                    leaderboard.add(rank);
                }
            }
        }
        sortRankingList(leaderboard);
        return leaderboard;
    }


 //   @ApiMethod(name = "getRankingForEvent2", path = "getRankingForEvent")
    @ApiMethod(name = "getRankingsForEvent")
    public List<RankingBean> getRankingsForEvent(@Named("token") String token,
                                         @Named("eventId") long eventId) throws UnauthorizedException, EntityNotFoundException {
        _getUserIdForToken(token); // Try to authenticate the user
        return _getRankingsforEvent(eventId);
    }




    @ApiMethod(name = "getRankings", path = "getRankings")
    public List<RankingBean> getRankings(@Named("token") String token,
                                         @Named("venueId") String venueId,
                                         @Named("minGroupSize") int minGroupSize,
                                         @Named("maxGroupSize") int maxGroupSize,
                                         @Named("groupType") String groupType) throws UnauthorizedException, EntityNotFoundException {
        _getUserIdForToken(token); // Try to authenticate the user

        return _getRankings(venueId, (long) minGroupSize, (long) maxGroupSize, groupType);
    }

    @ApiMethod(name = "getOverview", path = "getOverview")
    public OverviewBean getOverview(@Named("token") String token,
                                    @Nullable @Named("venueIds") ArrayList<String> venueIds) throws UnauthorizedException, EntityNotFoundException {
        String userId = _getUserIdForToken(token);
        GroupsBean tmp = _getGroupsForUser(userId);
        ArrayList<VenueBean> venues = new ArrayList<>();
        VenueBean mVenueBean = null;
        GroupBean group = null;
        if(tmp.getGroups().size() != 0){
            group = _getGroupsForUser(userId).getGroups().get(0);
        }
        if(venueIds != null) {
            for (String venueId : venueIds) {
                try {
                    mVenueBean = _getVenueBean(venueId);
                    venues.add(mVenueBean);
                } catch (EntityNotFoundException e) {
                }
            }
        }
        OverviewBean result = new OverviewBean();
        result.setGroup(group);
        result.setVenues(venues);
        return result;
    }
    @ApiMethod(name = "acceptUserInGroup", path = "acceptUserInGroup")
    public void acceptUserInGroup(@Named("token") String token,
                                  @Named("userId") String userId,
                                  @Named("groupId") long groupId) throws UnauthorizedException, EntityNotFoundException {
        // TODO: check admin
        _getUserIdForToken(token); // Try to authenticate the user
        _acceptUserInGroup(userId, groupId, false);
    }
    @ApiMethod(name = "denyUserInGroup", path = "denyUserInGroup")
    public void denyUserInGroup(@Named("token") String token,
                                @Named("userId") String userId,
                                @Named("groupId") long groupId) throws UnauthorizedException {
        // TODO: check admin
        _getUserIdForToken(token); // Try to authenticate the user
        _denyUserInGroup(userId, groupId);
    }
    @ApiMethod(name = "getPendingRequests", path = "getPendingRequests")
    public List<UserBean> getPendingRequests(@Named("token") String token,
                                             @Named("groupId") long groupId) throws UnauthorizedException, EntityNotFoundException {
        // TODO: check admin
        _getUserIdForToken(token); // Try to authenticate the user
        return _getPendingRequests(groupId);
    }



    /************************
     * Private helper methods
     **************************/

    private String _getUserIdForToken(String token) throws UnauthorizedException {
        String userId;
        try {
            DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
            Key userKey = KeyFactory.createKey(SESSION_ENTITY, token);
            Entity session = datastore.get(userKey);
            userId = (String) session.getProperty(SESSION_USER_ID);
        } catch (EntityNotFoundException e) {
            throw new UnauthorizedException("Not authorized for this request.");
        }

        return userId;
    }

    private void _registerUserInGroup(String userId, long groupId) throws EntityNotFoundException {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        GroupsBean groups = _getGroupsForUser(userId);
        GroupBean group;
        try {
            group = _getGroupBean(groupId);
        } catch (EntityNotFoundException e) {
            e.printStackTrace();
            throw e;
        }
        //check if user isn't already member of the group
        for(GroupBean g:groups.getGroups()){
            if(g.getGroupId()==group.getGroupId()){
                return;
            }
        }
        Entity userGroup = new Entity(USERGROUP_ENTITY);
        userGroup.setProperty(USERGROUP_USER_ID, userId);
        userGroup.setProperty(USERGROUP_GROUP_ID, groupId);
        userGroup.setProperty(USERGROUP_JOINED, new Date());
        userGroup.setProperty(USERGROUP_ACCEPTED, false);
        userGroup.setProperty(USERGROUP_IS_ADMIN, false);
        datastore.put(userGroup);
    }


    private void _acceptUserInGroup(String userId, long groupId, boolean isAdmin) throws EntityNotFoundException {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        Query.Filter propertyFilter =
                new Query.FilterPredicate(USERGROUP_GROUP_ID,
                        Query.FilterOperator.EQUAL,
                        groupId);

        Query.Filter propertyFilter2 =
                new Query.FilterPredicate(USERGROUP_USER_ID,
                        Query.FilterOperator.EQUAL,
                        userId);

        Query.Filter filter = Query.CompositeFilterOperator.and(propertyFilter, propertyFilter2);

        Query q = new Query(USERGROUP_ENTITY).setFilter(filter);
        PreparedQuery pq = datastore.prepare(q);
        Entity e = pq.asSingleEntity();

        boolean alreadyAccepted = (boolean) e.getProperty(USERGROUP_ACCEPTED);

        e.setProperty(USERGROUP_ACCEPTED, true);
        e.setProperty(USERGROUP_IS_ADMIN, isAdmin);
        datastore.put(e);

        if(!alreadyAccepted){
            Entity groupEntity = datastore.get(KeyFactory.createKey(GROUP_ENTITY, groupId));
            long numMembers = (long) groupEntity.getProperty(GROUP_NUM_MEMBERS);
            long newNumMembers = numMembers + 1;
            groupEntity.setProperty(GROUP_NUM_MEMBERS, newNumMembers);
            datastore.put(groupEntity);
        }
    }

    private void _denyUserInGroup(String userId, long groupId){
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        Query.Filter propertyFilter =
                new Query.FilterPredicate(USERGROUP_GROUP_ID,
                        Query.FilterOperator.EQUAL,
                        groupId);

        Query.Filter propertyFilter2 =
                new Query.FilterPredicate(USERGROUP_USER_ID,
                        Query.FilterOperator.EQUAL,
                        userId);

        Query.Filter filter = Query.CompositeFilterOperator.and(propertyFilter, propertyFilter2);

        Query q = new Query(USERGROUP_ENTITY).setFilter(filter);
        PreparedQuery pq = datastore.prepare(q);
        Entity e = pq.asSingleEntity();

        datastore.delete(e.getKey());
    }

    private List<UserBean> _getPendingRequests(long groupId) throws EntityNotFoundException {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        Query.Filter propertyFilter =
                new Query.FilterPredicate(USERGROUP_GROUP_ID,
                        Query.FilterOperator.EQUAL,
                        groupId);

        Query.Filter propertyFilter2 =
                new Query.FilterPredicate(USERGROUP_ACCEPTED,
                        Query.FilterOperator.EQUAL,
                        false);

        Query.Filter filter = Query.CompositeFilterOperator.and(propertyFilter, propertyFilter2);

        Query q = new Query(USERGROUP_ENTITY).setFilter(filter);
        PreparedQuery pq = datastore.prepare(q);

        ArrayList<UserBean> requests = new ArrayList<>();

        for (Entity r : pq.asIterable()) {
            requests.add(_getUserBeanForId((String) r.getProperty(USERGROUP_USER_ID)));
        }

        return requests;
    }



    private CheckinBean _checkInVenue(String userId, long groupId, String venueId){

        //TODO: check if checkin is valid?
        // - niet te snel na elkaar?
        // - user wel in de omgeving van de venue?


        //TODO: calc points
        // - look for certain combo that result in extra points
        Integer points = 1;

        Entity checkinEnt = new Entity(CHECKIN_ENTITY);
        checkinEnt.setProperty(CHECKIN_DATE, new Date());
        checkinEnt.setProperty(CHECKIN_POINTS, points);
        checkinEnt.setProperty(CHECKIN_USER_ID, userId);
        checkinEnt.setProperty(CHECKIN_VENUE_ID, venueId);
        checkinEnt.setProperty(CHECKIN_GROUP_ID, groupId);

        DatastoreServiceFactory.getDatastoreService().put(checkinEnt);

        return _getCheckinBean(checkinEnt);
    }

    private GroupsBean _getGroupsForUser(String userId){
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        GroupsBean groupsbean = new GroupsBean();
        List<GroupBean> groups = new ArrayList<GroupBean>();
        Query.Filter propertyFilter =
                new Query.FilterPredicate(USERGROUP_USER_ID,
                        Query.FilterOperator.EQUAL,
                        userId);
        Query.Filter propertyFilter2 =
                new Query.FilterPredicate(USERGROUP_ACCEPTED,
                        Query.FilterOperator.EQUAL,
                        true);
        Query.Filter filter = Query.CompositeFilterOperator.and(propertyFilter, propertyFilter2);


        Query q = new Query(USERGROUP_ENTITY).setFilter(filter);
        PreparedQuery pq = datastore.prepare(q);
        for (Entity r : pq.asIterable()) {
            long groupId = ((long) r.getProperty(USERGROUP_GROUP_ID));
            try {
                GroupBean g = _getGroupBean(groupId);
                g.setUserAdmin(g.getAdminId().equals(userId));
                groups.add(g);
            } catch (EntityNotFoundException e) {
                e.printStackTrace();
            }
        }
        groupsbean.setGroups(groups);
        groupsbean.setNumGroups(groups.size());

        return groupsbean;
    }

    private List<EventBean> _getEventsForUser(String userId) throws EntityNotFoundException {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        List<EventBean> events = new ArrayList<>();
        long verified = 1;
        Query.Filter verifiedFilter =
                new Query.FilterPredicate(EVENT_VERIFIED,
                        Query.FilterOperator.EQUAL,
                        verified);
        Query.Filter processedFilter =
                new Query.FilterPredicate(EVENT_PROCESSED,
                        Query.FilterOperator.EQUAL,
                        0);
        Query.Filter filter = Query.CompositeFilterOperator.and(verifiedFilter, processedFilter);
        Query verifiedQuery = new Query(EVENT_ENTITY).setFilter(filter);
        PreparedQuery preparedVerified = datastore.prepare(verifiedQuery);
        for (Entity r : preparedVerified.asIterable()) {
            events.add(_getEventBean(r));
//TODO: still need to add groups
        }
        GroupsBean groups  = _getGroupsForUser(userId);
        if(groups!=null) {
            if (groups.getGroups() != null) {
                for (GroupBean g : groups.getGroups()) {
                    Query.Filter groupFilter =
                            new Query.FilterPredicate(GROUPEVENT_GROUP_ID,
                                    Query.FilterOperator.EQUAL,
                                    g.getGroupId());
                    Query q = new Query(GROUPEVENT_ENTITY).setFilter(groupFilter);
                    PreparedQuery pq = datastore.prepare(q);
                    for (Entity r : pq.asIterable()) {
                        Key eventKey = KeyFactory.createKey(EVENT_ENTITY, (long) r.getProperty(GROUPEVENT_EVENT_ID));
                        Entity event = datastore.get(eventKey);
                        events.add(_getEventBean(event));
                    }
                }
            }
        }
        return events;
    }

    private List<EventBean> _getRewardsForUser(String userId)throws EntityNotFoundException {
       DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
       List<EventBean> result = new ArrayList<>();
        Query.Filter userFilter =
                new Query.FilterPredicate(USEREVENT_USER_ID,
                        Query.FilterOperator.EQUAL,
                        userId);
        Query.Filter receivedFilter =
                new Query.FilterPredicate(USEREVENT_RECEIVED,
                        Query.FilterOperator.EQUAL,
                        0);
        Query.Filter filter = Query.CompositeFilterOperator.and(userFilter, receivedFilter);
        Query verifiedQuery = new Query(USEREVENT_ENTITY).setFilter(filter);
        PreparedQuery preparedVerified = datastore.prepare(verifiedQuery);
        for (Entity r : preparedVerified.asIterable()) {
            result.add(_getEventBean((long)r.getProperty(USEREVENT_EVENT_ID)));
        }
        return result;
    }


    private EventBean _getEventBean(long eventId) throws EntityNotFoundException {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Key eventKey = KeyFactory.createKey(EVENT_ENTITY, eventId);
        Entity event = datastore.get(eventKey);

        return _getEventBean(event);
    }

    private List<EventBean> _getEventsForVenue(String venueId) throws EntityNotFoundException {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        List<EventBean> events = new ArrayList<>();
        Query.Filter venueFilter = new Query.FilterPredicate(EVENT_VENUE_ID, Query.FilterOperator.EQUAL, venueId);
        Query verifiedQuery = new Query(EVENT_ENTITY).setFilter(venueFilter);
        PreparedQuery preparedVerified = datastore.prepare(verifiedQuery);
        for (Entity r : preparedVerified.asIterable()) {
            events.add(_getEventBean(r));
        }
        return events;
    }

    private GroupBean _getGroupBean(long groupId) throws EntityNotFoundException {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Key groupKey = KeyFactory.createKey(GROUP_ENTITY, groupId);
        Entity group = datastore.get(groupKey);

        return _getGroupBean(group);
    }

    private GroupBean _getGroupBean(Entity group) throws EntityNotFoundException {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        GroupBean groupBean = new GroupBean();
        groupBean.setGroupId(group.getKey().getId());
        groupBean.setName((String) group.getProperty(GROUP_NAME));
        groupBean.setDescription((String) group.getProperty(GROUP_DESCRIPTION));
        groupBean.setAdminId(((String) group.getProperty(GROUP_ADMIN_ID)));
        groupBean.setCreated((Date) group.getProperty(GROUP_CREATED));
        groupBean.setType((String) group.getProperty(GROUP_TYPE));
        groupBean.setNumMembers((long) group.getProperty(GROUP_NUM_MEMBERS));

        ArrayList<UserBean> members = new ArrayList<>();

        Query.Filter propertyFilter =
                new Query.FilterPredicate(USERGROUP_GROUP_ID,
                        Query.FilterOperator.EQUAL,
                        group.getKey().getId());
        Query.Filter propertyFilter2 =
                new Query.FilterPredicate(USERGROUP_ACCEPTED,
                        Query.FilterOperator.EQUAL,
                        true);
        Query.Filter filter = Query.CompositeFilterOperator.and(propertyFilter, propertyFilter2);


        Query q = new Query(USERGROUP_ENTITY).setFilter(filter);

        PreparedQuery pq = datastore.prepare(q);

        // Limit the number of returned members to save on datastore reads
        for (Entity r : pq.asList(FetchOptions.Builder.withLimit(5))){
            members.add(_getUserBeanForId((String) r.getProperty(USERGROUP_USER_ID)));
        }

        groupBean.setMembers(members);

        return groupBean;
    }

    private CheckinBean _getCheckinBean(Entity checkin) {

        CheckinBean checkinbean = new CheckinBean();
        checkinbean.setVenueId((String)checkin.getProperty(CHECKIN_VENUE_ID));
        checkinbean.setGroupId((Long) checkin.getProperty(CHECKIN_GROUP_ID));
        checkinbean.setUserId((String) checkin.getProperty(CHECKIN_USER_ID));
        //    checkinbean.setPoints((Integer) checkin.getProperty("points"));
        checkinbean.setPoints(1);
        checkinbean.setDate((Date) checkin.getProperty(CHECKIN_DATE));

        return checkinbean;
    }

    private UserBean _getUserBeanForId(String userId) throws EntityNotFoundException{
        UserBean bean = new UserBean();

        Key userKey = KeyFactory.createKey(USER_ENTITY, userId);
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        Entity user = datastore.get(userKey);

        return _getUserBean(user);
    }

    private UserBean _getUserBean(Entity user) throws EntityNotFoundException{
        UserBean bean = new UserBean();

        bean.setUserId(user.getKey().getName());
        bean.setEmail((String) user.getProperty(USER_EMAIL));
        bean.setFirstName((String) user.getProperty(USER_FIRST_NAME));
        bean.setLastName((String) user.getProperty(USER_LAST_NAME));
        bean.setJoined((Date) user.getProperty(USER_JOINED));
        bean.setProfilePictureUrl((String) user.getProperty(USER_PICTURE));

        return bean;
    }

    private List<GroupBean> _getAllGroups() throws EntityNotFoundException {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        ArrayList<GroupBean> groups = new ArrayList<>();
        Query q = new Query(GROUP_ENTITY);
        PreparedQuery pq = datastore.prepare(q);
        for (Entity r : pq.asIterable()){
            groups.add(_getGroupBean(r));
        }
        return groups;
    }

    private List<RankingBean> _getRankings(String venueId, long minGroupSize, long maxGroupSize, String groupType){
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        List<RankingBean> ranking = new ArrayList<>();

        HashMap<Long, Integer> groupPoints = new HashMap<>();
        Query.Filter filter1 =
                new Query.FilterPredicate(CHECKIN_VENUE_ID,
                        Query.FilterOperator.EQUAL,
                        venueId);

        Query q = new Query(CHECKIN_ENTITY).setFilter(filter1);
        PreparedQuery pq = datastore.prepare(q);

        ArrayList<Long> unwantedGroups = new ArrayList<>();
        ArrayList<Long> wantedGroups = new ArrayList<>();
        HashMap<Long, GroupBean> groupBeans = new HashMap<>();

        for (Entity r : pq.asIterable()) {
            CheckinBean checkin = _getCheckinBean(r);
            long groupId = checkin.getGroupId();

            // Do we already know to skip this group?
            if(unwantedGroups.contains(groupId))
                continue;

            // We do not know yet, so check
            if(!wantedGroups.contains(groupId)) {
                String currentGroupType = getGroupType(groupId);

                if(currentGroupType.equals(groupType) || groupType.equals(GroupBean.GROUP_TYPE_ALL)){
                    // Correct group type, now check size
                    try {
                        GroupBean currentGB = _getGroupBean(groupId);
                        groupBeans.put(groupId, currentGB);

                        if(isCorrectGroupSize(currentGB.getNumMembers(), minGroupSize, maxGroupSize))
                            wantedGroups.add(groupId);
                        else
                            unwantedGroups.add(groupId);
                    } catch (EntityNotFoundException e) {
                        e.printStackTrace();
                    }
                }else{
                    // Wrong group type, ignore group
                    unwantedGroups.add(checkin.getGroupId());
                }
            }

            // We are interested in this group, so update points
            if(wantedGroups.contains(groupId)){
                if (!groupPoints.containsKey(groupId)) {
                    groupPoints.put(groupId, checkin.getPoints());
                } else {
                    int currentPoints = groupPoints.get(groupId);
                    int newPoints = currentPoints + checkin.getPoints();

                    groupPoints.put(groupId, newPoints);
                }
            }
        }

        // Create ranking beans
        for (long groupId2 : groupPoints.keySet()) {
            RankingBean groupRanking = new RankingBean();

            groupRanking.setGroupBean(groupBeans.get(groupId2));
            groupRanking.setPoints(groupPoints.get(groupId2));

            ranking.add(groupRanking);
        }

        sortRankingList(ranking);
        return ranking;
    }


    private List<RankingBean> _getRankingsforEvent(long eventId){
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        List<RankingBean> ranking = new ArrayList<>();
        EventBean event;
        try {
            event = _getEventBean(eventId);
        } catch (EntityNotFoundException e) {
           return ranking;
        }

        ArrayList<Long> unwantedGroups = new ArrayList<>();
        ArrayList<Long> wantedGroups = new ArrayList<>();
        if(!event.isVerified()) {
            for (GroupBean g : event.getGroups()) {
                wantedGroups.add(g.getGroupId());
            }
        }

        HashMap<Long, Integer> groupPoints = new HashMap<>();
       Query.Filter venueFilter =
               new Query.FilterPredicate(CHECKIN_VENUE_ID,
                        Query.FilterOperator.EQUAL,
                       event.getVenueId());

      Query.Filter beforeFilter =
                new Query.FilterPredicate(CHECKIN_DATE,
                        Query.FilterOperator.LESS_THAN_OR_EQUAL,
                        event.getEnd());
        Query.Filter afterFilter =
                new Query.FilterPredicate(CHECKIN_DATE,
                        Query.FilterOperator.GREATER_THAN_OR_EQUAL,
                        event.getStart());


        Query.Filter filter = Query.CompositeFilterOperator.and(beforeFilter,venueFilter,afterFilter);
        Query q = new Query(CHECKIN_ENTITY).setFilter(filter);
        PreparedQuery pq = datastore.prepare(q);

        HashMap<Long, GroupBean> groupBeans = new HashMap<>();

        for (Entity r : pq.asIterable()) {
            CheckinBean checkin = _getCheckinBean(r);
            long groupId = checkin.getGroupId();
            GroupBean currentGB = null;
            try {
                currentGB = _getGroupBean(groupId);
            } catch (EntityNotFoundException e) {
                e.printStackTrace();
            }
            groupBeans.put(groupId, currentGB);
            // Do we already know to skip this group?
            if(unwantedGroups.contains(groupId))
                continue;

            // We do not know yet, so check
            if(event.isVerified()&&!wantedGroups.contains(groupId)) {

                    // Correct group type, now check size
                    try {
                         currentGB = _getGroupBean(groupId);
                        groupBeans.put(groupId, currentGB);

                        if(isCorrectGroupSize(currentGB.getNumMembers(), event.getMinParticipants(), event.getMaxParticipants()))
                            wantedGroups.add(groupId);
                        else
                            unwantedGroups.add(groupId);
                    } catch (EntityNotFoundException e) {
                        e.printStackTrace();
                    }
            }

            // We are interested in this group, so update points
            if(wantedGroups.contains(groupId)){
                if (!groupPoints.containsKey(groupId)) {
                    groupPoints.put(groupId, checkin.getPoints());
                } else {
                    int currentPoints = groupPoints.get(groupId);
                    int newPoints = currentPoints + checkin.getPoints();

                    groupPoints.put(groupId, newPoints);
                }
            }
        }

        // Create ranking beans
        for (long groupId2 : groupPoints.keySet()) {
            RankingBean groupRanking = new RankingBean();

            groupRanking.setGroupBean(groupBeans.get(groupId2));
            groupRanking.setPoints(groupPoints.get(groupId2));

            ranking.add(groupRanking);
        }

        sortRankingList(ranking);
        return ranking;
    }

    private boolean isCorrectGroupSize(long size, long min, long max){
        return ((min == -1 || min <= size)
                && (max == -1 || max >= size));
    }

    private String getGroupType(long groupId){
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Key keyGroup = KeyFactory.createKey(GROUP_ENTITY, groupId);
        Entity groupEntity = null;
        try{
            groupEntity = datastore.get(keyGroup);
        }
        catch(EntityNotFoundException e){
            e.printStackTrace();
        }
        return ((String) groupEntity.getProperty(GROUP_TYPE));
    }

    // returns VenueBean
    // Note: Ranking in VenueBean is not sorted.

    // TODO: Do we still need this?
    private VenueBean _getVenueBean(String venueId) throws EntityNotFoundException{
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Entity venueEnt = null;
        Key keyVenue = KeyFactory.createKey(VENUE_ENTITY, venueId);
        try {
            venueEnt = datastore.get(keyVenue);
        } catch(EntityNotFoundException e){
            Query.Filter filter =
                    new Query.FilterPredicate("venueId",
                            Query.FilterOperator.EQUAL,
                            venueId);
            Query q = new Query(VENUE_ENTITY).setFilter(filter);
            PreparedQuery pq = datastore.prepare(q);
            for(Entity r: pq.asIterable()){
                venueEnt = r;
            }
            if(venueEnt == null){
                throw new EntityNotFoundException(keyVenue);
            }
        }

        return _getVenueBean(venueEnt);
    }



    private EventBean _getEventBean(Entity event) throws EntityNotFoundException{
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        EventBean eventbean = new EventBean();
        eventbean.setDescription((String) event.getProperty(EVENT_DESCRIPTION));
        eventbean.setReward((String) event.getProperty(EVENT_REWARD));
        eventbean.setEnd((Date) event.getProperty(EVENT_END));
        eventbean.setStart((Date) event.getProperty(EVENT_START));
        eventbean.setMinParticipants((long) event.getProperty(EVENT_MIN_PARTICIPANTS));
        eventbean.setMaxParticipants((long) event.getProperty(EVENT_MAX_PARTICIPANTS));
        eventbean.setVerified(((long)event.getProperty(EVENT_VERIFIED)==(long)1)?(long)1:(long)0);
        eventbean.setOrganizer(_getUserBeanForId((String) event.getProperty(EVENT_USER_ID)));
        eventbean.setVenue(_getVenueBean((String) event.getProperty(EVENT_VENUE_ID)));
        eventbean.setVenueId((String)event.getProperty(EVENT_VENUE_ID));
        eventbean.setRequirement((long) 1);
        eventbean.setEventId((long) event.getKey().getId());
        Query.Filter filter =
                new Query.FilterPredicate(GROUPEVENT_EVENT_ID,
                        Query.FilterOperator.EQUAL,
                        event.getKey().getId());
        Query q = new Query(GROUPEVENT_ENTITY).setFilter(filter);
        PreparedQuery pq = datastore.prepare(q);
        List<GroupBean> groups = new ArrayList<>();
        for(Entity r: pq.asIterable()){
            groups.add(_getGroupBean((long)r.getProperty(GROUPEVENT_GROUP_ID)));
        }
        eventbean.setGroups(groups);
        return eventbean;
    }

    private void sortRankingList(List<RankingBean> l){
        RankingBean temp;
        int length = l.size();
        if (length>1) // check if the number of orders is larger than 1
        {
            for (int x=0; x<length; x++) // bubble sort outer loop
            {
                for (int i=0; i < length-x-1; i++) {
                    if (l.get(i).getPoints()<l.get(i+1).getPoints())
                    {
                        temp = l.get(i);
                        l.set(i,l.get(i+1) );
                        l.set(i+1, temp);
                    }
                }
            }
        }
    }

    // TODO: Again, do we still need this?
    private VenueBean _getVenueBean(Entity venue) {
        VenueBean venue2 = new VenueBean();

        venue2.setVenueId((String) venue.getProperty(VENUE_ID));
        venue2.setAdminId((String) venue.getProperty(VENUE_ADMIN));
        venue2.setVerified((boolean) venue.getProperty(VENUE_VERIFIED));
        venue2.setFirstCheckin((Date) venue.getProperty(VENUE_FIRST_CHECKIN));

        return venue2;
    }


    /******************************************************
     *
     *                      GCM
     *
     ******************************************************/

    //note: userIds may contain multiple same ids
    private void _notifyUsersOfEventEndingInTopRanking(List<String> userIds){
        List<String> membersGcmIds = new ArrayList<>();
        List<String> evaluated_ids = new ArrayList<>();
        try {
            for(String id:userIds) {
                if(!evaluated_ids.contains(id)){
                    evaluated_ids.add(id);
                    String gcmId=_getGcmIdForUserId(id);
                    if(gcmId!=null){
                        membersGcmIds.add(gcmId);
                    }
                }
            }
            if(!membersGcmIds.isEmpty()) {
                Message message = new Message.Builder()
                        .addData(NotificationConstants.NOTIFICATION_TYPE, NotificationConstants.TYPE_END_EVENT_TOP_RANKING)
                        .build();
                sender.send(message, membersGcmIds, 3);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private void _askForUserFeedback(){
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        List<String> membersGcmIds = new ArrayList<>();
        try {
            Query q = new Query(GCM_ENTITY);
            PreparedQuery pq = datastore.prepare(q);
            for(Entity r: pq.asIterable()){
                membersGcmIds.add( (String) r.getProperty(GCM_GCM_ID));
            }
            if(!membersGcmIds.isEmpty()) {
                Message message = new Message.Builder()
                        .addData(NotificationConstants.NOTIFICATION_TYPE, NotificationConstants.TYPE_FEEDBACK)
                        .build();
                sender.send(message, membersGcmIds, 3);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    private void _registerGcmId(String userId, String gcmId ){
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Query.Filter filter =
                new Query.FilterPredicate(GCM_USER_ID,
                        Query.FilterOperator.EQUAL,
                        userId);
        Query q = new Query(GCM_ENTITY).setFilter(filter);
        PreparedQuery pq = datastore.prepare(q);
        try{
            Entity e = pq.asSingleEntity();
            if(e!=null){
                datastore.delete(e.getKey());
            }
        }catch(PreparedQuery.TooManyResultsException e){}


        Entity e = new Entity(GCM_ENTITY);
        e.setProperty(GCM_USER_ID,  userId);
        e.setProperty(GCM_GCM_ID, gcmId);
        datastore.put(e);
    }

    /*
        !! Opgelet geeft null terug indien er meer dan 1 entities bestaan met zelfde userId
            of indien userId niet in db zit.
            (Mag normaal niet voorkomen)
     */
    private String _getGcmIdForUserId(String userId){
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Query.Filter filter =
                new Query.FilterPredicate(GCM_USER_ID,
                        Query.FilterOperator.EQUAL,
                       userId);
        Query q = new Query(GCM_ENTITY).setFilter(filter);
        PreparedQuery pq = datastore.prepare(q);
        try{
            Entity e = pq.asSingleEntity();
            if(e!=null)
                return (String) e.getProperty(GCM_GCM_ID);
        }catch(PreparedQuery.TooManyResultsException e){
            return null;
        }
        return null;
    }

}
