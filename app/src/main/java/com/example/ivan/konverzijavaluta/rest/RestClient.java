package com.example.ivan.konverzijavaluta.rest;

import android.content.Context;

import com.example.ivan.konverzijavaluta.BuildConfig;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.logging.HttpLoggingInterceptor.Level;
import okhttp3.logging.HttpLoggingInterceptor.Logger;
import retrofit2.Retrofit;
import retrofit2.Retrofit.Builder;
import timber.log.Timber;

/**
 * Created by ivan on 5/23/2016.
 */
public class RestClient {

    private static Retrofit s_retrofit;

    public static void init(Context p_context) {
        init(p_context, "https://sdw-wsrest.ecb.europa.eu/service/");
    }

    public static void init(Context p_context, String p_endpoint) {
        Builder builder = new Retrofit.Builder()
                .baseUrl(p_endpoint)
                .client(provideHttpClient(p_context));

        s_retrofit = builder.build();
    }

    public static OkHttpClient provideHttpClient(Context p_context) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor(new Logger() {
            @Override
            public void log(String message) {
                Timber.tag("OkHttp").d(message);
            }
        });
        logging.setLevel(BuildConfig.DEBUG ? Level.BODY : Level.NONE);

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(100, TimeUnit.SECONDS)
                .readTimeout(100, TimeUnit.SECONDS)
                .followRedirects(true)
                .addInterceptor(logging)
                .addInterceptor(new RequestInterceptor());

        return builder.build();
    }

    public static <T> T create(Class<T> serviceClass) {
        return s_retrofit.create(serviceClass);
    }
}
