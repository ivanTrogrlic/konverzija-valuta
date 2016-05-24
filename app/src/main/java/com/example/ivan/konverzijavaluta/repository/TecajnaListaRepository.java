package com.example.ivan.konverzijavaluta.repository;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.ivan.konverzijavaluta.cursor.TecajnaListaCursor;
import com.example.ivan.konverzijavaluta.database.KonverzijaContract;
import com.example.ivan.konverzijavaluta.entitet.TecajnaLista;
import com.example.ivan.konverzijavaluta.util.DbUtils;

/**
 * Created by Ivan on 12.4.2016..
 */
public class TecajnaListaRepository {

    private ContentResolver m_contentResolver;

    public TecajnaListaRepository(ContentResolver p_contentResolver) {
        m_contentResolver = p_contentResolver;
    }

    /**
     * Returns Tecajna lista for given id, or null if it doesn't exists.
     */
    public TecajnaLista getById(long p_id) {
        String[] projection = getProjection();
        String whereClause = KonverzijaContract.TecajnaLista._ID + "=?";
        String[] whereArgs = {String.valueOf(p_id)};

        return query(projection, whereClause, whereArgs);
    }

    /**
     * Returns TecajnaLista for given Drzava ID, or null if it doesn't exists.
     */
    public TecajnaLista getByDrzava(long p_drzavaId) {
        String[] projection = getProjection();
        String whereClause = KonverzijaContract.TecajnaLista.DRZAVA_ID + "=?";
        String[] whereArgs = {String.valueOf(p_drzavaId)};

        return query(projection, whereClause, whereArgs);
    }

    /**
     * Returns TecajnaLista for given Dan ID, or null if it doesn't exists.
     */
    public TecajnaLista getByDan(long p_danId) {
        String[] projection = getProjection();
        String whereClause = KonverzijaContract.TecajnaLista.DAN_ID + "=?";
        String[] whereArgs = {String.valueOf(p_danId)};

        return query(projection, whereClause, whereArgs);
    }

    /**
     * Returns TecajnaLista for given Dan ID and Drzava ID, or null if it doesn't exists.
     */
    public TecajnaLista getByDanAndDrzava(long p_danId, long p_drzavaId) {
        String[] projection = getProjection();
        String whereClause = KonverzijaContract.TecajnaLista.DAN_ID + "=? AND " + KonverzijaContract.TecajnaLista.DRZAVA_ID + "=?";
        String[] whereArgs = {String.valueOf(p_danId), String.valueOf(p_drzavaId)};

        return query(projection, whereClause, whereArgs);
    }

    /**
     * Inserts TecajnaLista into db, and returns the ID of inserted TecajnaLista.
     */
    public long insert(TecajnaLista p_tecajnaLista) {
        ContentValues values = new ContentValues();
        values.put(KonverzijaContract.TecajnaLista._ID, p_tecajnaLista.getId());
        values.put(KonverzijaContract.TecajnaLista.DAN_ID, p_tecajnaLista.getDan().getId());
        values.put(KonverzijaContract.TecajnaLista.DRZAVA_ID, p_tecajnaLista.getDrzava().getId());
        values.put(KonverzijaContract.TecajnaLista.KUPOVNI_TECAJ,
                   DbUtils.toDbBigDecimal(p_tecajnaLista.getKupovniTecaj()));
        values.put(KonverzijaContract.TecajnaLista.SREDNJI_TECAJ,
                   DbUtils.toDbBigDecimal(p_tecajnaLista.getSrednjiTecaj()));
        values.put(KonverzijaContract.TecajnaLista.PRODAJNI_TECAJ,
                   DbUtils.toDbBigDecimal(p_tecajnaLista.getProdajniTecaj()));
        Uri uri = m_contentResolver.insert(KonverzijaContract.TecajnaLista.CONTENT_URI, values);
        return KonverzijaContract.getId(uri);
    }

    /**
     * Deletes TecajnaLista where date is equal given date, and returns 0 if TecajnaLista is deleted, or 0 if its not deleted.
     */
    public long delete(Long p_id) {
        return m_contentResolver.delete
                (KonverzijaContract.TecajnaLista.CONTENT_URI,
                 KonverzijaContract.TecajnaLista._ID + " = ? ",
                 new String[]{String.valueOf(p_id)});
    }

    @NonNull
    private String[] getProjection() {
        return new String[]{KonverzijaContract.TecajnaLista._ID, KonverzijaContract.TecajnaLista.DAN_ID,
                KonverzijaContract.TecajnaLista.DRZAVA_ID, KonverzijaContract.TecajnaLista.KUPOVNI_TECAJ,
                KonverzijaContract.TecajnaLista.SREDNJI_TECAJ, KonverzijaContract.TecajnaLista.PRODAJNI_TECAJ};
    }

    @Nullable
    private TecajnaLista query(String[] p_projection, String p_whereClause, String[] p_whereArgs) {
        Cursor cursor = m_contentResolver.query(KonverzijaContract.TecajnaLista.CONTENT_URI, p_projection,
                                                p_whereClause, p_whereArgs, null);
        if (cursor == null) {
            return null;
        }

        TecajnaLista tecajnaLista = null;
        if (cursor.moveToFirst()) {
            tecajnaLista = new TecajnaListaCursor(m_contentResolver, cursor).toTecajnaLista();
        }

        cursor.close();
        return tecajnaLista;
    }
}
