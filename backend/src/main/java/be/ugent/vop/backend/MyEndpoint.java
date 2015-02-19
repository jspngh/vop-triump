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

import javax.inject.Named;

/**
 * An endpoint class we are exposing
 */
@Api(name = "myApi", version = "v1", namespace = @ApiNamespace(ownerDomain = "backend.vop.ugent.be", ownerName = "backend.vop.ugent.be", packagePath = ""))
public class MyEndpoint {

    private SecureRandom random = new SecureRandom();

    /**
     * A simple endpoint method that takes a name and says Hi back
     */
    @ApiMethod(name = "sayHi")
    public MyBean sayHi(@Named("name") String name) {
        MyBean response = new MyBean();
        response.setData("Hi, " + name);

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Entity user = new Entity("User");
        user.setProperty("name", name);
        datastore.put(user);

        return response;
    }

    @ApiMethod(name = "listUsers")
    public UserList listUsers(@Named("token") String token) throws UnauthorizedException {
        long                     userId = getUserIdForToken(token);
        if (userId == 0)
            throw new UnauthorizedException("Not authorized for this request.");

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Query q = new Query("User");

        PreparedQuery pq = datastore.prepare(q);
        UserList result = new UserList();
        ArrayList<String> users = new ArrayList<>();
        for (Entity r : pq.asIterable()){
            String name = (String) r.getProperty("name");
            users.add(name);
        }

        result.setUserList(users);

        return result;
    }

    private long getUserIdForToken(String token){
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        Query.Filter tokenFilter = new Query.FilterPredicate("sessionToken",
                Query.FilterOperator.EQUAL,
                token);
        Query q = new Query("Session").setFilter(tokenFilter);

        PreparedQuery pq = datastore.prepare(q);

        long userId = 0;
        for (Entity result : pq.asIterable()){
            userId = ((Long) result.getProperty("userId")).longValue();
            break;
        }

        return userId;
    }

    @ApiMethod(name = "getAuthToken")
    public AuthTokenResponse getAuthToken(@Named("fsUserID") long fsUserId, @Named("fsToken") String fsToken) throws UnauthorizedException, InternalServerErrorException{
        AuthTokenResponse response = new AuthTokenResponse();
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

            response.setUserId(returnedUserId);

            if(returnedUserId == fsUserId){
                // User is who he claims to be
                // Create session for user and send back auth token

                // Create random token
                String sessionToken = new BigInteger(256, random).toString(32);

                // Store session information in datastore
                DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
                Entity session = new Entity("Session");
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

}
