package com.honeycontrol;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.button.MaterialButton;
import com.honeycontrol.models.Cost;
import com.honeycontrol.models.Product;
import com.honeycontrol.models.Sale;
import com.honeycontrol.models.User;
import com.honeycontrol.services.ApiCallback;
import com.honeycontrol.services.SupabaseApi;
import com.honeycontrol.services.SupabaseClient;
import com.honeycontrol.utils.SessionUtils;
import com.honeycontrol.utils.UserSession;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class DashboardActivity extends BaseActivity {
    
    private static final String TAG = "DashboardActivity";
    
    private MaterialButton logoutButton;
    private CardView customersCard, productsCard, costsCard, salesCard;
    private TextView salesSummary, costsSummary, stockSummary;
    private LineChart salesChart;
    private PieChart costsChart;
    private BarChart stockChart;
    
    private SupabaseApi supabaseApi;
    private User currentUser;
    
    // Data for charts
    private List<Sale> sales = new ArrayList<>();
    private List<Cost> costs = new ArrayList<>();
    private List<Product> products = new ArrayList<>();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        
        // Verificar se o usuário está logado
        if (!SessionUtils.isUserLoggedIn()) {
            Log.w(TAG, "Usuário não está logado, redirecionando para login");
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }
        
        initViews();
        setupClickListeners();
        
        // Inicializar API
        supabaseApi = SupabaseClient.createService(SupabaseApi.class);
        if (supabaseApi == null) {
            Log.e(TAG, "Falha ao criar instância da API");
            Toast.makeText(this, "Erro de configuração da API", Toast.LENGTH_LONG).show();
            return;
        }
        
        loadDashboardData();
    }
    
    private void initViews() {
        logoutButton = findViewById(R.id.logoutButton);
        customersCard = findViewById(R.id.customersCard);
        productsCard = findViewById(R.id.productsCard);
        costsCard = findViewById(R.id.costsCard);
        salesCard = findViewById(R.id.salesCard);
        
        salesSummary = findViewById(R.id.salesSummary);
        costsSummary = findViewById(R.id.costsSummary);
        stockSummary = findViewById(R.id.stockSummary);
        
        salesChart = findViewById(R.id.salesChart);
        costsChart = findViewById(R.id.costsChart);
        stockChart = findViewById(R.id.stockChart);
        
        setupCharts();
    }
    
    private void setupCharts() {
        // Configurar gráfico de vendas
        setupSalesChart();
        
        // Configurar gráfico de custos
        setupCostsChart();
        
        // Configurar gráfico de estoque
        setupStockChart();
    }
    
    private void setupSalesChart() {
        Description desc = new Description();
        desc.setText("");
        salesChart.setDescription(desc);
        salesChart.setTouchEnabled(true);
        salesChart.setDragEnabled(true);
        salesChart.setScaleEnabled(true);
        salesChart.getAxisRight().setEnabled(false);
        
        // Cores do tema escuro
        salesChart.setBackgroundColor(Color.TRANSPARENT);
        salesChart.getLegend().setTextColor(ContextCompat.getColor(this, R.color.neutral_50));
        
        XAxis xAxis = salesChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(ContextCompat.getColor(this, R.color.neutral_400));
        xAxis.setDrawGridLines(false);
        
        YAxis leftAxis = salesChart.getAxisLeft();
        leftAxis.setTextColor(ContextCompat.getColor(this, R.color.neutral_400));
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(ContextCompat.getColor(this, R.color.neutral_700));
    }
    
    private void setupCostsChart() {
        Description desc = new Description();
        desc.setText("");
        costsChart.setDescription(desc);
        costsChart.setUsePercentValues(true);
        costsChart.setBackgroundColor(Color.TRANSPARENT);
        costsChart.getLegend().setTextColor(ContextCompat.getColor(this, R.color.neutral_50));
        costsChart.setEntryLabelColor(ContextCompat.getColor(this, R.color.neutral_900));
        costsChart.setHoleColor(ContextCompat.getColor(this, R.color.neutral_800));
    }
    
    private void setupStockChart() {
        Description desc = new Description();
        desc.setText("");
        stockChart.setDescription(desc);
        stockChart.setTouchEnabled(true);
        stockChart.setDragEnabled(true);
        stockChart.setScaleEnabled(true);
        stockChart.getAxisRight().setEnabled(false);
        
        // Cores do tema escuro
        stockChart.setBackgroundColor(Color.TRANSPARENT);
        stockChart.getLegend().setTextColor(ContextCompat.getColor(this, R.color.neutral_50));
        
        XAxis xAxis = stockChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(ContextCompat.getColor(this, R.color.neutral_400));
        xAxis.setDrawGridLines(false);
        xAxis.setLabelRotationAngle(-45f);
        
        YAxis leftAxis = stockChart.getAxisLeft();
        leftAxis.setTextColor(ContextCompat.getColor(this, R.color.neutral_400));
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(ContextCompat.getColor(this, R.color.neutral_700));
        leftAxis.setAxisMinimum(0f);
    }
    
    private void loadDashboardData() {
        String companyId = SessionUtils.getCurrentUserCompanyId();
        if (companyId == null || companyId.isEmpty()) {
            Toast.makeText(this, "Erro: Company ID não encontrado", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Carregar dados em paralelo
        loadSales(companyId);
        loadCosts(companyId);
        loadProducts(companyId);
    }
    
    private void loadSales(String companyId) {
        supabaseApi.getSalesByCompany(companyId).enqueue(new ApiCallback<List<Sale>>() {
            @Override
            public void onSuccess(List<Sale> saleList, int statusCode) {
                if (saleList != null) {
                    sales = saleList;
                    updateSalesSummary();
                    updateSalesChart();
                }
            }
            
            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Erro ao carregar vendas: " + e.getMessage());
            }
        });
    }
    
    private void loadCosts(String companyId) {
        supabaseApi.getCostsByCompany(companyId).enqueue(new ApiCallback<List<Cost>>() {
            @Override
            public void onSuccess(List<Cost> costList, int statusCode) {
                if (costList != null) {
                    costs = costList;
                    updateCostsSummary();
                    updateCostsChart();
                }
            }
            
            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Erro ao carregar custos: " + e.getMessage());
            }
        });
    }
    
    private void loadProducts(String companyId) {
        supabaseApi.getProductsWithStockByCompany(companyId).enqueue(new ApiCallback<List<Product>>() {
            @Override
            public void onSuccess(List<Product> productList, int statusCode) {
                if (productList != null) {
                    products = productList;
                    updateStockSummary();
                    updateStockChart();
                }
            }
            
            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Erro ao carregar produtos: " + e.getMessage());
            }
        });
    }
    
    private void updateSalesSummary() {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        
        // Calcular vendas de hoje
        LocalDate today = LocalDate.now();
        float todaySales = 0;
        
        for (Sale sale : sales) {
            if (sale.getCreatedAt() != null && sale.getCreatedAt().toLocalDate().equals(today)) {
                todaySales += sale.getTotal();
            }
        }
        
        salesSummary.setText("Vendas hoje: " + currencyFormat.format(todaySales));
    }
    
    private void updateCostsSummary() {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        
        // Calcular custos do mês atual
        LocalDate now = LocalDate.now();
        float monthCosts = 0;
        
        for (Cost cost : costs) {
            if (cost.getCreated_at() != null && 
                cost.getCreated_at().getYear() == now.getYear() &&
                cost.getCreated_at().getMonthValue() == now.getMonthValue()) {
                monthCosts += cost.getAmount();
            }
        }
        
        costsSummary.setText("Custos do mês: " + currencyFormat.format(monthCosts));
    }
    
    private void updateStockSummary() {
        int totalProducts = products.size();
        int totalStock = 0;
        
        for (Product product : products) {
            totalStock += product.getStockQuantity() != null ? product.getStockQuantity() : 0;
        }
        
        stockSummary.setText("Produtos: " + totalProducts + " --- Estoque total: " + totalStock);
    }
    
    private void updateSalesChart() {
        List<Entry> entries = new ArrayList<>();
        String[] labels = new String[7];
        
        // Últimos 7 dias
        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            labels[6 - i] = date.format(DateTimeFormatter.ofPattern("dd/MM"));
            
            float dailySales = 0;
            for (Sale sale : sales) {
                if (sale.getCreatedAt() != null && sale.getCreatedAt().toLocalDate().equals(date)) {
                    dailySales += sale.getTotal();
                }
            }
            entries.add(new Entry(6 - i, dailySales));
        }
        
        LineDataSet dataSet = new LineDataSet(entries, "Vendas Diárias");
        dataSet.setColor(ContextCompat.getColor(this, R.color.amber_400));
        dataSet.setCircleColor(ContextCompat.getColor(this, R.color.amber_400));
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setValueTextColor(ContextCompat.getColor(this, R.color.neutral_50));
        
        LineData lineData = new LineData(dataSet);
        salesChart.setData(lineData);
        
        // Configurar eixo X com labels
        XAxis xAxis = salesChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setGranularity(1f);
        
        salesChart.invalidate();
    }
    
    private void updateCostsChart() {
        Map<String, Float> categoryTotals = new HashMap<>();
        
        // Agrupar custos por categoria
        for (Cost cost : costs) {
            String category = cost.getCategory() != null ? cost.getCategory() : "Outros";
            categoryTotals.put(category, categoryTotals.getOrDefault(category, 0f) + cost.getAmount());
        }
        
        if (categoryTotals.isEmpty()) {
            categoryTotals.put("Nenhum custo", 1f);
        }
        
        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Float> entry : categoryTotals.entrySet()) {
            entries.add(new PieEntry(entry.getValue(), entry.getKey()));
        }
        
        PieDataSet dataSet = new PieDataSet(entries, "Categorias de Custo");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextColor(ContextCompat.getColor(this, R.color.neutral_900));
        dataSet.setValueTextSize(12f);
        
        PieData pieData = new PieData(dataSet);
        costsChart.setData(pieData);
        costsChart.invalidate();
    }
    
    private void updateStockChart() {
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        
        // Ordenar produtos por quantidade de estoque (decrescente) e pegar apenas os primeiros 10
        List<Product> sortedProducts = new ArrayList<>(products);
        sortedProducts.sort((p1, p2) -> {
            int stock1 = p1.getStockQuantity() != null ? p1.getStockQuantity() : 0;
            int stock2 = p2.getStockQuantity() != null ? p2.getStockQuantity() : 0;
            return Integer.compare(stock2, stock1);
        });
        
        int maxProducts = Math.min(10, sortedProducts.size());
        for (int i = 0; i < maxProducts; i++) {
            Product product = sortedProducts.get(i);
            int stock = product.getStockQuantity() != null ? product.getStockQuantity() : 0;
            entries.add(new BarEntry(i, stock));
            
            // Truncar nome do produto se for muito longo
            String productName = product.getName();
            if (productName.length() > 10) {
                productName = productName.substring(0, 10) + "...";
            }
            labels.add(productName);
        }
        
        if (entries.isEmpty()) {
            entries.add(new BarEntry(0, 0));
            labels.add("Nenhum produto");
        }
        
        BarDataSet dataSet = new BarDataSet(entries, "Quantidade em Estoque");
        dataSet.setColor(ContextCompat.getColor(this, R.color.amber_400));
        dataSet.setValueTextColor(ContextCompat.getColor(this, R.color.neutral_50));
        
        BarData barData = new BarData(dataSet);
        stockChart.setData(barData);
        
        // Configurar eixo X com labels
        XAxis xAxis = stockChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setGranularity(1f);
        
        stockChart.invalidate();
    }
    
    private void setupClickListeners() {
        logoutButton.setOnClickListener(v -> {
            // Limpar a sessão do usuário
            UserSession.getInstance().clearSession(this);
            
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
    
    @Override
    protected void onResume() {
        super.onResume();
        
        // Recarregar dados quando voltar para o dashboard
        if (supabaseApi != null) {
            loadDashboardData();
        }
    }
}
