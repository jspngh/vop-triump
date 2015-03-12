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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import static com.google.appengine.api.datastore.FetchOptions.Builder.*;
import javax.inject.Named;

/**
 * An endpoint class we are exposing
 */
@Api(name = "myApi", version = "v1", namespace = @ApiNamespace(ownerDomain = "backend.vop.ugent.be", ownerName = "backend.vop.ugent.be", packagePath = ""))
public class MyEndpoint {

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
    public GroupBean createGroup(@Named("token") String token, @Named("groupName") String groupName, @Named("description") String description, @Named("type") int type) throws UnauthorizedException {
        String userId = _getUserIdForToken(token);
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Entity group = new Entity("Group");
        group.setProperty("name", groupName);
        group.setProperty("type", type);
        group.setProperty("description", description);
        group.setProperty("adminId", userId);

        Date created = new Date( );
        group.setProperty("created", created);
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
    public VenueBean createVenue( @Named("token") String token, @Named("city") String city, @Named("street") String street,
                                  @Named("housenr") String housenr,@Named("latitude") float latitude,@Named("longitude") float longitude,
                                  @Named("description") String description, @Named("type") int type ) throws UnauthorizedException{

        String adminId = _getUserIdForToken(token);
        Entity venue = new Entity("Venue");
        venue.setProperty("created", new Date());
        venue.setProperty("adminId", adminId);
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
        String userId = _getUserIdForToken(token);
        GroupBean response = null;
        try {
            response = _getGroupBean(groupId);
        } catch (EntityNotFoundException e) {
            e.printStackTrace();
        }

        return response;
    }

    @ApiMethod(name = "getVenueInfo")
    public VenueBean getVenueInfo(@Named("token") String token, @Named("venueId") long venueId) throws UnauthorizedException {
        String userId = _getUserIdForToken(token);
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
        String userId = _getUserIdForToken(token);

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
    public VenueBean checkInVenue(@Named("token") String token, @Named("venueId") long venueId, @Named("groupId") long groupId) throws UnauthorizedException, InternalServerErrorException {
        String userId = _getUserIdForToken(token);
        _checkInVenue(userId, groupId, venueId);
        return _orderVenueBean(_getVenueBean(venueId));

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

        Key userKey = KeyFactory.createKey("Session", token);
        datastore.delete(userKey);

        CloseSessionResponse response = new CloseSessionResponse();
        response.setMessage("Success");
        return response;
    }

    @ApiMethod(name = "getNearbyVenues")
    public VenuesBean getNearbyVenues( @Named("latitude") float latitude, @Named("longitude") float longitude){
        VenuesBean result = new VenuesBean();
        ArrayList<VenueBean> venues = new ArrayList<>();
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Query q = new Query("Venue");
        PreparedQuery pq = datastore.prepare(q);
        for (Entity r : pq.asIterable()){

        }

        return result;
    }




    /**
     * Private helper methods
     */
    private String _getUserIdForToken(String token) throws UnauthorizedException {
        String userId;
        try {
            DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

            Key userKey = KeyFactory.createKey("Session", token);
            Entity user = datastore.get(userKey);
            userId = ((String) user.getProperty("userId"));
        } catch (EntityNotFoundException e) {
            throw new UnauthorizedException("Not authorized for this request.");
        }

        return userId;
    }

    private void _registerUserInGroup(String userId, long groupId) throws EntityNotFoundException{
        //TODO: Check whether group exists before registering
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        GroupsBean groups = _getGroupsForUser(userId);
        GroupBean group;
        try {
            group = _getGroupBean(groupId);
        } catch (EntityNotFoundException e) {
            e.printStackTrace();
            throw e;
        }
        //checking 1
        for(GroupBean g:groups.getGroups()){
            if(g.getType()==group.getType()){
                // User is already member of group with same type
                // TODO: handle exception!
                return;
            }
        }
        //checking if amount of members of group doesn't exceed the limit
        Query.Filter filter1 =
                new Query.FilterPredicate("userGroup",
                        Query.FilterOperator.EQUAL,
                        groupId);
        Query q = new Query("userGroup").setFilter(filter1);
        PreparedQuery pq = datastore.prepare(q);
        int amount = pq.countEntities(FetchOptions.Builder.withDefaults());

        if((group.getType()==GroupBean.SMALL && amount+1>GroupBean.AMOUNT_SMALL)
            ||(group.getType()==GroupBean.MEDIUM && amount+1>GroupBean.AMOUNT_MEDIUM)){
            // groups to small to add extra member
            // TODO: handle exception!
            return;
        }


        Entity userGroup = new Entity("userGroup");
        userGroup.setProperty("userId", userId);
        userGroup.setProperty("groupId", groupId);
        userGroup.setProperty("joined", new Date());
        datastore.put(userGroup);
    }

    private CheckinBean _checkInVenue(String userId, long groupId, long venueId){

        //TODO: check if checkin is valid?
        // - niet te snel na elkaar?
        // - user wel in de omgeving van de venue?

        //TODO: calc points
        int points = 1;

        Entity checkinEnt = new Entity("Checkin");
        checkinEnt.setProperty("time", new Date());
        checkinEnt.setProperty("points", points);
        checkinEnt.setProperty("userId", userId);
        checkinEnt.setProperty("venueId", venueId);
        checkinEnt.setProperty("groupId", groupId);

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
        groupBean.setType((Integer) group.getProperty("type"));

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

    private VenueBean _getVenueBean(Entity venue) {

        VenueBean venue2 = new VenueBean();
        venue2.setVenueId(venue.getKey().getId());
        venue2.setCreated((Date) venue.getProperty("created"));
        venue2.setAdminId((String) venue.getProperty("adminId"));
        venue2.setCity((String) venue.getProperty("city"));
        venue2.setStreet((String) venue.getProperty("street"));
        venue2.setHouseNr((String) venue.getProperty("housenr"));
        venue2.setDescription((String) venue.getProperty("description"));
        venue2.setLatitude((float) venue.getProperty("latitude"));
        venue2.setLongitude((float) venue.getProperty("longitude"));
        venue2.setType((int) venue.getProperty("type"));

        return venue2;
    }


    private CheckinBean _getCheckinBean(Entity checkin) {

        CheckinBean checkin2 = new CheckinBean();


        return checkin2;
    }


    private UserBean _getUserBeanForId(String userId) throws EntityNotFoundException {
        UserBean bean = new UserBean();

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Key userKey = KeyFactory.createKey("User", userId);
        Entity user = datastore.get(userKey);
        bean.setUserId(userId);
        bean.setEmail((String) user.getProperty("email"));
        bean.setFirstName((String) user.getProperty("firstName"));
        bean.setLastName((String) user.getProperty("lastName"));
        bean.setJoind((Date) user.getProperty("joined"));

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


    private void _registerGroupInVenue(long venueId, long groupId){
        //TODO: Check whether group exists before registering
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Entity groupVenue = new Entity("groupVenue");
        groupVenue.setProperty("venueId", venueId);
        groupVenue.setProperty("groupId", groupId);
        groupVenue.setProperty("points", 1);
        datastore.put(groupVenue);
    }


    private VenueBean _getVenueBean(long venueId) {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        VenueBean venuebean = new VenueBean();
        venuebean.setVenueId(venueId);
        RankingBean rank = null;
        List<RankingBean> ranking = new ArrayList<RankingBean>();
        Query.Filter propertyFilter =
                new Query.FilterPredicate("venueId",
                        Query.FilterOperator.EQUAL,
                        venueId);
        Query q = new Query("groupVenue").setFilter(propertyFilter);
        PreparedQuery pq = datastore.prepare(q);
        for (Entity r : pq.asIterable()) {
            rank = new RankingBean();
            rank.setPoints((long) r.getProperty("points"));
            try {
                rank.setGroupBean(_getGroupBean((long) r.getProperty("groupId")));
            } catch (EntityNotFoundException e) {
                e.printStackTrace();
            }
            ranking.add(rank);
        }
        venuebean.setRanking(ranking);
        return venuebean;
    }

    private VenueBean _orderVenueBean(VenueBean venue) {
        List<RankingBean> ranking = venue.getRanking();
        Collections.sort(ranking, comparator);
        venue.setRanking(ranking);
        return venue;
    }

    private static Comparator<RankingBean> comparator = new Comparator<RankingBean>() {

        public int compare(RankingBean bean1, RankingBean bean2) {
            return Long.valueOf(bean1.getPoints())
                    .compareTo(Long.valueOf(bean2.getPoints()));
        }

    };
}
