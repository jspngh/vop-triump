package be.ugent.vop.database.contentproviders;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import be.ugent.vop.backend.myApi.model.NewMemberInGroup;
import be.ugent.vop.database.MySQLiteHelper;
import be.ugent.vop.database.NewMemberTable;

/**
 * Created by jonas on 24-4-2015.
 */
public class NewMemberContentProvider {
    // database
    private SQLiteDatabase database;
    private MySQLiteHelper dbHelper;

    private static final String TAG = "NewMemberCP";

    public NewMemberContentProvider(Context context) {
        dbHelper = MySQLiteHelper.getInstance(context);
    }

    public void open() {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }
    public void createMember(NewMemberInGroup member) {
        ContentValues values = new ContentValues();
        values.put(NewMemberTable.COLUMN_GROUP_ID, member.getGroupId());
        values.put(NewMemberTable.COLUMN_GROUP_NAME, member.getGroupName());
        values.put(NewMemberTable.COLUMN_MEMBER_NAME, member.getMemberName());
        values.put(NewMemberTable.COLUMN_MEMBER_ICON, member.getMemberIconUrl());
        values.put(NewMemberTable.COLUMN_DATE, member.getDate().toString());

        long insertId = database.insert(NewMemberTable.TABLE_NEW_MEMBER, null,
                values);
    }

    public void deleteAllMembers() {
        database.delete(NewMemberTable.TABLE_NEW_MEMBER, null, null);
    }

    public List<NewMemberInGroup> getAllNewMembers() {
        List<NewMemberInGroup> members = new ArrayList<NewMemberInGroup>();

        Cursor cursor = database.query(NewMemberTable.TABLE_NEW_MEMBER,
                NewMemberTable.COLUMNS, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            NewMemberInGroup member = cursorToMember(cursor);
            members.add(member);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return members;
    }

    private NewMemberInGroup cursorToMember(Cursor cursor) {
        NewMemberInGroup member = new NewMemberInGroup();
        member.setGroupId(cursor.getLong(1));
        member.setGroupName(cursor.getString(2));
        member.setMemberName(cursor.getString(3));
        member.setMemberIconUrl(cursor.getString(4));
        return member;
    }
}