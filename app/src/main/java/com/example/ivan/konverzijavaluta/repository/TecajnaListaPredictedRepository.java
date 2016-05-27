package com.example.ivan.konverzijavaluta.repository;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.ivan.konverzijavaluta.cursor.TecajnaListaPredictedCursor;
import com.example.ivan.konverzijavaluta.database.KonverzijaContract;
import com.example.ivan.konverzijavaluta.database.KonverzijaContract.Dan;
import com.example.ivan.konverzijavaluta.database.KonverzijaContract.Drzava;
import com.example.ivan.konverzijavaluta.database.KonverzijaDatabase.Tables;
import com.example.ivan.konverzijavaluta.entitet.TecajnaListaPredicted;
import com.example.ivan.konverzijavaluta.util.DbUtils;

import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ivan on 5/25/2016.
 */
public class TecajnaListaPredictedRepository {

    private ContentResolver m_contentResolver;

    public TecajnaListaPredictedRepository(ContentResolver p_contentResolver) {
        m_contentResolver = p_contentResolver;
    }

    /**
     * Returns Tecajna lista for given id, or null if it doesn't exists.
     */
    public TecajnaListaPredicted getById(long p_id) {
        String[] projection = getProjection();
        String whereClause = KonverzijaContract.TecajnaListaPredicted._ID + "=?";
        String[] whereArgs = {String.valueOf(p_id)};

        List<TecajnaListaPredicted> list = query(projection, whereClause, whereArgs);
        return list == null || list.isEmpty() ? null : list.get(0);
    }

    /**
     * Returns TecajnaListaPredicted for given Drzava ID, or null if it doesn't exists.
     */
    public List<TecajnaListaPredicted> getByDrzava(long p_drzavaId, boolean p_soloPredicted) {
        String[] projection = getProjection();
        String whereClause = KonverzijaContract.TecajnaListaPredicted.DRZAVA_ID + "=? AND " + KonverzijaContract.TecajnaListaPredicted.SOLO_PREDICTED + "=?";
        String[] whereArgs = {String.valueOf(p_drzavaId), String.valueOf(p_soloPredicted ? 1 : 0)};

        return query(projection, whereClause, whereArgs);
    }

    /**
     * Returns TecajnaListaPredicted for given Dan ID, or null if it doesn't exists.
     */
    public List<TecajnaListaPredicted> getByDan(long p_danId, boolean p_soloPredicted) {
        String[] projection = getProjection();
        String whereClause = KonverzijaContract.TecajnaListaPredicted.DAN_ID + "=? AND " + KonverzijaContract.TecajnaListaPredicted.SOLO_PREDICTED + "=?";
        String[] whereArgs = {String.valueOf(p_danId), String.valueOf(p_soloPredicted ? 1 : 0)};

        return query(projection, whereClause, whereArgs);
    }

    /**
     * Returns TecajnaListaPredicted for given Dan ID and Drzava ID, or null if it doesn't exists.
     */
    public TecajnaListaPredicted getByDanAndDrzava(long p_danId, long p_drzavaId, boolean p_soloPredicted) {
        String[] projection = getProjection();
        String whereClause = KonverzijaContract.TecajnaListaPredicted.DAN_ID + "=? AND " + KonverzijaContract.TecajnaListaPredicted.DRZAVA_ID + "=? AND " + KonverzijaContract.TecajnaListaPredicted.SOLO_PREDICTED + "=?";
        String[] whereArgs = {String.valueOf(p_danId), String.valueOf(p_drzavaId),
                String.valueOf(p_soloPredicted ? 1 : 0)};

        List<TecajnaListaPredicted> list = query(projection, whereClause, whereArgs);
        return list == null || list.isEmpty() ? null : list.get(0);
    }

    /**
     * Inserts TecajnaListaPredicted into db, and returns the ID of inserted TecajnaListaPredicted.
     */
    public long insert(TecajnaListaPredicted p_tecajnaListaPredicted) {
        ContentValues values = new ContentValues();
        values.put(KonverzijaContract.TecajnaListaPredicted._ID, p_tecajnaListaPredicted.getId());
        values.put(KonverzijaContract.TecajnaListaPredicted.DAN_ID, p_tecajnaListaPredicted.getDan().getId());
        values.put(KonverzijaContract.TecajnaListaPredicted.DRZAVA_ID, p_tecajnaListaPredicted.getDrzava().getId());
        values.put(KonverzijaContract.TecajnaListaPredicted.KUPOVNI_TECAJ,
                   DbUtils.toDbBigDecimal(p_tecajnaListaPredicted.getKupovniTecaj()));
        values.put(KonverzijaContract.TecajnaListaPredicted.SREDNJI_TECAJ,
                   DbUtils.toDbBigDecimal(p_tecajnaListaPredicted.getSrednjiTecaj()));
        values.put(KonverzijaContract.TecajnaListaPredicted.PRODAJNI_TECAJ,
                   DbUtils.toDbBigDecimal(p_tecajnaListaPredicted.getProdajniTecaj()));
        values.put(KonverzijaContract.TecajnaListaPredicted.SOLO_PREDICTED,
                   p_tecajnaListaPredicted.isSoloPredicted() ? 1 : 0);
        Uri uri = m_contentResolver.insert(KonverzijaContract.TecajnaListaPredicted.CONTENT_URI, values);
        return KonverzijaContract.getId(uri);
    }

    /**
     * Deletes TecajnaListaPredicted where date is equal given date, and returns 0 if TecajnaListaPredicted is deleted, or 0 if its not deleted.
     */
    public long delete(Long p_id) {
        return m_contentResolver.delete
                (KonverzijaContract.TecajnaListaPredicted.CONTENT_URI,
                 KonverzijaContract.TecajnaListaPredicted._ID + " = ? ",
                 new String[]{String.valueOf(p_id)});
    }

    @NonNull
    private String[] getProjection() {
        return new String[]{KonverzijaContract.TecajnaListaPredicted._ID, KonverzijaContract.TecajnaListaPredicted.DAN_ID,
                KonverzijaContract.TecajnaListaPredicted.DRZAVA_ID, KonverzijaContract.TecajnaListaPredicted.KUPOVNI_TECAJ,
                KonverzijaContract.TecajnaListaPredicted.SREDNJI_TECAJ, KonverzijaContract.TecajnaListaPredicted.PRODAJNI_TECAJ,
                KonverzijaContract.TecajnaListaPredicted.SOLO_PREDICTED,
                Tables.DAN + "$" + Dan._ID, Tables.DAN + "$" + Dan.DAN,
                Tables.DRZAVA + "$" + Drzava._ID, Tables.DRZAVA + "$" + Drzava.JEDINICA, Tables.DRZAVA + "$" + Drzava.SIFRA, Tables.DRZAVA + "$" + Drzava.VALUTA};
    }

    @Nullable
    private List<TecajnaListaPredicted> query(String[] p_projection, String p_whereClause, String[] p_whereArgs) {
        Uri uri = KonverzijaContract.TecajnaListaPredicted.CONTENT_URI
                .buildUpon()
                .appendPath(KonverzijaContract.PATH_WITH_DAN_AND_DRZAVA)
                .build();
        Cursor cursor = m_contentResolver.query(uri, p_projection, p_whereClause, p_whereArgs, null);
        if (cursor == null) {
            return null;
        }

        List<TecajnaListaPredicted> lista = new ArrayList<>();
        while (cursor.moveToNext()) {
            lista.add(new TecajnaListaPredictedCursor(m_contentResolver, cursor).toTecajnaListaPredicted());
        }

        cursor.close();
        return lista;
    }

}
