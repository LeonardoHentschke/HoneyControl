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
import com.honeycontrol.adapters.CostAdapter;
import com.honeycontrol.models.Cost;
import com.honeycontrol.models.User;
import com.honeycontrol.utils.SessionUtils;

import java.util.ArrayList;
import java.util.List;

public class CostsActivity extends BaseActivity implements CostAdapter.OnCostClickListener {
    
    private static final String TAG = "CostsActivity";
    
    private MaterialButton backToDashboardButton;
    private FloatingActionButton addCostFab;
    private RecyclerView costsRecyclerView;
    private LinearLayout emptyStateLayout;
    private ProgressBar loadingProgressBar;
    
    private CostAdapter costAdapter;
    private final List<Cost> costs = new ArrayList<>();
    private SupabaseApi supabaseApi;
    private User currentUser;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Log.d(TAG, "CostsActivity iniciada");
        
        // Verificar se o usuário está logado antes de continuar
        if (!SessionUtils.isUserLoggedIn()) {
            Log.w(TAG, "Usuário não está logado, redirecionando para login");
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }
        
        setContentView(R.layout.activity_costs);
        
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
        addCostFab = findViewById(R.id.addCostFab);
        costsRecyclerView = findViewById(R.id.costsRecyclerView);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        loadingProgressBar = findViewById(R.id.loadingProgressBar);
    }
    
    private void setupRecyclerView() {
        costAdapter = new CostAdapter(this, costs);
        costAdapter.setOnCostClickListener(this);
        costsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        costsRecyclerView.setAdapter(costAdapter);
    }
    
    private void setupClickListeners() {
        backToDashboardButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, DashboardActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
        
        addCostFab.setOnClickListener(v -> {
            Intent intent = new Intent(this, CostFormActivity.class);
            startActivity(intent);
        });
    }
    
    private void loadCurrentUser() {
        if (UserSession.getInstance().isUserLoggedIn()) {
            currentUser = UserSession.getInstance().getCurrentUser();
            String companyId = SessionUtils.getCurrentUserCompanyId();

            if (companyId != null && !companyId.isEmpty()) {
                loadCosts();
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
                        loadCosts();
                    } else {
                        Log.e(TAG, "Company ID é nulo ou vazio após carregar das preferências!");
                        Toast.makeText(CostsActivity.this, "Erro: Company ID não encontrado", Toast.LENGTH_SHORT).show();
                        showEmptyState();
                    }
                }

                @Override
                public void onLoadFailed(String error) {
                    Log.e(TAG, "Erro ao carregar usuário: " + error);
                    Toast.makeText(CostsActivity.this, "Erro ao carregar dados do usuário", Toast.LENGTH_SHORT).show();
                    showEmptyState();
                }
            });
        }
    }
    
    private void loadCosts() {
        String companyId = SessionUtils.getCurrentUserCompanyId();
        Log.d(TAG, "Fazendo chamada à API para buscar custos da empresa: " + companyId);
        showLoading(true);

        supabaseApi.getCostsByCompany(companyId).enqueue(new ApiCallback<List<Cost>>() {
            @Override
            public void onSuccess(List<Cost> costList, int statusCode) {
                Log.d(TAG, "Resposta da API recebida com status: " + statusCode);
                showLoading(false);

                if (costList != null && !costList.isEmpty()) {
                    Log.d(TAG, "Custos carregados: " + costList.size());
                    costs.clear();
                    costs.addAll(costList);
                    costAdapter.notifyDataSetChanged();
                    showCostsList();

                    for (Cost cost : costList) {
                        Log.d(TAG, "Custo: " + cost.getName() + " (ID: " + cost.getId() + ")");
                    }
                } else {
                    showEmptyState();
                }
            }
            
            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Falha ao carregar custos: " + e.getMessage(), e);
                showLoading(false);
                Toast.makeText(CostsActivity.this, "Erro ao carregar custos: " + e.getMessage(), Toast.LENGTH_LONG).show();
                showEmptyState();
            }
        });
    }
    
    private void showLoading(boolean show) {
        loadingProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        costsRecyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
        emptyStateLayout.setVisibility(View.GONE);
    }
    
    private void showCostsList() {
        loadingProgressBar.setVisibility(View.GONE);
        costsRecyclerView.setVisibility(View.VISIBLE);
        emptyStateLayout.setVisibility(View.GONE);
    }
    
    private void showEmptyState() {
        loadingProgressBar.setVisibility(View.GONE);
        costsRecyclerView.setVisibility(View.GONE);
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

        Log.d(TAG, "Recarregando custos no onResume");
        if (currentUser != null) {
            loadCosts();
        } else {
            Log.d(TAG, "currentUser é null, carregando usuário novamente");
            loadCurrentUser();
        }
    }

    @Override
    public void onCostClick(Cost cost) {
        Toast.makeText(this, "Custo: " + cost.getName(), Toast.LENGTH_SHORT).show();
        // Abrir tela de edição ao clicar no custo
        Intent intent = new Intent(this, CostFormActivity.class);
        intent.putExtra("edit_mode", true);
        intent.putExtra("cost_id", cost.getId());
        startActivity(intent);
    }
    
    @Override
    public void onCostEdit(Cost cost) {
        // Abrir tela de edição
        Intent intent = new Intent(this, CostFormActivity.class);
        intent.putExtra("edit_mode", true);
        intent.putExtra("cost_id", cost.getId());
        startActivity(intent);
    }
    
    @Override
    public void onCostDelete(Cost cost) {
        Toast.makeText(this, "Função de excluir em desenvolvimento", Toast.LENGTH_SHORT).show();
    }
}
