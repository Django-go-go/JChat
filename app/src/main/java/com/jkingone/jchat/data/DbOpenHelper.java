package com.jkingone.jchat.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.view.ViewConfiguration;

/**
 * Created by Jkingone on 2018/1/29.
 */

public class DbOpenHelper extends SQLiteOpenHelper{

    private static final String DB_NAME = "jchatprovider.db";
    private static final int VERSION = 1;

    private static final String TEXT_TYPE = " TEXT";
    private static final String INTEGER_TYPE = " INTEGER";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + MyContract.DescriptionEntry.TABLE_NAME + " (" +
                    MyContract.DescriptionEntry._ID + " INTEGER PRIMARY KEY," +
                    MyContract.DescriptionEntry.COLUMN_ENTRY_ID + TEXT_TYPE + COMMA_SEP +
                    MyContract.DescriptionEntry.COLUMN_USERNAME + TEXT_TYPE + COMMA_SEP +
                    MyContract.DescriptionEntry.COLUMN_AVATAR + TEXT_TYPE + COMMA_SEP +
                    MyContract.DescriptionEntry.COLUMN_CONTENT + TEXT_TYPE + COMMA_SEP +
                    MyContract.DescriptionEntry.COLUMN_STATUS + INTEGER_TYPE +
            " )";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + MyContract.DescriptionEntry.TABLE_NAME;


    public DbOpenHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
