package com.example.ivan.konverzijavaluta.repository;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.ivan.konverzijavaluta.cursor.DrzavaCursor;
import com.example.ivan.konverzijavaluta.database.KonverzijaContract;
import com.example.ivan.konverzijavaluta.entitet.Drzava;
import com.example.ivan.konverzijavaluta.util.DbUtils;

import java.util.Date;

/**
 * Created by Ivan on 12.4.2016..
 */
public class DrzavaRepository {

    private ContentResolver m_contentResolver;

    public DrzavaRepository(ContentResolver p_contentResolver) {
        m_contentResolver = p_contentResolver;
    }

    /**
     * Returns Drzava for given id, or null if it doesn't exists.
     */
    public Drzava getById(long p_id) {
        String[] projection = getProjection();
        String whereClause = KonverzijaContract.Drzava._ID + "=?";
        String[] whereArgs = {String.valueOf(p_id)};

        return query(projection, whereClause, whereArgs);
    }

    /**
     * Returns Drzava for given sifra, or null if it doesn't exists.
     */
    public Drzava getBySifra(String p_sifra) {
        String[] projection = getProjection();
        String whereClause = KonverzijaContract.Drzava.SIFRA + "=?";
        String[] whereArgs = {p_sifra};

        return query(projection, whereClause, whereArgs);
    }

    /**
     * Returns Drzava for given valuta, or null if it doesn't exists.
     */
    public Drzava getByValuta(String p_valuta) {
        String[] projection = getProjection();
        String whereClause = KonverzijaContract.Drzava.VALUTA + "=?";
        String[] whereArgs = {p_valuta};

        return query(projection, whereClause, whereArgs);
    }

    /**
     * Inserts Drzava into db, and returns the ID of inserted Drzava.
     */
    public long insert(Drzava p_drzava) {
        ContentValues values = new ContentValues();
        values.put(KonverzijaContract.Drzava._ID, p_drzava.getId());
        values.put(KonverzijaContract.Drzava.SIFRA, p_drzava.getSifra());
        values.put(KonverzijaContract.Drzava.VALUTA, p_drzava.getValuta());
        values.put(KonverzijaContract.Drzava.JEDINICA, p_drzava.getJedinica());
        Uri uri = m_contentResolver.insert(KonverzijaContract.Drzava.CONTENT_URI, values);
        return KonverzijaContract.getId(uri);
    }

    /**
     * Deletes Drzava where date is equal given date, and returns 0 if Drzava is deleted, or 0 if its not deleted.
     */
    public long delete(Date p_date) {
        return m_contentResolver.delete
                (KonverzijaContract.Dan.CONTENT_URI,
                 KonverzijaContract.Dan.DAN + " = ? ",
                 new String[]{DbUtils.toDbDate(p_date)});
    }

    @NonNull
    private String[] getProjection() {
        return new String[]{KonverzijaContract.Drzava._ID, KonverzijaContract.Drzava.SIFRA,
                KonverzijaContract.Drzava.JEDINICA, KonverzijaContract.Drzava.VALUTA};
    }

    @Nullable
    private Drzava query(String[] p_projection, String p_whereClause, String[] p_whereArgs) {
        Cursor cursor = m_contentResolver.query(KonverzijaContract.Drzava.CONTENT_URI, p_projection, p_whereClause,
                                                p_whereArgs, null);
        if (cursor == null) {
            return null;
        }

        Drzava drzava = null;
        if (cursor.moveToFirst()) {
            drzava = new DrzavaCursor(m_contentResolver, cursor).toDrzava();
        }

        cursor.close();
        return drzava;
    }

}
