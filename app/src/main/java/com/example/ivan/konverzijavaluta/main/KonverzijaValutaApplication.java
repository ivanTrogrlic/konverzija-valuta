package com.example.ivan.konverzijavaluta.main;

import android.app.Application;

import com.example.ivan.konverzijavaluta.BuildConfig;
import com.example.ivan.konverzijavaluta.rest.RestClient;
import com.facebook.stetho.Stetho;

import net.danlew.android.joda.JodaTimeAndroid;

import timber.log.Timber;

/**
 * Created by Ivan on 16.4.2016..
 */
public class KonverzijaValutaApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
            Stetho.initializeWithDefaults(this);
        }

        JodaTimeAndroid.init(this);
        RestClient.init();

    }
}
