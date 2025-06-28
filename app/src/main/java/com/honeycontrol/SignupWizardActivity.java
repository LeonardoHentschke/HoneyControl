package com.honeycontrol;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.honeycontrol.adapters.SignupWizardAdapter;
import com.honeycontrol.models.Companies;
import com.honeycontrol.models.User;
import com.honeycontrol.services.ApiService;
import com.honeycontrol.viewmodels.SignupWizardViewModel;

public class SignupWizardActivity extends AppCompatActivity {
    
    private ViewPager2 viewPager;
    private MaterialButton btnPrevious, btnNext;
    private LinearProgressIndicator progressIndicator;
    private TextView stepCounter;
    private SignupWizardAdapter adapter;
    private SignupWizardViewModel viewModel;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_wizard);
        
        initViewModel();
        initViews();
        setupViewPager();
        setupObservers();

        setupClickListeners();
    }
    
    private void initViewModel() {
        viewModel = new ViewModelProvider(this).get(SignupWizardViewModel.class);
    }
    
    private void initViews() {
        viewPager = findViewById(R.id.viewPager);
        btnPrevious = findViewById(R.id.btnPrevious);
        btnNext = findViewById(R.id.btnNext);
        progressIndicator = findViewById(R.id.progressIndicator);
        stepCounter = findViewById(R.id.stepCounter);
        
        // Configurar ViewPager para não permitir swipe
        viewPager.setUserInputEnabled(false);
        
        // Configurar toolbar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
    
    private void setupViewPager() {
        adapter = new SignupWizardAdapter(this);
        viewPager.setAdapter(adapter);
        
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                viewModel.setCurrentStep(position);
                updateProgress();
                updateButtonsState();
            }
        });
    }
    
    private void setupObservers() {
        viewModel.getCurrentStep().observe(this, step -> {
            if (step != null) {
                updateProgress();
                updateButtonsState();
            }
        });
        
        viewModel.getIsCompanyStepValid().observe(this, isValid -> {
            updateButtonsState();
        });
        
        viewModel.getIsUserStepValid().observe(this, isValid -> {
            updateButtonsState();
        });
    }
    
    private void setupClickListeners() {
        btnPrevious.setOnClickListener(v -> {
            int currentItem = viewPager.getCurrentItem();
            if (currentItem > 0) {
                viewPager.setCurrentItem(currentItem - 1);
            }
        });
        
        btnNext.setOnClickListener(v -> {
            int currentItem = viewPager.getCurrentItem();
            int totalPages = adapter.getItemCount();
            
            if (currentItem < totalPages - 1) {
                // Navegar para próxima etapa
                viewPager.setCurrentItem(currentItem + 1);
            } else {
                // Última etapa - finalizar cadastro
                finishSignup();
            }
        });
    }
    
    private void updateProgress() {
        Integer currentStep = viewModel.getCurrentStep().getValue();
        if (currentStep != null) {
            int totalSteps = adapter.getItemCount();
            int progress = (int) (((currentStep + 1.0) / totalSteps) * 100);
            progressIndicator.setProgress(progress);
            
            // Atualizar contador de etapas
            String stepText = String.format("Etapa %d de %d", currentStep + 1, totalSteps);
            stepCounter.setText(stepText);
        }
    }
    
    private void updateButtonsState() {
        Integer currentStep = viewModel.getCurrentStep().getValue();
        if (currentStep == null) return;
        
        // Botão Anterior
        btnPrevious.setEnabled(currentStep > 0);
        
        // Botão Próximo/Finalizar
        boolean canProceed = false;
        
        if (currentStep == 0) {
            // Etapa da empresa
            Boolean isCompanyValid = viewModel.getIsCompanyStepValid().getValue();
            canProceed = isCompanyValid != null && isCompanyValid;
        } else if (currentStep == 1) {
            // Etapa do usuário
            Boolean isUserValid = viewModel.getIsUserStepValid().getValue();
            canProceed = isUserValid != null && isUserValid;
        }
        
        btnNext.setEnabled(canProceed);
        
        // Alterar texto do botão na última etapa
        if (currentStep == adapter.getItemCount() - 1) {
            btnNext.setText("Criar Conta");
        } else {
            btnNext.setText("Próximo");
        }
    }
    
    private void finishSignup() {
        SignupWizardViewModel.SignupData data = viewModel.getSignupData();
        
        // Mostrar carregamento
        showLoading(true);
        
        // 1. Criar a companhia primeiro
        createCompany(data);
    }
    
    private void createCompany(SignupWizardViewModel.SignupData data) {
        // Criar objeto Companies
        Companies company = new Companies();
        company.setName(data.companyName);
        
        // Chamar API para criar companhia
        ApiService.getInstance().getApi().createCompanies(company)
            .enqueue(new retrofit2.Callback<Companies>() {
                @Override
                public void onResponse(@NonNull retrofit2.Call<Companies> call, @NonNull retrofit2.Response<Companies> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        // Companhia criada com sucesso, agora criar usuário
                        Companies createdCompany = response.body();
                        android.util.Log.d("SignupWizard", "Empresa criada com ID: " + createdCompany.getId());
                        
                        // Com o ID da companhia, criar usuário
                        createUser(data, createdCompany.getId());
                    } else {
                        // Erro ao criar companhia
                        android.util.Log.e("SignupWizard", "Erro ao criar empresa: " + response.code());
                        showLoading(false);
                        
                        // Verificar se quer tentar novamente
                        showRetryDialog("Erro ao criar empresa", 
                                       "Ocorreu um erro ao criar a empresa. Deseja tentar novamente?",
                                       () -> createCompany(data));
                    }
                }
                
                @Override
                public void onFailure(retrofit2.Call<Companies> call, Throwable t) {
                    // Falha na requisição
                    android.util.Log.e("SignupWizard", "Falha na requisição para criar empresa", t);
                    showLoading(false);
                    
                    // Verificar se quer tentar novamente
                    showRetryDialog("Falha na conexão", 
                                   "Ocorreu um problema de conexão ao criar a empresa. Deseja tentar novamente?",
                                   () -> createCompany(data));
                }
            });
    }
    
    private void createUser(SignupWizardViewModel.SignupData data, String companyId) {
        // Criar objeto User
        User user = new User();
        user.setName(data.userName);
        user.setEmail(data.userEmail);
        user.setPassword_hash(data.userPassword);
        user.setCompanyId(companyId);
        
        // Chamar API para criar usuário
        ApiService.getInstance().getApi().createUser(user)
            .enqueue(new retrofit2.Callback<User>() {
                @Override
                public void onResponse(retrofit2.Call<User> call, retrofit2.Response<User> response) {
                    showLoading(false);
                    
                    if (response.isSuccessful() && response.body() != null) {
                        // Usuário criado com sucesso
                        User createdUser = response.body();
                        android.util.Log.d("SignupWizard", "Usuário criado com ID: " + createdUser.getId());
                        
                        String message = String.format(
                            "Conta criada com sucesso!\nEmpresa: %s\nNome: %s\nEmail: %s\nData: %s",
                            data.companyName,
                            data.userName,
                            data.userEmail,
                            data.createdAt.toString()
                        );
                        
                        // Mostrar diálogo de sucesso
                        showSuccessDialog("Conta criada com sucesso!", message);
                        
                        android.util.Log.d("SignupWizard", message);
                    } else {
                        // Erro ao criar usuário
                        android.util.Log.e("SignupWizard", "Erro ao criar usuário: " + response.code());
                        
                        // Verificar se quer tentar novamente
                        showRetryDialog("Erro ao criar usuário", 
                                       "Ocorreu um erro ao criar o usuário. Deseja tentar novamente?",
                                       () -> createUser(data, companyId));
                    }
                }
                
                @Override
                public void onFailure(@NonNull retrofit2.Call<User> call, @NonNull Throwable t) {
                    showLoading(false);
                    // Falha na requisição
                    android.util.Log.e("SignupWizard", "Falha na requisição para criar usuário", t);
                    
                    // Verificar se quer tentar novamente
                    showRetryDialog("Falha na conexão", 
                                   "Ocorreu um problema de conexão ao criar o usuário. Deseja tentar novamente?",
                                   () -> createUser(data, companyId));
                }
            });
    }
    
    private androidx.appcompat.app.AlertDialog loadingDialog;
    
    private void showLoading(boolean isLoading) {
        if (isLoading) {
            // Mostrar indicador de progresso, desabilitar botões, etc.
            btnNext.setEnabled(false);
            btnPrevious.setEnabled(false);
            
            // Criar e mostrar o diálogo de carregamento
            if (loadingDialog == null) {
                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
                android.view.View view = getLayoutInflater().inflate(R.layout.dialog_loading, null);
                builder.setView(view);
                builder.setCancelable(false);
                loadingDialog = builder.create();
                loadingDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            }
            
            loadingDialog.show();
        } else {
            // Esconder indicador de progresso, reabilitar botões, etc.
            updateButtonsState();
            
            // Esconder o diálogo de carregamento
            if (loadingDialog != null && loadingDialog.isShowing()) {
                loadingDialog.dismiss();
            }
        }
    }
    
    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public void onBackPressed() {
        int currentItem = viewPager.getCurrentItem();
        if (currentItem > 0) {
            viewPager.setCurrentItem(currentItem - 1);
        } else {
            super.onBackPressed();
        }
    }
    
    // Interface para callback de retry
    private interface RetryCallback {
        void retry();
    }
    
    // Mostrar diálogo para tentar novamente
    private void showRetryDialog(String title, String message, RetryCallback callback) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Tentar novamente", (dialog, which) -> {
                dialog.dismiss();
                showLoading(true);
                callback.retry();
            })
            .setNegativeButton("Cancelar", (dialog, which) -> {
                dialog.dismiss();
            })
            .setCancelable(false)
            .show();
    }
    
    // Mostrar diálogo de sucesso
    private void showSuccessDialog(String title, String message) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK", (dialog, which) -> {
                dialog.dismiss();
                // Fechar activity e voltar para login
                finish();
            })
            .setCancelable(false)
            .show();
    }
}
