package com.honeycontrol;

import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.button.MaterialButton;

public class CustomersActivity extends BaseActivity {
    
    private MaterialButton backToDashboardButton;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customers);
        
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
