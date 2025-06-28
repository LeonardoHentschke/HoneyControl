package com.honeycontrol.services;

import com.honeycontrol.SupabaseApi;
import com.honeycontrol.SupabaseClient;

public class ApiService {
    private static ApiService instance;
    private final SupabaseApi supabaseApi;

    private ApiService() {
        supabaseApi = SupabaseClient.getClient().create(SupabaseApi.class);
    }

    public static synchronized ApiService getInstance() {
        if (instance == null) {
            instance = new ApiService();
        }
        return instance;
    }

    public SupabaseApi getApi() {
        return supabaseApi;
    }
}
