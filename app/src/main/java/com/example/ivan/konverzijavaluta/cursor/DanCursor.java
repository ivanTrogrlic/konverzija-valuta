package com.example.ivan.konverzijavaluta.cursor;

import android.content.ContentResolver;
import android.database.Cursor;

import com.example.ivan.konverzijavaluta.database.KonverzijaContract;
import com.example.ivan.konverzijavaluta.entitet.Dan;
import com.example.ivan.konverzijavaluta.util.AbstractCursor;

import org.joda.time.LocalDate;

/**
 * Created by Ivan on 12.4.2016..
 */
public class DanCursor extends AbstractCursor {

    private ContentResolver m_contentResolver;

    public DanCursor(ContentResolver p_contentResolver, Cursor p_cursor) {
        super(p_cursor);
        m_contentResolver = p_contentResolver;
    }

    // Use this constructor if you selected specified columns
    public DanCursor(ContentResolver p_contentResolver, Cursor p_cursor, String p_columnPrefix) {
        super(p_cursor, p_columnPrefix);
        m_contentResolver = p_contentResolver;
    }

    public Dan toDan() {
        Dan dan = new Dan();
        dan.setId(getLong(KonverzijaContract.Dan._ID));
        dan.setDan(new LocalDate(getDate(KonverzijaContract.Dan.DAN)));
        return dan;
    }
}
