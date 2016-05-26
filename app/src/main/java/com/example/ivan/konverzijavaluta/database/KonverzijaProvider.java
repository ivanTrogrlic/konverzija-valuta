package com.example.ivan.konverzijavaluta.database;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import com.example.ivan.konverzijavaluta.database.KonverzijaContract.Dan;
import com.example.ivan.konverzijavaluta.database.KonverzijaContract.Drzava;
import com.example.ivan.konverzijavaluta.database.KonverzijaContract.TecajnaLista;
import com.example.ivan.konverzijavaluta.database.KonverzijaContract.TecajnaListaPredicted;
import com.example.ivan.konverzijavaluta.database.KonverzijaDatabase.Tables;

import java.util.HashMap;
import java.util.Map;


public class KonverzijaProvider extends ContentProvider {
    private SQLiteOpenHelper m_sqLiteHelper;

    static final UriMatcher URI_MATCHER = buildUriMatcher();

    static final int DRZAVA    = 100;
    static final int DRZAVA_ID = 101;

    static final int TECAJNA_LISTA                      = 200;
    static final int TECAJNA_LISTA_ID                   = 201;
    static final int TECAJNA_LISTA_WITH_DAN_WITH_DRZAVA = 202;

    static final int DAN    = 300;
    static final int DAN_ID = 301;

    static final int TECAJNA_LISTA_PREDICTED                      = 400;
    static final int TECAJNA_LISTA_PREDICTED_ID                   = 401;
    static final int TECAJNA_LISTA_PREDICTED_WITH_DAN_WITH_DRZAVA = 403;

    private static UriMatcher buildUriMatcher() {
        UriMatcher retVal = new UriMatcher(UriMatcher.NO_MATCH);

        retVal.addURI(KonverzijaContract.AUTHORITY, "drzava", DRZAVA);
        retVal.addURI(KonverzijaContract.AUTHORITY, "drzava/#", DRZAVA_ID);

        retVal.addURI(KonverzijaContract.AUTHORITY, "tecajna_lista", TECAJNA_LISTA);
        retVal.addURI(KonverzijaContract.AUTHORITY, "tecajna_lista/#", TECAJNA_LISTA_ID);
        retVal.addURI(KonverzijaContract.AUTHORITY, "tecajna_lista/with_dan_and_drzava",
                      TECAJNA_LISTA_WITH_DAN_WITH_DRZAVA);

        retVal.addURI(KonverzijaContract.AUTHORITY, "dan", DAN);
        retVal.addURI(KonverzijaContract.AUTHORITY, "dan/#", DAN_ID);

        retVal.addURI(KonverzijaContract.AUTHORITY, "tecajna_lista_predicted", TECAJNA_LISTA_PREDICTED);
        retVal.addURI(KonverzijaContract.AUTHORITY, "tecajna_lista_predicted/#", TECAJNA_LISTA_PREDICTED_ID);
        retVal.addURI(KonverzijaContract.AUTHORITY, "tecajna_lista_predicted/with_dan_and_drzava",
                      TECAJNA_LISTA_PREDICTED_WITH_DAN_WITH_DRZAVA);

        return retVal;
    }

    private static Map tecajnaListaProjectionMap() {
        Map map = new HashMap<>();
        map.put(Tables.DRZAVA + "$" + Drzava._ID,
                Tables.DRZAVA + "." + Drzava._ID + " AS '" + Tables.DRZAVA + "$" + Drzava._ID + "'");
        map.put(Tables.DRZAVA + "$" + Drzava.JEDINICA,
                Tables.DRZAVA + "." + Drzava.JEDINICA + " AS '" + Tables.DRZAVA + "$" + Drzava.JEDINICA + "'");
        map.put(Tables.DRZAVA + "$" + Drzava.SIFRA,
                Tables.DRZAVA + "." + Drzava.SIFRA + " AS '" + Tables.DRZAVA + "$" + Drzava.SIFRA + "'");
        map.put(Tables.DRZAVA + "$" + Drzava.VALUTA,
                Tables.DRZAVA + "." + Drzava.VALUTA + " AS '" + Tables.DRZAVA + "$" + Drzava.VALUTA + "'");

        map.put(Tables.DAN + "$" + Dan._ID, Tables.DAN + "." + Dan._ID + " AS '" + Tables.DAN + "$" + Dan._ID + "'");
        map.put(Tables.DAN + "$" + Dan.DAN, Tables.DAN + "." + Dan.DAN + " AS '" + Tables.DAN + "$" + Dan.DAN + "'");

        map.put(TecajnaLista._ID,
                Tables.TECAJNA_LISTA + "." + TecajnaLista._ID + " AS '" + Tables.TECAJNA_LISTA + "$" + TecajnaLista._ID + "'");
        map.put(TecajnaLista.DAN_ID, TecajnaLista.DAN_ID);
        map.put(TecajnaLista.DRZAVA_ID, TecajnaLista.DRZAVA_ID);
        map.put(TecajnaLista.KUPOVNI_TECAJ, TecajnaLista.KUPOVNI_TECAJ);
        map.put(TecajnaLista.SREDNJI_TECAJ, TecajnaLista.SREDNJI_TECAJ);
        map.put(TecajnaLista.PRODAJNI_TECAJ, TecajnaLista.PRODAJNI_TECAJ);

        return map;
    }

    private static Map tecajnaListaPredictedProjectionMap() {
        Map map = new HashMap<>();
        map.put(Tables.DRZAVA + "$" + Drzava._ID,
                Tables.DRZAVA + "." + Drzava._ID + " AS '" + Tables.DRZAVA + "$" + Drzava._ID + "'");
        map.put(Tables.DRZAVA + "$" + Drzava.JEDINICA,
                Tables.DRZAVA + "." + Drzava.JEDINICA + " AS '" + Tables.DRZAVA + "$" + Drzava.JEDINICA + "'");
        map.put(Tables.DRZAVA + "$" + Drzava.SIFRA,
                Tables.DRZAVA + "." + Drzava.SIFRA + " AS '" + Tables.DRZAVA + "$" + Drzava.SIFRA + "'");
        map.put(Tables.DRZAVA + "$" + Drzava.VALUTA,
                Tables.DRZAVA + "." + Drzava.VALUTA + " AS '" + Tables.DRZAVA + "$" + Drzava.VALUTA + "'");

        map.put(Tables.DAN + "$" + Dan._ID, Tables.DAN + "." + Dan._ID + " AS '" + Tables.DAN + "$" + Dan._ID + "'");
        map.put(Tables.DAN + "$" + Dan.DAN, Tables.DAN + "." + Dan.DAN + " AS '" + Tables.DAN + "$" + Dan.DAN + "'");

        map.put(TecajnaListaPredicted._ID,
                Tables.TECAJNA_LISTA_PREDCITED + "." + TecajnaListaPredicted._ID + " AS '" + Tables.TECAJNA_LISTA_PREDCITED + "$" + TecajnaListaPredicted._ID + "'");
        map.put(TecajnaListaPredicted.DAN_ID, TecajnaListaPredicted.DAN_ID);
        map.put(TecajnaListaPredicted.DRZAVA_ID, TecajnaListaPredicted.DRZAVA_ID);
        map.put(TecajnaListaPredicted.KUPOVNI_TECAJ, TecajnaListaPredicted.KUPOVNI_TECAJ);
        map.put(TecajnaListaPredicted.SREDNJI_TECAJ, TecajnaListaPredicted.SREDNJI_TECAJ);
        map.put(TecajnaListaPredicted.PRODAJNI_TECAJ, TecajnaListaPredicted.PRODAJNI_TECAJ);

        return map;
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

        switch (URI_MATCHER.match(uri)) {
            case DRZAVA_ID:
                queryBuilder.setTables(Tables.DRZAVA);
                queryBuilder.appendWhere(Drzava._ID + "=" + uri.getLastPathSegment());
                break;
            case DRZAVA:
                queryBuilder.setTables(Tables.DRZAVA);
                break;
            case TECAJNA_LISTA_ID:
                queryBuilder.setTables(Tables.TECAJNA_LISTA);
                queryBuilder.appendWhere(TecajnaLista._ID + "=" + uri.getLastPathSegment());
                break;
            case TECAJNA_LISTA:
                queryBuilder.setTables(Tables.TECAJNA_LISTA);
                break;
            case TECAJNA_LISTA_WITH_DAN_WITH_DRZAVA:
                queryBuilder.setTables(Tables.TECAJNA_LISTA_JOIN_DAN_JOIN_DRZAVA);
                queryBuilder.setProjectionMap(tecajnaListaProjectionMap());
                break;
            case TECAJNA_LISTA_PREDICTED_ID:
                queryBuilder.setTables(Tables.TECAJNA_LISTA_PREDCITED);
                queryBuilder.appendWhere(TecajnaListaPredicted._ID + "=" + uri.getLastPathSegment());
                break;
            case TECAJNA_LISTA_PREDICTED:
                queryBuilder.setTables(Tables.TECAJNA_LISTA_PREDCITED);
                break;
            case TECAJNA_LISTA_PREDICTED_WITH_DAN_WITH_DRZAVA:
                queryBuilder.setTables(Tables.TECAJNA_LISTA_PREDICTED_JOIN_DAN_JOIN_DRZAVA);
                queryBuilder.setProjectionMap(tecajnaListaPredictedProjectionMap());
                break;
            case DAN_ID:
                queryBuilder.setTables(Tables.DAN);
                queryBuilder.appendWhere(Dan._ID + "=" + uri.getLastPathSegment());
                break;
            case DAN:
                queryBuilder.setTables(Tables.DAN);
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
                _id = db.insertOrThrow(Tables.DRZAVA, "", contentValues);
                getContext().getContentResolver().notifyChange(uri, null, false);
                return KonverzijaContract.buildUri(Drzava.CONTENT_URI, _id);
            case TECAJNA_LISTA:
                _id = db.insertOrThrow(Tables.TECAJNA_LISTA, "", contentValues);
                getContext().getContentResolver().notifyChange(uri, null, false);
                return KonverzijaContract.buildUri(TecajnaLista.CONTENT_URI, _id);
            case TECAJNA_LISTA_PREDICTED:
                _id = db.insertOrThrow(Tables.TECAJNA_LISTA_PREDCITED, "", contentValues);
                getContext().getContentResolver().notifyChange(uri, null, false);
                return KonverzijaContract.buildUri(TecajnaListaPredicted.CONTENT_URI, _id);
            case DAN:
                _id = db.insertOrThrow(Tables.DAN, "", contentValues);
                getContext().getContentResolver().notifyChange(uri, null, false);
                return KonverzijaContract.buildUri(Dan.CONTENT_URI, _id);
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
            case TECAJNA_LISTA_PREDICTED:
                count = db.delete(Tables.TECAJNA_LISTA_PREDCITED, selection, selectionArgs);
                break;
            case TECAJNA_LISTA_PREDICTED_ID:
                id = uri.getPathSegments().get(1);
                count = db.delete(Tables.TECAJNA_LISTA_PREDCITED, TecajnaListaPredicted._ID + " = " + id +
                        (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
                break;
            case DAN:
                count = db.delete(Tables.DAN, selection, selectionArgs);
                break;
            case DAN_ID:
                id = uri.getPathSegments().get(1);
                count = db.delete(Tables.DAN, Dan._ID + " = " + id +
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
            case TECAJNA_LISTA_PREDICTED:
                count = db.update(Tables.TECAJNA_LISTA_PREDCITED, contentValues, selection, selectionArgs);
                break;
            case TECAJNA_LISTA_PREDICTED_ID:
                count = db.update(Tables.TECAJNA_LISTA_PREDCITED, contentValues,
                                  TecajnaListaPredicted._ID + " = " + uri.getPathSegments().get(1) +
                                          (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""),
                                  selectionArgs);
                break;
            case DAN:
                count = db.update(Tables.DAN, contentValues, selection, selectionArgs);
                break;
            case DAN_ID:
                count = db.update(Tables.DAN, contentValues, Dan._ID + " = " + uri.getPathSegments().get(1) +
                        (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
}

