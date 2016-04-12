package com.example.ivan.konverzijavaluta.repository;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;

import com.example.ivan.konverzijavaluta.cursor.DanCursor;
import com.example.ivan.konverzijavaluta.database.KonverzijaContract;
import com.example.ivan.konverzijavaluta.entitet.Dan;

/**
 * Created by Ivan on 12.4.2016..
 */
public class DanRepository {

    private Context m_context;

    public DanRepository(Context p_context) {
        m_context = p_context;
    }

    public Dan getLast() {
        ContentResolver resolver = m_context.getContentResolver();
        String[] projection = new String[]{KonverzijaContract.Dan.DAN};
        String sortOrder = " " + KonverzijaContract.Dan.DAN + " DESC";

        Cursor cursor = resolver.query(KonverzijaContract.Dan.CONTENT_URI, projection, null, null, sortOrder);
        if (cursor == null) {
            return null;
        }

        Dan dan = null;
        if (cursor.moveToFirst()) {
            dan = new DanCursor(m_context.getContentResolver(), cursor).toDan();
        }

        cursor.close();
        return dan;
    }


}
