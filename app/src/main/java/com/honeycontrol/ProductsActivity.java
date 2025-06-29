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
import com.honeycontrol.adapters.ProductAdapter;
import com.honeycontrol.forms.ProductFormActivity;
import com.honeycontrol.models.Product;
import com.honeycontrol.models.User;
import com.honeycontrol.services.ApiCallback;
import com.honeycontrol.services.SupabaseApi;
import com.honeycontrol.services.SupabaseClient;
import com.honeycontrol.utils.SessionUtils;
import com.honeycontrol.utils.UserSession;

import java.util.ArrayList;
import java.util.List;

public class ProductsActivity extends BaseActivity implements ProductAdapter.OnProductClickListener {
    
    private static final String TAG = "ProductsActivity";
    
    private MaterialButton backToDashboardButton;
    private FloatingActionButton addProductFab;
    private RecyclerView productsRecyclerView;
    private LinearLayout emptyStateLayout;
    private ProgressBar loadingProgressBar;
    
    private ProductAdapter productAdapter;
    private final List<Product> products = new ArrayList<>();
    private SupabaseApi supabaseApi;
    private User currentUser;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Log.d(TAG, "ProductsActivity iniciada");
        
        // Verificar se o usuário está logado antes de continuar
        if (!SessionUtils.isUserLoggedIn()) {
            Log.w(TAG, "Usuário não está logado, redirecionando para login");
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }
        
        setContentView(R.layout.activity_products);
        
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
        addProductFab = findViewById(R.id.addProductFab);
        productsRecyclerView = findViewById(R.id.productsRecyclerView);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        loadingProgressBar = findViewById(R.id.loadingProgressBar);
    }
    
    private void setupRecyclerView() {
        productAdapter = new ProductAdapter(this, products);
        productAdapter.setOnProductClickListener(this);
        productsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        productsRecyclerView.setAdapter(productAdapter);
    }
    
    private void setupClickListeners() {
        backToDashboardButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, DashboardActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
        
        addProductFab.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProductFormActivity.class);
            startActivity(intent);
        });
    }
    
    private void loadCurrentUser() {
        if (UserSession.getInstance().isUserLoggedIn()) {
            currentUser = UserSession.getInstance().getCurrentUser();
            String companyId = SessionUtils.getCurrentUserCompanyId();

            if (companyId != null && !companyId.isEmpty()) {
                loadProducts();
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
                        loadProducts();
                    } else {
                        Log.e(TAG, "Company ID é nulo ou vazio após carregar das preferências!");
                        Toast.makeText(ProductsActivity.this, "Erro: Company ID não encontrado", Toast.LENGTH_SHORT).show();
                        showEmptyState();
                    }
                }

                @Override
                public void onLoadFailed(String error) {
                    Log.e(TAG, "Erro ao carregar usuário: " + error);
                    Toast.makeText(ProductsActivity.this, "Erro ao carregar dados do usuário", Toast.LENGTH_SHORT).show();
                    showEmptyState();
                }
            });
        }
    }
    
    private void loadProducts() {
        String companyId = SessionUtils.getCurrentUserCompanyId();
        Log.d(TAG, "Fazendo chamada à API para buscar produtos da empresa: " + companyId);
        showLoading(true);

        supabaseApi.getProductsWithStockByCompany(companyId).enqueue(new ApiCallback<List<Product>>() {
            @Override
            public void onSuccess(List<Product> productList, int statusCode) {
                Log.d(TAG, "Resposta da API recebida com status: " + statusCode);
                showLoading(false);

                if (productList != null && !productList.isEmpty()) {
                    Log.d(TAG, "Produtos carregados: " + productList.size());
                    products.clear();
                    products.addAll(productList);
                    productAdapter.notifyDataSetChanged();
                    showProductsList();

                    for (Product product : productList) {
                        Log.d(TAG, "Produto: " + product.getName() + " (ID: " + product.getId() + ") - Estoque: " + product.getStockQuantity());
                    }
                } else {
                    showEmptyState();
                }
            }
            
            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Falha ao carregar produtos: " + e.getMessage(), e);
                showLoading(false);
                Toast.makeText(ProductsActivity.this, "Erro ao carregar produtos: " + e.getMessage(), Toast.LENGTH_LONG).show();
                showEmptyState();
            }
        });
    }
    
    private void showLoading(boolean show) {
        loadingProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        productsRecyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
        emptyStateLayout.setVisibility(View.GONE);
    }
    
    private void showProductsList() {
        loadingProgressBar.setVisibility(View.GONE);
        productsRecyclerView.setVisibility(View.VISIBLE);
        emptyStateLayout.setVisibility(View.GONE);
    }
    
    private void showEmptyState() {
        loadingProgressBar.setVisibility(View.GONE);
        productsRecyclerView.setVisibility(View.GONE);
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

        Log.d(TAG, "Recarregando produtos no onResume");
        if (currentUser != null) {
            loadProducts();
        } else {
            Log.d(TAG, "currentUser é null, carregando usuário novamente");
            loadCurrentUser();
        }
    }

    @Override
    public void onProductClick(Product product) {
        Toast.makeText(this, "Produto: " + product.getName(), Toast.LENGTH_SHORT).show();
        // Abrir tela de edição ao clicar no produto
        Intent intent = new Intent(this, ProductFormActivity.class);
        intent.putExtra("edit_mode", true);
        intent.putExtra("product_id", product.getId());
        startActivity(intent);
    }
    
    @Override
    public void onProductEdit(Product product) {
        // Abrir tela de edição
        Intent intent = new Intent(this, ProductFormActivity.class);
        intent.putExtra("edit_mode", true);
        intent.putExtra("product_id", product.getId());
        startActivity(intent);
    }
    
    @Override
    public void onProductDelete(Product product) {
        Toast.makeText(this, "Função de excluir em desenvolvimento", Toast.LENGTH_SHORT).show();
    }
}
