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
import com.honeycontrol.utils.SessionUtils;

import java.util.ArrayList;
import java.util.List;

public class CustomersActivity extends BaseActivity implements CustomerAdapter.OnCustomerClickListener {
    
    private static final String TAG = "CustomersActivity";
    
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
        // Usar a sessão global para obter o usuário
        if (UserSession.getInstance().isUserLoggedIn()) {
            currentUser = UserSession.getInstance().getCurrentUser();
            Log.d(TAG, "Usuário carregado da sessão: " + SessionUtils.getCurrentUserName() + ", Company ID: " + SessionUtils.getCurrentUserCompanyId());
            loadCustomers();
        } else {
            // Se não estiver na sessão, tentar carregar das preferências
            UserSession.getInstance().loadUserFromPreferences(this, new UserSession.UserLoadCallback() {
                @Override
                public void onUserLoaded(User user) {
                    currentUser = user;
                    Log.d(TAG, "Usuário carregado das preferências: " + user.getName() + ", Company ID: " + user.getCompanyId());
                    loadCustomers();
                }
                
                @Override
                public void onLoadFailed(String error) {
                    Log.e(TAG, "Erro ao carregar usuário: " + error);
                    Toast.makeText(CustomersActivity.this, "Erro ao carregar dados do usuário", Toast.LENGTH_SHORT).show();
                    showEmptyState();
                }
            });
        }
    }
    
    private void loadCustomers() {
        // Usar a função utilitária para obter o company_id de forma segura
        long companyId = SessionUtils.getCurrentUserCompanyIdAsLong();
        if (companyId == -1) {
            Log.e(TAG, "Company ID inválido ou usuário não logado");
            showEmptyState();
            return;
        }
        
        showLoading(true);
        
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
        if (SessionUtils.isUserLoggedIn()) {
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
