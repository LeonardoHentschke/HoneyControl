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
import com.honeycontrol.requests.CustomerCreateRequest;
import com.honeycontrol.utils.SessionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
        
        Log.d(TAG, "CustomersActivity iniciada");
        
        // Verificar se o usuário está logado antes de continuar
        if (!SessionUtils.isUserLoggedIn()) {
            Log.w(TAG, "Usuário não está logado, redirecionando para login");
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }
        
        setContentView(R.layout.activity_customers);
        
        initViews();
        setupRecyclerView();
        setupClickListeners();
        
        // Inicializa API
        supabaseApi = SupabaseClient.createService(SupabaseApi.class);
        if (supabaseApi == null) {
            Log.e(TAG, "Falha ao criar instância da API");
            Toast.makeText(this, "Erro de configuração da API", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
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
        if (UserSession.getInstance().isUserLoggedIn()) {
            currentUser = UserSession.getInstance().getCurrentUser();
            String companyId = SessionUtils.getCurrentUserCompanyId();

            if (companyId != null && !companyId.isEmpty()) {
                loadCustomers();
            } else {
                Toast.makeText(this, "Erro: Company ID não encontrado", Toast.LENGTH_SHORT).show();
                showEmptyState();
            }
        } else {
            UserSession.getInstance().loadUserFromPreferences(this, new UserSession.UserLoadCallback() {
                @Override
                public void onUserLoaded(User user) {
                    currentUser = user;
                    if (user.getCompanyId() != null && !user.getCompanyId().isEmpty()) {
                        loadCustomers();
                    } else {
                        Log.e(TAG, "Company ID é nulo ou vazio após carregar das preferências!");
                        Toast.makeText(CustomersActivity.this, "Erro: Company ID não encontrado", Toast.LENGTH_SHORT).show();
                        showEmptyState();
                    }
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
        String companyId = SessionUtils.getCurrentUserCompanyIdAsLong();
        Log.d(TAG, "Company ID convertido para long: " + companyId);
        
        if (Objects.equals(companyId, "")) {
            Log.e(TAG, "Company ID inválido ou usuário não logado");
            String rawCompanyId = SessionUtils.getCurrentUserCompanyId();
            Log.e(TAG, "Company ID bruto: '" + rawCompanyId + "'");
            showEmptyState();
            return;
        }
        
        Log.d(TAG, "Fazendo chamada à API para buscar clientes da empresa: " + companyId);
        showLoading(true);
        
        supabaseApi.getCustomersByCompany(companyId).enqueue(new ApiCallback<List<Customer>>() {
            @Override
            public void onSuccess(List<Customer> customerList, int statusCode) {
                Log.d(TAG, "Resposta da API recebida com status: " + statusCode);
                showLoading(false);
                
                if (customerList != null && !customerList.isEmpty()) {
                    Log.d(TAG, "Clientes carregados: " + customerList.size());
                    customers.clear();
                    customers.addAll(customerList);
                    customerAdapter.notifyDataSetChanged();
                    showCustomersList();
                    
                    // Log dos nomes dos clientes para debug
                    for (Customer customer : customerList) {
                        Log.d(TAG, "Cliente: " + customer.getName() + " (ID: " + customer.getId() + ")");
                    }
                } else {
                    Log.d(TAG, "Nenhum cliente encontrado na resposta");
                    if (customerList == null) {
                        Log.d(TAG, "A lista de clientes retornada é null");
                    } else {
                        Log.d(TAG, "A lista de clientes retornada está vazia");
                    }
                    showEmptyState();
                }
            }
            
            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Falha ao carregar clientes: " + e.getMessage(), e);
                showLoading(false);
                Toast.makeText(CustomersActivity.this, "Erro ao carregar clientes: " + e.getMessage(), Toast.LENGTH_LONG).show();
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
        Log.d(TAG, "onResume() chamado");
        
        // Verificar novamente se o usuário está logado
        if (!SessionUtils.isUserLoggedIn()) {
            Log.w(TAG, "Usuário não está mais logado, redirecionando");
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }
        
        // Recarregar clientes quando voltar de outras telas
        Log.d(TAG, "Recarregando clientes no onResume");
        if (currentUser != null) {
            loadCustomers();
        } else {
            Log.d(TAG, "currentUser é null, carregando usuário novamente");
            loadCurrentUser();
        }    }
    
    // Método de debug para testar carregamento de clientes
    private void testCustomerLoading() {
        Log.d(TAG, "=== INICIANDO TESTE DE CARREGAMENTO ===");
        
        // 1. Verificar sessão
        Log.d(TAG, "1. Verificando sessão do usuário...");
        Log.d(TAG, "   isUserLoggedIn(): " + SessionUtils.isUserLoggedIn());
        
        User user = SessionUtils.getCurrentUser();
        if (user != null) {
            Log.d(TAG, "   Usuário: " + user.getName());
            Log.d(TAG, "   Email: " + user.getEmail());
            Log.d(TAG, "   Company ID: " + user.getCompanyId());
        } else {
            Log.e(TAG, "   Usuário é NULL!");
        }
        
        // 2. Verificar company ID
        Log.d(TAG, "2. Verificando Company ID...");
        String companyId = SessionUtils.getCurrentUserCompanyId();
        Log.d(TAG, "   Company ID (String): '" + companyId + "'");

        String companyIdLong = SessionUtils.getCurrentUserCompanyIdAsLong();
        Log.d(TAG, "   Company ID (Long): " + companyIdLong);
        
        // 3. Verificar API
        Log.d(TAG, "3. Verificando instância da API...");
        Log.d(TAG, "   SupabaseApi é null: " + (supabaseApi == null));
        
        // 4. Tentar carregar clientes apenas se tudo estiver OK
        if (companyIdLong != "" && supabaseApi != null) {
            Log.d(TAG, "4. Tentando carregar clientes...");
            loadCustomers();
        } else {
            Log.e(TAG, "4. ERRO: Condições não atendidas para carregar clientes");
            if (companyIdLong == "") {
                Log.e(TAG, "   - Company ID inválido");
            }
            if (supabaseApi == null) {
                Log.e(TAG, "   - API é null");
            }
        }
        
        Log.d(TAG, "=== FIM DO TESTE DE CARREGAMENTO ===");
    }
    
    // Método de debug para adicionar clientes de teste
    private void addTestCustomers() {
        Log.d(TAG, "Adicionando clientes de teste para debug");
        
        // Verificar se temos uma sessão válida
        if (!SessionUtils.isUserLoggedIn()) {
            Log.e(TAG, "Usuário não está logado para adicionar clientes de teste");
            Toast.makeText(this, "Erro: usuário não logado", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String companyId = SessionUtils.getCurrentUserCompanyId();
        if (companyId == null || companyId.isEmpty()) {
            Log.e(TAG, "Company ID é nulo para adicionar clientes de teste");
            Toast.makeText(this, "Erro: Company ID não encontrado", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Log.d(TAG, "Criando cliente de teste para company ID: " + companyId);
        
        // Criar um cliente de teste
        CustomerCreateRequest testCustomer = new CustomerCreateRequest(
            "Cliente Teste Debug",
            "teste@debug.com",
            "(11) 99999-9999",
            "123.456.789-00",
            "Rua de Teste, 123",
            "São Paulo",
            companyId
        );
        
        supabaseApi.createCustomer(testCustomer).enqueue(new ApiCallback<Customer>() {
            @Override
            public void onSuccess(Customer customer, int statusCode) {
                Log.d(TAG, "Cliente de teste criado com sucesso: " + customer.getName());
                Toast.makeText(CustomersActivity.this, "Cliente de teste criado!", Toast.LENGTH_SHORT).show();
                // Recarregar a lista
                loadCustomers();
            }
            
            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Erro ao criar cliente de teste: " + e.getMessage(), e);
                Toast.makeText(CustomersActivity.this, "Erro ao criar cliente de teste: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
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
