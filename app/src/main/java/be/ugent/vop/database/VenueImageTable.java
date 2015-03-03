package be.ugent.vop.database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class VenueImageTable {
    private static final String TAG = "VenueImageTable";

    public static final String TABLE_VENUE_IMAGE = "venue_image";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_VENUE_ID = "venueid";
    public static final String COLUMN_PREFIX = "prefix";
    public static final String COLUMN_SUFFIX = "suffix";
    public static final String COLUMN_WIDTH = "width";
    public static final String COLUMN_HEIGHT = "height";

    public final static String COLUMNS[] = {COLUMN_ID, COLUMN_VENUE_ID, COLUMN_PREFIX, COLUMN_SUFFIX, COLUMN_WIDTH, COLUMN_HEIGHT};

    // Database creation sql statement
    private static final String CREATE_TABLE_CATEGORY = "create table if not exists "
            + TABLE_VENUE_IMAGE + "(" + COLUMN_ID
            + " integer primary key autoincrement, " + COLUMN_VENUE_ID
            + " text not null, " + COLUMN_PREFIX
            + " text not null, " + COLUMN_SUFFIX
            + " text not null, " + COLUMN_WIDTH
            + " integer, " + COLUMN_HEIGHT
            + " integer);";

    public static void onCreate(SQLiteDatabase database) {
        System.err.println("creating table");
        database.execSQL(CREATE_TABLE_CATEGORY);
    }

    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG,
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_VENUE_IMAGE);
        onCreate(db);
    }
}
