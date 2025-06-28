package com.honeycontrol;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {
    
    private MaterialButton loginButton;
    private TextInputLayout emailLayout, passwordLayout;
    private TextInputEditText emailInput, passwordInput;
    private TextView createAccountLink;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        
        initViews();
        setupClickListeners();
    }
    
    private void initViews() {
        // Botão de login
        loginButton = findViewById(R.id.loginButton);
        
        // Layouts dos campos
        emailLayout = findViewById(R.id.emailLayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        
        // Campos de entrada
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);

        // Links
        createAccountLink = findViewById(R.id.createAccountLink);
    }
    
    private void setupClickListeners() {
        loginButton.setOnClickListener(v -> performLogin());
        
        createAccountLink.setOnClickListener(v -> {
            // Navegar para o wizard de cadastro
            Intent intent = new Intent(this, SignupWizardActivity.class);
            startActivity(intent);
        });
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
//        Intent intent = new Intent(this, MainActivity.class);
//        startActivity(intent);
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
}
