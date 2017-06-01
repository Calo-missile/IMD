package edu.wtu.sj.imdemo.dbhelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * Created by admin on 2017/5/17.
 */

public class SmsOpenHelper extends SQLiteOpenHelper {
    public static final String T_SMS = "t_sms";

    public class SmsTable implements BaseColumns {
        public static final String FROM_ACCOUNT = "from_account";
        public static final String TO_ACCOUNT = "to_account";
        public static final String BODY = "body";
        public static final String STATUS = "status";
        public static final String TYPE = "type";
        public static final String TIME = "time";
        public static final String SESSION_ACCOUNT = "session_account";
    }

    public SmsOpenHelper(Context context) {
        super(context, "sms.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table " + T_SMS + " (_id integer primary key autoincrement,"
                + SmsTable.FROM_ACCOUNT + " text,"
                + SmsTable.TO_ACCOUNT + " text,"
                + SmsTable.BODY + " text,"
                + SmsTable.STATUS + " text,"
                + SmsTable.TYPE + " text,"
                + SmsTable.TIME + " text,"
                + SmsTable.SESSION_ACCOUNT + " text)";
        System.out.println("创建表结构完成" + sql);
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
