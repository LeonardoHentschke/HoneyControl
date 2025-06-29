package com.honeycontrol.services;

public class ApiService {
    private static ApiService instance;
    private final SupabaseApi supabaseApi;

    private ApiService() {
        supabaseApi = SupabaseClient.createService(SupabaseApi.class);
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
