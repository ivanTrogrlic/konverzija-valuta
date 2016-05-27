package com.example.ivan.konverzijavaluta.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.example.ivan.konverzijavaluta.service.DownloadIntentService;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;

/**
 * Created by ivan on 5/24/2016.
 */
public class Preferences {

    public static final String LAST_DOWNLOADED_EXCHANGE_LIST = "last_downloaded_exchange_list";
    public static final String LAST_PREDICTED_DATE           = "last_predicted_date";
    public static final String LAST_PREDICTED_HRK_DATE       = "last_predicted_hrk_date";
    public static final String INITIAL_EXCHANGE_LIST_SAVED   = "initial_exchange_list_saved";
    public static final String INITIAL_HRK_SAVED             = "initial_hrk_saved";
    public static final String TRAINING_EXCHANGE_LIST_SAVED  = "training_exchange_list_saved";
    public static final String TRAINING_HRK_SAVED            = "training_hrk_saved";
    public static final String CONVERTED_CSV_TO_SQL          = "converted_csv_to_sql";

    public static void saveDate(Context p_context, String p_key, LocalDate p_date) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(p_context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(p_key, p_date.toDate().getTime());
        editor.apply();
    }

    public static LocalDate loadDate(Context p_context, String p_key, LocalDate p_default) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(p_context);
        long value = preferences.getLong(p_key, p_default.toDate().getTime());
        return new LocalDate(value);
    }

    public static void saveBoolean(Context p_context, String p_key, boolean p_saved) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(p_context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(p_key, p_saved);
        editor.apply();
    }

    public static boolean loadBoolean(Context p_context, String p_key, boolean p_default) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(p_context);
        return preferences.getBoolean(p_key, p_default);
    }

    public static LocalDate getLastDownloadDate(Context p_context) {
        LocalDate defaultDate = LocalDate.parse(DownloadIntentService.DEFAULT_DATE,
                                                DateTimeFormat.forPattern(DownloadIntentService.DATE_FORMAT));
        return Preferences.loadDate(p_context, Preferences.LAST_DOWNLOADED_EXCHANGE_LIST, defaultDate);
    }

}
