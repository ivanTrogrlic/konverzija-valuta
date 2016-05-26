package com.example.ivan.konverzijavaluta.cursor;

import android.content.ContentResolver;
import android.database.Cursor;

import com.example.ivan.konverzijavaluta.database.KonverzijaContract;
import com.example.ivan.konverzijavaluta.database.KonverzijaDatabase.Tables;
import com.example.ivan.konverzijavaluta.entitet.TecajnaListaPredicted;
import com.example.ivan.konverzijavaluta.util.AbstractCursor;

/**
 * Created by ivan on 5/25/2016.
 */
public class TecajnaListaPredictedCursor extends AbstractCursor {

    private ContentResolver m_contentResolver;

    public TecajnaListaPredictedCursor(ContentResolver p_contentResolver, Cursor p_cursor) {
        super(p_cursor);
        m_contentResolver = p_contentResolver;
    }

    public TecajnaListaPredicted toTecajnaListaPredicted() {
        TecajnaListaPredicted tecajnaListaPredicted = new TecajnaListaPredicted();

        for (String columnName : m_cursor.getColumnNames()) {
            if (columnName.startsWith(Tables.DAN + "$")) {
                // Create DanCursor
                DanCursor danCursor = new DanCursor(m_contentResolver, m_cursor, Tables.DAN);
                // Set Dan
                tecajnaListaPredicted.setDan(danCursor.toDan());
                break;
            }
        }

        for (String columnName : m_cursor.getColumnNames()) {
            if (columnName.startsWith(Tables.DRZAVA + "$")) {
                // Create DrzavaCursor
                DrzavaCursor drzavaCursor = new DrzavaCursor(m_contentResolver, m_cursor, Tables.DRZAVA);
                // Set Drzava
                tecajnaListaPredicted.setDrzava(drzavaCursor.toDrzava());
                break;
            }
        }

        Long id = getLong(KonverzijaContract.TecajnaListaPredicted._ID);
        if (id == null) {
            id = getLong(Tables.TECAJNA_LISTA_PREDCITED + "$" + KonverzijaContract.TecajnaListaPredicted._ID);
        }
        tecajnaListaPredicted.setId(id);
        tecajnaListaPredicted.setKupovniTecaj(getBigDecimal(KonverzijaContract.TecajnaListaPredicted.KUPOVNI_TECAJ));
        tecajnaListaPredicted.setSrednjiTecaj(getBigDecimal(KonverzijaContract.TecajnaListaPredicted.SREDNJI_TECAJ));
        tecajnaListaPredicted.setProdajniTecaj(getBigDecimal(KonverzijaContract.TecajnaListaPredicted.PRODAJNI_TECAJ));

        return tecajnaListaPredicted;
    }

}

