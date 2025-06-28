package com.honeycontrol;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.honeycontrol.adapters.SignupWizardAdapter;
import com.honeycontrol.models.Companies;
import com.honeycontrol.models.CompanyCreateRequest;
import com.honeycontrol.models.User;
import com.honeycontrol.models.UserCreateRequest;
import com.honeycontrol.services.ApiService;
import com.honeycontrol.viewmodels.SignupWizardViewModel;
import com.honeycontrol.ApiCall;
import com.honeycontrol.ApiCallback;

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
        // Criar objeto CompanyCreateRequest com apenas o campo name
        CompanyCreateRequest companyRequest = new CompanyCreateRequest(data.companyName);

        // Chamar API para criar companhia
        ApiService.getInstance().getApi().createCompanies(companyRequest)
            .enqueue(new ApiCallback<>() {
                @Override
                public void onSuccess(Companies createdCompany, int code) {
                    if (code >= 200 && code < 300 && createdCompany != null) {
                        // Companhia criada com sucesso, agora criar usuário
                        android.util.Log.d("SignupWizard", "Empresa criada com ID: " + createdCompany.getId());

                        if (createdCompany.getId() == null) {
                            android.util.Log.e("SignupWizard", "ID da empresa é null. Response body: " + createdCompany);
                            showLoading(false);

                            // Verificar se quer tentar novamente
                            showRetryDialog("Erro ao obter ID da empresa",
                                    "Ocorreu um erro ao obter o ID da empresa criada. Deseja tentar novamente?",
                                    () -> createCompany(data));
                            return;
                        }

                        // Com o ID da companhia, criar usuário
                        createUser(data, createdCompany.getId());
                    } else {
                        // Erro ao criar companhia
                        android.util.Log.e("SignupWizard", "Erro ao criar empresa: " + code);
                        showLoading(false);

                        // Verificar se quer tentar novamente
                        showRetryDialog("Erro ao criar empresa",
                                "Ocorreu um erro ao criar a empresa (código " + code + "). Deseja tentar novamente?",
                                () -> createCompany(data));
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    // Falha na requisição
                    android.util.Log.e("SignupWizard", "Falha na requisição para criar empresa", e);
                    android.util.Log.e("SignupWizard", "Erro detalhado: " + e.getMessage());
                    showLoading(false);

                    // Verificar se quer tentar novamente
                    showRetryDialog("Falha na conexão",
                            "Ocorreu um problema de conexão ao criar a empresa. Deseja tentar novamente?",
                            () -> createCompany(data));
                }
            });
    }
    
    private void createUser(SignupWizardViewModel.SignupData data, String companyId) {
        // Criar objeto UserCreateRequest
        UserCreateRequest userRequest = new UserCreateRequest(
            data.userName,
            data.userEmail, 
            data.userPassword,
            companyId
        );
        
        android.util.Log.d("SignupWizard", "Criando usuário: " + userRequest.getName());
        
        // Chamar API para criar usuário
        ApiService.getInstance().getApi().createUser(userRequest)
            .enqueue(new ApiCallback<>() {
                @Override
                public void onSuccess(User createdUser, int code) {
                    showLoading(false);

                    if (code >= 200 && code < 300 && createdUser != null) {
                        // Usuário criado com sucesso
                        android.util.Log.d("SignupWizard", "Usuário criado com ID: " + createdUser.getId());

                        if (createdUser.getId() == null) {
                            android.util.Log.e("SignupWizard", "ID do usuário é null. Response body: " + createdUser);

                            // Verificar se quer tentar novamente
                            showRetryDialog("Erro ao obter ID do usuário",
                                    "Ocorreu um erro ao obter o ID do usuário criado. Deseja tentar novamente?",
                                    () -> createUser(data, companyId));
                            return;
                        }

                        String message = String.format(
                                "Conta criada com sucesso!\nEmpresa: %s\nNome: %s\nEmail: %s\nData: %s",
                                data.companyName,
                                data.userName,
                                data.userEmail,
                                data.createdAt != null ? data.createdAt.toString() : "N/A"
                        );

                        // Mostrar diálogo de sucesso
                        showSuccessDialog("Conta criada com sucesso!", message);

                        android.util.Log.d("SignupWizard", message);
                    } else {
                        // Erro ao criar usuário
                        android.util.Log.e("SignupWizard", "Erro ao criar usuário: " + code);

                        // Verificar se quer tentar novamente
                        showRetryDialog("Erro ao criar usuário",
                                "Ocorreu um erro ao criar o usuário. Deseja tentar novamente?",
                                () -> createUser(data, companyId));
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    showLoading(false);

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
