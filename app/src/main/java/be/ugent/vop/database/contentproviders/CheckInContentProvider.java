package be.ugent.vop.database.contentproviders;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.google.api.client.util.DateTime;
import java.util.ArrayList;
import java.util.List;
import be.ugent.vop.backend.myApi.model.OverviewCheckin;
import be.ugent.vop.database.CheckInTable;
import be.ugent.vop.database.MySQLiteHelper;

/**
 * Created by jonas on 24-4-2015.
 */
public class CheckInContentProvider {
    // database
    private SQLiteDatabase database;
    private MySQLiteHelper dbHelper;

    private static final String TAG = "CheckInCP";

    public CheckInContentProvider(Context context) {
        dbHelper = MySQLiteHelper.getInstance(context);
    }

    public void open() {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public void createCheckIn(OverviewCheckin checkin) {
        ContentValues values = new ContentValues();
        values.put(CheckInTable.COLUMN_GROUP_ID, checkin.getGroupId());
        values.put(CheckInTable.COLUMN_GROUP_NAME, checkin.getGroupName());
        values.put(CheckInTable.COLUMN_MEMBER_NAME, checkin.getMemberName());
        values.put(CheckInTable.COLUMN_MEMBER_ICON, checkin.getMemberIconUrl());
        values.put(CheckInTable.COLUMN_VENUE_NAME, checkin.getVenueName());
        values.put(CheckInTable.COLUMN_VENUE_ID, checkin.getVenueId());
        values.put(CheckInTable.COLUMN_DATE, checkin.getDate().toString());


        long insertId = database.insert(CheckInTable.TABLE_CHECKIN, null,
                values);
    }

    public void deleteAllCheckIns() {
        database.delete(CheckInTable.TABLE_CHECKIN, null, null);
    }

    public List<OverviewCheckin> getAllCheckIns() {
        List<OverviewCheckin> checkins = new ArrayList<OverviewCheckin>();

        Cursor cursor = database.query(CheckInTable.TABLE_CHECKIN,
                CheckInTable.COLUMNS, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            OverviewCheckin checkin = cursorToCheckin(cursor);
            checkins.add(checkin);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return checkins;
    }

    private OverviewCheckin cursorToCheckin(Cursor cursor) {
        OverviewCheckin checkin = new OverviewCheckin();
        checkin.setGroupId(cursor.getLong(1));
        checkin.setGroupName(cursor.getString(2));
        checkin.setMemberName(cursor.getString(3));
        checkin.setMemberIconUrl(cursor.getString(4));
        checkin.setVenueName(cursor.getString(5));
        checkin.setVenueId(cursor.getString(6));
        DateTime date = new DateTime(cursor.getString(7));
        checkin.setDate(date);
        return checkin;
    }
}