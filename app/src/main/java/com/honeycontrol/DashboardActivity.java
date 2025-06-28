package com.honeycontrol;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.android.material.button.MaterialButton;

public class DashboardActivity extends AppCompatActivity {
    
    private MaterialButton logoutButton;
    private CardView customersCard, productsCard, costsCard, salesCard;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        
        initViews();
        setupClickListeners();
    }
    
    private void initViews() {
        logoutButton = findViewById(R.id.logoutButton);
        customersCard = findViewById(R.id.customersCard);
        productsCard = findViewById(R.id.productsCard);
        costsCard = findViewById(R.id.costsCard);
        salesCard = findViewById(R.id.salesCard);
    }
    
    private void setupClickListeners() {
        logoutButton.setOnClickListener(v -> {
            // Voltar para a tela de login
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        customersCard.setOnClickListener(v -> {
            Intent intent = new Intent(this, CustomersActivity.class);
            startActivity(intent);
        });

        productsCard.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProductsActivity.class);
            startActivity(intent);
        });

        costsCard.setOnClickListener(v -> {
            Intent intent = new Intent(this, CostsActivity.class);
            startActivity(intent);
        });

        salesCard.setOnClickListener(v -> {
            Intent intent = new Intent(this, SalesActivity.class);
            startActivity(intent);
        });
    }
}
