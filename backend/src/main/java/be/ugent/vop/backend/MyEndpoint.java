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
import com.google.appengine.api.datastore.Blob;
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
import java.lang.reflect.Array;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import static com.google.appengine.api.datastore.FetchOptions.Builder.*;
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

    private static final String USERGROUP_ENTITY = "userGroup";
    private static final String USERGROUP_USER_ID = "userId";
    private static final String USERGROUP_GROUP_ID = "groupId";
    private static final String USERGROUP_JOINED = "joined";
    private static final String USERGROUP_ACCEPTED = "accepted";

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
    public VenueBean createVenue( @Named("token") String token,@Named("name") String name, @Named("city") String city, @Named("street") String street,
                                  @Named("housenr") String housenr,@Named("latitude") double latitude,@Named("longitude") double longitude,
                                  @Named("description") String description, @Named("type") String type ) throws UnauthorizedException{

        String adminId = _getUserIdForToken(token);
        Entity venue = new Entity("Venue");
        venue.setProperty("created", new Date());
        venue.setProperty("adminId", adminId);
        venue.setProperty("name", name);
        venue.setProperty("city", city);
        venue.setProperty("street", street);
        venue.setProperty("housenr", housenr);
        venue.setProperty("description", description);
        venue.setProperty("latitude", latitude);
        venue.setProperty("longitude", longitude);
        venue.setProperty("type",type);

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
        return _orderVenueBean(response);
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
    public List<RankingBean> checkInVenue(@Named("token") String token, @Named("venueId") String venueId, @Named("groupId") long groupId) throws UnauthorizedException, InternalServerErrorException, EntityNotFoundException {
        String userId = _getUserIdForToken(token);
        _checkInVenue(userId, groupId, venueId);
        return _getRankings(venueId);
        //return _orderVenueBean(_getVenueBean(venueId));

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

    @ApiMethod(name = "getNearbyVenues")
    public VenuesBean getNearbyVenues( @Named("token") String token, @Named("latitude") double latitude, @Named("longitude") double longitude) throws UnauthorizedException {
        String userId = _getUserIdForToken(token);
        VenuesBean result = new VenuesBean();
        ArrayList<VenueBean> venues = new ArrayList<>();

        Query q = new Query("Venue");
        PreparedQuery pq = DatastoreServiceFactory.getDatastoreService().prepare(q);

        double longitude2;
        double latitude2;
        VenueBean venue;
        double dist;

        double maxDistance = 500.0; //meters
        //search for venues within a radius of maxDistance
        //multiple augmentations are possible like looking for special types only etc...
        for (Entity r : pq.asIterable()){
            longitude2= (Double) r.getProperty("longitude");
            latitude2 = (Double) r.getProperty("latitude");
            dist = distance(latitude,longitude,latitude2,longitude2);
            if(dist<maxDistance){
                venue = _getVenueBean(r);
                venue.setCurrentDistance(dist);
                venues.add(venue);
            }
        }
        VenueArrayList venueArrayList =  new VenueArrayList(venues);

        venueArrayList.sort(new MyComparator<VenueBean>() {
            public boolean compare(VenueBean venue1, VenueBean venue2) {
                int pointsVenue1 = 0;
                int pointsVenue2 = 0;
                for (RankingBean r : venue1.getRanking()) pointsVenue1 += r.getPoints();
                for (RankingBean r : venue1.getRanking()) pointsVenue2 += r.getPoints();
                double venue1_pointsRatio = (double) pointsVenue1 / (double)(pointsVenue1+pointsVenue2);
                double venue2_pointsRatio = (double) pointsVenue2 / (double)(pointsVenue1+pointsVenue2);

                //flipping of venue1 and venue2 is not an error
                //Larger distance is bad
                double venue1_distRatio = (double) venue2.getCurrentDistance() /
                        (double)(venue2.getCurrentDistance()+venue1.getCurrentDistance());
                double venue2_distRatio = (double) venue1.getCurrentDistance() /
                        (double)(venue2.getCurrentDistance()+venue1.getCurrentDistance());

                return venue1_pointsRatio+venue1_distRatio*3.0 > venue2_pointsRatio+venue2_distRatio*3.0;
            }
        });

        result.setVenues(venueArrayList.getVenues());

        return result;
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
    public List<RankingBean> getRankings(@Named("token") String token, @Named("venueId") String venueId) throws UnauthorizedException, EntityNotFoundException {
        _getUserIdForToken(token); // Try to authenticate the user
        return _getRankings(venueId);
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

    private void _registerUserInGroup(String userId, long groupId) throws EntityNotFoundException{
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        GroupsBean groups = _getGroupsForUser(userId);
        GroupBean group;
        try {
            group = _getGroupBean(groupId);
        } catch (EntityNotFoundException e) {
            e.printStackTrace();
            throw e;
        }
        //check 1
        for(GroupBean g:groups.getGroups()){
            if(g.getType()==group.getType()){
                // User is already member of group with same type
                // TODO: handle exception!
                return;
            }
        }
        //checking if amount of members of group doesn't exceed the limit
        if(group.getType().equals(GroupBean.TYPE_SMALL) || group.getType().equals(GroupBean.TYPE_MEDIUM)) {
            Query.Filter filter1 =
                    new Query.FilterPredicate(USERGROUP_ENTITY,
                            Query.FilterOperator.EQUAL,
                            groupId);
            Query q = new Query(USERGROUP_ENTITY).setFilter(filter1);
            PreparedQuery pq = datastore.prepare(q);
            int amount = pq.countEntities(FetchOptions.Builder.withDefaults());

            if ((group.getType().equals(GroupBean.TYPE_SMALL) && amount + 1 > GroupBean.AMOUNT_SMALL)
                    || (group.getType().equals(GroupBean.TYPE_MEDIUM) && amount + 1 > GroupBean.AMOUNT_MEDIUM)) {
                // groups to small to add extra member
                // TODO: handle exception!
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
        List<GroupBean> groups = new ArrayList<>();
        Query.Filter propertyFilter =
                new Query.FilterPredicate(USERGROUP_USER_ID,
                        Query.FilterOperator.EQUAL,
                        userId);

        Query q = new Query(USERGROUP_ENTITY).setFilter(propertyFilter);
        PreparedQuery pq = datastore.prepare(q);
        for (Entity r : pq.asIterable()) {
            long groupId = (Long) r.getProperty(USERGROUP_GROUP_ID);
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

        ArrayList<UserBean> members = new ArrayList<>();

        Query.Filter propertyFilter =
                new Query.FilterPredicate(USERGROUP_GROUP_ID,
                        Query.FilterOperator.EQUAL,
                        group.getKey().getId());

        Query q = new Query(USERGROUP_ENTITY).setFilter(propertyFilter);

        PreparedQuery pq = datastore.prepare(q);
        for (Entity r : pq.asIterable()){
            String userId = ((String) r.getProperty(USERGROUP_USER_ID));
            members.add(_getUserBeanForId(userId));
        }

        groupBean.setMembers(members);

        return groupBean;
    }

    private CheckinBean _getCheckinBean(Entity checkin) {

        CheckinBean checkinbean = new CheckinBean();
        checkinbean.setVenueId((String)checkin.getProperty(CHECKIN_VENUE_ID));
        checkinbean.setGroupId((Long) checkin.getProperty(CHECKIN_GROUP_ID));
        checkinbean.setUserId((String) checkin.getProperty(CHECKIN_USER_ID));
        //  checkinbean.setPoints(((Integer) checkin.getProperty("points")).intValue());
        checkinbean.setPoints(1);
        checkinbean.setDate((Date) checkin.getProperty(CHECKIN_DATE));

        return checkinbean;
    }

    private UserBean _getUserBeanForId(String userId) throws EntityNotFoundException{
        UserBean bean = new UserBean();
        Entity user = null;
        Key userKey = KeyFactory.createKey(USER_ENTITY, userId);
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
        bean.setEmail((String) user.getProperty(USER_EMAIL));
        bean.setFirstName((String) user.getProperty(USER_FIRST_NAME));
        bean.setLastName((String) user.getProperty(USER_LAST_NAME));
        bean.setJoined((Date) user.getProperty(USER_JOINED));

        return bean;
    }

    private AllGroupsBean _getAllGroups() throws EntityNotFoundException {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        AllGroupsBean result = new AllGroupsBean();
        ArrayList<GroupBean> groups = new ArrayList<>();

        Query q = new Query(GROUP_ENTITY);
        PreparedQuery pq = datastore.prepare(q);
        for (Entity r : pq.asIterable()){
            groups.add(_getGroupBean(r));
        }

        result.setNumGroups(groups.size());
        result.setGroups(groups);

        return result;
    }

    private List<RankingBean> _getRankings(String venueId){
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        List<RankingBean> ranking = new ArrayList<>();

        HashMap<Long, Integer> groupPoints = new HashMap<>();
        Query.Filter filter1 =
                new Query.FilterPredicate(CHECKIN_VENUE_ID,
                        Query.FilterOperator.EQUAL,
                        venueId);

        CheckinBean checkin;

        Query q = new Query(CHECKIN_ENTITY).setFilter(filter1);
        PreparedQuery pq = datastore.prepare(q);

        for (Entity r : pq.asIterable()) {
            checkin = _getCheckinBean(r);
            if (!(groupPoints.containsKey(checkin.getGroupId()))) {
                groupPoints.put(checkin.getGroupId(), checkin.getPoints());
            } else {
                int currentPoints = groupPoints.get(checkin.getGroupId());
                int newPoints = currentPoints + checkin.getPoints();

                groupPoints.put(checkin.getGroupId(), newPoints);
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

    // returns VenueBean
    // Note: Ranking in VenueBean is not sorted.

    // TODO: Do we still need this?
    private VenueBean _getVenueBean(String venueId) throws EntityNotFoundException{
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        Key keyVenue = KeyFactory.createKey("Venue", venueId);
        Entity venueEnt = datastore.get(keyVenue);
        VenueBean venue = _getVenueBean(venueEnt);

        return venue;

    }

    // TODO: Again, do we still need this?
    private VenueBean _getVenueBean(Entity venue) {
        VenueBean venue2 = new VenueBean();
        long venueId  = venue.getKey().getId();

        venue2.setVenueId(venue.getKey().getId());
        venue2.setCreated((Date) venue.getProperty("created"));
        venue2.setName((String) venue.getProperty("name"));
        venue2.setAdminId((String) venue.getProperty("adminId"));
        venue2.setCity((String) venue.getProperty("city"));
        venue2.setStreet((String) venue.getProperty("street"));
        venue2.setHouseNr((String) venue.getProperty("housenr"));
        venue2.setDescription((String) venue.getProperty("description"));
        venue2.setLatitude((Double) venue.getProperty("latitude"));
        venue2.setLongitude((Double) venue.getProperty("longitude"));
        venue2.setType((String) venue.getProperty("type"));

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
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

        venue2.setRanking(ranking);

        return venue2;
    }

    private VenueBean _orderVenueBean(VenueBean venue) {
        List<RankingBean> ranking = venue.getRanking();
        Collections.sort(ranking, comparator);
        venue.setRanking(ranking);
        return venue;
    }

    private static Comparator<RankingBean> comparator = new Comparator<RankingBean>() {

        public int compare(RankingBean bean1, RankingBean bean2) {
            if(bean1.getPoints() < bean2.getPoints()) return -1;
            else if(bean1.getPoints() == bean2.getPoints()) return 0;
            else return 1;
        }

    };

    // Calculates distance between 2 coordinates
    // param latitude and logitude in degrees
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
        This will allow us to adjust and improve the searchresults on the go.
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
