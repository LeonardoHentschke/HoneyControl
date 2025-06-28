package com.honeycontrol;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.honeycontrol.models.Customer;
import com.honeycontrol.models.User;
import com.honeycontrol.requests.CustomerCreateRequest;
import com.honeycontrol.utils.SessionUtils;

import java.util.Objects;

public class CustomerFormActivity extends BaseActivity {
    
    private static final String TAG = "CustomerFormActivity";
    
    private TextInputLayout nameInputLayout;
    private TextInputEditText nameEditText;
    private TextInputLayout emailInputLayout;
    private TextInputEditText emailEditText;
    private TextInputLayout phoneInputLayout;
    private TextInputEditText phoneEditText;
    private TextInputLayout documentInputLayout;
    private TextInputEditText documentEditText;
    private TextInputLayout addressInputLayout;
    private TextInputEditText addressEditText;
    private TextInputLayout cityInputLayout;
    private TextInputEditText cityEditText;
    private MaterialButton backButton;
    private MaterialButton saveButton;
    private ProgressBar loadingProgressBar;
    
    private SupabaseApi supabaseApi;
    private Customer editingCustomer;
    private boolean isEditMode = false;
    private String customerId = "";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_form);
        
        // Verificar se é modo de edição
        isEditMode = getIntent().getBooleanExtra("edit_mode", false);
        customerId = getIntent().getStringExtra("customer_id");
        
        initViews();
        setupClickListeners();
        loadCurrentUser();
        
        if (isEditMode && !Objects.equals(customerId, "")) {
            loadCustomerForEdit();
        }
    }
    
    private void initViews() {
        nameInputLayout = findViewById(R.id.nameInputLayout);
        nameEditText = findViewById(R.id.nameEditText);
        emailInputLayout = findViewById(R.id.emailInputLayout);
        emailEditText = findViewById(R.id.emailEditText);
        phoneInputLayout = findViewById(R.id.phoneInputLayout);
        phoneEditText = findViewById(R.id.phoneEditText);
        documentInputLayout = findViewById(R.id.documentInputLayout);
        documentEditText = findViewById(R.id.documentEditText);
        addressInputLayout = findViewById(R.id.addressInputLayout);
        addressEditText = findViewById(R.id.addressEditText);
        cityInputLayout = findViewById(R.id.cityInputLayout);
        cityEditText = findViewById(R.id.cityEditText);
        
        backButton = findViewById(R.id.backButton);
        saveButton = findViewById(R.id.saveButton);
        loadingProgressBar = findViewById(R.id.loadingProgressBar);
        
        // Inicializa API
        supabaseApi = SupabaseClient.createService(SupabaseApi.class);
        
        // Atualizar textos baseado no modo
        if (isEditMode) {
            saveButton.setText("Atualizar cliente");
        } else {
            saveButton.setText("Salvar cliente");
        }
    }
    
    private void setupClickListeners() {
        backButton.setOnClickListener(v -> finish());
        
        saveButton.setOnClickListener(v -> {
            if (validateInputs()) {
                if (isEditMode) {
                    updateCustomer();
                } else {
                    createCustomer();
                }
            }
        });
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
                    Toast.makeText(CustomerFormActivity.this, "Erro ao carregar dados do usuário", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    
    private void loadCustomerForEdit() {
        if (customerId == null || customerId.isEmpty()) {
            Log.e(TAG, "Customer ID é nulo ou vazio");
            Toast.makeText(this, "Erro: ID do cliente não encontrado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        showLoading(true);
        
        supabaseApi.getCustomerById(customerId).enqueue(new ApiCallback<Customer>() {
            @Override
            public void onSuccess(Customer customer, int statusCode) {
                showLoading(false);
                if (customer != null) {
                    editingCustomer = customer;
                    populateFormWithCustomerData(customer);
                    Log.d(TAG, "Dados do cliente carregados: " + customer.getName());
                } else {
                    Log.e(TAG, "Cliente não encontrado");
                    Toast.makeText(CustomerFormActivity.this, "Cliente não encontrado", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
            
            @Override
            public void onFailure(Exception e) {
                showLoading(false);
                Log.e(TAG, "Erro ao carregar cliente: " + e.getMessage());
                Toast.makeText(CustomerFormActivity.this, "Erro ao carregar dados do cliente", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
    
    private void populateFormWithCustomerData(Customer customer) {
        nameEditText.setText(customer.getName());
        emailEditText.setText(customer.getEmail());
        phoneEditText.setText(customer.getPhone());
        
        // Campos opcionais
        if (customer.getDocument() != null) {
            documentEditText.setText(customer.getDocument());
        }
        if (customer.getAddress() != null) {
            addressEditText.setText(customer.getAddress());
        }
        if (customer.getCity() != null) {
            cityEditText.setText(customer.getCity());
        }
    }

    private boolean validateInputs() {
        boolean isValid = true;
        
        // Validar nome
        String name = nameEditText.getText().toString().trim();
        if (name.isEmpty()) {
            nameInputLayout.setError("Nome é obrigatório");
            isValid = false;
        } else {
            nameInputLayout.setError(null);
        }
        
        // Validar email
        String email = emailEditText.getText().toString().trim();
        if (email.isEmpty()) {
            emailInputLayout.setError("Email é obrigatório");
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInputLayout.setError("Email inválido");
            isValid = false;
        } else {
            emailInputLayout.setError(null);
        }
        
        // Validar telefone
        String phone = phoneEditText.getText().toString().trim();
        if (phone.isEmpty()) {
            phoneInputLayout.setError("Telefone é obrigatório");
            isValid = false;
        } else {
            phoneInputLayout.setError(null);
        }
        
        // Limpar erros dos campos opcionais
        documentInputLayout.setError(null);
        addressInputLayout.setError(null);
        cityInputLayout.setError(null);
        
        return isValid;
    }

    private void createCustomer() {
        // Usar a função utilitária para verificar se o usuário está logado
        if (!SessionUtils.isUserLoggedIn()) {
            Toast.makeText(this, "Erro: usuário não carregado", Toast.LENGTH_SHORT).show();
            return;
        }
        
        showLoading(true);
        
        // Capturar dados dos campos
        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String document = documentEditText.getText().toString().trim();
        String address = addressEditText.getText().toString().trim();
        String city = cityEditText.getText().toString().trim();
        String companyId = SessionUtils.getCurrentUserCompanyId();

        CustomerCreateRequest request = new CustomerCreateRequest(
            name, email, phone, document, address, city, companyId
        );

        supabaseApi.createCustomer(request).enqueue(new ApiCallback<>() {
            @Override
            public void onSuccess(Customer customer, int statusCode) {
                showLoading(false);
                Toast.makeText(CustomerFormActivity.this, "Cliente criado com sucesso!", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(Exception e) {
                showLoading(false);
                Log.e(TAG, "Erro ao criar cliente: " + e.getMessage());
                Toast.makeText(CustomerFormActivity.this, "Erro ao criar cliente", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void updateCustomer() {
        // Usar a função utilitária para verificar se o usuário está logado
        if (!SessionUtils.isUserLoggedIn()) {
            Toast.makeText(this, "Erro: usuário não carregado", Toast.LENGTH_SHORT).show();
            return;
        }
        
        showLoading(true);
        
        // Capturar dados dos campos
        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String document = documentEditText.getText().toString().trim();
        String address = addressEditText.getText().toString().trim();
        String city = cityEditText.getText().toString().trim();
        String companyId = SessionUtils.getCurrentUserCompanyId();

        if (document.isEmpty()) document = null;
        if (address.isEmpty()) address = null;
        if (city.isEmpty()) city = null;

        CustomerCreateRequest request = new CustomerCreateRequest(
            name, email, phone, document, address, city, companyId
        );
        
        supabaseApi.updateCustomer(customerId, request).enqueue(new ApiCallback<>() {
            @Override
            public void onSuccess(Customer customer, int statusCode) {
                showLoading(false);
                Log.d(TAG, "Cliente atualizado com sucesso! Status: " + statusCode);
                Toast.makeText(CustomerFormActivity.this, "Cliente atualizado com sucesso!", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(Exception e) {
                showLoading(false);
                Log.e(TAG, "Erro ao atualizar cliente: " + e.getMessage());
                Toast.makeText(CustomerFormActivity.this, "Erro ao atualizar cliente", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void showLoading(boolean show) {
        loadingProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        saveButton.setEnabled(!show);
        backButton.setEnabled(!show);
    }
}
