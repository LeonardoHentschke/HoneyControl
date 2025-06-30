package com.honeycontrol;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.honeycontrol.adapters.CustomerAdapter;
import com.honeycontrol.forms.CustomerFormActivity;
import com.honeycontrol.models.Customer;
import com.honeycontrol.models.User;
import com.honeycontrol.services.ApiCallback;
import com.honeycontrol.services.SupabaseApi;
import com.honeycontrol.services.SupabaseClient;
import com.honeycontrol.utils.SessionUtils;
import com.honeycontrol.utils.UserSession;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CustomersActivity extends BaseActivity implements CustomerAdapter.OnCustomerClickListener {
    
    private static final String TAG = "CustomersActivity";
    
    private MaterialButton backToDashboardButton;
    private FloatingActionButton addCustomerFab;
    private FloatingActionButton exportCsvFab;
    private RecyclerView customersRecyclerView;
    private LinearLayout emptyStateLayout;
    private ProgressBar loadingProgressBar;
    
    private CustomerAdapter customerAdapter;
    private final List<Customer> customers = new ArrayList<>();
    private SupabaseApi supabaseApi;
    private User currentUser;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Log.d(TAG, "CustomersActivity iniciada");
        
        // Verificar se o usuário está logado antes de continuar
        if (!SessionUtils.isUserLoggedIn()) {
            Log.w(TAG, "Usuário não está logado, redirecionando para login");
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }
        
        setContentView(R.layout.activity_customers);
        
        initViews();
        setupRecyclerView();
        setupClickListeners();
        
        // Inicializa API
        supabaseApi = SupabaseClient.createService(SupabaseApi.class);
        if (supabaseApi == null) {
            Log.e(TAG, "Falha ao criar instância da API");
            Toast.makeText(this, "Erro de configuração da API", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
        loadCurrentUser();
    }
    
    private void initViews() {
        backToDashboardButton = findViewById(R.id.backToDashboardButton);
        addCustomerFab = findViewById(R.id.addCustomerFab);
        exportCsvFab = findViewById(R.id.exportCsvFab);
        customersRecyclerView = findViewById(R.id.customersRecyclerView);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        loadingProgressBar = findViewById(R.id.loadingProgressBar);
    }
    
    private void setupRecyclerView() {
        customerAdapter = new CustomerAdapter(this, customers);
        customerAdapter.setOnCustomerClickListener(this);
        customersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        customersRecyclerView.setAdapter(customerAdapter);
    }
    
    private void setupClickListeners() {
        backToDashboardButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, DashboardActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
        
        addCustomerFab.setOnClickListener(v -> {
            Intent intent = new Intent(this, CustomerFormActivity.class);
            startActivity(intent);
        });
        
        exportCsvFab.setOnClickListener(v -> exportCustomersToCSV());
    }
    
    private void loadCurrentUser() {
        if (UserSession.getInstance().isUserLoggedIn()) {
            currentUser = UserSession.getInstance().getCurrentUser();
            String companyId = SessionUtils.getCurrentUserCompanyId();

            if (companyId != null && !companyId.isEmpty()) {
                loadCustomers();
            } else {
                Toast.makeText(this, "Erro: Company ID não encontrado", Toast.LENGTH_SHORT).show();
                showEmptyState();
            }
        } else {
            UserSession.getInstance().loadUserFromPreferences(this, new UserSession.UserLoadCallback() {
                @Override
                public void onUserLoaded(User user) {
                    currentUser = user;
                    if (user.getCompanyId() != null && !user.getCompanyId().isEmpty()) {
                        loadCustomers();
                    } else {
                        Log.e(TAG, "Company ID é nulo ou vazio após carregar das preferências!");
                        Toast.makeText(CustomersActivity.this, "Erro: Company ID não encontrado", Toast.LENGTH_SHORT).show();
                        showEmptyState();
                    }
                }

                @Override
                public void onLoadFailed(String error) {
                    Log.e(TAG, "Erro ao carregar usuário: " + error);
                    Toast.makeText(CustomersActivity.this, "Erro ao carregar dados do usuário", Toast.LENGTH_SHORT).show();
                    showEmptyState();
                }
            });
        }
    }
    
    private void loadCustomers() {
        String companyId = SessionUtils.getCurrentUserCompanyId();
        Log.d(TAG, "Fazendo chamada à API para buscar clientes da empresa: " + companyId);
        showLoading(true);

        supabaseApi.getCustomersByCompany(companyId).enqueue(new ApiCallback<List<Customer>>() {
            @Override
            public void onSuccess(List<Customer> customerList, int statusCode) {
                Log.d(TAG, "Resposta da API recebida com status: " + statusCode);
                showLoading(false);

                if (customerList != null && !customerList.isEmpty()) {
                    Log.d(TAG, "Clientes carregados: " + customerList.size());
                    customers.clear();
                    customers.addAll(customerList);
                    customerAdapter.notifyDataSetChanged();
                    showCustomersList();

                    for (Customer customer : customerList) {
                        Log.d(TAG, "Cliente: " + customer.getName() + " (ID: " + customer.getId() + ")");
                    }
                } else {
                    showEmptyState();
                }
            }
            
            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Falha ao carregar clientes: " + e.getMessage(), e);
                showLoading(false);
                Toast.makeText(CustomersActivity.this, "Erro ao carregar clientes: " + e.getMessage(), Toast.LENGTH_LONG).show();
                showEmptyState();
            }
        });
    }
    
    private void showLoading(boolean show) {
        loadingProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        customersRecyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
        emptyStateLayout.setVisibility(View.GONE);
    }
    
    private void showCustomersList() {
        loadingProgressBar.setVisibility(View.GONE);
        customersRecyclerView.setVisibility(View.VISIBLE);
        emptyStateLayout.setVisibility(View.GONE);
    }
    
    private void showEmptyState() {
        loadingProgressBar.setVisibility(View.GONE);
        customersRecyclerView.setVisibility(View.GONE);
        emptyStateLayout.setVisibility(View.VISIBLE);
    }
    
    @Override
    protected void onResume() {
        super.onResume();

        if (!SessionUtils.isUserLoggedIn()) {
            Log.w(TAG, "Usuário não está mais logado, redirecionando");
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        Log.d(TAG, "Recarregando clientes no onResume");
        if (currentUser != null) {
            loadCustomers();
        } else {
            Log.d(TAG, "currentUser é null, carregando usuário novamente");
            loadCurrentUser();
        }
    }

    @Override
    public void onCustomerClick(Customer customer) {
        Toast.makeText(this, "Cliente: " + customer.getName(), Toast.LENGTH_SHORT).show();
        // Abrir tela de edição ao clicar no cliente
        Intent intent = new Intent(this, CustomerFormActivity.class);
        intent.putExtra("edit_mode", true);
        intent.putExtra("customer_id", customer.getId());
        startActivity(intent);
    }
    
    @Override
    public void onCustomerEdit(Customer customer) {
        // Abrir tela de edição
        Intent intent = new Intent(this, CustomerFormActivity.class);
        intent.putExtra("edit_mode", true);
        intent.putExtra("customer_id", customer.getId());
        startActivity(intent);
    }
    
    @Override
    public void onCustomerDelete(Customer customer) {
        Toast.makeText(this, "Função de excluir em desenvolvimento", Toast.LENGTH_SHORT).show();
    }
    
    private void exportCustomersToCSV() {
        if (customers.isEmpty()) {
            Toast.makeText(this, "Não há clientes para exportar", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            // Criar o arquivo CSV no diretório de Downloads
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = "clientes_honeycontrol_" + timestamp + ".csv";
            
            // Para Android 10+ (API 29+), usar scoped storage
            File csvFile;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                // Usar o diretório de Downloads público
                csvFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);
            } else {
                // Para versões anteriores, usar o diretório externo
                csvFile = new File(Environment.getExternalStorageDirectory() + "/Download", fileName);
            }
            
            // Garantir que o diretório existe
            csvFile.getParentFile().mkdirs();
            
            FileWriter writer = new FileWriter(csvFile);
            
            // Escrever o cabeçalho do CSV
            writer.append("ID,Nome,Email,Telefone,Documento,Endereço,Cidade,Data de Criação\n");
            
            // Escrever os dados dos clientes
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            
            for (Customer customer : customers) {
                writer.append(escapeCSV(customer.getId())).append(",");
                writer.append(escapeCSV(customer.getName())).append(",");
                writer.append(escapeCSV(customer.getEmail())).append(",");
                writer.append(escapeCSV(customer.getPhone())).append(",");
                writer.append(escapeCSV(customer.getDocument() != null ? customer.getDocument() : "")).append(",");
                writer.append(escapeCSV(customer.getAddress() != null ? customer.getAddress() : "")).append(",");
                writer.append(escapeCSV(customer.getCity() != null ? customer.getCity() : "")).append(",");
                
                String createdAt = "";
                if (customer.getCreated_at() != null) {
                    createdAt = customer.getCreated_at().format(formatter);
                }
                writer.append(escapeCSV(createdAt)).append("\n");
            }
            
            writer.flush();
            writer.close();
            
            // Mostrar mensagem de sucesso
            String message = "Arquivo CSV exportado com sucesso!\n" + 
                           "Total de clientes: " + customers.size() + "\n" + 
                           "Local: Downloads/" + fileName;
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            
            Log.d(TAG, "CSV exportado: " + csvFile.getAbsolutePath());
            
            // Opcional: Abrir o arquivo ou compartilhar
            shareCSVFile(csvFile);
            
        } catch (IOException e) {
            Log.e(TAG, "Erro ao exportar CSV: " + e.getMessage());
            Toast.makeText(this, "Erro ao exportar arquivo CSV: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e(TAG, "Erro inesperado ao exportar CSV: " + e.getMessage());
            Toast.makeText(this, "Erro inesperado ao exportar arquivo: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private String escapeCSV(String value) {
        if (value == null) {
            return "";
        }
        
        // Escapar aspas duplas duplicando-as e envolver em aspas se necessário
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            value = "\"" + value.replace("\"", "\"\"") + "\"";
        }
        
        return value;
    }
    
    private void shareCSVFile(File csvFile) {
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/csv");
            
            // Usar FileProvider para Android 7.0+
            android.net.Uri csvUri = FileProvider.getUriForFile(
                this,
                "com.honeycontrol.fileprovider",
                csvFile
            );
            
            shareIntent.putExtra(Intent.EXTRA_STREAM, csvUri);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Lista de Clientes - HoneyControl");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Arquivo CSV com a lista de clientes exportada do HoneyControl.");
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            
            startActivity(Intent.createChooser(shareIntent, "Compartilhar arquivo CSV"));
        } catch (Exception e) {
            Log.e(TAG, "Erro ao compartilhar arquivo: " + e.getMessage());
            // Não mostrar erro, pois o arquivo já foi salvo com sucesso
        }
    }
}
