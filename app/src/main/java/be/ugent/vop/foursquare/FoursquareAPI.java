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

    private FoursquareAPI(){

    }


    public static FoursquareAPI get(Context context){
        if(instance==null){
            SharedPreferences prefs = context.getSharedPreferences(context.getString(R.string.sharedprefs), Context.MODE_PRIVATE);
            FSQToken = prefs.getString(context.getString(R.string.foursquaretoken), "N.A.");
            instance= new FoursquareAPI();
            instance.FSQToken=FSQToken;
        }
        return instance;
    }

    public ArrayList<FoursquareVenue> getNearBy(double longitude, double latitude/*toevoegen radius*/) {
        ArrayList<FoursquareVenue> venueList = new ArrayList<FoursquareVenue>();

        String URL = API_URL + "/venues/search?ll=" + longitude + "," + latitude + "&oauth_token=" + "OMUUX4BHXRTBNRLJ2QVQMC4UGRRR5TESI1XD02I4GCMV3G21" + "&v=" + VERSION + "&m=" + MODE;
		/* OPGELET: token is hier hard gecodeerd, moet nog aangepast worden! */
        try {
            String response = request(URL);
            JSONObject jsonObject = new JSONObject(response);

            JSONArray venuesJSON = (JSONArray) jsonObject.getJSONObject("response").getJSONArray("venues");
                    for (int j = 0; j < venuesJSON.length(); j++) {
                        JSONObject item = (JSONObject) venuesJSON.get(j);

                        FoursquareVenue venue = new FoursquareVenue();

                        venue.id = item.getString("id");
                        venue.name = item.getString("name");

                        JSONObject location = (JSONObject) item.getJSONObject("location");

                        Location loc = new Location(LocationManager.GPS_PROVIDER);

                        loc.setLatitude(Double.valueOf(location.getString("lat")));
                        loc.setLongitude(Double.valueOf(location.getString("lng")));

                        venue.location = loc;
                        venue.address = location.getString("address");
                        venue.distance = location.getInt("distance");
                        venue.herenow = item.getJSONObject("hereNow").getInt("count");

                        venueList.add(venue);
                    }
        }catch(Exception e){
        e.printStackTrace();
    }
            return venueList;
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





