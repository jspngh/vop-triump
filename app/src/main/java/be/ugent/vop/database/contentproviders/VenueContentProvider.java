/**
 * Created by vincent on 19/03/15.
 */
package be.ugent.vop.database.contentproviders;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import java.util.Arrays;
import java.util.HashSet;

import be.ugent.vop.database.MySQLiteHelper;
import be.ugent.vop.database.VenueTable;


public class VenueContentProvider extends ContentProvider {
    // database
    private MySQLiteHelper database;

    private static final String TAG = "VenueCP";

    // used for the UriMacher
    private static final int VENUES = 10;
    private static final int VENUE_ID = 20;

    private static final String AUTHORITY = "be.ugent.vop.database.contentproviders.venuecontentprovider";

    private static final String BASE_PATH = "venue";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
            + "/" + BASE_PATH);

    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
            + "/locations";
    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
            + "/location";

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sURIMatcher.addURI(AUTHORITY, BASE_PATH, VENUES);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/*", VENUE_ID);
    }

    @Override
    public boolean onCreate() {
        database = MySQLiteHelper.getInstance(getContext());
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        // Uisng SQLiteQueryBuilder instead of query() method
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        // check if the caller has requested a column which does not exists
        checkColumns(projection);

        // Set the table
        queryBuilder.setTables(VenueTable.TABLE_VENUE);

        int uriType = sURIMatcher.match(uri);
        switch (uriType) {
            case VENUES:
                break;
            case VENUE_ID:
                // adding the ID to the original query
                queryBuilder.appendWhere(VenueTable.COLUMN_VENUE_ID + "='"
                        + uri.getLastPathSegment()+"'");
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        SQLiteDatabase db = database.getWritableDatabase();
        Cursor cursor = queryBuilder.query(db, projection, selection,
                selectionArgs, null, null, sortOrder);
        // make sure that potential listeners are getting notified
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        long id = 0;
        switch (uriType) {
            case VENUES:
                try {
                    id = sqlDB.insertOrThrow(VenueTable.TABLE_VENUE, null, values);
                }
                catch(Exception e){

                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(BASE_PATH + "/" + id);
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values){
        int numInserted = 0;
        String table;

        int uriType = sURIMatcher.match(uri);

        switch (uriType) {
            case VENUES:
                table = VenueTable.TABLE_VENUE;
                SQLiteDatabase sqlDB = database.getWritableDatabase();
                sqlDB.beginTransaction();
                try {
                    for (ContentValues cv : values) {
                        long newID = sqlDB.insertOrThrow(table, null, cv);
                        if (newID <= 0) {
                            throw new SQLException("Failed to insert row into " + uri);
                        }
                    }
                    sqlDB.setTransactionSuccessful();
                    getContext().getContentResolver().notifyChange(uri, null);
                    numInserted = values.length;
                } finally {
                    sqlDB.endTransaction();
                }
                break;
            case VENUE_ID:
                return 0;
        }

        return numInserted;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        int rowsDeleted = 0;
        switch (uriType) {
            case VENUES:
                rowsDeleted = sqlDB.delete(VenueTable.TABLE_VENUE, selection,
                        selectionArgs);
                break;
            case VENUE_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = sqlDB.delete(VenueTable.TABLE_VENUE,
                            VenueTable.COLUMN_VENUE_ID + "= '" + id+"'",
                            null);
                } else {
                    rowsDeleted = sqlDB.delete(VenueTable.TABLE_VENUE,
                            VenueTable.COLUMN_VENUE_ID + "='" + id
                                    + "' and " + selection,
                            selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {

        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        int rowsUpdated = 0;
        switch (uriType) {
            case VENUES:
                rowsUpdated = sqlDB.update(VenueTable.TABLE_VENUE,
                        values,
                        selection,
                        selectionArgs);
                break;
            case VENUE_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = sqlDB.update(VenueTable.TABLE_VENUE,
                            values,
                            VenueTable.COLUMN_VENUE_ID + "='" + id +"'",
                            null);
                } else {
                    rowsUpdated = sqlDB.update(VenueTable.TABLE_VENUE,
                            values,
                            VenueTable.COLUMN_VENUE_ID + "='" + id
                                    + "' and "
                                    + selection,
                            selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

    private void checkColumns(String[] projection) {
        String[] available = VenueTable.COLUMNS;
        if (projection != null) {
            HashSet<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
            HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(available));
            // check if all columns which are requested are available
            if (!availableColumns.containsAll(requestedColumns)) {
                throw new IllegalArgumentException("Unknown columns in projection");
            }
        }
    }
}
