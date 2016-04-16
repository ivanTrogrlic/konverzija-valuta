package com.example.ivan.konverzijavaluta.util;

/**
 * Created by Ivan on 12.4.2016..
 */

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DbUtils {
    private static final SimpleDateFormat  SDF_DATE = new SimpleDateFormat("yyyy-MM-dd");
    private static final DateTimeFormatter DTF_DATE = DateTimeFormat.forPattern("yyyy-MM-dd");

    private static Object s_sdfLock = new Object();

    /**
     * java.sql.Date to 'yyyy-MM-dd'</br>
     * java.util.Date to 'yyyy-MM-dd HH:mm:ss'
     *
     * @return <code>null</code> if input param is <code>null</code>.
     */
    public static String toDbDate(Date p_date) {
        String retVal = null;
        if (p_date == null) {
            return retVal;
        }

        synchronized (s_sdfLock) {
            SDF_DATE.setTimeZone(TimeZone.getDefault());
            retVal = SDF_DATE.format(p_date);
        }

        return retVal;
    }

    public static String toDbDate(LocalDate p_localDate) {
        if (p_localDate == null) return null;

        String retVal;
        synchronized (s_sdfLock) {
            retVal = DTF_DATE.print(p_localDate);
        }
        return retVal;
    }

    /**
     * @return <code>null</code> if string doesn't match pattern or if param is <code>null</code>
     */
    public static java.sql.Date fromDbDate(String p_dateString) {
        if (p_dateString == null) {
            return null;
        }
        java.sql.Date retVal = null;

        synchronized (s_sdfLock) {
            try {
                SDF_DATE.setTimeZone(TimeZone.getDefault());
                Date date = SDF_DATE.parse(p_dateString);
                retVal = new java.sql.Date(date.getTime());
                return retVal;
            } catch (ParseException e) {
            }
        }

        return null;
    }

    public static String toDbBigDecimal(BigDecimal p_val) {
        String retVal = null;
        if (p_val != null) {
            retVal = p_val.toString();
        }

        return retVal;
    }

    public static BigDecimal fromDbBigDecimal(String p_arg) {
        if (p_arg == null) {
            return null;
        }

        BigDecimal retVal = new BigDecimal(p_arg);
        return retVal;
    }

    public static String dbCreateForeignKey(String p_sourceColumn, String p_targetTable, String p_targetColumn) {
        String retVal = new StringBuilder()
                .append("FOREIGN KEY(").append(p_sourceColumn).append(") ")
                .append("REFERENCES ").append(p_targetTable).append("(").append(p_targetColumn).append(")")
                .toString();
        return retVal;
    }

}

