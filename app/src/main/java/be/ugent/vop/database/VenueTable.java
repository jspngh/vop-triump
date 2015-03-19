package be.ugent.vop.database;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
/**
 * Created by vincent on 19/03/15.
 */

public class VenueTable {
    private static final String TAG = "VenueTable";

    public static final String TABLE_VENUE = "venue";
    public static final String COLUMN_VENUE_ID = "venueid";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_ADDRESS = "address";
    public static final String COLUMN_CITY = "city";
    public static final String COLUMN_COUNTRY = "country";
    public static final String COLUMN_LATITUDE = "latitude";
    public static final String COLUMN_LONGITUDE = "longitude";
    public static final String COLUMN_VERIFIED = "verified";
    public static final String COLUMN_LAST_UPDATED = "last_updated";

    public final static String COLUMNS[] =
            {COLUMN_VENUE_ID, COLUMN_NAME, COLUMN_ADDRESS,COLUMN_COUNTRY,
                    COLUMN_CITY, COLUMN_LATITUDE,COLUMN_LONGITUDE,COLUMN_VERIFIED, COLUMN_LAST_UPDATED};

    // Database creation sql statement
    private static final String CREATE_TABLE_CATEGORY = "create table if not exists "
            + TABLE_VENUE + "(" + COLUMN_VENUE_ID
            + " text primary key, " + COLUMN_NAME
            + " text not null, " + COLUMN_ADDRESS
            + " text not null, " + COLUMN_CITY
            + " text not null, " + COLUMN_COUNTRY
            + " text not null, " + COLUMN_LATITUDE
            + " real not null, " + COLUMN_LONGITUDE
            + " real not null, " + COLUMN_VERIFIED //SQLite doesn't support boolean type, therefor we use integers. 0 false and 1 true
            + " integer not null, " + COLUMN_LAST_UPDATED
            + " date not null); ";

    public static void onCreate(SQLiteDatabase database) {
        System.err.println("creating table");
        Log.d("VenueTable", "onCreate");
        database.execSQL(CREATE_TABLE_CATEGORY);
    }

    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG,
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_VENUE);
        onCreate(db);
    }
}

