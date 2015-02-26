package be.ugent.vop.foursquare;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.app.ProgressDialog;

import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Message;

import be.ugent.vop.R;

public class FoursquareAPI {

    private static String FSQToken;
    private static final String API_URL = "https://api.foursquare.com/v2";
    private static final String VERSION = "20150101";
    private static final String MODE = "foursquare";

    private static FoursquareAPI instance;

    private boolean DEBUG = true;

    private FoursquareAPI(){}


    public static FoursquareAPI get(Context context){
        if(instance==null){
            SharedPreferences prefs = context.getSharedPreferences(context.getString(R.string.sharedprefs), Context.MODE_PRIVATE);
            FSQToken = prefs.getString(context.getString(R.string.foursquaretoken), "N.A.");
            instance= new FoursquareAPI();
            instance.FSQToken=FSQToken;
        }
        return instance;
    }

    public ArrayList<FoursquareVenue> getNearby(double longitude, double latitude/*toevoegen radius*/)  {
        String URL =API_URL + "/venues/search?ll=" + longitude +","+ latitude+ "&oauth_token=" 		+ FSQToken + "&v=" + VERSION+ "&m=" + MODE;
        ArrayList<FoursquareVenue> venueList = new ArrayList<>();

        try {
            String response = request(URL);
            JSONArray venues = new JSONObject(response).getJSONObject("response").getJSONArray("venues");

            int numVenues = venues.length();
                for (int i = 0; i < numVenues; i++) {
                    JSONObject venue = venues.getJSONObject(i);
                    String id = "no id available";
                    String city="no city info available";
                    String name="no name info available";
                    String address="no address available";
                    String country="no country info available";
                    double lon=-1;
                    double lat=-1;

                    if(venue.has("id")) id = venue.getString("id");
                    if(venue.has("name")) name = venue.getString("name");

                    JSONObject location = venue.getJSONObject("location");

                    if(location.has("lng")) lon = location.getDouble("lng");
                    if(location.has("lat")) lat = location.getDouble("lat");


                    if(location.has("address"))  address = location.getString("address");
                    if(location.has("city"))  city = location.getString("city");
                    if(location.has("country"))  country = location.getString("country");

                    venueList.add(new FoursquareVenue(id,name,address,city,country,lon,lat));
                }
        } catch(IOException e){
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(DEBUG){
        String s = "size: "+venueList.size()+" ";
        for(FoursquareVenue v:venueList){
          s+=v.toString();
        }
        Log.d("FoursquareAPI",s);
        }
        return venueList;
    }

    public int checkin(FoursquareVenue checkinVenue)  {
       /* String URL =API_URL + "/checkins/add?venueId="+checkinVenue.id+"&oauth_token="+ FSQToken + "&v=" + VERSION+ "&m=" + MODE;
        ArrayList<FoursquareVenue> venueList = new ArrayList<>();

        try {
            String response = request(URL);
            JSONArray venues = new JSONObject(response).getJSONObject("response").getJSONArray("venues");

            int numVenues = venues.length();
            for (int i = 0; i < numVenues; i++) {
                JSONObject venue = venues.getJSONObject(i);

                FoursquareVenue v = new FoursquareVenue();
                v.id = venue.getString("id");
                v.name = venue.getString("name");

                JSONObject location = venue.getJSONObject("location");

                Location loc = new Location(LocationManager.GPS_PROVIDER);

                loc.setLatitude(Double.valueOf(location.getString("lat")));
                loc.setLongitude(Double.valueOf(location.getString("lng")));

                v.location = loc;
                v.address = location.getString("address");
                v.distance = location.getInt("distance");

                if(venue.has("hereNow")){ // hereNow is optional in response
                    JSONObject hereNow = venue.getJSONObject("hereNow");
                    v.herenow = hereNow.getInt("count");
                    JSONArray groups = hereNow.getJSONArray("groups");
                    if(groups.length() > 0)
                        v.type = groups.getJSONObject(0).getString("type");
                }

                venueList.add(v);
            }
        } catch(IOException e){
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        */
        return 1;
    }



    private String request(String URL) throws IOException {
        String response = "";
        try {
            URL url 	= new URL(URL);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoInput(true);
            urlConnection.connect();
            Log.d("request.fsqapi", "Response Code: " + urlConnection.getResponseCode());
            response = streamToString(urlConnection.getInputStream());

        } catch (Exception ex) {
            throw ex;
        }
        return response;
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





