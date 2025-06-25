package com.honeycontrol;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {
    
    private MaterialButton loginTabButton, signupTabButton, actionButton;
    private TextInputLayout nameLayout, emailLayout, passwordLayout, confirmPasswordLayout;
    private TextInputEditText nameInput, emailInput, passwordInput, confirmPasswordInput;
    private TextView forgotPasswordLink;
    
    private boolean isLoginMode = true;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        
        initViews();
        setupClickListeners();
        updateUI();
    }
    
    private void initViews() {
        // Botões de alternância
        loginTabButton = findViewById(R.id.loginTabButton);
        signupTabButton = findViewById(R.id.signupTabButton);
        actionButton = findViewById(R.id.actionButton);
        
        // Layouts dos campos
        nameLayout = findViewById(R.id.nameLayout);
        emailLayout = findViewById(R.id.emailLayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        confirmPasswordLayout = findViewById(R.id.confirmPasswordLayout);
        
        // Campos de entrada
        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        
        // Link "Esqueci minha senha"
        forgotPasswordLink = findViewById(R.id.forgotPasswordLink);
    }
    
    private void setupClickListeners() {
        loginTabButton.setOnClickListener(v -> {
            if (!isLoginMode) {
                isLoginMode = true;
                updateUI();
                clearInputs();
            }
        });
        
        signupTabButton.setOnClickListener(v -> {
            if (isLoginMode) {
                isLoginMode = false;
                updateUI();
                clearInputs();
            }
        });
        
        actionButton.setOnClickListener(v -> {
            if (isLoginMode) {
                performLogin();
            } else {
                performSignup();
            }
        });
        
        forgotPasswordLink.setOnClickListener(v -> {
            // TODO: Implementar funcionalidade de recuperação de senha
            Toast.makeText(this, "Funcionalidade de recuperação de senha em desenvolvimento", Toast.LENGTH_SHORT).show();
        });
    }
    
    private void updateUI() {
        if (isLoginMode) {
            // Configurar modo login
            loginTabButton.setBackgroundTintList(getColorStateList(R.color.amber_400));
            loginTabButton.setTextColor(getColor(R.color.neutral_900));
            
            signupTabButton.setBackgroundTintList(getColorStateList(android.R.color.transparent));
            signupTabButton.setTextColor(getColor(R.color.neutral_400));
            
            nameLayout.setVisibility(View.GONE);
            confirmPasswordLayout.setVisibility(View.GONE);
            forgotPasswordLink.setVisibility(View.VISIBLE);
            
            actionButton.setText("Entrar");
            
        } else {
            // Configurar modo cadastro
            signupTabButton.setBackgroundTintList(getColorStateList(R.color.amber_400));
            signupTabButton.setTextColor(getColor(R.color.neutral_900));
            
            loginTabButton.setBackgroundTintList(getColorStateList(android.R.color.transparent));
            loginTabButton.setTextColor(getColor(R.color.neutral_400));
            
            nameLayout.setVisibility(View.VISIBLE);
            confirmPasswordLayout.setVisibility(View.VISIBLE);
            forgotPasswordLink.setVisibility(View.GONE);
            
            actionButton.setText("Criar Conta");
        }
    }
    
    private void clearInputs() {
        nameInput.setText("");
        emailInput.setText("");
        passwordInput.setText("");
        confirmPasswordInput.setText("");
        
        // Limpar erros
        nameLayout.setError(null);
        emailLayout.setError(null);
        passwordLayout.setError(null);
        confirmPasswordLayout.setError(null);
    }
    
    private void performLogin() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        
        // Validar campos
        if (!validateLoginFields(email, password)) {
            return;
        }
        
        // TODO: Implementar lógica de login com Supabase
        // Por enquanto, simular login bem-sucedido
        Toast.makeText(this, "Login realizado com sucesso!", Toast.LENGTH_SHORT).show();
        
        // Navegar para MainActivity
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
    
    private void performSignup() {
        String name = nameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();
        
        // Validar campos
        if (!validateSignupFields(name, email, password, confirmPassword)) {
            return;
        }
        
        // TODO: Implementar lógica de cadastro com Supabase
        // Por enquanto, simular cadastro bem-sucedido
        Toast.makeText(this, "Conta criada com sucesso!", Toast.LENGTH_SHORT).show();
        
        // Navegar para MainActivity
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
    
    private boolean validateLoginFields(String email, String password) {
        boolean isValid = true;
        
        // Validar email
        if (email.isEmpty()) {
            emailLayout.setError("Email é obrigatório");
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError("Email inválido");
            isValid = false;
        } else {
            emailLayout.setError(null);
        }
        
        // Validar senha
        if (password.isEmpty()) {
            passwordLayout.setError("Senha é obrigatória");
            isValid = false;
        } else if (password.length() < 6) {
            passwordLayout.setError("Senha deve ter pelo menos 6 caracteres");
            isValid = false;
        } else {
            passwordLayout.setError(null);
        }
        
        return isValid;
    }
    
    private boolean validateSignupFields(String name, String email, String password, String confirmPassword) {
        boolean isValid = true;
        
        // Validar nome
        if (name.isEmpty()) {
            nameLayout.setError("Nome é obrigatório");
            isValid = false;
        } else if (name.length() < 2) {
            nameLayout.setError("Nome deve ter pelo menos 2 caracteres");
            isValid = false;
        } else {
            nameLayout.setError(null);
        }
        
        // Validar email
        if (email.isEmpty()) {
            emailLayout.setError("Email é obrigatório");
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError("Email inválido");
            isValid = false;
        } else {
            emailLayout.setError(null);
        }
        
        // Validar senha
        if (password.isEmpty()) {
            passwordLayout.setError("Senha é obrigatória");
            isValid = false;
        } else if (password.length() < 6) {
            passwordLayout.setError("Senha deve ter pelo menos 6 caracteres");
            isValid = false;
        } else {
            passwordLayout.setError(null);
        }
        
        // Validar confirmação de senha
        if (confirmPassword.isEmpty()) {
            confirmPasswordLayout.setError("Confirmação de senha é obrigatória");
            isValid = false;
        } else if (!password.equals(confirmPassword)) {
            confirmPasswordLayout.setError("Senhas não coincidem");
            isValid = false;
        } else {
            confirmPasswordLayout.setError(null);
        }
        
        return isValid;
    }
}
