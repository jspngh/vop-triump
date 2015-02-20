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

import javax.inject.Named;

/**
 * An endpoint class we are exposing
 */
@Api(name = "myApi", version = "v1", namespace = @ApiNamespace(ownerDomain = "backend.vop.ugent.be", ownerName = "backend.vop.ugent.be", packagePath = ""))
public class MyEndpoint {

    private SecureRandom random = new SecureRandom();

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
        group.setProperty("groupName", groupName);
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
            String lastName = user.getString("lastName");
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
            throw new InternalServerErrorException("Whoops, we screwed something up :(");
        } catch (IOException e) {
            e.printStackTrace();
            throw new InternalServerErrorException("Whoops, we screwed something up :(");
        } catch (JSONException e) {
            e.printStackTrace();
            throw new InternalServerErrorException("Whoops, we screwed something up :(");
        }


        return response;
    }

    @ApiMethod(name = "closeSession")
    public void closeSession( @Named("token") String token){
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        Key userKey = KeyFactory.createKey("Session", token);
        datastore.delete(userKey);
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
}
