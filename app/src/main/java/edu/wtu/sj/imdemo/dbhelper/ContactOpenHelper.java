package edu.wtu.sj.imdemo.dbhelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * Created by admin on 2017/5/3.
 */

public class ContactOpenHelper extends SQLiteOpenHelper {
    public static final String T_CONTACT = "t_contact";

    public class ContactTable implements BaseColumns {//BaseColumns会自动生成id字段
        /**
         * 1. _id:主键
         * 2. account:账号
         * 3. nickname:昵称
         * 4. avatar:头像
         * 5. pinyin:账号拼音
         */
        public static final String ACCOUNT = "account";
        public static final String NICKNAME = "nickname";
        public static final String AVATAR = "avatar";
        public static final String PINYIN = "pinyin";
    }

    public ContactOpenHelper(Context context) {
        super(context, "contact.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table " + T_CONTACT + " (_id integer primary key autoincrement,"
                + ContactTable.ACCOUNT + " text,"
                + ContactTable.NICKNAME + " text,"
                + ContactTable.AVATAR + " text,"
                + ContactTable.PINYIN + " text)";
        System.out.println("创建表结构完成" + sql);
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
