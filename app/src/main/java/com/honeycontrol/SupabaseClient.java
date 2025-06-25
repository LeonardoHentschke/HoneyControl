package com.honeycontrol;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class SupabaseClient {
    private static final String BASE_URL = "https://dcwdfnuziqlhfhrtcgjw.supabase.co/rest/v1/";
    private static final String API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImRjd2RmbnV6aXFsaGZocnRjZ2p3Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTA2MjQ5MDEsImV4cCI6MjA2NjIwMDkwMX0.mAoIg8VBng_Ua1-yCTxU4ESofa9NY1zv_3ZkcDCFrjE";

    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            // Configurar Gson para lidar com o formato de data do Supabase
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            
            Gson gson = new GsonBuilder()
                    .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                    .setLenient()
                    .create();
            
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(chain -> {
                        Request request = chain.request().newBuilder()
                                .addHeader("apikey", API_KEY)
                                .addHeader("Authorization", "Bearer " + API_KEY)
                                .addHeader("Content-Type", "application/json")
                                .build();
                        return chain.proceed(request);
                    })
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }

        return retrofit;
    }
}

