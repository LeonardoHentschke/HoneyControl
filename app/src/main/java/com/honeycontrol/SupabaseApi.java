package com.honeycontrol;

import com.honeycontrol.models.Companies;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface SupabaseApi {
    @GET("companies")
    Call<List<Companies>> getCompanies();

    @POST("companies")
    Call<Companies> createCompanies(@Body Companies companies);

    
}
