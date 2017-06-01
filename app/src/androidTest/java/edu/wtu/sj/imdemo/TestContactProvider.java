package edu.wtu.sj.imdemo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import edu.wtu.sj.imdemo.Provider.ContactsProvider;
import edu.wtu.sj.imdemo.dbhelper.ContactOpenHelper;

/**
 * Created by admin on 2017/5/3.
 */
@RunWith(AndroidJUnit4.class)
public class TestContactProvider {
    Context appContext = InstrumentationRegistry.getTargetContext();

    @Test
    public void testInsert() {
        ContentValues values = new ContentValues();
        values.put(ContactOpenHelper.ContactTable.ACCOUNT, "test@localhost");
        values.put(ContactOpenHelper.ContactTable.NICKNAME, "测试");
        values.put(ContactOpenHelper.ContactTable.AVATAR, "");
        values.put(ContactOpenHelper.ContactTable.PINYIN, "ceshi");
        appContext.getContentResolver().insert(ContactsProvider.URI_CONTACT, values);
    }

    @Test
    public void testQuery() {
        Cursor cursor = appContext.getContentResolver().query(ContactsProvider.URI_CONTACT, null, null, null, null);
        int columncount = cursor.getColumnCount();
        while (cursor.moveToNext()) {
            for (int i = 0; i < columncount; i++) {
                System.out.println(cursor.getColumnName(i) + ";" + cursor.getString(i));
            }
        }
    }

    @Test
    public void testUpdate() {
        ContentValues values = new ContentValues();
        values.put(ContactOpenHelper.ContactTable.ACCOUNT, "test@localhost");
        values.put(ContactOpenHelper.ContactTable.NICKNAME, "测试用例");
        values.put(ContactOpenHelper.ContactTable.AVATAR, "");
        values.put(ContactOpenHelper.ContactTable.PINYIN, "ceshi用例");
        appContext.getContentResolver().update(ContactsProvider.URI_CONTACT,
                values,
                ContactOpenHelper.ContactTable.ACCOUNT + "=?",
                new String[]{"test@localhost"});
    }

    @Test
    public void testDel() {
        appContext.getContentResolver().delete(ContactsProvider.URI_CONTACT,
                ContactOpenHelper.ContactTable.ACCOUNT + "=?",
                new String[]{"test@localhost"});
    }
}
