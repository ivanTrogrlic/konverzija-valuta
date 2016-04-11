package com.example.ivan.konverzijavaluta.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import com.example.ivan.konverzijavaluta.database.KonverzijaValutaDatabase;


public class KonverzijaValutaProvider extends ContentProvider {
    private KonverzijaValutaDatabase mDB;


    private static final String AUTHORITY = "com.example.ivan.konverzijavaluta.provider.KonverzijaValutaProvider";
    public static final int VALUTE = 1;
    public static final int VALUTE_ID = 2;

    private static final String VALUTE_BASE_PATH = "valute";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
            + "/" + VALUTE_BASE_PATH);

    private static final UriMatcher sURIMatcher = new UriMatcher(
            UriMatcher.NO_MATCH);

    static {
        sURIMatcher.addURI(AUTHORITY, VALUTE_BASE_PATH, VALUTE);
        sURIMatcher.addURI(AUTHORITY, VALUTE_BASE_PATH + "/#", VALUTE_ID);
    }

    @Override
    public boolean onCreate() {
        mDB = new KonverzijaValutaDatabase(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(KonverzijaValutaDatabase.TABLE_VALUTE);

        int uriType = sURIMatcher.match(uri);
        switch (uriType) {
            case VALUTE:
                queryBuilder.appendWhere(KonverzijaValutaDatabase.ID + "="
                        + uri.getLastPathSegment());
                break;
            case VALUTE_ID:
                break;
            default:
                throw new IllegalArgumentException("Unknown URI");
        }

        Cursor cursor = queryBuilder.query(mDB.getReadableDatabase(),
                projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        SQLiteDatabase db = mDB.getWritableDatabase();
        switch (sURIMatcher.match(uri)) {
            case VALUTE:

                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        long id = db.insert(KonverzijaValutaDatabase.TABLE_VALUTE, null, contentValues);
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(CONTENT_URI + "/" + id);
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {

        SQLiteDatabase db = mDB.getWritableDatabase();
        switch (sURIMatcher.match(uri)) {
            case VALUTE:

                break;
            case VALUTE_ID:
                String id = uri.getPathSegments().get(1);
                s = KonverzijaValutaDatabase.ID + "=" + id
                        + (!TextUtils.isEmpty(s) ?
                        " AND (" + s + ')' : "");
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        int deleteCount = db.delete(KonverzijaValutaDatabase.TABLE_VALUTE, s, strings);
        getContext().getContentResolver().notifyChange(uri, null);
        return deleteCount;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        SQLiteDatabase db = mDB.getWritableDatabase();
        switch (sURIMatcher.match(uri)) {
            case VALUTE:

                break;
            case VALUTE_ID:
                String id = uri.getPathSegments().get(1);
                s = KonverzijaValutaDatabase.ID + "=" + id
                        + (!TextUtils.isEmpty(s) ?
                        " AND (" + s + ')' : "");
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        int updateCount = db.update(KonverzijaValutaDatabase.TABLE_VALUTE, contentValues, s, strings);
        getContext().getContentResolver().notifyChange(uri, null);
        return updateCount;
    }
}
