package com.honeycontrol;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.honeycontrol.models.User;
import com.honeycontrol.services.ApiService;

public class LoginActivity extends BaseActivity {
    
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

        // Mostrar loading
        loginButton.setEnabled(false);
        loginButton.setText("Entrando...");
        
        // Buscar usuário no banco de dados
        ApiService.getInstance().getApi().getUserByEmail(email)
            .enqueue(new ApiCallback<>() {
                @Override
                public void onSuccess(User user, int code) {
                    // Reabilitar botão
                    loginButton.setEnabled(true);
                    loginButton.setText("Entrar");

                    if (code >= 200 && code < 300 && user != null) {
                        // Usuário encontrado, verificar senha
                        if (user.getPassword_hash() != null && user.getPassword_hash().equals(password)) {
                            // Login bem-sucedido
                            Toast.makeText(LoginActivity.this, "Login realizado com sucesso!", Toast.LENGTH_SHORT).show();

                            // Salvar usuário na sessão global
                            UserSession.getInstance().setCurrentUser(user);
                            UserSession.getInstance().saveUserEmail(LoginActivity.this, email);

                            // Navegar para DashboardActivity
                            Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            // Senha incorreta
                            Toast.makeText(LoginActivity.this, "Senha incorreta!", Toast.LENGTH_SHORT).show();
                            passwordLayout.setError("Senha incorreta");
                            emailLayout.setError(null);
                        }
                    } else {
                        // Usuário não encontrado
                        Toast.makeText(LoginActivity.this, "Usuário não encontrado!", Toast.LENGTH_SHORT).show();
                        emailLayout.setError("Email não encontrado");
                        passwordLayout.setError(null);
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    // Reabilitar botão
                    loginButton.setEnabled(true);
                    loginButton.setText("Entrar");

                    // Erro na requisição
                    Toast.makeText(LoginActivity.this, "Erro na conexão. Tente novamente.", Toast.LENGTH_SHORT).show();
                    android.util.Log.e("LoginActivity", "Erro ao buscar usuário: " + e.getMessage());
                }
            });
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
