package be.ugent.vop.foursquare;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
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
import android.net.Uri;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;

import be.ugent.vop.R;
import be.ugent.vop.database.MySQLiteHelper;
import be.ugent.vop.database.VenueTable;
import be.ugent.vop.database.contentproviders.VenueContentProvider;
import be.ugent.vop.database.contentproviders.VenueImageContentProvider;
import be.ugent.vop.database.VenueImageTable;

public class FoursquareAPI {

    private static final String TAG = "FoursquareAPI";

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
            longitude= prefs.getFloat(context.getString(R.string.locationLongitude), (float) 50.846);
            latitude=prefs.getFloat(context.getString(R.string.locationLatitude), (float) 4.352);
        }
        ArrayList<FoursquareVenue> result = getNearbyVenues(latitude, longitude, 4, 100000);
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
    public ArrayList<FoursquareVenue> getNearbyVenues(Location loc)  {
        //default coordinates (Brussels) in case GPS Provider is disabled
        double longitude= (double) 50.846;
        double latitude= (double) 4.352;

        /*SharedPreferences prefs = context.getSharedPreferences(context.getString(R.string.sharedprefs), Context.MODE_PRIVATE);
        if(prefs.getBoolean(context.getString(R.string.locationAvailable),false)){
            longitude= prefs.getFloat(context.getString(R.string.locationLongitude),(float)50.846);
            latitude=prefs.getFloat(context.getString(R.string.locationLatitude),(float)4.352);
        }*/

        if(loc != null){
            longitude = loc.getLongitude();
            latitude = loc.getLatitude();
        }

        return getNearbyVenues(latitude,longitude,50,100000);
    }

    public FoursquareVenue getVenueInfo(String venueId)  {

     //   return new FoursquareVenue("id","naam","adres","stad","land",-1.0,-1.0,true);
        String id = "no id available";
        String city="no city info available";
        String name="no name info available";
        String address="no address available";
        String country="no country info available";
        boolean verified = false;
        double lon=-1;
        double lat=-1;

        Uri venueUri = Uri.parse(VenueContentProvider.CONTENT_URI + "/" + venueId);
        String[] projection =
                {VenueTable.COLUMN_VENUE_ID, VenueTable.COLUMN_CITY, VenueTable.COLUMN_NAME,
                        VenueTable.COLUMN_ADDRESS,VenueTable.COLUMN_COUNTRY,VenueTable.COLUMN_VERIFIED,
                        VenueTable.COLUMN_LATITUDE,VenueTable.COLUMN_LONGITUDE};
        Cursor v = context.getContentResolver().query(venueUri, projection, null, null, null);

        if(v.getCount()>=0){
            v.moveToFirst();
            id = v.getString(v.getColumnIndexOrThrow(VenueTable.COLUMN_VENUE_ID));
            city=v.getString(v.getColumnIndexOrThrow(VenueTable.COLUMN_CITY));
            name=v.getString(v.getColumnIndexOrThrow(VenueTable.COLUMN_NAME));
            address=v.getString(v.getColumnIndexOrThrow(VenueTable.COLUMN_ADDRESS));
            country=v.getString(v.getColumnIndexOrThrow(VenueTable.COLUMN_COUNTRY));
            verified =
                    v.getInt(v.getColumnIndexOrThrow(VenueTable.COLUMN_VERIFIED))==0? false:true;
            lat= v.getDouble(v.getColumnIndexOrThrow(VenueTable.COLUMN_LATITUDE));
            lon=v.getDouble(v.getColumnIndexOrThrow(VenueTable.COLUMN_LONGITUDE));
            v.close();
            //Log.d(TAG, "getVenueInfo: loaded venue from local db");

        }else{
            String url =API_URL + "/venues/" + venueId +"?oauth_token="+ FSQToken + "&v=" + VERSION+ "&m=" + MODE;
            //Log.d("FoursquareAPI", "getVenueInfo used url:"+url);

            try {
                String response = request(url);
                JSONObject obj = new JSONObject(response);
                if((obj.getJSONObject("meta").getInt("code"))==200){

                        JSONObject venue = obj.getJSONObject("response").getJSONObject("venue");

                        if(venue.has("id")) id = venue.getString("id");
                        if(venue.has("name")) name = venue.getString("name");
                        if(venue.has("verified")) verified = venue.getBoolean("verified");

                        JSONObject location = venue.getJSONObject("location");

                        if(location.has("lng")) lon = location.getDouble("lng");
                        if(location.has("lat")) lat = location.getDouble("lat");


                        if(location.has("address"))  address = location.getString("address");
                        if(location.has("city"))  city = location.getString("city");
                        if(location.has("country"))  country = location.getString("country");

                }
            } catch(IOException e){
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            //Log.d(TAG, "getVenueInfo: loaded venue from foursquare");
        }

        FoursquareVenue venue = new FoursquareVenue(id,name,address,city,country,lon,lat,verified);
        venue.setPhotos(getPhotos(venue));
        return venue;
    }

    public void saveVenue(FoursquareVenue venue){
        Uri venueUri = Uri.parse(VenueContentProvider.CONTENT_URI + "/" + venue.getId());
        String[] projection =
                {VenueTable.COLUMN_VENUE_ID};
        Cursor v = context.getContentResolver().query(venueUri, projection, null, null, null);
        if(v.getCount()==0){
        ContentValues contentValues = new ContentValues();
        contentValues.put(VenueTable.COLUMN_VENUE_ID, venue.getId());
        contentValues.put(VenueTable.COLUMN_NAME, venue.getName());
        contentValues.put(VenueTable.COLUMN_ADDRESS, venue.getAddress());
        contentValues.put(VenueTable.COLUMN_CITY, venue.getCity());
        contentValues.put(VenueTable.COLUMN_COUNTRY, venue.getCountry());
        contentValues.put(VenueTable.COLUMN_LATITUDE,venue.getLatitude());
        contentValues.put(VenueTable.COLUMN_LONGITUDE,venue.getLongitude() );
        contentValues.put(VenueTable.COLUMN_VERIFIED, venue.isVerified());
        contentValues.put(VenueTable.COLUMN_LAST_UPDATED, (new Date()).getTime());
        context.getContentResolver().insert(VenueContentProvider.CONTENT_URI, contentValues);
        //Log.d(TAG, "saved venue in db with id: "+venue.getId());
        }
        else{
            //Log.d(TAG,"venue "+venue.getId()+" already in db.");
        }
    }

    //only returns verified venues
    public ArrayList<FoursquareVenue> getNearbyVenues(double latitude, double longitude, int limit, int radius)  {

        String url =API_URL + "/venues/search?ll=" + latitude +","+longitude +"&radius="+radius+"&limit="+limit+"&oauth_token="+ FSQToken + "&v=" + VERSION+ "&m=" + MODE;
        //Log.d("FoursquareAPI", url);
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
                        boolean verified = false;
                        double lon=-1;
                        double lat=-1;

                        if(venue.has("id")) id = venue.getString("id");
                        if(venue.has("name")) name = venue.getString("name");
                        if(venue.has("verified")) verified = venue.getBoolean("verified");

                        JSONObject location = venue.getJSONObject("location");

                        if(location.has("lng")) lon = location.getDouble("lng");
                        if(location.has("lat")) lat = location.getDouble("lat");


                        if(location.has("address"))  address = location.getString("address");
                        if(location.has("city"))  city = location.getString("city");
                        if(location.has("country"))  country = location.getString("country");

                        FoursquareVenue foursquareVenue =
                                new FoursquareVenue(id,name,address,city,country,lon,lat,verified);
                        foursquareVenue.setPhotos(getPhotos(foursquareVenue));

                        // only adds verified venues.
                        if(true || foursquareVenue.isVerified()) {
                            venueList.add(foursquareVenue);
                            saveVenue(foursquareVenue);
                        }

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
        //Log.d("FoursquareAPI",s);
        }
        return venueList;
    }

    public ArrayList<Photo> getPhotos(FoursquareVenue venue) {
        ArrayList<Photo> photos = new ArrayList<>();

        /*
        Try to get images from local database
         */
        Uri venueUri = Uri.parse(VenueImageContentProvider.CONTENT_URI + "/" + venue.getId());
        String[] projection = {VenueImageTable.COLUMN_PREFIX, VenueImageTable.COLUMN_SUFFIX, VenueImageTable.COLUMN_HEIGHT, VenueImageTable.COLUMN_WIDTH};
        Cursor images = context.getContentResolver().query(venueUri, projection, null, null, null);

        if (images.getCount() > 0) {
            //Log.d(TAG, "" + images.getCount() + " images found for venue " + venue.getId() + " in database");
            while (images.moveToNext()) {
                String prefix = images.getString(images.getColumnIndexOrThrow(VenueImageTable.COLUMN_PREFIX));
                String suffix = images.getString(images.getColumnIndexOrThrow(VenueImageTable.COLUMN_SUFFIX));
                int width = images.getInt(images.getColumnIndexOrThrow(VenueImageTable.COLUMN_WIDTH));
                int height = images.getInt(images.getColumnIndexOrThrow(VenueImageTable.COLUMN_HEIGHT));

                if(width == 0)
                    break; // The width of the photo is 0, indicating this venue has no associated photos. So break and return empty List.

                photos.add(new Photo(prefix, suffix, width, height));
            }

            images.close();
        } else {
            //Log.d(TAG, "no images found for venue " + venue.getId() + " in database, retrieving from Foursquare");
            photos = getPhotosFromFS(venue);
            savePhotos(photos, venue.getId());
        }

        return photos;
    }

    private void savePhotos(ArrayList<Photo> photos, String venueId){
        ContentValues[] values = new ContentValues[photos.size()];
        Date lastUpdated = new Date();
        int i = 0;

        if(photos.size() == 0){
            // Foursquare doesn't have any photos for this venue, but we still have to save a record so we know there aren't any
            ContentValues v = new ContentValues();
            v.put(VenueImageTable.COLUMN_VENUE_ID, venueId);
            v.put(VenueImageTable.COLUMN_PREFIX, "a");
            v.put(VenueImageTable.COLUMN_SUFFIX, "a");
            v.put(VenueImageTable.COLUMN_WIDTH, 0); // a width of 0 indicates this is not a photo
            v.put(VenueImageTable.COLUMN_HEIGHT, 0);
            v.put(VenueImageTable.COLUMN_LAST_UPDATED, lastUpdated.getTime());

            context.getContentResolver().insert(VenueImageContentProvider.CONTENT_URI, v);
        }else{
            for(Photo p : photos){
                ContentValues v = new ContentValues();
                v.put(VenueImageTable.COLUMN_VENUE_ID, venueId);
                v.put(VenueImageTable.COLUMN_PREFIX, p.getPrefix());
                v.put(VenueImageTable.COLUMN_SUFFIX, p.getSuffix());
                v.put(VenueImageTable.COLUMN_WIDTH, p.getWidth());
                v.put(VenueImageTable.COLUMN_HEIGHT, p.getHeight());
                v.put(VenueImageTable.COLUMN_LAST_UPDATED, lastUpdated.getTime());

                values[i++] = v;
            }
        }

        int numSaved = context.getContentResolver().bulkInsert(VenueImageContentProvider.CONTENT_URI, values);

        //Log.d(TAG, "" + numSaved + " images saved for venue " + venueId + " in database");
    }

    private ArrayList<Photo> getPhotosFromFS(FoursquareVenue venue){
        ArrayList<Photo> photos = new ArrayList<>();
        String prefix, suffix;
        int width, height;
        String url =API_URL + "/venues/"+venue.getId()+"/photos?&limit=10&oauth_token=" + FSQToken + "&v=" + VERSION+ "&m=" + MODE;
        //Log.d("FoursquareAPI getPhotos", url);

        try {
            String response = request(url);
            JSONObject obj = new JSONObject(response);
            if((obj.getJSONObject("meta").getInt("code"))==200){
                JSONArray photoArray = obj.getJSONObject("response").getJSONObject("photos").getJSONArray("items");
                    for(int i=0;i<photoArray.length();i++){
                        prefix = photoArray.getJSONObject(i).getString("prefix");
                        suffix = photoArray.getJSONObject(i).getString("suffix");
                        width = photoArray.getJSONObject(i).getInt("width");
                        height = photoArray.getJSONObject(i).getInt("height");
                        photos.add(new Photo(prefix, suffix, width, height));
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
                //Log.d("Checking In", "Succes");
                JSONObject checkIn = obj.getJSONObject("response").getJSONObject("checkin");
                if(checkIn.has("id")) result.setId(checkIn.getString("id"));
                if(checkIn.has("createdAt")) result.setCreatedAt(checkIn.getString("createdAt"));
                if(checkIn.has("type")) result.setType(checkIn.getString("type"));
                if(checkIn.has("timeZoneOffset")) result.setTimeZoneOffset(checkIn.getString("timeZoneOffset"));
            }
        }
        catch (IOException | JSONException e){
                //Log.d("Checking In", "Failure");
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





