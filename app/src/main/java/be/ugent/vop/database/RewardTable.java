package be.ugent.vop.database;

import android.database.sqlite.SQLiteDatabase;
/**
 * Created by jonas on 24-4-2015.
 */
public class RewardTable {
    private static final String TAG = "RewardTable";

    public static final String TABLE_REWARD = "reward";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_EVENT_DESCRIPTION = "eventDescription";
    public static final String COLUMN_EVENT_REWARD = "eventReward";
    public static final String COLUMN_DATE = "date";

    public final static String COLUMNS[] = {COLUMN_ID, COLUMN_EVENT_DESCRIPTION, COLUMN_EVENT_REWARD, COLUMN_DATE};

    // Database creation sql statement
    private static final String DATABASE_CREATE = "create table if not exists "
            + TABLE_REWARD + "(" + COLUMN_ID
            + " integer primary key autoincrement, " + COLUMN_EVENT_DESCRIPTION
            + " text not null, " + COLUMN_EVENT_REWARD
            + " text not null, " + COLUMN_DATE
            + " date not null); ";

    public static void onCreate(SQLiteDatabase database) {
        System.err.println("creating table");
        database.execSQL(DATABASE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REWARD);
        onCreate(db);
    }
}