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

/**
 * An endpoint class we are exposing
 */
@Api(name = "myApi", version = "v1", namespace = @ApiNamespace(ownerDomain = "backend.vop.ugent.be", ownerName = "backend.vop.ugent.be", packagePath = ""))
public class MyEndpoint {

    private SecureRandom random = new SecureRandom();

    @ApiMethod(name = "sayHi")
    public MyBean sayHi (@Named("name") String name) {
        MyBean response = new MyBean();
        response.setData("Hi, "+ name);
        return response;
    }

    @ApiMethod(name = "getUserInfo")
    public UserBean getUserInfo(@Named("token") String token) throws UnauthorizedException {
        long userId = _getUserIdForToken(token);
        UserBean response = null;
        try {
            response = _getUserBeanForId(userId);
        } catch (EntityNotFoundException e) {
            e.printStackTrace();
        }

        return response;
    }

    @ApiMethod(name = "createGroup")
    public GroupBean createGroup(@Named("token") String token, @Named("groupName") String groupName) throws UnauthorizedException {
        long userId = _getUserIdForToken(token);
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Entity group = new Entity("Group");
        group.setProperty("name", groupName);
        group.setProperty("adminId", userId);

        Date created = new Date();
        group.setProperty("created", created);
        datastore.put(group);

        long groupId = group.getKey().getId();
        _registerUserInGroup(userId, groupId);

        GroupBean groupBean = null;
        try {
            groupBean = _getGroupBean(groupId);
        } catch (EntityNotFoundException e) {
            e.printStackTrace();
        }

        return groupBean;
    }

    @ApiMethod(name = "getGroupInfo")
    public GroupBean getGroupInfo(@Named("token") String token, @Named("groupId") long groupId) throws UnauthorizedException {
        long userId = _getUserIdForToken(token);
        GroupBean response = null;
        try {
            response = _getGroupBean(groupId);
        } catch (EntityNotFoundException e) {
            e.printStackTrace();
        }

        return response;
    }


    @ApiMethod(name = "getGroupsForUser")
    public GroupsBean getGroupsForUser(@Named("token") String token) throws UnauthorizedException, InternalServerErrorException {
        long userId = _getUserIdForToken(token);

        AllGroupsBean response = null;
        UserBean user = null;

        try {
            response = _getAllGroups();
        } catch (EntityNotFoundException e) {
            e.printStackTrace();
            throw new InternalServerErrorException("Sorry, we screwed something up...");
        }
        try {
            user = _getUserBeanForId(userId);
        } catch (EntityNotFoundException e) {
            e.printStackTrace();
        }
        GroupsBean groupsbean = new GroupsBean();
        List<GroupBean> groups = response.getGroups();
        for(GroupBean g:groups){
            if(!g.getMembers().contains(user)){
                groups.remove(g);
            }
        }
        groupsbean.setGroups(groups);
        groupsbean.setNumGroups(groups.size());
        return groupsbean;
    }



    @ApiMethod(name = "getAllGroups")
    public AllGroupsBean getAllGroups(@Named("token") String token) throws UnauthorizedException, InternalServerErrorException {
        long userId = _getUserIdForToken(token);

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
        long userId = _getUserIdForToken(token);
        GroupBean response = null;
        try{
            _registerUserInGroup(userId, groupId);
            response = _getGroupBean(groupId);
        }catch(EntityNotFoundException e){
            throw new InternalServerErrorException("Requested group does not exist!");
        }

        return response;
    }

    @ApiMethod(name = "checkInVenue")
    public VenueBean checkInVenue(@Named("token") String token, @Named("venueId") String venueId, @Named("groupId") long groupId) throws UnauthorizedException, InternalServerErrorException {
        long userId = _getUserIdForToken(token);
        if(!(_existingVenue(venueId))){
        _createVenue(venueId);
        }
        _updateVenueRanking(venueId,groupId);
        try {
            return _getVenueBean(venueId);
        } catch (EntityNotFoundException e) {
            e.printStackTrace();
            throw new InternalServerErrorException("Sorry, we screwed something up...");
        }


    }


    @ApiMethod(name = "getAuthToken")
    public AuthTokenResponse getAuthToken(@Named("fsUserID") long fsUserId, @Named("fsToken") String fsToken) throws UnauthorizedException, InternalServerErrorException{
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

            long returnedUserId = user.getInt("id");
            String firstName = user.getString("firstName");
            String lastName = "";

            if(user.has("lastName"))
                lastName = user.getString("lastName");

            JSONObject contact = user.getJSONObject("contact");
            String email = contact.getString("email");

            response.setUserId(returnedUserId);

            if(returnedUserId == fsUserId){
                // User is who he claims to be
                // Check if we already saved his info
                try{
                    UserBean userBean = _getUserBeanForId(returnedUserId);
                }catch (EntityNotFoundException e){
                    // User not in our database
                    Entity userEntity = new Entity("User", returnedUserId);
                    userEntity.setProperty("fsUserId", returnedUserId);
                    userEntity.setProperty("firstName", firstName);
                    userEntity.setProperty("lastName", lastName);
                    userEntity.setProperty("email", email);
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

    /**
     * Private helper methods
     */
    private long _getUserIdForToken(String token) throws UnauthorizedException {
        long userId;
        try {
            DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

            Key userKey = KeyFactory.createKey("Session", token);
            Entity user = datastore.get(userKey);
            userId = ((Long) user.getProperty("userId")).longValue();
        } catch (EntityNotFoundException e) {
            throw new UnauthorizedException("Not authorized for this request.");
        }

        return userId;
    }

    private void _registerUserInGroup(long fsUserId, long groupId){
        //TODO: Check whether group exists before registering
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Entity userGroup = new Entity("userGroup");
        userGroup.setProperty("fsUserId", fsUserId);
        userGroup.setProperty("groupId", groupId);
        datastore.put(userGroup);
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
        groupBean.setAdminId(((Long) group.getProperty("adminId")).longValue());
        groupBean.setCreated((Date) group.getProperty("created"));

        ArrayList<UserBean> members = new ArrayList<>();

        Query.Filter propertyFilter =
                new Query.FilterPredicate("groupId",
                        Query.FilterOperator.EQUAL,
                        group.getKey().getId());

        Query q = new Query("userGroup").setFilter(propertyFilter);

        PreparedQuery pq = datastore.prepare(q);
        for (Entity r : pq.asIterable()){
            long fsUserId = ((Long) r.getProperty("fsUserId")).longValue();
            members.add(_getUserBeanForId(fsUserId));
        }

        groupBean.setMembers(members);

        return groupBean;
    }

    private UserBean _getUserBeanForId(long fsUserId) throws EntityNotFoundException {
        UserBean bean = new UserBean();

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Key userKey = KeyFactory.createKey("User", fsUserId);
        Entity user = datastore.get(userKey);
        bean.setFsUserId(fsUserId);
        bean.setEmail((String) user.getProperty("email"));
        bean.setFirstName((String) user.getProperty("firstName"));
        bean.setLastName((String) user.getProperty("lastName"));

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

    private VenueBean _getVenueBean(String venueId) throws EntityNotFoundException {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Key venueKey = KeyFactory.createKey("Venue", venueId);
        Entity venue = datastore.get(venueKey);

        return _getVenueBean(venue);
    }

    private VenueBean _getVenueBean(Entity venue){
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        VenueBean venuebean = new VenueBean();
        venuebean.setVenueId((String)venue.getProperty("venueId"));
        venuebean.setRanking((ArrayList<RankingBean>) venue.getProperty("ranking"));

        return venuebean;
    }

    private void _createVenue(String venueId) {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Entity venue = new Entity("Venue",venueId);
        venue.setProperty("venueId", venueId);
        List<RankingBean> ranking = new ArrayList<RankingBean>();
        venue.setProperty("ranking", ranking);
        datastore.put(venue);
    }

    private boolean _existingVenue(String venueId) {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        try {
            Key venueKey = KeyFactory.createKey("Venue", venueId);
            datastore.get(venueKey);
            return true;
        }catch (EntityNotFoundException e){
            return false;
        }
    }

    private void _updateVenueRanking(String venueId,long groupId){
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        VenueBean venue = null;
        Entity venue_entity = null;
        Key venueKey = KeyFactory.createKey("Venue", venueId);
        try {
            venue_entity = datastore.get(venueKey);
        } catch (EntityNotFoundException e) {
            e.printStackTrace();
        }
        try {
            venue = _getVenueBean(venueId);
        } catch (EntityNotFoundException e) {
            e.printStackTrace();
        }
        GroupBean group = null;
        try {
            group=_getGroupBean(groupId);
        } catch (EntityNotFoundException e) {
            e.printStackTrace();
        }
        boolean group_in_ranking = false;
        ArrayList<RankingBean> ranking = (ArrayList<RankingBean>)venue.getRanking();
        for(RankingBean i: ranking){
            if(i.getGroup()==group){
                i.setPoints(i.getPoints()+1);
                group_in_ranking = true;
            }
        }
        if(!group_in_ranking){
            RankingBean rank = new RankingBean();
            rank.setPoints(1);
            rank.setGroup(group);
            rank.setVenue(venue);
            ranking.add(rank);
        }
        venue_entity.setProperty("ranking",ranking);
        datastore.put(venue_entity);

    }

}
