package com.honeycontrol.forms;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.honeycontrol.services.ApiCallback;
import com.honeycontrol.BaseActivity;
import com.honeycontrol.R;
import com.honeycontrol.services.SupabaseApi;
import com.honeycontrol.services.SupabaseClient;
import com.honeycontrol.utils.UserSession;
import com.honeycontrol.adapters.StockLogAdapter;
import com.honeycontrol.models.Product;
import com.honeycontrol.models.Stock;
import com.honeycontrol.models.StockLog;
import com.honeycontrol.models.User;
import com.honeycontrol.requests.ProductCreateRequest;
import com.honeycontrol.requests.StockCreateRequest;
import com.honeycontrol.requests.StockUpdateRequest;
import com.honeycontrol.requests.StockLogCreateRequest;
import com.honeycontrol.utils.SessionUtils;

import java.util.ArrayList;
import java.util.List;
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
    private FloatingActionButton moveStockFab;
    
    // Stock history views
    private LinearLayout stockHistorySection;
    private TextView stockHistoryCount;
    private ProgressBar stockHistoryLoadingProgressBar;
    private RecyclerView stockHistoryRecyclerView;
    private LinearLayout emptyStockHistoryLayout;
    private StockLogAdapter stockLogAdapter;
    
    private SupabaseApi supabaseApi;
    private Product editingProduct;
    private Stock currentStock;
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
        moveStockFab = findViewById(R.id.moveStockFab);
        
        // Initialize stock history views
        stockHistorySection = findViewById(R.id.stockHistorySection);
        stockHistoryCount = findViewById(R.id.stockHistoryCount);
        stockHistoryLoadingProgressBar = findViewById(R.id.stockHistoryLoadingProgressBar);
        stockHistoryRecyclerView = findViewById(R.id.stockHistoryRecyclerView);
        emptyStockHistoryLayout = findViewById(R.id.emptyStockHistoryLayout);
        
        // Setup RecyclerView
        stockHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        stockLogAdapter = new StockLogAdapter(this, new ArrayList<>());
        stockHistoryRecyclerView.setAdapter(stockLogAdapter);
        
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
        
        moveStockFab.setOnClickListener(v -> showStockMovementDialog());
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
        unitPriceEditText.setText(product.getUnitPrice() != null ? String.valueOf(product.getUnitPrice()) : "");
        unitAutoComplete.setText(product.getUnit() != null ? product.getUnit() : "");
        
        // Show stock history section when in edit mode
        if (isEditMode) {
            stockHistorySection.setVisibility(View.VISIBLE);
            loadStockHistory();
        }
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
    
    private void loadStockHistory() {
        if (editingProduct == null || editingProduct.getId() == null) {
            showEmptyStockHistory();
            return;
        }
        
        showStockHistoryLoading(true);

        supabaseApi.getStockByProductId(editingProduct.getId()).enqueue(new ApiCallback<>() {
            @Override
            public void onSuccess(List<Stock> stocks, int statusCode) {
                if (stocks != null && !stocks.isEmpty()) {
                    currentStock = stocks.get(0);
                    // Now get the stock logs for this stock
                    loadStockLogs(currentStock.getId());
                    // Show FAB when stock exists
                    moveStockFab.setVisibility(View.VISIBLE);
                } else {
                    // Create initial stock with 0 quantity
                    createInitialStock();
                }
            }

            @Override
            public void onFailure(Exception e) {
                showStockHistoryLoading(false);
                Log.e(TAG, "Erro ao carregar estoque: " + e.getMessage());
                showEmptyStockHistory();
            }
        });
    }
    
    private void loadStockLogs(String stockId) {
        supabaseApi.getStockLogsByStockId(stockId).enqueue(new ApiCallback<>() {
            @Override
            public void onSuccess(List<StockLog> stockLogs, int statusCode) {
                showStockHistoryLoading(false);
                if (stockLogs != null && !stockLogs.isEmpty()) {
                    updateStockHistoryCount(stockLogs.size());
                    stockLogAdapter.updateStockLogs(stockLogs);
                    showStockHistoryList();
                } else {
                    showEmptyStockHistory();
                }
            }

            @Override
            public void onFailure(Exception e) {
                showStockHistoryLoading(false);
                Log.e(TAG, "Erro ao carregar histórico de estoque: " + e.getMessage());
                Toast.makeText(ProductFormActivity.this, "Erro ao carregar histórico de movimentações", Toast.LENGTH_SHORT).show();
                showEmptyStockHistory();
            }
        });
    }
    
    private void showStockHistoryLoading(boolean show) {
        stockHistoryLoadingProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        stockHistoryRecyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
        emptyStockHistoryLayout.setVisibility(View.GONE);
    }
    
    private void showStockHistoryList() {
        stockHistoryLoadingProgressBar.setVisibility(View.GONE);
        stockHistoryRecyclerView.setVisibility(View.VISIBLE);
        emptyStockHistoryLayout.setVisibility(View.GONE);
    }
    
    private void showEmptyStockHistory() {
        stockHistoryLoadingProgressBar.setVisibility(View.GONE);
        stockHistoryRecyclerView.setVisibility(View.GONE);
        emptyStockHistoryLayout.setVisibility(View.VISIBLE);
        updateStockHistoryCount(0);
    }
    
    private void updateStockHistoryCount(int count) {
        String countText = count == 1 ? "1 movimentação" : count + " movimentações";
        stockHistoryCount.setText(countText);
    }
    
    private void createInitialStock() {
        if (editingProduct == null) return;
        
        StockCreateRequest stockRequest = new StockCreateRequest(editingProduct.getId(), 0);
        supabaseApi.createStock(stockRequest).enqueue(new ApiCallback<Stock>() {
            @Override
            public void onSuccess(Stock stock, int statusCode) {
                currentStock = stock;
                moveStockFab.setVisibility(View.VISIBLE);
                showStockHistoryLoading(false);
                showEmptyStockHistory();
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Erro ao criar estoque inicial: " + e.getMessage());
                showStockHistoryLoading(false);
                showEmptyStockHistory();
            }
        });
    }
    
    private void showStockMovementDialog() {
        if (currentStock == null) {
            Toast.makeText(this, "Estoque não encontrado", Toast.LENGTH_SHORT).show();
            return;
        }

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_stock_movement, null);
        
        // Find views
        AutoCompleteTextView movementTypeAutoComplete = dialogView.findViewById(R.id.movementTypeAutoComplete);
        TextInputEditText quantityEditText = dialogView.findViewById(R.id.quantityEditText);
        TextInputEditText reasonEditText = dialogView.findViewById(R.id.reasonEditText);
        TextView currentStockTextView = dialogView.findViewById(R.id.currentStockTextView);
        MaterialButton cancelButton = dialogView.findViewById(R.id.cancelButton);
        MaterialButton confirmButton = dialogView.findViewById(R.id.confirmButton);
        
        // Setup movement type dropdown
        String[] movementTypes = {"ENTRADA", "SAIDA", "AJUSTE"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_dropdown_item_1line, movementTypes);
        movementTypeAutoComplete.setAdapter(adapter);
        
        // Show current stock
        currentStockTextView.setText(String.valueOf(currentStock.getQuantity()));
        
        // Create dialog
        AlertDialog dialog = new AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create();
            
        // Setup button listeners
        cancelButton.setOnClickListener(v -> dialog.dismiss());
        
        confirmButton.setOnClickListener(v -> {
            String movementType = movementTypeAutoComplete.getText().toString().trim();
            String quantityStr = quantityEditText.getText().toString().trim();
            String reason = reasonEditText.getText().toString().trim();
            
            if (movementType.isEmpty()) {
                Toast.makeText(this, "Selecione o tipo de movimentação", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (quantityStr.isEmpty()) {
                Toast.makeText(this, "Informe a quantidade", Toast.LENGTH_SHORT).show();
                return;
            }
            
            try {
                int quantity = Integer.parseInt(quantityStr);
                if (quantity <= 0) {
                    Toast.makeText(this, "Quantidade deve ser maior que zero", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                // Calculate new stock quantity
                int currentQuantity = currentStock.getQuantity();
                int newQuantity;
                
                switch (movementType) {
                    case "ENTRADA":
                    case "AJUSTE":
                        newQuantity = currentQuantity + quantity;
                        break;
                    case "SAIDA":
                        newQuantity = currentQuantity - quantity;
                        if (newQuantity < 0) {
                            Toast.makeText(this, "Estoque insuficiente", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        break;
                    default:
                        Toast.makeText(this, "Tipo de movimentação inválido", Toast.LENGTH_SHORT).show();
                        return;
                }
                
                dialog.dismiss();
                processStockMovement(movementType, quantity, newQuantity, reason);
                
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Quantidade inválida", Toast.LENGTH_SHORT).show();
            }
        });
        
        dialog.show();
    }
    
    private void processStockMovement(String movementType, int quantity, int newQuantity, String reason) {
        // Update stock quantity
        StockUpdateRequest stockUpdate = new StockUpdateRequest(newQuantity);
        supabaseApi.updateStock(currentStock.getId(), stockUpdate).enqueue(new ApiCallback<>() {
            @Override
            public void onSuccess(Stock updatedStock, int statusCode) {
                currentStock = updatedStock;

                // Create stock log
                StockLogCreateRequest logRequest = new StockLogCreateRequest(
                        currentStock.getId(),
                        quantity,
                        movementType,
                        reason.isEmpty() ? null : reason
                );

                supabaseApi.createStockLog(logRequest).enqueue(new ApiCallback<>() {
                    @Override
                    public void onSuccess(StockLog stockLog, int statusCode) {
                        Toast.makeText(ProductFormActivity.this,
                                "Movimentação registrada com sucesso", Toast.LENGTH_SHORT).show();

                        // Update product stock quantity for display
                        if (editingProduct != null) {
                            editingProduct.setStockQuantity(newQuantity);
                        }

                        // Reload stock history
                        loadStockHistory();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.e(TAG, "Erro ao criar log de estoque: " + e.getMessage());
                        Toast.makeText(ProductFormActivity.this,
                                "Erro ao registrar movimentação", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Erro ao atualizar estoque: " + e.getMessage());
                Toast.makeText(ProductFormActivity.this,
                        "Erro ao atualizar estoque", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
