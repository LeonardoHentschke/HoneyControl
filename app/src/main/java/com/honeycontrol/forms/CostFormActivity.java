package com.honeycontrol.forms;

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
import com.honeycontrol.services.ApiCallback;
import com.honeycontrol.BaseActivity;
import com.honeycontrol.R;
import com.honeycontrol.services.SupabaseApi;
import com.honeycontrol.services.SupabaseClient;
import com.honeycontrol.utils.UserSession;
import com.honeycontrol.models.Cost;
import com.honeycontrol.models.User;
import com.honeycontrol.requests.CostCreateRequest;
import com.honeycontrol.utils.SessionUtils;

import java.util.Objects;

public class CostFormActivity extends BaseActivity {
    
    private static final String TAG = "CostFormActivity";
    
    private TextInputLayout nameInputLayout;
    private TextInputEditText nameEditText;
    private TextInputLayout descriptionInputLayout;
    private TextInputEditText descriptionEditText;
    private TextInputLayout categoryInputLayout;
    private AutoCompleteTextView categoryAutoComplete;
    private TextInputLayout amountInputLayout;
    private TextInputEditText amountEditText;
    private MaterialButton backButton;
    private MaterialButton saveButton;
    private ProgressBar loadingProgressBar;
    
    private SupabaseApi supabaseApi;
    private Cost editingCost;
    private boolean isEditMode = false;
    private String costId = "";
    
    // Categories for the dropdown
    private final String[] categories = {
        "Aluguel", "Energia", "Água", "Internet", "Telefone", "Materiais",
        "Equipamentos", "Manutenção", "Transporte", "Alimentação",
        "Marketing", "Consultorias", "Impostos", "Seguro", "Outros"
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cost_form);
        
        // Verificar se é modo de edição
        isEditMode = getIntent().getBooleanExtra("edit_mode", false);
        costId = getIntent().getStringExtra("cost_id");
        
        initViews();
        setupClickListeners();
        setupCategoryDropdown();
        loadCurrentUser();
        
        if (isEditMode && !Objects.equals(costId, "")) {
            loadCostForEdit();
        }
    }
    
    private void initViews() {
        nameInputLayout = findViewById(R.id.nameInputLayout);
        nameEditText = findViewById(R.id.nameEditText);
        descriptionInputLayout = findViewById(R.id.descriptionInputLayout);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        categoryInputLayout = findViewById(R.id.categoryInputLayout);
        categoryAutoComplete = findViewById(R.id.categoryAutoComplete);
        amountInputLayout = findViewById(R.id.amountInputLayout);
        amountEditText = findViewById(R.id.amountEditText);
        
        backButton = findViewById(R.id.backButton);
        saveButton = findViewById(R.id.saveButton);
        loadingProgressBar = findViewById(R.id.loadingProgressBar);
        
        // Inicializa API
        supabaseApi = SupabaseClient.createService(SupabaseApi.class);
        
        // Atualizar textos baseado no modo
        if (isEditMode) {
            saveButton.setText("Atualizar custo");
        } else {
            saveButton.setText("Salvar custo");
        }
    }
    
    private void setupClickListeners() {
        backButton.setOnClickListener(v -> finish());
        
        saveButton.setOnClickListener(v -> {
            if (validateInputs()) {
                if (isEditMode) {
                    updateCost();
                } else {
                    createCost();
                }
            }
        });
    }
    
    private void setupCategoryDropdown() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, categories);
        categoryAutoComplete.setAdapter(adapter);
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
                    Toast.makeText(CostFormActivity.this, "Erro ao carregar dados do usuário", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    
    private void loadCostForEdit() {
        if (costId == null || costId.isEmpty()) {
            Log.e(TAG, "Cost ID é nulo ou vazio");
            Toast.makeText(this, "Erro: ID do custo não encontrado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        showLoading(true);
        
        supabaseApi.getCostById(costId).enqueue(new ApiCallback<Cost>() {
            @Override
            public void onSuccess(Cost cost, int statusCode) {
                showLoading(false);
                if (cost != null) {
                    editingCost = cost;
                    populateFormWithCostData(cost);
                    Log.d(TAG, "Dados do custo carregados: " + cost.getName());
                } else {
                    Log.e(TAG, "Custo não encontrado");
                    Toast.makeText(CostFormActivity.this, "Custo não encontrado", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
            
            @Override
            public void onFailure(Exception e) {
                showLoading(false);
                Log.e(TAG, "Erro ao carregar custo: " + e.getMessage());
                Toast.makeText(CostFormActivity.this, "Erro ao carregar dados do custo", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
    
    private void populateFormWithCostData(Cost cost) {
        nameEditText.setText(cost.getName());
        descriptionEditText.setText(cost.getDescription() != null ? cost.getDescription() : "");
        categoryAutoComplete.setText(cost.getCategory() != null ? cost.getCategory() : "");
        amountEditText.setText(cost.getAmount() != null ? String.valueOf(cost.getAmount()) : "");
    }

    private boolean validateInputs() {
        boolean isValid = true;
        
        // Reset error states
        nameInputLayout.setError(null);
        categoryInputLayout.setError(null);
        amountInputLayout.setError(null);
        
        // Validate name
        String name = nameEditText.getText().toString().trim();
        if (name.isEmpty()) {
            nameInputLayout.setError("Nome é obrigatório");
            isValid = false;
        }
        
        // Validate category
        String category = categoryAutoComplete.getText().toString().trim();
        if (category.isEmpty()) {
            categoryInputLayout.setError("Categoria é obrigatória");
            isValid = false;
        }
        
        // Validate amount
        String amountStr = amountEditText.getText().toString().trim();
        if (amountStr.isEmpty()) {
            amountInputLayout.setError("Valor é obrigatório");
            isValid = false;
        } else {
            try {
                float amount = Float.parseFloat(amountStr);
                if (amount <= 0) {
                    amountInputLayout.setError("Valor deve ser maior que zero");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                amountInputLayout.setError("Valor inválido");
                isValid = false;
            }
        }
        
        return isValid;
    }

    private void createCost() {
        // Usar a função utilitária para verificar se o usuário está logado
        if (!SessionUtils.isUserLoggedIn()) {
            Toast.makeText(this, "Erro: usuário não carregado", Toast.LENGTH_SHORT).show();
            return;
        }
        
        showLoading(true);
        
        // Capturar dados dos campos
        String name = nameEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        String category = categoryAutoComplete.getText().toString().trim();
        float amount = Float.parseFloat(amountEditText.getText().toString().trim());
        String companyId = SessionUtils.getCurrentUserCompanyId();
        String userId = SessionUtils.getCurrentUserId();

        if (description.isEmpty()) description = null;

        CostCreateRequest request = new CostCreateRequest(
            name, description, category, amount, companyId, userId
        );

        supabaseApi.createCost(request).enqueue(new ApiCallback<>() {
            @Override
            public void onSuccess(Cost cost, int statusCode) {
                showLoading(false);
                Toast.makeText(CostFormActivity.this, "Custo criado com sucesso!", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(Exception e) {
                showLoading(false);
                Log.e(TAG, "Erro ao criar custo: " + e.getMessage());
                Toast.makeText(CostFormActivity.this, "Erro ao criar custo", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void updateCost() {
        // Usar a função utilitária para verificar se o usuário está logado
        if (!SessionUtils.isUserLoggedIn()) {
            Toast.makeText(this, "Erro: usuário não carregado", Toast.LENGTH_SHORT).show();
            return;
        }
        
        showLoading(true);
        
        // Capturar dados dos campos
        String name = nameEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        String category = categoryAutoComplete.getText().toString().trim();
        float amount = Float.parseFloat(amountEditText.getText().toString().trim());
        String companyId = SessionUtils.getCurrentUserCompanyId();
        String userId = SessionUtils.getCurrentUserId();

        if (description.isEmpty()) description = null;

        CostCreateRequest request = new CostCreateRequest(
            name, description, category, amount, companyId, userId
        );
        
        supabaseApi.updateCost(costId, request).enqueue(new ApiCallback<>() {
            @Override
            public void onSuccess(Cost cost, int statusCode) {
                showLoading(false);
                Log.d(TAG, "Custo atualizado com sucesso! Status: " + statusCode);
                Toast.makeText(CostFormActivity.this, "Custo atualizado com sucesso!", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(Exception e) {
                showLoading(false);
                Log.e(TAG, "Erro ao atualizar custo: " + e.getMessage());
                Toast.makeText(CostFormActivity.this, "Erro ao atualizar custo", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void showLoading(boolean show) {
        loadingProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        saveButton.setEnabled(!show);
        backButton.setEnabled(!show);
    }
}
