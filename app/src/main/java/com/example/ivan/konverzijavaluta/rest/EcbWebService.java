package com.example.ivan.konverzijavaluta.rest;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

/**
 * Created by ivan on 5/23/2016.
 */
public interface EcbWebService {
    @GET("data/EXR/{path}")
    Call<ResponseBody> get(@Path("path") String p_path, @QueryMap Map<String, String> p_params);

    @GET("data/FM/M.U2.EUR.4F.CY.OILBRNI.HSTA")
    Call<ResponseBody> getOil(@QueryMap Map<String, String> p_params);
}
