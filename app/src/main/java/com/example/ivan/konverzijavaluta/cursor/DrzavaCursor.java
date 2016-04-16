package com.example.ivan.konverzijavaluta.cursor;

import android.content.ContentResolver;
import android.database.Cursor;

import com.example.ivan.konverzijavaluta.database.KonverzijaContract;
import com.example.ivan.konverzijavaluta.entitet.Drzava;
import com.example.ivan.konverzijavaluta.util.AbstractCursor;

/**
 * Created by Ivan on 12.4.2016..
 */
public class DrzavaCursor extends AbstractCursor {

    private ContentResolver m_contentResolver;

    public DrzavaCursor(ContentResolver p_contentResolver, Cursor p_cursor) {
        super(p_cursor);
        m_contentResolver = p_contentResolver;
    }

    // Use this constructor if you selected specified columns
    public DrzavaCursor(ContentResolver p_contentResolver, Cursor p_cursor, String p_columnPrefix) {
        super(p_cursor, p_columnPrefix);
        m_contentResolver = p_contentResolver;
    }

    public Drzava toDrzava() {
        Drzava drzava = new Drzava();
        drzava.setId(getLong(KonverzijaContract.Drzava._ID));
        drzava.setJedinica(getInt(KonverzijaContract.Drzava.JEDINICA));
        drzava.setSifra(getString(KonverzijaContract.Drzava.SIFRA));
        drzava.setValuta(getString(KonverzijaContract.Drzava.VALUTA));

        return drzava;
    }

}
