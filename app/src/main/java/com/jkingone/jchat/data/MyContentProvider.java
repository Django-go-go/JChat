package com.jkingone.jchat.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.jkingone.jchat.Constant;
import com.jkingone.jchat.bean.Friend;
import com.jkingone.jchat.bean.NewFriend;

import java.util.ArrayList;
import java.util.List;

public class MyContentProvider extends ContentProvider {

    private static final String TAG = "MyContentProvider";
    private static final String AUTHORITIES = "com.jkingone.jchat";

    public static final Uri MY_CONTENT_URI = Uri.parse("content://" + AUTHORITIES + "/"
            + MyContract.DescriptionEntry.TABLE_NAME);

    private SQLiteDatabase mDb;
    private Context mContext;

    public MyContentProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count = mDb.delete(MyContract.DescriptionEntry.TABLE_NAME, selection, selectionArgs);
        if (count > 0) {
            mContext.getContentResolver().notifyChange(uri, null);
        }
        return count;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Log.i(TAG, "insert: " + uri.toString());
        mDb.insert(MyContract.DescriptionEntry.TABLE_NAME, null, values);
        mContext.getContentResolver().notifyChange(uri, null);
        return uri;
    }

    @Override
    public boolean onCreate() {
        mContext = getContext();
        mDb = new DbOpenHelper(mContext).getWritableDatabase();
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        return mDb.query(MyContract.DescriptionEntry.TABLE_NAME,
                projection, selection, selectionArgs, null, null, sortOrder, null);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        int row = mDb.update(MyContract.DescriptionEntry.TABLE_NAME,
                values, selection, selectionArgs);
        if (row > 0) {
            mContext.getContentResolver().notifyChange(uri, null);
        }
        return row;
    }

    public static boolean updateFriend(Context context, String uid, int status) {
        ContentValues values = new ContentValues();
        values.put(MyContract.DescriptionEntry.COLUMN_STATUS, status);
        String update = MyContract.DescriptionEntry.COLUMN_ENTRY_ID + " = '" + uid + "'";
        int count = context.getContentResolver().update(MY_CONTENT_URI, values, update, null);
        if (count >= 0) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean deleteFriend(Context context, String uid) {
        String delete = MyContract.DescriptionEntry.COLUMN_ENTRY_ID + " = '" + uid + "'";
        int count = context.getContentResolver().delete(MY_CONTENT_URI, delete, null);
        if (count >= 0) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean deleteFriend(Context context) {
        int count = context.getContentResolver().delete(MY_CONTENT_URI, null, null);
        if (count >= 0) {
            return true;
        } else {
            return false;
        }
    }

    public static List<NewFriend> queryFriends(Context context) {
        List<NewFriend> newFriends = new ArrayList<>();
        Cursor cursor = context.getContentResolver().query(MY_CONTENT_URI, null,
                null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                NewFriend friend = new NewFriend();
                friend.setUid(cursor.getString(cursor.getColumnIndex(MyContract.DescriptionEntry.COLUMN_ENTRY_ID)));
                friend.setUsername(cursor.getString(cursor.getColumnIndex(MyContract.DescriptionEntry.COLUMN_USERNAME)));
                friend.setAvatar(cursor.getString(cursor.getColumnIndex(MyContract.DescriptionEntry.COLUMN_AVATAR)));
                friend.setStatus(cursor.getInt(cursor.getColumnIndex(MyContract.DescriptionEntry.COLUMN_STATUS)));
                friend.setContent(cursor.getString(cursor.getColumnIndex(MyContract.DescriptionEntry.COLUMN_CONTENT)));
                newFriends.add(friend);
            }
            Log.i(TAG, "queryFriends: " + newFriends);
            cursor.close();
            return newFriends;
        } else {
            Log.i(TAG, "queryFriends: null");
            return newFriends;
        }
    }

    public static List<NewFriend> getFriends(Context context, int status) {
        List<NewFriend> newFriends = new ArrayList<>();
        String query = MyContract.DescriptionEntry.COLUMN_STATUS +
                " = " + status;
        Cursor cursor = context.getContentResolver().query(MY_CONTENT_URI, null,
                    query, null, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                NewFriend friend = new NewFriend();
                friend.setUid(cursor.getString(cursor.getColumnIndex(MyContract.DescriptionEntry.COLUMN_ENTRY_ID)));
                friend.setUsername(cursor.getString(cursor.getColumnIndex(MyContract.DescriptionEntry.COLUMN_USERNAME)));
                friend.setAvatar(cursor.getString(cursor.getColumnIndex(MyContract.DescriptionEntry.COLUMN_AVATAR)));
                friend.setStatus(cursor.getInt(cursor.getColumnIndex(MyContract.DescriptionEntry.COLUMN_STATUS)));
                friend.setContent(cursor.getString(cursor.getColumnIndex(MyContract.DescriptionEntry.COLUMN_CONTENT)));
                newFriends.add(friend);
            }
            Log.i(TAG, "getFriends: " + newFriends);
            cursor.close();
            return newFriends;
        } else {
            Log.i(TAG, "getFriends: null");
            return newFriends;
        }
    }

    public static void addFriend(Context context, NewFriend newFriend) {
        if (!isHasFriend(context, newFriend.getUid())) {
            ContentValues values = new ContentValues();
            values.put(MyContract.DescriptionEntry.COLUMN_ENTRY_ID, newFriend.getUid());
            values.put(MyContract.DescriptionEntry.COLUMN_USERNAME, newFriend.getUsername());
            values.put(MyContract.DescriptionEntry.COLUMN_AVATAR, newFriend.getAvatar());
            values.put(MyContract.DescriptionEntry.COLUMN_STATUS, newFriend.getStatus());
            values.put(MyContract.DescriptionEntry.COLUMN_CONTENT, newFriend.getContent());
            context.getContentResolver().insert(MY_CONTENT_URI, values);
        } else {
            Log.i(TAG, "addFriend: has friend");
        }
    }

    public static boolean isNotLikeFriend(Context context, String uid) {
        String query = MyContract.DescriptionEntry.COLUMN_ENTRY_ID +
                " = '" + uid + "'";
        String[] projection = {MyContract.DescriptionEntry.COLUMN_STATUS};
        Cursor cursor = context.getContentResolver().query(MY_CONTENT_URI, projection,
                query, null, null);
        if (cursor != null) {
            if (cursor.getCount() <= 0) {
                cursor.close();
                return false;
            } else {
                cursor.moveToNext();
                int status = cursor.getInt(cursor.getColumnIndex(MyContract.DescriptionEntry.COLUMN_STATUS));
                if (status == 0) {
                    cursor.close();
                    return true;
                } else {
                    cursor.close();
                    return false;
                }
            }
        } else {
            return false;
        }
    }

    public static boolean isHasFriend(Context context, String uid) {
        String query = MyContract.DescriptionEntry.COLUMN_ENTRY_ID +
                " = '" + uid + "'";
        Cursor cursor = context.getContentResolver().query(MY_CONTENT_URI, null,
                query, null, null);
        if (cursor != null) {
            if (cursor.getCount() <= 0) {
                cursor.close();
                return false;
            } else {
                cursor.close();
                return true;
            }
        } else {
            return false;
        }
    }
}
