package be.ugent.vop;


import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;

import be.ugent.vop.database.VenueImageTable;
import be.ugent.vop.database.VenueTable;
import be.ugent.vop.database.contentproviders.VenueContentProvider;
import be.ugent.vop.database.contentproviders.VenueImageContentProvider;
import be.ugent.vop.foursquare.FoursquareVenue;
import be.ugent.vop.foursquare.Photo;

/**
 * A service responsible for saving changes to the content providers.
 */
public class SaveService extends IntentService {
    private static final String TAG = "SaveService";

    public static final String ACTION_SAVE_VENUE = "saveVenue";
    public static final String ACTION_SAVE_VENUE_PHOTOS = "saveVenuePhotos";

    public static final String EXTRA_VENUE = "venue";
    public static final String EXTRA_VENUE_ID = "venueId";
    public static final String EXTRA_PHOTOS = "photos";
    public static final String EXTRA_LOCATION_ID = "productLocationId";
    public static final String EXTRA_CATEGORIES_ARRAY = "catsArray";


    private static final int PERSIST_TRIES = 3;
    private static final String CONTENT_VALUES = "contentvalues";

    private Handler mMainHandler;

    public SaveService() {
        super(TAG);
        setIntentRedelivery(true);
        mMainHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public Object getSystemService(String name) {
        Object service = super.getSystemService(name);
        if (service != null) {
            return service;
        }

        return getApplicationContext().getSystemService(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Call an appropriate method. If we're sure it affects how incoming phone calls are
        // handled, then notify the fact to in-call screen.
        String action = intent.getAction();
        if (ACTION_SAVE_VENUE.equals(action)) {
            saveVenue(intent);
        } else if (ACTION_SAVE_VENUE_PHOTOS.equals(action)) {
            saveVenuePhotos(intent);
        }
    }

    /**
     * Creates an intent that can be sent to this service to create a new raw contact
     * using data presented as a set of ContentValues.
     */
    public static Intent createSaveVenueIntent(Context context, FoursquareVenue venue) {
        Intent serviceIntent = new Intent(
                context, SaveService.class);
        serviceIntent.setAction(SaveService.ACTION_SAVE_VENUE);

        serviceIntent.putExtra(SaveService.EXTRA_VENUE, venue);

        return serviceIntent;
    }

    private void saveVenue(Intent intent) {
        FoursquareVenue venue = intent.getParcelableExtra(SaveService.EXTRA_VENUE);

        Uri venueUri = Uri.parse(VenueContentProvider.CONTENT_URI + "/" + venue.getId());
        String[] projection =
                {VenueTable.COLUMN_VENUE_ID};
        Cursor v = getContentResolver().query(venueUri, projection, null, null, null);
        if (v.getCount() == 0) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(VenueTable.COLUMN_VENUE_ID, venue.getId());
            contentValues.put(VenueTable.COLUMN_NAME, venue.getName());
            contentValues.put(VenueTable.COLUMN_ADDRESS, venue.getAddress());
            contentValues.put(VenueTable.COLUMN_CITY, venue.getCity());
            contentValues.put(VenueTable.COLUMN_COUNTRY, venue.getCountry());
            contentValues.put(VenueTable.COLUMN_LATITUDE, venue.getLatitude());
            contentValues.put(VenueTable.COLUMN_LONGITUDE, venue.getLongitude());
            contentValues.put(VenueTable.COLUMN_VERIFIED, venue.isVerified());
            contentValues.put(VenueTable.COLUMN_LAST_UPDATED, (new Date()).getTime());
            getContentResolver().insert(VenueContentProvider.CONTENT_URI, contentValues);
            Log.d(TAG, "saved venue in db with id: " + venue.getId());
        } else {
            Log.d(TAG,"venue "+venue.getId()+" already in db.");
        }
        v.close();
    }

    /**
     * Creates an intent that can be sent to this service to create a new raw contact
     * using data presented as a set of ContentValues.
     */
    public static Intent createSaveVenuePhotosIntent(Context context, ArrayList<Photo> photos, String venueId) {
        Intent serviceIntent = new Intent(
                context, SaveService.class);
        serviceIntent.setAction(SaveService.ACTION_SAVE_VENUE_PHOTOS);

        serviceIntent.putParcelableArrayListExtra(SaveService.EXTRA_PHOTOS, photos);
        serviceIntent.putExtra(SaveService.EXTRA_VENUE_ID, venueId);

        return serviceIntent;
    }

    private void saveVenuePhotos(Intent intent){

        ArrayList<Photo> photos = intent.getParcelableArrayListExtra(SaveService.EXTRA_PHOTOS);
        String venueId = intent.getStringExtra(SaveService.EXTRA_VENUE_ID);

        Log.d(TAG, "photos: " + photos.size());

        for(Photo p : photos){
            Log.d(TAG, "Photo: " + p.getPrefix());
        }

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

            getContentResolver().insert(VenueImageContentProvider.CONTENT_URI, v);
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

        int numSaved = getContentResolver().bulkInsert(VenueImageContentProvider.CONTENT_URI, values);

        Log.d(TAG, "" + numSaved + " images saved for venue " + venueId + " in database");
    }

}