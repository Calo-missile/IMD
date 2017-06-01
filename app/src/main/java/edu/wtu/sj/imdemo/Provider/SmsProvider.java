package edu.wtu.sj.imdemo.Provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import edu.wtu.sj.imdemo.dbhelper.SmsOpenHelper;

/**
 * Created by admin on 2017/5/17.
 */

public class SmsProvider extends ContentProvider {
    public static final String AUTHORITIES = SmsProvider.class.getCanonicalName();
    public static final Uri URI_SMS = Uri.parse("content://" + AUTHORITIES + "/sms");
    public static final Uri URI_SESSION = Uri.parse("content://" + AUTHORITIES + "/session");
    public static final int SMS = 1;
    public static final int SESSION = 2;
    private SmsOpenHelper mHelper;
    private static UriMatcher sUriMatcher;

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(AUTHORITIES, "/sms", SMS);
        sUriMatcher.addURI(AUTHORITIES, "/session", SESSION);
    }

    @Override
    public boolean onCreate() {
        mHelper = new SmsOpenHelper(getContext());
        if (mHelper != null) {
            return true;
        }
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        int match = sUriMatcher.match(uri);
        Cursor cursor = null;
        SQLiteDatabase db = mHelper.getReadableDatabase();
        switch (match) {
            case SMS:
                cursor = db.query(SmsOpenHelper.T_SMS, projection, selection, selectionArgs, null, null, sortOrder);
                if (cursor != null) {
                    System.out.println("-----------------SmsProvider query success------------------");
                }
                break;
            case SESSION:
                cursor = db.rawQuery("SELECT * FROM "
                        + "(SELECT * FROM t_sms WHERE from_account = ? or to_account = ? ORDER BY time ASC)"
                        + " GROUP BY session_account", selectionArgs);
                break;
            default:
                break;
        }
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        int match = sUriMatcher.match(uri);
        switch (match) {
            case SMS:
                SQLiteDatabase db = mHelper.getWritableDatabase();
                long id = db.insert(SmsOpenHelper.T_SMS, "", values);
                if (id != -1) {
                    System.out.println("-----------------SmsProvider insert success------------------");
                    uri = ContentUris.withAppendedId(uri, id);
                    getContext().getContentResolver().notifyChange(SmsProvider.URI_SMS, null);
                }
                break;
            default:
                break;
        }
        return uri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
