package com.jkingone.jchat.data;

import android.provider.BaseColumns;

/**
 * Created by Jkingone on 2018/1/29.
 */

public final class MyContract {

    public static abstract class DescriptionEntry implements BaseColumns {
        public static final String TABLE_NAME = "newfriend";
        public static final String COLUMN_ENTRY_ID = "uid";
        public static final String COLUMN_USERNAME = "uername";
        public static final String COLUMN_AVATAR = "avatar";
        public static final String COLUMN_STATUS = "status";
        public static final String COLUMN_CONTENT = "content";
    }
}
