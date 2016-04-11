package com.example.ivan.konverzijavaluta.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import com.example.ivan.konverzijavaluta.database.KonverzijaContract;
import com.example.ivan.konverzijavaluta.database.KonverzijaContract.Drzava;
import com.example.ivan.konverzijavaluta.database.KonverzijaContract.TecajnaLista;
import com.example.ivan.konverzijavaluta.database.KonverzijaDatabase;
import com.example.ivan.konverzijavaluta.database.KonverzijaDatabase.Tables;


public class KonverzijaProvider extends ContentProvider {
    private SQLiteOpenHelper m_sqLiteHelper;

    static final UriMatcher URI_MATCHER = buildUriMatcher();

    static final int DRZAVA           = 100;
    static final int DRZAVA_ID        = 101;
    static final int TECAJNA_LISTA    = 200;
    static final int TECAJNA_LISTA_ID = 201;

    private static UriMatcher buildUriMatcher() {
        UriMatcher retVal = new UriMatcher(UriMatcher.NO_MATCH);

        retVal.addURI(KonverzijaContract.AUTHORITY, "drzava", DRZAVA);
        retVal.addURI(KonverzijaContract.AUTHORITY, "drzava/#", DRZAVA_ID);

        retVal.addURI(KonverzijaContract.AUTHORITY, "tecajna_lista", TECAJNA_LISTA);
        retVal.addURI(KonverzijaContract.AUTHORITY, "tecajna_lista/#", TECAJNA_LISTA_ID);

        return retVal;
    }

    @Override
    public boolean onCreate() {
        m_sqLiteHelper = new KonverzijaDatabase(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = m_sqLiteHelper.getReadableDatabase();
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(Tables.DRZAVA);

        switch (URI_MATCHER.match(uri)) {
            case DRZAVA_ID:
                queryBuilder.appendWhere(Drzava._ID + "=" + uri.getLastPathSegment());
            case DRZAVA:
                break;
            case TECAJNA_LISTA_ID:
                queryBuilder.appendWhere(TecajnaLista._ID + "=" + uri.getLastPathSegment());
            case TECAJNA_LISTA:
                break;
        }

        Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return new String();
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        SQLiteDatabase db = m_sqLiteHelper.getWritableDatabase();

        long _id;
        switch (URI_MATCHER.match(uri)) {
            case DRZAVA:
                _id = db.insert(Tables.DRZAVA, "", contentValues);
                getContext().getContentResolver().notifyChange(uri, null, false);
                return KonverzijaContract.buildUri(Drzava.CONTENT_URI, _id);
            case TECAJNA_LISTA:
                _id = db.insert(Tables.TECAJNA_LISTA, "", contentValues);
                getContext().getContentResolver().notifyChange(uri, null, false);
                return KonverzijaContract.buildUri(Drzava.CONTENT_URI, _id);
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        SQLiteDatabase db = m_sqLiteHelper.getWritableDatabase();
        int count;
        String id;

        switch (URI_MATCHER.match(uri)) {
            case DRZAVA:
                count = db.delete(Tables.DRZAVA, selection, selectionArgs);
                break;
            case DRZAVA_ID:
                id = uri.getPathSegments().get(1);
                count = db.delete(Tables.DRZAVA, Drzava._ID + " = " + id +
                        (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
                break;
            case TECAJNA_LISTA:
                count = db.delete(Tables.TECAJNA_LISTA, selection, selectionArgs);
                break;
            case TECAJNA_LISTA_ID:
                id = uri.getPathSegments().get(1);
                count = db.delete(Tables.TECAJNA_LISTA, TecajnaLista._ID + " = " + id +
                        (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        SQLiteDatabase db = m_sqLiteHelper.getWritableDatabase();
        int count;

        switch (URI_MATCHER.match(uri)) {
            case DRZAVA:
                count = db.update(Tables.DRZAVA, contentValues, selection, selectionArgs);
                break;
            case DRZAVA_ID:
                count = db.update(Tables.DRZAVA, contentValues, Drzava._ID + " = " + uri.getPathSegments().get(1) +
                        (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
                break;
            case TECAJNA_LISTA:
                count = db.update(Tables.TECAJNA_LISTA, contentValues, selection, selectionArgs);
                break;
            case TECAJNA_LISTA_ID:
                count = db.update(Tables.TECAJNA_LISTA, contentValues,
                                  TecajnaLista._ID + " = " + uri.getPathSegments().get(1) +
                                          (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""),
                                  selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
}

