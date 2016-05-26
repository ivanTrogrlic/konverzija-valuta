package com.example.ivan.konverzijavaluta.cursor;

import android.content.ContentResolver;
import android.database.Cursor;

import com.example.ivan.konverzijavaluta.database.KonverzijaContract;
import com.example.ivan.konverzijavaluta.database.KonverzijaDatabase.Tables;
import com.example.ivan.konverzijavaluta.entitet.TecajnaLista;
import com.example.ivan.konverzijavaluta.util.AbstractCursor;

/**
 * Created by Ivan on 12.4.2016..
 */
public class TecajnaListaCursor extends AbstractCursor {

    private ContentResolver m_contentResolver;

    public TecajnaListaCursor(ContentResolver p_contentResolver, Cursor p_cursor) {
        super(p_cursor);
        m_contentResolver = p_contentResolver;
    }

    public TecajnaLista toTecajnaLista() {
        TecajnaLista tecajnaLista = new TecajnaLista();

        for (String columnName : m_cursor.getColumnNames()) {
            if (columnName.startsWith(Tables.DAN + "$")) {
                // Create DanCursor
                DanCursor danCursor = new DanCursor(m_contentResolver, m_cursor, Tables.DAN);
                // Set Dan
                tecajnaLista.setDan(danCursor.toDan());
                break;
            }
        }

        for (String columnName : m_cursor.getColumnNames()) {
            if (columnName.startsWith(Tables.DRZAVA + "$")) {
                // Create DrzavaCursor
                DrzavaCursor drzavaCursor = new DrzavaCursor(m_contentResolver, m_cursor, Tables.DRZAVA);
                // Set Drzava
                tecajnaLista.setDrzava(drzavaCursor.toDrzava());
                break;
            }
        }

        Long id = getLong(KonverzijaContract.TecajnaLista._ID);
        if (id == null) {
            id = getLong(Tables.TECAJNA_LISTA + "$" + KonverzijaContract.TecajnaLista._ID);
        }
        tecajnaLista.setId(id);
        tecajnaLista.setKupovniTecaj(getBigDecimal(KonverzijaContract.TecajnaLista.KUPOVNI_TECAJ));
        tecajnaLista.setSrednjiTecaj(getBigDecimal(KonverzijaContract.TecajnaLista.SREDNJI_TECAJ));
        tecajnaLista.setProdajniTecaj(getBigDecimal(KonverzijaContract.TecajnaLista.PRODAJNI_TECAJ));

        return tecajnaLista;
    }

}
