package edu.wtu.sj.imdemo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import edu.wtu.sj.imdemo.Provider.SmsProvider;
import edu.wtu.sj.imdemo.dbhelper.SmsOpenHelper;

/**
 * Created by admin on 2017/5/17.
 */
@RunWith(AndroidJUnit4.class)
public class TestSmsProvider {
    Context appContext = InstrumentationRegistry.getTargetContext();

    @Test
    public void testInsert() {
        ContentValues values = new ContentValues();
        values.put(SmsOpenHelper.SmsTable.FROM_ACCOUNT, "t1@localhost");
        values.put(SmsOpenHelper.SmsTable.TO_ACCOUNT, "admin@localhost");
        values.put(SmsOpenHelper.SmsTable.BODY, "I am fine");
        values.put(SmsOpenHelper.SmsTable.STATUS, "ceshi");
        values.put(SmsOpenHelper.SmsTable.TYPE, "chat");
        values.put(SmsOpenHelper.SmsTable.TIME, System.currentTimeMillis());
        values.put(SmsOpenHelper.SmsTable.SESSION_ACCOUNT, "t1@localhost");
        appContext.getContentResolver().insert(SmsProvider.URI_SMS, values);
    }

    @Test
    public void testQuery() {
        Cursor cursor = appContext.getContentResolver().query(SmsProvider.URI_SMS, null, null, null, null);
        int columncount = cursor.getColumnCount();
        while (cursor.moveToNext()) {
            for (int i = 0; i < columncount; i++) {
                System.out.println(cursor.getColumnName(i) + ";" + cursor.getString(i));
            }
        }
    }
}
