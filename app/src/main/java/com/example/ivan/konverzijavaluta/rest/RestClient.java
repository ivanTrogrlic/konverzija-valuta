package com.example.ivan.konverzijavaluta.rest;

import com.example.ivan.konverzijavaluta.BuildConfig;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.logging.HttpLoggingInterceptor.Level;
import okhttp3.logging.HttpLoggingInterceptor.Logger;
import retrofit2.Retrofit;
import retrofit2.Retrofit.Builder;
import retrofit2.converter.jackson.JacksonConverterFactory;
import timber.log.Timber;

/**
 * Created by ivan on 5/23/2016.
 */
public class RestClient {

    private static Retrofit s_retrofitEcb;
    private static Retrofit s_retrofitWorld;

    public static void init() {
        initEcb("https://sdw-wsrest.ecb.europa.eu/service/");
        initWord("http://api.worldbank.org/");
    }

    public static void initEcb(String p_endpoint) {
        Builder builder = new Retrofit.Builder()
                .baseUrl(p_endpoint)
                .client(provideHttpClient());

        s_retrofitEcb = builder.build();
    }

    public static void initWord(String p_endpoint) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        Builder builder = new Retrofit.Builder()
                .baseUrl(p_endpoint)
                .addConverterFactory(JacksonConverterFactory.create(objectMapper))
                .client(provideHttpClient());

        s_retrofitWorld = builder.build();
    }

    public static OkHttpClient provideHttpClient() {
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

    public static <T> T createEcb(Class<T> serviceClass) {
        return s_retrofitEcb.create(serviceClass);
    }

    public static <T> T createWorld(Class<T> serviceClass) {
        return s_retrofitWorld.create(serviceClass);
    }
}
