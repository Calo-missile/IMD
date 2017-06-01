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

import edu.wtu.sj.imdemo.dbhelper.ContactOpenHelper;

/**
 * Created by admin on 2017/5/3.
 */

public class ContactsProvider extends ContentProvider {
    public static final String AUTHORITIES = ContactsProvider.class.getCanonicalName();
    public static final Uri URI_CONTACT = Uri.parse("content://" + AUTHORITIES + "/contact");
    public static final int CONTACT = 1;
    private ContactOpenHelper mHelper;
    private static UriMatcher sUriMatcher;

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(AUTHORITIES, "/contact", CONTACT);
    }

    @Override
    public boolean onCreate() {
        mHelper = new ContactOpenHelper(getContext());
        if (mHelper != null) {
            return true;
        }
        return false;
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
            case CONTACT:
                SQLiteDatabase db = mHelper.getWritableDatabase();
                long id = db.insert(ContactOpenHelper.T_CONTACT, "", values);
                if (id != -1) {
                    System.out.println("-----------------ContactProvider insert success------------------");
                    uri = ContentUris.withAppendedId(uri, id);
                    getContext().getContentResolver().notifyChange(ContactsProvider.URI_CONTACT, null);
                }
                break;
            default:
                break;
        }
        return uri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int match = sUriMatcher.match(uri);
        int deletecount = 0;
        switch (match) {
            case CONTACT:
                SQLiteDatabase db = mHelper.getWritableDatabase();
                deletecount = db.delete(ContactOpenHelper.T_CONTACT, selection, selectionArgs);
                if (deletecount > 0) {
                    System.out.println("-----------------ContactProvider delete success------------------");
                    getContext().getContentResolver().notifyChange(ContactsProvider.URI_CONTACT, null);  //唤醒数据库改变，监听
                }
                break;
            default:
                break;
        }
        return deletecount;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        int match = sUriMatcher.match(uri);
        int updatecount = 0;
        switch (match) {
            case CONTACT:
                SQLiteDatabase db = mHelper.getWritableDatabase();
                updatecount = db.update(ContactOpenHelper.T_CONTACT, values, selection, selectionArgs);
                if (updatecount > 0) {
                    System.out.println("-----------------ContactProvider update success------------------");
                    getContext().getContentResolver().notifyChange(ContactsProvider.URI_CONTACT, null);
                }
                break;
            default:
                break;
        }
        return updatecount;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        int match = sUriMatcher.match(uri);
        Cursor cursor = null;
        switch (match) {
            case CONTACT:
                SQLiteDatabase db = mHelper.getReadableDatabase();
                cursor = db.query(ContactOpenHelper.T_CONTACT, projection, selection, selectionArgs, null, null, sortOrder);
                if (cursor != null) {
                    System.out.println("-----------------ContactProvider query success------------------");
                }
                break;
            default:
                break;
        }
        return cursor;
    }

}
