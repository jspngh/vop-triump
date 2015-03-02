package be.ugent.vop.foursquare;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import be.ugent.vop.R;

public class FoursquareAPI {

    private String FSQToken;
    private final String API_URL = "https://api.foursquare.com/v2";
    private final String VERSION = "20150101";
    private final String MODE = "foursquare";

    private Context context;


    private static FoursquareAPI instance;

    private boolean DEBUG = false;

    private FoursquareAPI(Context context){
        SharedPreferences prefs = context.getSharedPreferences(context.getString(R.string.sharedprefs), Context.MODE_PRIVATE);
        this.FSQToken = prefs.getString(context.getString(R.string.foursquaretoken), "N.A.");
        this.context = context;
    }


    public static FoursquareAPI get(Context context){
        if(instance==null){
            instance= new FoursquareAPI(context);
        }
        return instance;
    }

    public ArrayList<FoursquareVenue> getTopVenues(){
        //default coordinates (Brussels) in case GPS Provider is disabled
        float longitude= (float) 50.846;
        float latitude= (float) 4.352;

        SharedPreferences prefs = context.getSharedPreferences(context.getString(R.string.sharedprefs), Context.MODE_PRIVATE);
        if(prefs.getBoolean(context.getString(R.string.locationAvailable),false)){
            longitude= prefs.getFloat(context.getString(R.string.locationLongitude),(float)50.846);
            latitude=prefs.getFloat(context.getString(R.string.locationLatitude),(float)4.352);
        }
        ArrayList<FoursquareVenue> result = getNearbyVenues(latitude,longitude,4,100000);
        for(final FoursquareVenue venue:result){
            new Runnable(){
                @Override
                public void run() {
                    venue.setPhotos(getPhotos(venue));
                }
            }.run();
        }
        return result;
    }

    //
    // TODO: Functie uitbreiden zodat de straal groter wordt indien er te weinig locaties zijn?
    //
    public ArrayList<FoursquareVenue> getNearbyVenues(){
        //default coordinates (Brussels) in case GPS Provider is disabled
        float longitude= (float) 50.846;
        float latitude= (float) 4.352;

        SharedPreferences prefs = context.getSharedPreferences(context.getString(R.string.sharedprefs), Context.MODE_PRIVATE);
        if(prefs.getBoolean(context.getString(R.string.locationAvailable),false)){
            longitude= prefs.getFloat(context.getString(R.string.locationLongitude),(float)50.846);
            latitude=prefs.getFloat(context.getString(R.string.locationLatitude),(float)4.352);
        }
        return getNearbyVenues(latitude,longitude,50,100000);
    }



    public ArrayList<FoursquareVenue> getNearbyVenues(float latitude, float longitude, int limit, int radius)  {

        String url =API_URL + "/venues/search?ll=" + latitude +","+longitude +"&radius="+radius+"&limit="+limit+"&oauth_token="+ FSQToken + "&v=" + VERSION+ "&m=" + MODE;
        Log.d("FoursquareAPI", url);
        ArrayList<FoursquareVenue> venueList = new ArrayList<>();

        try {
            String response = request(url);
            JSONObject obj = new JSONObject(response);
            if((obj.getJSONObject("meta").getInt("code"))==200){

                JSONArray venues = obj.getJSONObject("response").getJSONArray("venues");

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

    public ArrayList<String> getPhotos(FoursquareVenue venue){
        ArrayList<String> photos = new ArrayList<>();
        String prefix, suffix;
        String url =API_URL + "/venues/"+venue.getId()+"/photos?&limit=10&oauth_token=" 		+ FSQToken + "&v=" + VERSION+ "&m=" + MODE;
        Log.d("FoursquareAPI getPhotos", url);


        try {
            String response = request(url);
            JSONObject obj = new JSONObject(response);
            if((obj.getJSONObject("meta").getInt("code"))==200){
                JSONArray photoArray = obj.getJSONObject("response").getJSONObject("photos").getJSONArray("items");
                    for(int i=0;i<photoArray.length();i++){
                        prefix = photoArray.getJSONObject(i).getString("prefix");
                        suffix = photoArray.getJSONObject(i).getString("suffix");
                        photos.add(prefix+"original"+suffix);
                    }

            }
        } catch(IOException e){
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }


        return photos;
    }

    public FoursquareCheckinObject checkIn(String venueId){
        FoursquareCheckinObject result = new FoursquareCheckinObject();
        String urlBase =API_URL + "/checkins/add";
        String urlParams = "venueId="+venueId+"&oauth_token="+FSQToken+"&v="+VERSION+"&m="+MODE;
        try {
            String response = post(urlBase, urlParams);
            JSONObject obj = new JSONObject(response);
            if ((obj.getJSONObject("meta").getInt("code")) == 200) {
                Log.d("Checking In", "Succes");
                Log.d("Response", obj.toString());
            }
        }
        catch (IOException | JSONException e){
                Log.d("Checking In", "Failure");
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Helpfunctions
     *
     */

    private String request(String URL) throws IOException {
        String response = "";
        try {
            URL url = new URL(URL);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoInput(true);
            urlConnection.connect();
            response = streamToString(urlConnection.getInputStream());

        } catch (Exception ex) {
            throw ex;
        }
        return response;
    }

    private String post(String base, String params) throws IOException {
        StringBuffer response = new StringBuffer();
        try {
            URL url = new URL(base);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            //add reuqest header
            urlConnection.setRequestMethod("POST");

            String urlParameters = params;

            // Send post request
            urlConnection.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.flush();
            wr.close();
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(urlConnection.getInputStream()));
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

        } catch (Exception e) {
            throw e;
        }
        return response.toString();
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





