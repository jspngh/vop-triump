package ugent.foursquareapi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.Context;
import android.util.Log;
import android.app.ProgressDialog;

import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Message;

/**
 * Created by vincent on 20/02/15.
 */
public class FSQApp {
    private static final String ACCESTOKEN = "OMUUX4BHXRTBNRLJ2QVQMC4UGRRR5TESI1XD02I4GCMV3G21";
    private static final String API_URL = "https://api.foursquare.com/v2";
    private static final String v = "20150219";

    private static final String LOG_TAG = "Method: fetchUserName";

    public FSQApp(){
        /* doorgeven van gebruikerstoken (verkregen na login). */
    }

    public String fetchUserName() {
        String firstName = new String();
        String lastName = new String();;
                try {
                    URL url 	= new URL(API_URL + "/users/self?oauth_token=" +ACCESTOKEN+ "&v=" + v+"&m=foursquare");

                    Log.d(LOG_TAG, "Opening URL " + url.toString());

                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                    urlConnection.setRequestMethod("GET");
                    urlConnection.setDoInput(true);

                    urlConnection.connect();

                    String response		= streamToString(urlConnection.getInputStream());
                    JSONObject jsonObj 	= (JSONObject) new JSONTokener(response).nextValue();

                    JSONObject resp		= (JSONObject) jsonObj.get("response");
                    JSONObject user		= (JSONObject) resp.get("user");

                    firstName 	= user.getString("firstName");
                    lastName		= user.getString("lastName");

                    Log.i(LOG_TAG, "Got user name: " + firstName + " " + lastName);


                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                return firstName + " " + lastName;
    }

    public ArrayList<FSQVenue> getNearby(double latitude, double longitude) throws Exception {
        ArrayList<FSQVenue> venueList = new ArrayList<FSQVenue>();

        try {
            String ll 	= String.valueOf(latitude) + "," + String.valueOf(longitude);
            URL url 	= new URL(API_URL + "/venues/search?ll=" + ll + "&oauth_token=" + ACCESTOKEN + "&v=" + v+"&m=foursquare");

            Log.d(LOG_TAG, "Opening URL " + url.toString());

            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.setRequestMethod("GET");
            urlConnection.setDoInput(true);
            //urlConnection.setDoOutput(true);

            urlConnection.connect();

            String response		= streamToString(urlConnection.getInputStream());
            JSONObject jsonObj 	= (JSONObject) new JSONTokener(response).nextValue();

            JSONArray groups	= (JSONArray) jsonObj.getJSONObject("response").getJSONArray("groups");

            int length			= groups.length();

            if (length > 0) {
                for (int i = 0; i < length; i++) {
                    JSONObject group 	= (JSONObject) groups.get(i);
                    JSONArray items 	= (JSONArray) group.getJSONArray("items");

                    int ilength 		= items.length();

                    for (int j = 0; j < ilength; j++) {
                        JSONObject item = (JSONObject) items.get(j);

                        FSQVenue venue 	= new FSQVenue();

                        venue.id 		= item.getString("id");
                        venue.name		= item.getString("name");

                        JSONObject location = (JSONObject) item.getJSONObject("location");

                        Location loc 	= new Location(LocationManager.GPS_PROVIDER);

                        loc.setLatitude(Double.valueOf(location.getString("lat")));
                        loc.setLongitude(Double.valueOf(location.getString("lng")));

                        venue.location	= loc;
                        venue.address	= location.getString("address");
                        venue.distance	= location.getInt("distance");
                        venue.herenow	= item.getJSONObject("hereNow").getInt("count");
                        venue.type		= group.getString("type");

                        venueList.add(venue);
                    }
                }
            }
        } catch (Exception ex) {
            throw ex;
        }

        return venueList;
    }



    private String streamToString(InputStream is) throws IOException {
        String str  = "";

        if (is != null) {
            StringBuilder sb = new StringBuilder();
            String line;

            try {
                BufferedReader reader 	= new BufferedReader(new InputStreamReader(is));

                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }

                reader.close();
            } finally {
                is.close();
            }

            str = sb.toString();
        }

        return str;
    }

}
