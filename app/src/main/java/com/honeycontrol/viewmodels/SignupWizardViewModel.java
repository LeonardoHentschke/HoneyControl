package com.honeycontrol.viewmodels;

import android.os.Build;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.time.LocalDateTime;

public class SignupWizardViewModel extends ViewModel {
    
    // Dados da empresa (Etapa 1)
    private final MutableLiveData<String> companyName = new MutableLiveData<>();
    
    // Dados do usuário (Etapa 2)
    private final MutableLiveData<String> userName = new MutableLiveData<>();
    private final MutableLiveData<String> userEmail = new MutableLiveData<>();
    private final MutableLiveData<String> userPassword = new MutableLiveData<>();
    
    // Estado de navegação
    private final MutableLiveData<Integer> currentStep = new MutableLiveData<>(0);
    private final MutableLiveData<Boolean> isCompanyStepValid = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isUserStepValid = new MutableLiveData<>(false);
    
    // Getters para LiveData (apenas leitura)
    public LiveData<String> getCompanyName() {
        return companyName;
    }
    
    public LiveData<String> getUserName() {
        return userName;
    }
    
    public LiveData<String> getUserEmail() {
        return userEmail;
    }
    
    public LiveData<String> getUserPassword() {
        return userPassword;
    }
    
    public LiveData<Integer> getCurrentStep() {
        return currentStep;
    }
    
    public LiveData<Boolean> getIsCompanyStepValid() {
        return isCompanyStepValid;
    }
    
    public LiveData<Boolean> getIsUserStepValid() {
        return isUserStepValid;
    }
    
    // Setters para os dados
    public void setCompanyName(String name) {
        companyName.setValue(name);
        validateCompanyStep();
    }
    
    public void setUserName(String name) {
        userName.setValue(name);
        validateUserStep();
    }
    
    public void setUserEmail(String email) {
        userEmail.setValue(email);
        validateUserStep();
    }
    
    public void setUserPassword(String password) {
        userPassword.setValue(password);
        validateUserStep();
    }
    
    public void setCurrentStep(int step) {
        currentStep.setValue(step);
    }
    
    // Validações
    private void validateCompanyStep() {
        String name = companyName.getValue();
        boolean isValid = name != null && !name.trim().isEmpty();
        isCompanyStepValid.setValue(isValid);
    }
    
    private void validateUserStep() {
        String name = userName.getValue();
        String email = userEmail.getValue();
        String password = userPassword.getValue();
        
        boolean isNameValid = name != null && !name.trim().isEmpty();
        boolean isEmailValid = email != null && isValidEmail(email);
        boolean isPasswordValid = password != null && password.length() >= 6;
        
        boolean isValid = isNameValid && isEmailValid && isPasswordValid;
        isUserStepValid.setValue(isValid);
    }
    
    private boolean isValidEmail(String email) {
        return email.contains("@") && email.contains(".");
    }
    
    // Método para obter dados completos para envio
    public SignupData getSignupData() {
        LocalDateTime now = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            now = LocalDateTime.now();
        }

        return new SignupData(
            companyName.getValue(),
            userName.getValue(),
            userEmail.getValue(),
            userPassword.getValue(),
            now,
            now
        );
    }
    
    // Classe interna para encapsular os dados de cadastro
    public static class SignupData {
        public final String companyName;
        public final String userName;
        public final String userEmail;
        public final String userPassword;
        public final LocalDateTime createdAt;
        public final LocalDateTime updatedAt;
        
        public SignupData(
            String companyName,
            String userName,
            String userEmail,
            String userPassword,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
        ) {
            this.companyName = companyName;
            this.userName = userName;
            this.userEmail = userEmail;
            this.userPassword = userPassword;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
        }
    }
}
