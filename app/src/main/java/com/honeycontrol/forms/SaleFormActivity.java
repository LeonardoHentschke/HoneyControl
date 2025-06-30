package com.honeycontrol.forms;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.honeycontrol.models.Stock;
import com.honeycontrol.models.StockLog;
import com.honeycontrol.requests.StockLogCreateRequest;
import com.honeycontrol.requests.StockCreateRequest;
import com.honeycontrol.requests.StockUpdateRequest;
import com.honeycontrol.services.ApiCallback;
import com.honeycontrol.BaseActivity;
import com.honeycontrol.R;
import com.honeycontrol.services.SupabaseApi;
import com.honeycontrol.services.SupabaseClient;
import com.honeycontrol.utils.UserSession;
import com.honeycontrol.models.Sale;
import com.honeycontrol.models.Customer;
import com.honeycontrol.models.Product;
import com.honeycontrol.models.TempSaleItem;
import com.honeycontrol.models.User;
import com.honeycontrol.models.SaleItem;
import com.honeycontrol.utils.SessionUtils;
import com.honeycontrol.adapters.SaleItemAdapter;
import com.honeycontrol.requests.SaleCreateRequest;
import com.honeycontrol.requests.SaleItemCreateRequest;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SaleFormActivity extends BaseActivity implements SaleItemAdapter.OnSaleItemClickListener {
    
    private static final String TAG = "SaleFormActivity";
    
    private TextInputLayout customerInputLayout;
    private AutoCompleteTextView customerAutoComplete;
    private RecyclerView saleItemsRecyclerView;
    private LinearLayout emptyItemsLayout;
    private MaterialButton addItemButton;
    private MaterialButton backButton;
    private MaterialButton saveButton;
    private ProgressBar loadingProgressBar;
    private TextView totalTextView;
    
    private SupabaseApi supabaseApi;
    private Sale editingSale;
    private boolean isEditMode = false;
    private String saleId = "";
    
    private List<Customer> customers = new ArrayList<>();
    private List<Product> products = new ArrayList<>();
    private List<TempSaleItem> saleItems = new ArrayList<>();
    
    private ArrayAdapter<String> customerAdapter;
    private ArrayAdapter<String> productAdapter;
    private SaleItemAdapter saleItemAdapter;
    
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
    
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
        setupProductDropdown();
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
        emptyItemsLayout = findViewById(R.id.emptyItemsLayout);
        addItemButton = findViewById(R.id.addItemButton);
        backButton = findViewById(R.id.backButton);
        saveButton = findViewById(R.id.saveButton);
        loadingProgressBar = findViewById(R.id.loadingProgressBar);
        totalTextView = findViewById(R.id.totalTextView);
        
        // Setup RecyclerView para itens da venda
        saleItemAdapter = new SaleItemAdapter(this, saleItems);
        saleItemAdapter.setOnSaleItemClickListener(this);
        saleItemsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        saleItemsRecyclerView.setAdapter(saleItemAdapter);
        
        // Inicializar estado da UI
        updateEmptyState();
        updateTotal();
        
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
        
        addItemButton.setOnClickListener(v -> showAddItemDialog());
    }
    
    private void setupCustomerDropdown() {
        List<String> customerNames = new ArrayList<>();
        customerAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_dropdown_item_1line, customerNames);
        customerAutoComplete.setAdapter(customerAdapter);
    }
    
    private void setupProductDropdown() {
        List<String> productNames = new ArrayList<>();
        productAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_dropdown_item_1line, productNames);
    }
    
    private void loadCustomers() {
        String companyId = SessionUtils.getCurrentUserCompanyId();
        Log.d(TAG, "Carregando clientes da empresa: " + companyId);
        
        supabaseApi.getCustomersByCompany(companyId).enqueue(new ApiCallback<>() {
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
        
        supabaseApi.getProductsWithStockByCompany(companyId).enqueue(new ApiCallback<>() {
            @Override
            public void onSuccess(List<Product> productList, int statusCode) {
                if (productList != null) {
                    products.clear();
                    products.addAll(productList);

                    // Atualizar adapter de produtos
                    List<String> productNames = new ArrayList<>();
                    for (Product product : products) {
                        productNames.add(product.getName());
                    }

                    productAdapter.clear();
                    productAdapter.addAll(productNames);
                    productAdapter.notifyDataSetChanged();

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
        supabaseApi.getSaleById(saleId).enqueue(new ApiCallback<>() {
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
        
        // Validar itens da venda
        if (saleItems.isEmpty()) {
            Toast.makeText(this, "Adicione pelo menos um item à venda", Toast.LENGTH_SHORT).show();
            isValid = false;
        }
        
        return isValid;
    }
    
    private void showAddItemDialog() {
        if (products.isEmpty()) {
            Toast.makeText(this, "Carregando produtos...", Toast.LENGTH_SHORT).show();
            return;
        }
        
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_sale_item, null);
        
        // Find views
        TextInputLayout productInputLayout = dialogView.findViewById(R.id.productInputLayout);
        AutoCompleteTextView productAutoComplete = dialogView.findViewById(R.id.productAutoComplete);
        LinearLayout productInfoLayout = dialogView.findViewById(R.id.productInfoLayout);
        TextView productPriceTextView = dialogView.findViewById(R.id.productPriceTextView);
        TextView productStockTextView = dialogView.findViewById(R.id.productStockTextView);
        TextInputLayout quantityInputLayout = dialogView.findViewById(R.id.quantityInputLayout);
        TextInputEditText quantityEditText = dialogView.findViewById(R.id.quantityEditText);
        TextInputLayout discountInputLayout = dialogView.findViewById(R.id.discountInputLayout);
        TextInputEditText discountEditText = dialogView.findViewById(R.id.discountEditText);
        TextView subtotalTextView = dialogView.findViewById(R.id.subtotalTextView);
        MaterialButton cancelButton = dialogView.findViewById(R.id.cancelButton);
        MaterialButton addButton = dialogView.findViewById(R.id.addButton);
        
        // Setup product dropdown
        productAutoComplete.setAdapter(productAdapter);
        
        // Variables to hold selected product and calculated values
        final Product[] selectedProduct = {null};
        
        // Product selection listener
        productAutoComplete.setOnItemClickListener((parent, view, position, id) -> {
            String productName = (String) parent.getItemAtPosition(position);
            selectedProduct[0] = findProductByName(productName);
            
            if (selectedProduct[0] != null) {
                // Show product info
                productInfoLayout.setVisibility(View.VISIBLE);
                productPriceTextView.setText(CURRENCY_FORMAT.format(selectedProduct[0].getUnitPrice()));
                productStockTextView.setText(String.valueOf(selectedProduct[0].getStockQuantity()));
                
                // Update stock color based on quantity
                int stockQuantity = selectedProduct[0].getStockQuantity();
                if (stockQuantity == 0) {
                    productStockTextView.setTextColor(getResources().getColor(R.color.red_400));
                } else if (stockQuantity <= 5) {
                    productStockTextView.setTextColor(getResources().getColor(R.color.amber_400));
                } else {
                    productStockTextView.setTextColor(getResources().getColor(R.color.green_400));
                }
            }
        });
        
        // Method to update subtotal
        Runnable updateSubtotalRunnable = () -> {
            if (selectedProduct[0] != null) {
                try {
                    String quantityStr = quantityEditText.getText().toString().trim();
                    String discountStr = discountEditText.getText().toString().trim();
                    
                    int quantity = quantityStr.isEmpty() ? 0 : Integer.parseInt(quantityStr);
                    float discount = discountStr.isEmpty() ? 0 : Float.parseFloat(discountStr);
                    
                    float unitPrice = selectedProduct[0].getUnitPrice();
                    float subtotal = (unitPrice * quantity) - discount;
                    subtotal = Math.max(0, subtotal);
                    
                    subtotalTextView.setText(CURRENCY_FORMAT.format(subtotal));
                } catch (NumberFormatException e) {
                    subtotalTextView.setText("R$ 0,00");
                }
            }
        };
        
        // Text watchers for real-time subtotal calculation
        quantityEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                updateSubtotalRunnable.run();
            }
        });
        
        discountEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                updateSubtotalRunnable.run();
            }
        });
        
        // Create dialog
        AlertDialog dialog = new AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create();
        
        // Setup button listeners
        cancelButton.setOnClickListener(v -> dialog.dismiss());
        
        addButton.setOnClickListener(v -> {
            // Validate inputs
            if (selectedProduct[0] == null) {
                productInputLayout.setError("Selecione um produto");
                return;
            }
            
            String quantityStr = quantityEditText.getText().toString().trim();
            if (quantityStr.isEmpty()) {
                quantityInputLayout.setError("Informe a quantidade");
                return;
            }
            
            try {
                int quantity = Integer.parseInt(quantityStr);
                if (quantity <= 0) {
                    quantityInputLayout.setError("Quantidade deve ser maior que zero");
                    return;
                }
                
                // Check stock availability
                if (quantity > selectedProduct[0].getStockQuantity()) {
                    quantityInputLayout.setError("Quantidade maior que o estoque disponível");
                    return;
                }
                
                // Check if product already exists in sale
                boolean productExists = false;
                for (TempSaleItem item : saleItems) {
                    if (item.getProduct().getId().equals(selectedProduct[0].getId())) {
                        Toast.makeText(SaleFormActivity.this, "Produto já adicionado à venda", Toast.LENGTH_SHORT).show();
                        productExists = true;
                        break;
                    }
                }
                
                if (productExists) {
                    return;
                }
                
                float discount = 0;
                String discountStr = discountEditText.getText().toString().trim();
                if (!discountStr.isEmpty()) {
                    discount = Float.parseFloat(discountStr);
                }
                
                // Create and add item
                TempSaleItem newItem = new TempSaleItem(
                    selectedProduct[0], 
                    quantity, 
                    selectedProduct[0].getUnitPrice(), 
                    discount
                );
                
                saleItems.add(newItem);
                saleItemAdapter.notifyItemInserted(saleItems.size() - 1);
                
                updateEmptyState();
                updateTotal();
                
                dialog.dismiss();
                
            } catch (NumberFormatException e) {
                quantityInputLayout.setError("Quantidade inválida");
            }
        });
        
        dialog.show();
    }
    
    private Product findProductByName(String name) {
        for (Product product : products) {
            if (product.getName().equals(name)) {
                return product;
            }
        }
        return null;
    }
    
    private void updateEmptyState() {
        if (saleItems.isEmpty()) {
            emptyItemsLayout.setVisibility(View.VISIBLE);
            saleItemsRecyclerView.setVisibility(View.GONE);
        } else {
            emptyItemsLayout.setVisibility(View.GONE);
            saleItemsRecyclerView.setVisibility(View.VISIBLE);
        }
    }
    
    private void updateTotal() {
        float total = 0;
        for (TempSaleItem item : saleItems) {
            total += item.getSubtotal();
        }
        totalTextView.setText(CURRENCY_FORMAT.format(total));
    }
    
    @Override
    public void onRemoveItem(int position) {
        if (position >= 0 && position < saleItems.size()) {
            saleItems.remove(position);
            saleItemAdapter.notifyItemRemoved(position);
            saleItemAdapter.notifyItemRangeChanged(position, saleItems.size());
            
            updateEmptyState();
            updateTotal();
        }
    }
    
    private void createSale() {
        showLoading(true);
        
        // Buscar customer ID pelo nome selecionado
        String customerName = customerAutoComplete.getText().toString().trim();
        Customer selectedCustomer = null;
        for (Customer customer : customers) {
            if (customer.getName().equals(customerName)) {
                selectedCustomer = customer;
                break;
            }
        }
        
        if (selectedCustomer == null) {
            showLoading(false);
            Toast.makeText(this, "Cliente não encontrado", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Calcular total
        float total = 0;
        for (TempSaleItem item : saleItems) {
            total += item.getSubtotal();
        }
        
        // Obter dados do usuário atual
        String companyId = SessionUtils.getCurrentUserCompanyId();
        String userId = SessionUtils.getCurrentUserId();
        
        // Criar request da venda
        SaleCreateRequest saleRequest = new SaleCreateRequest(
            companyId,
            selectedCustomer.getId(),
            userId,
            total
        );
        
        // Criar a venda primeiro
        supabaseApi.createSale(saleRequest).enqueue(new ApiCallback<>() {
            @Override
            public void onSuccess(Sale sale, int statusCode) {
                createSaleItems(sale.getId());
            }
            
            @Override
            public void onFailure(Exception e) {
                showLoading(false);
                Toast.makeText(SaleFormActivity.this, "Erro ao criar venda: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void createSaleItems(String saleId) {
        final int totalItems = saleItems.size();
        final int[] itemsCreated = {0};
        final boolean[] hasError = {false};

        // Criar cada item da venda
        for (TempSaleItem tempItem : saleItems) {
            SaleItemCreateRequest itemRequest = new SaleItemCreateRequest(
                saleId,
                tempItem.getProduct().getId(),
                tempItem.getQuantity(),
                tempItem.getUnitPrice(),
                tempItem.getSubtotal(),
                tempItem.getDiscount()
            );
            
            supabaseApi.createSaleItem(itemRequest).enqueue(new ApiCallback<>() {
                @Override
                public void onSuccess(SaleItem saleItem, int statusCode) {
                    itemsCreated[0]++;
                    Log.d(TAG, "Item criado: " + itemsCreated[0] + "/" + totalItems);
                    
                    // Verificar se todos os itens foram criados
                    if (itemsCreated[0] == totalItems && !hasError[0]) {
                        // Atualizar estoque dos produtos
                        updateProductsStock();
                    }
                }
                
                @Override
                public void onFailure(Exception e) {
                    if (!hasError[0]) {
                        hasError[0] = true;
                        showLoading(false);
                        Log.e(TAG, "Erro ao criar item da venda: " + e.getMessage());
                        Toast.makeText(SaleFormActivity.this, "Erro ao criar itens da venda", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
    
    private void updateProductsStock() {
        final int totalProducts = saleItems.size();
        final int[] productsUpdated = {0};
        final boolean[] hasError = {false};
        
        // Atualizar estoque de cada produto
        for (TempSaleItem tempItem : saleItems) {
            Product product = tempItem.getProduct();
            
            // Verificar se produto tem estoque cadastrado
            if (product.getStock() != null && product.getStock().getId() != null) {
                // Calcular nova quantidade do estoque
                int currentQuantity = product.getStockQuantity();
                int newQuantity = currentQuantity - tempItem.getQuantity();
                
                // Validar se há estoque suficiente
                if (newQuantity < 0) {
                    if (!hasError[0]) {
                        hasError[0] = true;
                        showLoading(false);
                        Log.e(TAG, "Estoque insuficiente para produto: " + product.getName());
                        Toast.makeText(SaleFormActivity.this, "Estoque insuficiente para o produto: " + product.getName(), Toast.LENGTH_LONG).show();
                        return;
                    }
                    continue;
                }
                
                // Primeiro: Atualizar a quantidade do estoque
                StockUpdateRequest stockUpdate = new StockUpdateRequest(newQuantity);
                supabaseApi.updateStock(product.getStock().getId(), stockUpdate).enqueue(new ApiCallback<>() {
                    @Override
                    public void onSuccess(Stock updatedStock, int statusCode) {
                        // Segundo: Criar log de saída do estoque
                        StockLogCreateRequest logRequest = new StockLogCreateRequest(
                            product.getStock().getId(),
                            -tempItem.getQuantity(), // Quantidade negativa para indicar saída
                            "SAIDA",
                            "Venda - Item vendido"
                        );
                        
                        supabaseApi.createStockLog(logRequest).enqueue(new ApiCallback<>() {
                            @Override
                            public void onSuccess(StockLog stockLog, int statusCode) {
                                productsUpdated[0]++;
                                Log.d(TAG, "Estoque atualizado: " + productsUpdated[0] + "/" + totalProducts);

                                // Verificar se todos os produtos foram atualizados
                                if (productsUpdated[0] == totalProducts && !hasError[0]) {
                                    showLoading(false);
                                    Toast.makeText(SaleFormActivity.this, "Venda criada com sucesso!", Toast.LENGTH_SHORT).show();
                                    finish(); // Voltar para a tela anterior
                                }
                            }

                            @Override
                            public void onFailure(Exception e) {
                                if (!hasError[0]) {
                                    hasError[0] = true;
                                    showLoading(false);
                                    Log.e(TAG, "Erro ao criar log de estoque: " + e.getMessage());
                                    Toast.makeText(SaleFormActivity.this, "Venda criada, mas houve erro ao registrar movimentação de estoque", Toast.LENGTH_LONG).show();
                                    finish();
                                }
                            }
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        if (!hasError[0]) {
                            hasError[0] = true;
                            showLoading(false);
                            Log.e(TAG, "Erro ao atualizar quantidade do estoque: " + e.getMessage());
                            Toast.makeText(SaleFormActivity.this, "Venda criada, mas houve erro ao atualizar estoque", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    }
                });
            } else {
                // Se não tem estoque cadastrado ou ID do stock é null, apenas conta como processado
                productsUpdated[0]++;
                Log.w(TAG, "Produto " + product.getName() + " não possui estoque cadastrado ou ID do stock é null");
                if (productsUpdated[0] == totalProducts && !hasError[0]) {
                    showLoading(false);
                    Toast.makeText(SaleFormActivity.this, "Venda criada com sucesso!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
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
