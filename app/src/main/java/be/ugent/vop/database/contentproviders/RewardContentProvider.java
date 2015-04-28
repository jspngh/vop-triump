package be.ugent.vop.database.contentproviders;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.google.api.client.util.DateTime;
import java.util.ArrayList;
import java.util.List;
import be.ugent.vop.backend.myApi.model.OverviewReward;
import be.ugent.vop.database.CheckInTable;
import be.ugent.vop.database.MySQLiteHelper;
import be.ugent.vop.database.RewardTable;

/**
 * Created by jonas on 24-4-2015.
 */
public class RewardContentProvider {
    // database
    private SQLiteDatabase database;
    private MySQLiteHelper dbHelper;

    private static final String TAG = "RewardCP";

    public RewardContentProvider(Context context) {
        dbHelper = MySQLiteHelper.getInstance(context);
    }

    public void open() {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public void createReward(OverviewReward reward) {
        ContentValues values = new ContentValues();
        values.put(RewardTable.COLUMN_EVENT_DESCRIPTION, reward.getEventDescription());
        values.put(RewardTable.COLUMN_EVENT_REWARD, reward.getEventReward());
        values.put(CheckInTable.COLUMN_VENUE_ID, reward.getVenueId());
        values.put(CheckInTable.COLUMN_VENUE_NAME, reward.getVenueName());
        values.put(RewardTable.COLUMN_DATE, reward.getDate().toString());

        long insertId = database.insert(RewardTable.TABLE_REWARD, null, values);
    }

    public void deleteAllRewards() {
        database.delete(RewardTable.TABLE_REWARD, null, null);
    }

    public List<OverviewReward> getAllRewards() {
        List<OverviewReward> rewards = new ArrayList<>();

        Cursor cursor = database.query(RewardTable.TABLE_REWARD,
                RewardTable.COLUMNS, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            OverviewReward reward = cursorToReward(cursor);
            rewards.add(reward);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return rewards;
    }

    private OverviewReward cursorToReward(Cursor cursor) {
        OverviewReward reward = new OverviewReward();
        reward.setEventDescription(cursor.getString(1));
        reward.setEventReward(cursor.getString(2));
        reward.setVenueId(cursor.getString(3));
        reward.setVenueName(cursor.getString(4));
        DateTime date = new DateTime(cursor.getString(5));
        reward.setDate(date);
        return reward;
    }
}