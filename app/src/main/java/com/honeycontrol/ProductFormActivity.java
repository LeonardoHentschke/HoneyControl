package com.honeycontrol;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.honeycontrol.models.Product;
import com.honeycontrol.models.User;
import com.honeycontrol.requests.ProductCreateRequest;
import com.honeycontrol.utils.SessionUtils;

import java.util.Objects;

public class ProductFormActivity extends BaseActivity {
    
    private static final String TAG = "ProductFormActivity";
    
    private TextInputLayout nameInputLayout;
    private TextInputEditText nameEditText;
    private TextInputLayout descriptionInputLayout;
    private TextInputEditText descriptionEditText;
    private TextInputLayout unitPriceInputLayout;
    private TextInputEditText unitPriceEditText;
    private TextInputLayout unitInputLayout;
    private AutoCompleteTextView unitAutoComplete;
    private MaterialButton backButton;
    private MaterialButton saveButton;
    private ProgressBar loadingProgressBar;
    
    private SupabaseApi supabaseApi;
    private Product editingProduct;
    private boolean isEditMode = false;
    private String productId = "";
    
    // Units for the dropdown
    private final String[] units = {
        "unidade", "kg", "g", "litro", "ml", "metro", "cm", "mm",
        "caixa", "pacote", "dúzia", "par", "m²", "m³"
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_form);
        
        // Verificar se é modo de edição
        isEditMode = getIntent().getBooleanExtra("edit_mode", false);
        productId = getIntent().getStringExtra("product_id");
        
        initViews();
        setupClickListeners();
        setupUnitDropdown();
        loadCurrentUser();
        
        if (isEditMode && !Objects.equals(productId, "")) {
            loadProductForEdit();
        }
    }
    
    private void initViews() {
        nameInputLayout = findViewById(R.id.nameInputLayout);
        nameEditText = findViewById(R.id.nameEditText);
        descriptionInputLayout = findViewById(R.id.descriptionInputLayout);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        unitPriceInputLayout = findViewById(R.id.unitPriceInputLayout);
        unitPriceEditText = findViewById(R.id.unitPriceEditText);
        unitInputLayout = findViewById(R.id.unitInputLayout);
        unitAutoComplete = findViewById(R.id.unitAutoComplete);
        
        backButton = findViewById(R.id.backButton);
        saveButton = findViewById(R.id.saveButton);
        loadingProgressBar = findViewById(R.id.loadingProgressBar);
        
        // Inicializa API
        supabaseApi = SupabaseClient.createService(SupabaseApi.class);
        
        // Atualizar textos baseado no modo
        if (isEditMode) {
            saveButton.setText("Atualizar produto");
        } else {
            saveButton.setText("Salvar produto");
        }
    }
    
    private void setupClickListeners() {
        backButton.setOnClickListener(v -> finish());
        
        saveButton.setOnClickListener(v -> {
            if (validateInputs()) {
                if (isEditMode) {
                    updateProduct();
                } else {
                    createProduct();
                }
            }
        });
    }
    
    private void setupUnitDropdown() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_dropdown_item_1line, units);
        unitAutoComplete.setAdapter(adapter);
    }
    
    private void loadCurrentUser() {
        // Usar a sessão global para obter o usuário
        if (SessionUtils.isUserLoggedIn()) {
            Log.d(TAG, "Usuário carregado da sessão: " + SessionUtils.getCurrentUserName());
        } else {
            // Se não estiver na sessão, tentar carregar das preferências
            UserSession.getInstance().loadUserFromPreferences(this, new UserSession.UserLoadCallback() {
                @Override
                public void onUserLoaded(User user) {
                    Log.d(TAG, "Usuário carregado das preferências: " + user.getName());
                }
                
                @Override
                public void onLoadFailed(String error) {
                    Log.e(TAG, "Erro ao carregar usuário: " + error);
                    Toast.makeText(ProductFormActivity.this, "Erro ao carregar dados do usuário", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    
    private void loadProductForEdit() {
        if (productId == null || productId.isEmpty()) {
            Log.e(TAG, "Product ID é nulo ou vazio");
            Toast.makeText(this, "Erro: ID do produto não encontrado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        showLoading(true);
        
        supabaseApi.getProductById(productId).enqueue(new ApiCallback<Product>() {
            @Override
            public void onSuccess(Product product, int statusCode) {
                showLoading(false);
                if (product != null) {
                    editingProduct = product;
                    populateFormWithProductData(product);
                    Log.d(TAG, "Dados do produto carregados: " + product.getName());
                } else {
                    Log.e(TAG, "Produto não encontrado");
                    Toast.makeText(ProductFormActivity.this, "Produto não encontrado", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
            
            @Override
            public void onFailure(Exception e) {
                showLoading(false);
                Log.e(TAG, "Erro ao carregar produto: " + e.getMessage());
                Toast.makeText(ProductFormActivity.this, "Erro ao carregar dados do produto", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
    
    private void populateFormWithProductData(Product product) {
        nameEditText.setText(product.getName());
        descriptionEditText.setText(product.getDescription() != null ? product.getDescription() : "");
        unitPriceEditText.setText(product.getUnit_price() != null ? String.valueOf(product.getUnit_price()) : "");
        unitAutoComplete.setText(product.getUnit() != null ? product.getUnit() : "");
    }

    private boolean validateInputs() {
        boolean isValid = true;
        
        // Reset error states
        nameInputLayout.setError(null);
        unitPriceInputLayout.setError(null);
        unitInputLayout.setError(null);
        
        // Validate name
        String name = nameEditText.getText().toString().trim();
        if (name.isEmpty()) {
            nameInputLayout.setError("Nome é obrigatório");
            isValid = false;
        }
        
        // Validate unit price
        String unitPriceStr = unitPriceEditText.getText().toString().trim();
        if (unitPriceStr.isEmpty()) {
            unitPriceInputLayout.setError("Preço unitário é obrigatório");
            isValid = false;
        } else {
            try {
                float unitPrice = Float.parseFloat(unitPriceStr);
                if (unitPrice < 0) {
                    unitPriceInputLayout.setError("Preço deve ser maior ou igual a zero");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                unitPriceInputLayout.setError("Preço inválido");
                isValid = false;
            }
        }
        
        // Validate unit
        String unit = unitAutoComplete.getText().toString().trim();
        if (unit.isEmpty()) {
            unitInputLayout.setError("Unidade é obrigatória");
            isValid = false;
        }
        
        // Limpar erros dos campos opcionais
        descriptionInputLayout.setError(null);
        
        return isValid;
    }

    private void createProduct() {
        // Usar a função utilitária para verificar se o usuário está logado
        if (!SessionUtils.isUserLoggedIn()) {
            Toast.makeText(this, "Erro: usuário não carregado", Toast.LENGTH_SHORT).show();
            return;
        }
        
        showLoading(true);
        
        // Capturar dados dos campos
        String name = nameEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        String unit = unitAutoComplete.getText().toString().trim();
        float unitPrice = Float.parseFloat(unitPriceEditText.getText().toString().trim());
        String companyId = SessionUtils.getCurrentUserCompanyId();

        if (description.isEmpty()) description = null;

        ProductCreateRequest request = new ProductCreateRequest(
            name, description, unitPrice, unit, companyId
        );

        supabaseApi.createProduct(request).enqueue(new ApiCallback<>() {
            @Override
            public void onSuccess(Product product, int statusCode) {
                showLoading(false);
                Toast.makeText(ProductFormActivity.this, "Produto criado com sucesso!", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(Exception e) {
                showLoading(false);
                Log.e(TAG, "Erro ao criar produto: " + e.getMessage());
                Toast.makeText(ProductFormActivity.this, "Erro ao criar produto", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void updateProduct() {
        // Usar a função utilitária para verificar se o usuário está logado
        if (!SessionUtils.isUserLoggedIn()) {
            Toast.makeText(this, "Erro: usuário não carregado", Toast.LENGTH_SHORT).show();
            return;
        }
        
        showLoading(true);
        
        // Capturar dados dos campos
        String name = nameEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        String unit = unitAutoComplete.getText().toString().trim();
        float unitPrice = Float.parseFloat(unitPriceEditText.getText().toString().trim());
        String companyId = SessionUtils.getCurrentUserCompanyId();

        if (description.isEmpty()) description = null;

        ProductCreateRequest request = new ProductCreateRequest(
            name, description, unitPrice, unit, companyId
        );
        
        supabaseApi.updateProduct(productId, request).enqueue(new ApiCallback<>() {
            @Override
            public void onSuccess(Product product, int statusCode) {
                showLoading(false);
                Log.d(TAG, "Produto atualizado com sucesso! Status: " + statusCode);
                Toast.makeText(ProductFormActivity.this, "Produto atualizado com sucesso!", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(Exception e) {
                showLoading(false);
                Log.e(TAG, "Erro ao atualizar produto: " + e.getMessage());
                Toast.makeText(ProductFormActivity.this, "Erro ao atualizar produto", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void showLoading(boolean show) {
        loadingProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        saveButton.setEnabled(!show);
    }
}
