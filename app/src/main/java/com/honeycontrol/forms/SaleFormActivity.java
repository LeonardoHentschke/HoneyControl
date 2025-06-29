package com.honeycontrol.forms;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.honeycontrol.services.ApiCallback;
import com.honeycontrol.BaseActivity;
import com.honeycontrol.R;
import com.honeycontrol.services.SupabaseApi;
import com.honeycontrol.services.SupabaseClient;
import com.honeycontrol.utils.UserSession;
import com.honeycontrol.models.Sale;
import com.honeycontrol.models.Customer;
import com.honeycontrol.models.Product;
import com.honeycontrol.models.User;
import com.honeycontrol.utils.SessionUtils;

import java.util.ArrayList;
import java.util.List;

public class SaleFormActivity extends BaseActivity {
    
    private static final String TAG = "SaleFormActivity";
    
    private TextInputLayout customerInputLayout;
    private AutoCompleteTextView customerAutoComplete;
    private RecyclerView saleItemsRecyclerView;
    private MaterialButton addItemButton;
    private MaterialButton backButton;
    private MaterialButton saveButton;
    private ProgressBar loadingProgressBar;
    
    private SupabaseApi supabaseApi;
    private Sale editingSale;
    private boolean isEditMode = false;
    private String saleId = "";
    
    private List<Customer> customers = new ArrayList<>();
    private List<Product> products = new ArrayList<>();
    private ArrayAdapter<String> customerAdapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Log.d(TAG, "SaleFormActivity iniciada");
        
        // Verificar se o usuário está logado
        if (!SessionUtils.isUserLoggedIn()) {
            Log.w(TAG, "Usuário não está logado, redirecionando para login");
            finish();
            return;
        }
        
        setContentView(R.layout.activity_sale_form);
        
        // Verificar se é modo de edição
        if (getIntent().hasExtra("edit_mode")) {
            isEditMode = getIntent().getBooleanExtra("edit_mode", false);
            saleId = getIntent().getStringExtra("sale_id");
            Log.d(TAG, "Modo de edição: " + isEditMode + ", Sale ID: " + saleId);
        }
        
        initViews();
        setupClickListeners();
        
        // Inicializa API
        supabaseApi = SupabaseClient.createService(SupabaseApi.class);
        if (supabaseApi == null) {
            Log.e(TAG, "Falha ao criar instância da API");
            Toast.makeText(this, "Erro de configuração da API", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
        setupCustomerDropdown();
        loadCustomers();
        loadProducts();
        
        if (isEditMode && saleId != null && !saleId.isEmpty()) {
            loadSaleForEdit();
        }
    }
    
    private void initViews() {
        customerInputLayout = findViewById(R.id.customerInputLayout);
        customerAutoComplete = findViewById(R.id.customerAutoComplete);
        saleItemsRecyclerView = findViewById(R.id.saleItemsRecyclerView);
        addItemButton = findViewById(R.id.addItemButton);
        backButton = findViewById(R.id.backButton);
        saveButton = findViewById(R.id.saveButton);
        loadingProgressBar = findViewById(R.id.loadingProgressBar);
        
        // Setup RecyclerView
        saleItemsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        // Atualizar textos baseado no modo
        if (isEditMode) {
            saveButton.setText("Atualizar venda");
        } else {
            saveButton.setText("Salvar venda");
        }
    }
    
    private void setupClickListeners() {
        backButton.setOnClickListener(v -> finish());
        
        saveButton.setOnClickListener(v -> {
            if (validateForm()) {
                if (isEditMode) {
                    updateSale();
                } else {
                    createSale();
                }
            }
        });
        
        addItemButton.setOnClickListener(v -> {
            // TODO: Implementar adição de itens à venda
            Toast.makeText(this, "Funcionalidade de adicionar itens em desenvolvimento", Toast.LENGTH_SHORT).show();
        });
    }
    
    private void setupCustomerDropdown() {
        List<String> customerNames = new ArrayList<>();
        customerAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_dropdown_item_1line, customerNames);
        customerAutoComplete.setAdapter(customerAdapter);
    }
    
    private void loadCustomers() {
        String companyId = SessionUtils.getCurrentUserCompanyId();
        Log.d(TAG, "Carregando clientes da empresa: " + companyId);
        
        supabaseApi.getCustomersByCompany(companyId).enqueue(new ApiCallback<List<Customer>>() {
            @Override
            public void onSuccess(List<Customer> customerList, int statusCode) {
                if (customerList != null) {
                    customers.clear();
                    customers.addAll(customerList);
                    
                    List<String> customerNames = new ArrayList<>();
                    for (Customer customer : customers) {
                        customerNames.add(customer.getName());
                    }
                    
                    customerAdapter.clear();
                    customerAdapter.addAll(customerNames);
                    customerAdapter.notifyDataSetChanged();
                    
                    Log.d(TAG, "Clientes carregados: " + customers.size());
                }
            }
            
            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Erro ao carregar clientes: " + e.getMessage());
                Toast.makeText(SaleFormActivity.this, "Erro ao carregar clientes", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void loadProducts() {
        String companyId = SessionUtils.getCurrentUserCompanyId();
        Log.d(TAG, "Carregando produtos da empresa: " + companyId);
        
        supabaseApi.getProductsWithStockByCompany(companyId).enqueue(new ApiCallback<List<Product>>() {
            @Override
            public void onSuccess(List<Product> productList, int statusCode) {
                if (productList != null) {
                    products.clear();
                    products.addAll(productList);
                    Log.d(TAG, "Produtos carregados: " + products.size());
                }
            }
            
            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Erro ao carregar produtos: " + e.getMessage());
                Toast.makeText(SaleFormActivity.this, "Erro ao carregar produtos", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void loadSaleForEdit() {
        showLoading(true);
        supabaseApi.getSaleById(saleId).enqueue(new ApiCallback<Sale>() {
            @Override
            public void onSuccess(Sale sale, int statusCode) {
                showLoading(false);
                if (sale != null) {
                    editingSale = sale;
                    populateFormWithSale(sale);
                }
            }
            
            @Override
            public void onFailure(Exception e) {
                showLoading(false);
                Log.e(TAG, "Erro ao carregar venda: " + e.getMessage());
                Toast.makeText(SaleFormActivity.this, "Erro ao carregar venda", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
    
    private void populateFormWithSale(Sale sale) {
        // TODO: Implementar preenchimento do formulário com dados da venda
        Log.d(TAG, "Populando formulário com venda: " + sale.getId());
    }
    
    private boolean validateForm() {
        boolean isValid = true;
        
        // Validar cliente selecionado
        String customerName = customerAutoComplete.getText().toString().trim();
        if (customerName.isEmpty()) {
            customerInputLayout.setError("Selecione um cliente");
            isValid = false;
        } else {
            customerInputLayout.setError(null);
        }
        
        // TODO: Validar itens da venda
        
        return isValid;
    }
    
    private void createSale() {
        // TODO: Implementar criação de venda
        Toast.makeText(this, "Funcionalidade de criar venda em desenvolvimento", Toast.LENGTH_SHORT).show();
    }
    
    private void updateSale() {
        // TODO: Implementar atualização de venda
        Toast.makeText(this, "Funcionalidade de atualizar venda em desenvolvimento", Toast.LENGTH_SHORT).show();
    }
    
    private void showLoading(boolean show) {
        loadingProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        saveButton.setEnabled(!show);
    }
}
