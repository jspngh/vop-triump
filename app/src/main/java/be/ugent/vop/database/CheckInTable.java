package be.ugent.vop.database;

import android.database.sqlite.SQLiteDatabase;
/**
 * Created by jonas on 24-4-2015.
 */
public class CheckInTable {
    private static final String TAG = "CheckInTable";

    public static final String TABLE_CHECKIN = "check_in";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_GROUP_ID = "groupId";
    public static final String COLUMN_GROUP_NAME = "groupName";
    public static final String COLUMN_MEMBER_NAME = "memberName";
    public static final String COLUMN_MEMBER_ICON = "memberIconUrl";
    public static final String COLUMN_VENUE_NAME = "venueName";
    public static final String COLUMN_VENUE_ID = "venueId";

    public final static String COLUMNS[] = {COLUMN_ID, COLUMN_GROUP_ID, COLUMN_GROUP_NAME, COLUMN_MEMBER_NAME, COLUMN_MEMBER_ICON, COLUMN_VENUE_NAME, COLUMN_VENUE_ID};

    // Database creation sql statement
    private static final String DATABASE_CREATE = "create table if not exists "
            + TABLE_CHECKIN + "(" + COLUMN_ID
            + " integer primary key autoincrement, " + COLUMN_GROUP_ID
            + " integer not null, " + COLUMN_GROUP_NAME
            + " text not null, " + COLUMN_MEMBER_NAME
            + " text not null, " + COLUMN_MEMBER_ICON
            + " text not null, " + COLUMN_VENUE_NAME
            + " text not null, " + COLUMN_VENUE_ID
            + " text not null); ";

    public static void onCreate(SQLiteDatabase database) {
        System.err.println("creating table");
        database.execSQL(DATABASE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CHECKIN);
        onCreate(db);
    }
}