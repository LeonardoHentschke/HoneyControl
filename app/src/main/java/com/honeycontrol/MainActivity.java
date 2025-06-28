package com.honeycontrol;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.ArrayList;
import com.honeycontrol.adapters.CompaniesAdapter;
import com.honeycontrol.models.Companies;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private CompaniesAdapter companiesAdapter;
    private List<Companies> currentCompaniesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        RecyclerView recyclerViewCompanies = findViewById(R.id.recyclerViewCompanies);
        recyclerViewCompanies.setLayoutManager(new LinearLayoutManager(this));
        companiesAdapter = new CompaniesAdapter(null);
        recyclerViewCompanies.setAdapter(companiesAdapter);

        SupabaseApi api = SupabaseClient.getClient().create(SupabaseApi.class);

        loadCompanies(api);
    }
    
    private void loadCompanies(SupabaseApi api) {
        Log.d("MainActivity", "Fazendo chamada para a API...");
        Call<List<Companies>> call = api.getCompanies();
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<Companies>> call, @NonNull Response<List<Companies>> response) {
                Log.d("MainActivity", "Resposta recebida - Código: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    currentCompaniesList = response.body();
                    Log.d("MainActivity", "Companies loaded successfully: " + currentCompaniesList.size() + " companies");
                    
                    // Log dos nomes das companies para debug
                    for (Companies company : currentCompaniesList) {
                        Log.d("MainActivity", "Company: " + company.getName());
                    }
                    
                    companiesAdapter.updateData(currentCompaniesList);
                } else {
                    Log.e("MainActivity", "Failed to load companies: " + response.code());
                    Log.e("MainActivity", "Response body: " + response.errorBody());
                    Toast.makeText(MainActivity.this, "Erro ao carregar empresas: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Companies>> call, @NonNull Throwable t) {
                Log.e("MainActivity", "Error loading companies", t);
                Log.e("MainActivity", "Error message: " + t.getMessage());
                Toast.makeText(MainActivity.this, "Erro de conexão ao carregar empresas: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
