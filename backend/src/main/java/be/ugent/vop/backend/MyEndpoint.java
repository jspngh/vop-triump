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

    private static final String USER_ENTITY = "User";
    private static final String USER_ID = "fsUserId";
    private static final String USER_FIRST_NAME = "firstName";
    private static final String USER_LAST_NAME = "lastName";
    private static final String USER_EMAIL = "email";
    private static final String USER_JOINED = "joined";

    private static final String GROUP_ENTITY = "Group";
    private static final String GROUP_NAME = "name";
    private static final String GROUP_DESCRIPTION = "description";
    private static final String GROUP_TYPE = "type";
    private static final String GROUP_ADMIN_ID = "adminId";
    private static final String GROUP_CREATED = "created";

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
    private static final String VENUE_ID = "VenueId";

    private static final String USERGROUP_ENTITY = "userGroup";
    private static final String USERGROUP_USER_ID = "userId";
    private static final String USERGROUP_GROUP_ID = "groupId";
    private static final String USERGROUP_JOINED = "joined";
    private static final String USERGROUP_ACCEPTED = "accepted";

    private static final String OVERVIEW_ENTITY = "Overview";

    private SecureRandom random = new SecureRandom();

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

    @ApiMethod(name = "createGroup")
    public GroupBean createGroup(@Named("token") String token, @Named("groupName") String groupName, @Named("description") String description, @Named("type") String type) throws UnauthorizedException {
        String userId = _getUserIdForToken(token);
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Entity group = new Entity(GROUP_ENTITY);
        group.setProperty(GROUP_NAME, groupName);
        group.setProperty(GROUP_TYPE, type);
        group.setProperty(GROUP_DESCRIPTION, description);
        group.setProperty(GROUP_ADMIN_ID, userId);

        Date created = new Date();
        group.setProperty(GROUP_CREATED, created);
        datastore.put(group);

        long groupId = group.getKey().getId();

        GroupBean groupBean = null;

        try{
            _registerUserInGroup(userId, groupId);
            groupBean = _getGroupBean(groupId);
        }
        catch(EntityNotFoundException e){
            e.printStackTrace();
        }


        return groupBean;
    }

    @ApiMethod(name = "createVenue")
    public VenueBean createVenue( @Named("token") String token, @Named("VenueId") String VenueId, @Named("verified") boolean verified) throws UnauthorizedException{

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
    public GroupBean getGroupInfo(@Named("token") String token, @Named("groupId") long groupId) throws UnauthorizedException {
        _getUserIdForToken(token); // Try to authenticate the user
        GroupBean response = null;
        try {
            response = _getGroupBean(groupId);
        } catch (EntityNotFoundException e) {
            e.printStackTrace();
        }
        return response;
    }

    @ApiMethod(name = "getVenueInfo")
    public VenueBean getVenueInfo(@Named("token") String token, @Named("venueId") String venueId) throws UnauthorizedException, EntityNotFoundException {
        _getUserIdForToken(token); // Try to authenticate the user
        VenueBean response = null;
        response = _getVenueBean(venueId);
        return response;
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

    @ApiMethod(name = "getAllGroups")
    public AllGroupsBean getAllGroups(@Named("token") String token) throws UnauthorizedException, InternalServerErrorException {
        _getUserIdForToken(token); // Try to authenticate the user

        AllGroupsBean response = null;

        try {
            response = _getAllGroups();
        } catch (EntityNotFoundException e) {
            e.printStackTrace();
            throw new InternalServerErrorException("Sorry, we screwed something up...");
        }

        return response;
    }

    @ApiMethod(name = "registerUserInGroup")
    public GroupBean registerUserInGroup(@Named("token") String token, @Named("groupId") long groupId) throws UnauthorizedException, InternalServerErrorException {
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

    @ApiMethod(name = "checkInVenue")
    public List<RankingBean> checkInVenue(@Named("token") String token, @Named("venueId") String venueId, @Named("groupId") long groupId, @Named("groupSize") String groupSize, @Named("groupType") String groupType ) throws UnauthorizedException, InternalServerErrorException, EntityNotFoundException {
        String userId = _getUserIdForToken(token);
        _checkInVenue(userId, groupId, venueId);
        return _getRankings(venueId,groupSize,groupType);
    }

    @ApiMethod(name = "getAuthToken")
    public AuthTokenResponse getAuthToken(@Named("fsUserID") String fsUserId, @Named("fsToken") String fsToken) throws UnauthorizedException, InternalServerErrorException{
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

    @ApiMethod(name = "getAuthTokenFB")
    public AuthTokenResponseFB getAuthTokenFB(@Named("fbUserID") String fbUserId, @Named("fbToken") String fbToken) throws UnauthorizedException, InternalServerErrorException{
        AuthTokenResponseFB response = new AuthTokenResponseFB();
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        response.setAuthToken("Siebe");

        try {
            URL url = new URL("https://graph.facebook.com/v2.2/me?access_token=" + fbToken);
            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuilder jsonResponseBuilder = new StringBuilder();

            String line;

            while ((line = reader.readLine()) != null) {
                jsonResponseBuilder.append(line);
            }

            String jsonResponse = jsonResponseBuilder.toString();
            JSONObject fbResponse = new JSONObject(jsonResponse);

            if(fbResponse.has("error")){
                JSONObject error = fbResponse.getJSONObject("error");
                int code = error.getInt("code");
                if(code == 190)
                    throw new UnauthorizedException("Invalid Facebook login");
            }

            String firstName;
            String lastName = "";

            String id = fbResponse.getString("id");
            firstName = fbResponse.getString("first_name");

            if(fbResponse.has("last_name"))
                lastName = fbResponse.getString("last_name");

            String returnedUserId = id;

            response.setUserId(returnedUserId);

            if(returnedUserId.equals(fbUserId)){
                // User is who he claims to be
                // Check if we already saved his info
                try{
                    UserBean userBean = _getUserBeanForId(returnedUserId);
                }catch (EntityNotFoundException e){
                    // User not in our database
                    Entity userEntity = new Entity("User", returnedUserId);
                    userEntity.setProperty("userId", returnedUserId);
                    userEntity.setProperty("firstName", firstName);
                    userEntity.setProperty("lastName", lastName);
                    userEntity.setProperty("joined",new Date());
                    //userEntity.setProperty("email", email); // TODO: get user email from FB
                    datastore.put(userEntity);
                }

                // Create session for user and send back auth token

                // Create random token
                String sessionToken = new BigInteger(256, random).toString(32);

                // Store session information in datastore

                Entity session = new Entity("Session", sessionToken);
                session.setProperty("userId", returnedUserId);
                session.setProperty("sessionToken", sessionToken);
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
    public List<RankingBean> getLeaderboard(@Named("token") String token) throws UnauthorizedException, EntityNotFoundException {
        _getUserIdForToken(token); // Try to authenticate the user
        List<RankingBean> leaderboard = new ArrayList<>();
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Query q = new Query(GROUP_ENTITY);
        PreparedQuery pq = datastore.prepare(q);
        for (Entity r : pq.asIterable()){
            RankingBean rank = new RankingBean();
            long groupId = r.getKey().getId();
            int points = 0;
            GroupBean group = _getGroupBean(groupId);
            rank.setGroupBean(group);
            Query.Filter propertyFilter =
                    new Query.FilterPredicate(CHECKIN_GROUP_ID,
                            Query.FilterOperator.EQUAL,
                            groupId);
            q  = new Query(CHECKIN_ENTITY).setFilter(propertyFilter);
            pq = datastore.prepare(q);
            for (Entity s : pq.asIterable()) {
                points += (int)s.getProperty(CHECKIN_POINTS);

            }
            rank.setPoints(points);
            leaderboard.add(rank);
        }
        return leaderboard;
    }

    @ApiMethod(name = "getRankings", path = "getRankings")
    public List<RankingBean> getRankings(@Named("token") String token, @Named("venueId") String venueId, @Named("groupSize") String groupSize, @Named("groupType") String groupType) throws UnauthorizedException, EntityNotFoundException {
        _getUserIdForToken(token); // Try to authenticate the user

        return _getRankings(venueId, groupSize,groupType );
    }

    @ApiMethod(name = "getOverview", path = "getOverview")
    public OverviewBean getOverview(@Named("token") String token, @Named("latitude") double latitude, @Named("longitude") double longitude) throws UnauthorizedException, EntityNotFoundException {
        String userId = _getUserIdForToken(token);
        GroupsBean tmp = _getGroupsForUser(userId);
        GroupBean group = null;
        if(tmp.getGroups().size() != 0){
            group = _getGroupsForUser(userId).getGroups().get(0);
        }
        VenuesBean venues = null ; //getNearbyVenues(token, latitude, longitude);
        OverviewBean result = new OverviewBean();
        result.setGroup(group);
        result.setVenues(null); //venues.getVenues());
        return result;
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
        datastore.put(userGroup);
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
                new Query.FilterPredicate("userId",
                        Query.FilterOperator.EQUAL,
                        userId);

        Query q = new Query("userGroup").setFilter(propertyFilter);
        PreparedQuery pq = datastore.prepare(q);
        for (Entity r : pq.asIterable()) {
            long groupId = ((Long) r.getProperty("groupId")).longValue();
            try {
                groups.add(_getGroupBean(groupId));
            } catch (EntityNotFoundException e) {
                e.printStackTrace();
            }

        }
        groupsbean.setGroups(groups);
        groupsbean.setNumGroups(groups.size());

        return groupsbean;
    }

    private GroupBean _getGroupBean(long groupId) throws EntityNotFoundException {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Key groupKey = KeyFactory.createKey("Group", groupId);
        Entity group = datastore.get(groupKey);

        return _getGroupBean(group);
    }

    private GroupBean _getGroupBean(Entity group) throws EntityNotFoundException {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        GroupBean groupBean = new GroupBean();
        groupBean.setGroupId(group.getKey().getId());
        groupBean.setName((String) group.getProperty("name"));
        groupBean.setDescription((String) group.getProperty("description"));
        groupBean.setAdminId(((String) group.getProperty("adminId")));
        groupBean.setCreated((Date) group.getProperty("created"));
        groupBean.setType((String) group.getProperty("type"));

        ArrayList<UserBean> members = new ArrayList<>();

        Query.Filter propertyFilter =
                new Query.FilterPredicate("groupId",
                        Query.FilterOperator.EQUAL,
                        group.getKey().getId());

        Query q = new Query("userGroup").setFilter(propertyFilter);

        PreparedQuery pq = datastore.prepare(q);
        for (Entity r : pq.asIterable()){
            String userId = ((String) r.getProperty("userId"));
            members.add(_getUserBeanForId(userId));
        }

        groupBean.setMembers(members);

        return groupBean;
    }

    private CheckinBean _getCheckinBean(Entity checkin) {

        CheckinBean checkinbean = new CheckinBean();
        checkinbean.setVenueId((String)checkin.getProperty("venueId"));
        checkinbean.setGroupId((Long) checkin.getProperty("groupId"));
        checkinbean.setUserId((String) checkin.getProperty("userId"));
        //  checkinbean.setPoints(((Integer) checkin.getProperty("points")).intValue());
        checkinbean.setPoints(1);
        checkinbean.setDate((Date) checkin.getProperty("date"));

        return checkinbean;
    }

    private UserBean _getUserBeanForId(String userId) throws EntityNotFoundException{
        UserBean bean = new UserBean();
        Entity user = null;
        Key userKey = KeyFactory.createKey("User", userId);
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        try {
            user = datastore.get(userKey);
        } catch(EntityNotFoundException e){
            // TODO: This catch should not be necessary as we already know the entity isn't there

            Query.Filter filter =
                    new Query.FilterPredicate("userId",
                            Query.FilterOperator.EQUAL,
                            userId);
            Query q = new Query("User").setFilter(filter);
            PreparedQuery pq = datastore.prepare(q);
            for(Entity r: pq.asIterable()){
                user = r;
            }
            if(user==null){
                throw new EntityNotFoundException(userKey);
            }
        }
        bean.setUserId(userId);
        bean.setEmail((String) user.getProperty("email"));
        bean.setFirstName((String) user.getProperty("firstName"));
        bean.setLastName((String) user.getProperty("lastName"));
        bean.setJoined((Date) user.getProperty("joined"));

        return bean;
    }

    private AllGroupsBean _getAllGroups() throws EntityNotFoundException {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        AllGroupsBean result = new AllGroupsBean();
        ArrayList<GroupBean> groups = new ArrayList<>();

        Query q = new Query("Group");
        PreparedQuery pq = datastore.prepare(q);
        for (Entity r : pq.asIterable()){
            groups.add(_getGroupBean(r));
        }

        result.setNumGroups(groups.size());
        result.setGroups(groups);

        return result;
    }

    private List<RankingBean> _getRankings(String venueId, String groupSize, String groupType){
        /**
         *
         * Merk op: gebruik van magic numbers is tijdelijk
         * We kunnen bespreken welke groepgroottes ons het meest geschikt lijken
         *
         */
        int minMembers=-1; //value -1 means no restriction on min members
        int maxMembers=-1; //value -1 means no restriction on max members
        switch(groupSize){
            case GroupBean.GROUP_SIZE_INDIVIDUAL:
                minMembers = 1;
                maxMembers = 1;
                break;
            case GroupBean.GROUP_SIZE_SMALL:
                minMembers = 2;
                maxMembers = 10;
                break;
            case GroupBean.GROUP_SIZE_MEDIUM:
                minMembers=11;
                maxMembers=50;
                break;
            case GroupBean.GROUP_SIZE_LARGE:
                minMembers=51;
                maxMembers=-1;
                break;
            case GroupBean.GROUP_SIZE_ALL:
                minMembers=-1;
                maxMembers=-1;
                break;
            default:
                minMembers=-1;
                maxMembers=-1;
        }

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        List<RankingBean> ranking = new ArrayList<RankingBean>();

        HashMap<Long, Integer> groupPoints = new HashMap<>();
        Query.Filter filter1 =
                new Query.FilterPredicate("venueId",
                        Query.FilterOperator.EQUAL,
                        venueId);

        CheckinBean checkin;

        Query q = new Query(CHECKIN_ENTITY).setFilter(filter1);
        PreparedQuery pq = datastore.prepare(q);

        ArrayList<Long> unwantedGroups = new ArrayList<>();

        int nrMembers = 0;
        for (Entity r : pq.asIterable()) {
            checkin = _getCheckinBean(r);
            nrMembers = countMembersOfGroup(checkin.getGroupId());
            if(!unwantedGroups.contains(checkin.getGroupId())) {
                if (minMembers <= nrMembers && maxMembers >= nrMembers
                        /*TODO: check grouptype*/) {
                    if (!(groupPoints.containsKey(checkin.getGroupId()))) {
                        groupPoints.put(checkin.getGroupId(), checkin.getPoints());
                    } else {
                        int currentPoints = groupPoints.get(checkin.getGroupId());
                        int newPoints = currentPoints + checkin.getPoints();

                        groupPoints.put(checkin.getGroupId(), newPoints);
                    }
                } else {
                    unwantedGroups.add(checkin.getGroupId());
                }
            }
        }

        for (long groupId : groupPoints.keySet()) {
            RankingBean groupRanking = new RankingBean();
            try {
                groupRanking.setGroupBean(_getGroupBean(groupId));
            } catch (EntityNotFoundException e) {
                e.printStackTrace();
            }

            groupRanking.setPoints(groupPoints.get(groupId));

            ranking.add(groupRanking);
        }

        return ranking;
    }

    private int countMembersOfGroup(long groupId){
        //TODO: verify if user is accepted in group!

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Query.Filter filter =
                new Query.FilterPredicate(USERGROUP_GROUP_ID,
                        Query.FilterOperator.EQUAL,
                        groupId);
        Query q = new Query(USERGROUP_ENTITY).setFilter(filter);
        PreparedQuery pq = datastore.prepare(q);
      //  return pq.countEntities(FetchOptions.Builder.withDefaults());
        return pq.countEntities();
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
                    new Query.FilterPredicate(VENUE_ID,
                            Query.FilterOperator.EQUAL,
                            venueId);
            Query q = new Query(VENUE_ENTITY).setFilter(filter);
            PreparedQuery pq = datastore.prepare(q);
            for(Entity r: pq.asIterable()){
                venueEnt = r;
            }
            if(venueEnt==null){
                throw new EntityNotFoundException(keyVenue);
            }
        }
        VenueBean venue = _getVenueBean(venueEnt);

        return venue;
    }

    // TODO: Again, do we still need this?
    private VenueBean _getVenueBean(Entity venue) {
        VenueBean venue2 = new VenueBean();

        venue2.setVenueId((String) venue.getProperty(VENUE_ID));
        venue2.setAdminId((String) venue.getProperty(VENUE_ADMIN));
        venue2.setVerified((boolean) venue.getProperty(VENUE_VERIFIED));
        venue2.setFirstCheckin((Date) venue.getProperty(VENUE_FIRST_CHECKIN));

/*        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        RankingBean rank = null;
        List<RankingBean> ranking = new ArrayList<RankingBean>();
        Query.Filter filter2;
        Query.Filter filter3;
        Query.Filter filter1 =
                new Query.FilterPredicate("venueId",
                        Query.FilterOperator.EQUAL,
                        venueId);

        CheckinBean checkin;
        ArrayList<Long> evaluatedGroups = new ArrayList<>();

        Query q = new Query("Checkin").setFilter(filter1);
        PreparedQuery pq = datastore.prepare(q);
        for (Entity r : pq.asIterable()) {
            checkin = _getCheckinBean(r);
            if(!(evaluatedGroups.contains(checkin.getGroupId()))){
                evaluatedGroups.add(checkin.getGroupId());
                filter2 = new Query.FilterPredicate("groupId",
                        Query.FilterOperator.EQUAL,
                        checkin.getGroupId());
                filter3 = Query.CompositeFilterOperator.and(filter1, filter2);
                Query q2 = new Query("Checkin").setFilter(filter3);
                PreparedQuery pq2 = datastore.prepare(q2);
                int points=0;
                for (Entity r2 : pq2.asIterable()) {
                    points += _getCheckinBean(r2).getPoints();
                }
                rank = new RankingBean();
                try {
                    rank.setGroupBean(_getGroupBean(checkin.getGroupId()));
                } catch (EntityNotFoundException e) {
                    e.printStackTrace();
                }
                rank.setPoints(points);
                ranking.add(rank);
            }
        }

        venue2.setRanking(ranking);*/

        return venue2;
    }

    // Calculates distance between 2 coordinates
    // param latitude and longitude in degrees
    // output distance in meters
    private double distance(double lat1, double lon1, double lat2, double lon2) {

        final int R = 6371; // Radius of the earth

        Double latDistance = deg2rad(lat2 - lat1);
        Double lonDistance = deg2rad(lon2 - lon1);
        Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return (R * c * 1000.0);


    }

    /*
        Sorting en selecting in the venues ArrayList in a generic way.
        This will allow us to adjust and improve the search results on the go.
     */

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private class VenueArrayList extends ArrayList<VenueBean>{
        ArrayList<VenueBean> venues;

        public VenueArrayList(ArrayList<VenueBean> venues){
            this.venues = venues;
        }

        public ArrayList<VenueBean> getVenues(){
            return venues;
        }

        public void setVenues(ArrayList<VenueBean> venues){
            this.venues = venues;
        }

        // simple bubblesort
        public synchronized void sort(MyComparator f){
            VenueBean temp;
            int length = venues.size();
            if (length>1) // check if the number of orders is larger than 1
            {
                for (int x=0; x<length; x++) // bubble sort outer loop
                {
                    for (int i=0; i < length-x-1; i++) {
                        if (!f.compare(venues.get(i),venues.get(i+1)))
                        {
                            temp = venues.get(i);
                            venues.set(i,venues.get(i+1) );
                            venues.set(i+1, temp);
                        }
                    }
                }
            }
        }

        // simple bubblesort
        public synchronized void select(MySelector f){
            ArrayList<VenueBean> newVenues = new ArrayList<>();
            for(VenueBean v:venues){
                if(f.select(v)) newVenues.add(v);
            }
            venues = newVenues;
        }
    }

    private interface MyComparator<T>{
        // returns true if t1 > t2
        public boolean compare(T t1, T t2);
    }

    private interface MySelector<T>{
        public boolean select(T t);
    }
}
