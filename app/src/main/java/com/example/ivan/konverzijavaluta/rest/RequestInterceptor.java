package com.example.ivan.konverzijavaluta.rest;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by ivan on 5/23/2016.
 */
public class RequestInterceptor implements Interceptor {

    @Override
    public Response intercept(Interceptor.Chain chain) throws IOException {
        Request original = chain.request();

        Request request = original.newBuilder()
                .addHeader("Accept", "text/csv")
                .build();

        return chain.proceed(request);
    }
}