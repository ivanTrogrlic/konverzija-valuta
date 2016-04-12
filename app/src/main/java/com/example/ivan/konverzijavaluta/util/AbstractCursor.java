package com.example.ivan.konverzijavaluta.util;

import android.database.Cursor;
import android.provider.BaseColumns;

import java.math.BigDecimal;

/**
 * Created by Ivan on 12.4.2016..
 */
public abstract class AbstractCursor {

    protected Cursor m_cursor;
    private   String m_columnPrefix;

    public AbstractCursor(Cursor p_cursor) {
        this(p_cursor, null);
    }

    public AbstractCursor(Cursor p_cursor, String p_columnPrefix) {
        m_cursor = p_cursor;
        m_columnPrefix = p_columnPrefix;
    }

    public Long getId() {
        String column = BaseColumns._ID;
        return getLong(column);
    }

    public Long getLong(String p_column) {
        int columnIndex = getColumnIndex(p_column);
        if (columnIndex == -1) {
            return null;
        }
        return m_cursor.isNull(columnIndex) ? null : m_cursor.getLong(columnIndex);
    }

    public BigDecimal getBigDecimal(String p_column) {
        return DbUtils.fromDbBigDecimal(getString(p_column));
    }

    public java.sql.Date getDate(String p_column) {
        return DbUtils.fromDbDate(getString(p_column));
    }

    public String getString(String p_column) {
        int columnIndex = getColumnIndex(p_column);
        if (columnIndex == -1) {
            return null;
        }

        return m_cursor.isNull(columnIndex) ? null : m_cursor.getString(columnIndex);
    }

    public Integer getInt(String p_column) {
        int columnIndex = getColumnIndex(p_column);
        if (columnIndex == -1) {
            return null;
        }

        return m_cursor.isNull(columnIndex) ? null : m_cursor.getInt(columnIndex);
    }

    private int getColumnIndex(String p_column) {
        if (m_columnPrefix != null) {
            p_column = m_columnPrefix + "$" + p_column;
        }

        return m_cursor.getColumnIndex(p_column);
    }

    public static int getColumnIndex(Cursor p_cursor, String p_tableName, String p_column) {
        if (p_cursor == null) {
            return -1;
        }

        if (p_tableName != null) {
            p_column = p_tableName + "$" + p_column;
        }

        return p_cursor.getColumnIndex(p_column);
    }

    public void close() {
        if (m_cursor != null) {
            m_cursor.close();
        }
    }

}
