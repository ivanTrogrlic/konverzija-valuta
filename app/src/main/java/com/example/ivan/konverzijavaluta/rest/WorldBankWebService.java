package com.example.ivan.konverzijavaluta.rest;

import com.example.ivan.konverzijavaluta.entitet.WorldBankModel;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

/**
 * Created by ivan on 7/7/2016.
 */
public interface WorldBankWebService {
    @GET("countries/{path}")
    Call<List<WorldBankModel>[]> get(@Path("path") String p_path, @QueryMap Map<String, String> p_params);
}
