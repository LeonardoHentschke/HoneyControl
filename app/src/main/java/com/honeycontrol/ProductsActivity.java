package com.honeycontrol;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.google.android.material.button.MaterialButton;
import com.honeycontrol.utils.SessionUtils;

public class ProductsActivity extends BaseActivity {
    
    private static final String TAG = "ProductsActivity";
    private MaterialButton backToDashboardButton;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Verificar se o usuário está logado
        if (!SessionUtils.isUserLoggedIn()) {
            Log.w(TAG, "User not logged in, redirecting to login");
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }
        
        setContentView(R.layout.activity_products);
        
        Log.d(TAG, "ProductsActivity initialized for company: " + SessionUtils.getCurrentUserCompanyId());
        
        initViews();
        setupClickListeners();
    }
    
    private void initViews() {
        backToDashboardButton = findViewById(R.id.backToDashboardButton);
    }
    
    private void setupClickListeners() {
        backToDashboardButton.setOnClickListener(v -> {
            // Voltar para o Dashboard
            Intent intent = new Intent(this, DashboardActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
    }
}
