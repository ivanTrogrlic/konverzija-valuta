package com.example.ivan.konverzijavaluta.repository;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.example.ivan.konverzijavaluta.cursor.DanCursor;
import com.example.ivan.konverzijavaluta.database.KonverzijaContract;
import com.example.ivan.konverzijavaluta.entitet.Dan;
import com.example.ivan.konverzijavaluta.util.DbUtils;

import org.joda.time.LocalDate;

import java.util.Date;

/**
 * Created by Ivan on 12.4.2016..
 */
public class DanRepository {

    private ContentResolver m_contentResolver;

    public DanRepository(ContentResolver p_contentResolver) {
        m_contentResolver = p_contentResolver;
    }

    /**
     * Returns last Dan from the table, or null if table is empty.
     */
    public Dan getLast() {
        String[] projection = new String[]{KonverzijaContract.Dan.DAN};
        String sortOrder = " " + KonverzijaContract.Dan.DAN + " DESC";

        return query(projection, null, null, sortOrder);
    }

    /**
     * Returns Dan for given date, or null if it doesn't exists.
     */
    public Dan getById(long p_id) {
        String[] projection = new String[]{KonverzijaContract.Dan.DAN};
        String whereClause = KonverzijaContract.Dan._ID + "=?";
        String[] whereArgs = {String.valueOf(p_id)};

        return query(projection, whereClause, whereArgs, null);
    }

    /**
     * Returns Dan for given date, or null if it doesn't exists.
     */
    public Dan getByDate(LocalDate p_date) {
        String[] projection = new String[]{KonverzijaContract.Dan.DAN};
        String whereClause = KonverzijaContract.Dan.DAN + "=?";
        String[] whereArgs = {DbUtils.toDbDate(p_date)};

        return query(projection, whereClause, whereArgs, null);
    }

    /**
     * Inserts Dan into db, and returns the ID of inserted Dan.
     */
    public long insert(Dan p_dan) {
        ContentValues values = new ContentValues();
        values.put(KonverzijaContract.Dan.DAN, DbUtils.toDbDate(p_dan.getDan()));
        Uri uri = m_contentResolver.insert(KonverzijaContract.Dan.CONTENT_URI, values);
        return KonverzijaContract.getId(uri);
    }

    /**
     * Deletes Dan where date is equal given date, and returns 0 if Dan is deleted, or 0 if its not deleted.
     */
    public long delete(LocalDate p_date) {
        return m_contentResolver.delete
                (KonverzijaContract.Dan.CONTENT_URI,
                 KonverzijaContract.Dan.DAN + " = ? ",
                 new String[]{DbUtils.toDbDate(p_date)});
    }

    @Nullable
    private Dan query(String[] p_projection, String p_whereClause, String[] p_whereArgs, String p_sortOrder) {
        Cursor cursor = m_contentResolver.query(KonverzijaContract.Dan.CONTENT_URI, p_projection, p_whereClause,
                                                p_whereArgs, p_sortOrder);
        if (cursor == null) {
            return null;
        }

        Dan dan = null;
        if (cursor.moveToFirst()) {
            dan = new DanCursor(m_contentResolver, cursor).toDan();
        }

        cursor.close();
        return dan;
    }

}
