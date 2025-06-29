package com.honeycontrol;

import android.content.Intent;
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
import com.honeycontrol.adapters.SaleAdapter;
import com.honeycontrol.forms.SaleFormActivity;
import com.honeycontrol.models.Sale;
import com.honeycontrol.models.User;
import com.honeycontrol.services.ApiCallback;
import com.honeycontrol.services.SupabaseApi;
import com.honeycontrol.services.SupabaseClient;
import com.honeycontrol.utils.SessionUtils;
import com.honeycontrol.utils.UserSession;

import java.util.ArrayList;
import java.util.List;

public class SalesActivity extends BaseActivity implements SaleAdapter.OnSaleClickListener {
    
    private static final String TAG = "SalesActivity";
    
    private MaterialButton backToDashboardButton;
    private FloatingActionButton addSaleFab;
    private RecyclerView salesRecyclerView;
    private LinearLayout emptyStateLayout;
    private ProgressBar loadingProgressBar;
    
    private SaleAdapter saleAdapter;
    private final List<Sale> sales = new ArrayList<>();
    private SupabaseApi supabaseApi;
    private User currentUser;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Log.d(TAG, "SalesActivity iniciada");
        
        // Verificar se o usuário está logado antes de continuar
        if (!SessionUtils.isUserLoggedIn()) {
            Log.w(TAG, "Usuário não está logado, redirecionando para login");
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }
        
        setContentView(R.layout.activity_sales);
        
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
        addSaleFab = findViewById(R.id.addSaleFab);
        salesRecyclerView = findViewById(R.id.salesRecyclerView);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        loadingProgressBar = findViewById(R.id.loadingProgressBar);
    }
    
    private void setupRecyclerView() {
        saleAdapter = new SaleAdapter(this, sales);
        saleAdapter.setOnSaleClickListener(this);
        salesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        salesRecyclerView.setAdapter(saleAdapter);
    }
    
    private void setupClickListeners() {
        backToDashboardButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, DashboardActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
        
        addSaleFab.setOnClickListener(v -> {
            Intent intent = new Intent(this, SaleFormActivity.class);
            startActivity(intent);
        });
    }
    
    private void loadCurrentUser() {
        if (UserSession.getInstance().isUserLoggedIn()) {
            currentUser = UserSession.getInstance().getCurrentUser();
            String companyId = SessionUtils.getCurrentUserCompanyId();

            if (companyId != null && !companyId.isEmpty()) {
                loadSales();
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
                        loadSales();
                    } else {
                        Log.e(TAG, "Company ID é nulo ou vazio após carregar das preferências!");
                        Toast.makeText(SalesActivity.this, "Erro: Company ID não encontrado", Toast.LENGTH_SHORT).show();
                        showEmptyState();
                    }
                }

                @Override
                public void onLoadFailed(String error) {
                    Log.e(TAG, "Erro ao carregar usuário: " + error);
                    Toast.makeText(SalesActivity.this, "Erro ao carregar dados do usuário", Toast.LENGTH_SHORT).show();
                    showEmptyState();
                }
            });
        }
    }
    
    private void loadSales() {
        String companyId = SessionUtils.getCurrentUserCompanyId();
        Log.d(TAG, "Fazendo chamada à API para buscar vendas da empresa: " + companyId);
        showLoading(true);

        supabaseApi.getSalesByCompany(companyId).enqueue(new ApiCallback<List<Sale>>() {
            @Override
            public void onSuccess(List<Sale> saleList, int statusCode) {
                Log.d(TAG, "Resposta da API recebida com status: " + statusCode);
                showLoading(false);

                if (saleList != null && !saleList.isEmpty()) {
                    Log.d(TAG, "Vendas carregadas: " + saleList.size());
                    sales.clear();
                    sales.addAll(saleList);
                    saleAdapter.notifyDataSetChanged();
                    showSalesList();

                    for (Sale sale : saleList) {
                        Log.d(TAG, "Venda: " + sale.getId() + " - Total: R$ " + sale.getTotal());
                    }
                } else {
                    showEmptyState();
                }
            }
            
            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Falha ao carregar vendas: " + e.getMessage(), e);
                showLoading(false);
                Toast.makeText(SalesActivity.this, "Erro ao carregar vendas: " + e.getMessage(), Toast.LENGTH_LONG).show();
                showEmptyState();
            }
        });
    }
    
    private void showLoading(boolean show) {
        loadingProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        salesRecyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
        emptyStateLayout.setVisibility(View.GONE);
    }
    
    private void showSalesList() {
        loadingProgressBar.setVisibility(View.GONE);
        salesRecyclerView.setVisibility(View.VISIBLE);
        emptyStateLayout.setVisibility(View.GONE);
    }
    
    private void showEmptyState() {
        loadingProgressBar.setVisibility(View.GONE);
        salesRecyclerView.setVisibility(View.GONE);
        emptyStateLayout.setVisibility(View.VISIBLE);
    }
    
    @Override
    protected void onResume() {
        super.onResume();

        if (!SessionUtils.isUserLoggedIn()) {
            Log.w(TAG, "Usuário não está mais logado, redirecionando");
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        Log.d(TAG, "Recarregando vendas no onResume");
        if (currentUser != null) {
            loadSales();
        } else {
            Log.d(TAG, "currentUser é null, carregando usuário novamente");
            loadCurrentUser();
        }
    }
    
    @Override
    public void onSaleClick(Sale sale) {
        Toast.makeText(this, "Venda: R$ " + sale.getTotal(), Toast.LENGTH_SHORT).show();
        // TODO: Implementar visualização detalhada da venda
    }
    
    @Override
    public void onSaleEdit(Sale sale) {
        // TODO: Implementar edição de venda
        Toast.makeText(this, "Função de editar venda em desenvolvimento", Toast.LENGTH_SHORT).show();
    }
    
    @Override
    public void onSaleDelete(Sale sale) {
        Toast.makeText(this, "Função de excluir venda em desenvolvimento", Toast.LENGTH_SHORT).show();
    }
}
