package com.honeycontrol;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.honeycontrol.adapters.CustomerAdapter;
import com.honeycontrol.models.Customer;
import com.honeycontrol.models.User;

import java.util.ArrayList;
import java.util.List;

public class CustomersActivity extends BaseActivity implements CustomerAdapter.OnCustomerClickListener {
    
    private static final String TAG = "CustomersActivity";
    private static final String PREFS_NAME = "HoneyControlPrefs";
    
    private MaterialButton backToDashboardButton;
    private FloatingActionButton addCustomerFab;
    private RecyclerView customersRecyclerView;
    private LinearLayout emptyStateLayout;
    private ProgressBar loadingProgressBar;
    
    private CustomerAdapter customerAdapter;
    private List<Customer> customers = new ArrayList<>();
    private SupabaseApi supabaseApi;
    private User currentUser;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customers);
        
        initViews();
        setupRecyclerView();
        setupClickListeners();
        
        // Inicializa API
        supabaseApi = SupabaseClient.createService(SupabaseApi.class);
        
        loadCurrentUser();
    }
    
    private void initViews() {
        backToDashboardButton = findViewById(R.id.backToDashboardButton);
        addCustomerFab = findViewById(R.id.addCustomerFab);
        customersRecyclerView = findViewById(R.id.customersRecyclerView);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        loadingProgressBar = findViewById(R.id.loadingProgressBar);
    }
    
    private void setupRecyclerView() {
        customerAdapter = new CustomerAdapter(this, customers);
        customerAdapter.setOnCustomerClickListener(this);
        customersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        customersRecyclerView.setAdapter(customerAdapter);
    }
    
    private void setupClickListeners() {
        backToDashboardButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, DashboardActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
        
        addCustomerFab.setOnClickListener(v -> {
            Intent intent = new Intent(this, CustomerFormActivity.class);
            startActivity(intent);
        });
    }
    
    private void loadCurrentUser() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String userEmail = prefs.getString("user_email", "");
        
        if (!userEmail.isEmpty()) {
            supabaseApi.getUserByEmail(userEmail).enqueue(new ApiCallback<User>() {
                @Override
                public void onSuccess(User user, int statusCode) {
                    currentUser = user;
                    Log.d(TAG, "Usuário carregado: " + user.getName() + ", Company ID: " + user.getCompanyId());
                    loadCustomers();
                }
                
                @Override
                public void onFailure(Exception e) {
                    Log.e(TAG, "Erro ao carregar usuário: " + e.getMessage());
                    Toast.makeText(CustomersActivity.this, "Erro ao carregar dados do usuário", Toast.LENGTH_SHORT).show();
                    showEmptyState();
                }
            });
        } else {
            Log.e(TAG, "Email do usuário não encontrado nas preferências");
            showEmptyState();
        }
    }
    
    private void loadCustomers() {
        if (currentUser == null) {
            Log.e(TAG, "Usuário atual é nulo");
            showEmptyState();
            return;
        }
        
        showLoading(true);
        
        // Converter company_id de string para long
        long companyId;
        try {
            companyId = Long.parseLong(currentUser.getCompanyId());
        } catch (NumberFormatException e) {
            Log.e(TAG, "Erro ao converter company_id: " + e.getMessage());
            showEmptyState();
            return;
        }
        
        supabaseApi.getCustomersByCompany(companyId).enqueue(new ApiCallback<List<Customer>>() {
            @Override
            public void onSuccess(List<Customer> customerList, int statusCode) {
                showLoading(false);
                
                if (customerList != null && !customerList.isEmpty()) {
                    customers.clear();
                    customers.addAll(customerList);
                    customerAdapter.notifyDataSetChanged();
                    showCustomersList();
                    Log.d(TAG, "Clientes carregados: " + customerList.size());
                } else {
                    showEmptyState();
                    Log.d(TAG, "Nenhum cliente encontrado");
                }
            }
            
            @Override
            public void onFailure(Exception e) {
                showLoading(false);
                Log.e(TAG, "Erro ao carregar clientes: " + e.getMessage());
                Toast.makeText(CustomersActivity.this, "Erro ao carregar clientes", Toast.LENGTH_SHORT).show();
                showEmptyState();
            }
        });
    }
    
    private void showLoading(boolean show) {
        loadingProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        customersRecyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
        emptyStateLayout.setVisibility(View.GONE);
    }
    
    private void showCustomersList() {
        loadingProgressBar.setVisibility(View.GONE);
        customersRecyclerView.setVisibility(View.VISIBLE);
        emptyStateLayout.setVisibility(View.GONE);
    }
    
    private void showEmptyState() {
        loadingProgressBar.setVisibility(View.GONE);
        customersRecyclerView.setVisibility(View.GONE);
        emptyStateLayout.setVisibility(View.VISIBLE);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Recarregar clientes quando voltar de outras telas
        if (currentUser != null) {
            loadCustomers();
        }
    }
    
    // Implementação da interface CustomerAdapter.OnCustomerClickListener
    @Override
    public void onCustomerClick(Customer customer) {
        // Mostrar detalhes ou ações do cliente
        Toast.makeText(this, "Cliente: " + customer.getName(), Toast.LENGTH_SHORT).show();
    }
    
    @Override
    public void onCustomerEdit(Customer customer) {
        // Abrir tela de edição
        Intent intent = new Intent(this, CustomerFormActivity.class);
        intent.putExtra("edit_mode", true);
        intent.putExtra("customer_id", Long.parseLong(customer.getId()));
        startActivity(intent);
    }
    
    @Override
    public void onCustomerDelete(Customer customer) {
        // Implementar confirmação de exclusão
        // Por enquanto, apenas mostra uma mensagem
        Toast.makeText(this, "Função de excluir em desenvolvimento", Toast.LENGTH_SHORT).show();
    }
}
