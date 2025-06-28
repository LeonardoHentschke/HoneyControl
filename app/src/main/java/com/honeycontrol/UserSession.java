package com.honeycontrol;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.honeycontrol.models.User;

/**
 * Singleton para gerenciar a sessão do usuário logado na aplicação.
 * Armazena os dados do usuário em memória para evitar consultas desnecessárias ao banco.
 */
public class UserSession {
    
    private static final String TAG = "UserSession";
    private static final String PREFS_NAME = "HoneyControlPrefs";
    private static final String USER_EMAIL_KEY = "user_email";
    
    private static UserSession instance;
    private User currentUser;
    private boolean isUserLoaded = false;
    
    private UserSession() {
        // Construtor privado para implementar Singleton
    }
    
    /**
     * Obtém a instância única da sessão do usuário
     */
    public static synchronized UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }
    
    /**
     * Define o usuário logado na sessão
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
        this.isUserLoaded = true;
        Log.d(TAG, "Usuário definido na sessão: " + (user != null ? user.getName() : "null"));
    }
    
    /**
     * Obtém o usuário atual da sessão
     */
    public User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Verifica se há um usuário logado
     */
    public boolean isUserLoggedIn() {
        boolean hasUser = currentUser != null;
        boolean isLoaded = isUserLoaded;
        Log.d(TAG, "isUserLoggedIn() - hasUser: " + hasUser + ", isLoaded: " + isLoaded);
        if (currentUser != null) {
            Log.d(TAG, "  - User name: " + currentUser.getName());
            Log.d(TAG, "  - User company_id: " + currentUser.getCompanyId());
        }
        return hasUser && isLoaded;
    }
    
    /**
     * Obtém o company_id do usuário logado
     */
    public String getCurrentUserCompanyId() {
        return currentUser != null ? currentUser.getCompanyId() : null;
    }
    
    /**
     * Obtém o nome do usuário logado
     */
    public String getCurrentUserName() {
        return currentUser != null ? currentUser.getName() : null;
    }
    
    /**
     * Obtém o email do usuário logado
     */
    public String getCurrentUserEmail() {
        return currentUser != null ? currentUser.getEmail() : null;
    }
    
    /**
     * Obtém o ID do usuário logado
     */
    public String getCurrentUserId() {
        return currentUser != null ? currentUser.getId() : null;
    }
    
    /**
     * Carrega o usuário da sessão usando o email armazenado nas preferências
     */
    public void loadUserFromPreferences(Context context, UserLoadCallback callback) {
        Log.d(TAG, "loadUserFromPreferences() iniciado");
        
        if (isUserLoaded && currentUser != null) {
            // Usuário já carregado, retornar imediatamente
            Log.d(TAG, "Usuário já carregado em memória, retornando: " + currentUser.getName());
            callback.onUserLoaded(currentUser);
            return;
        }
        
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String userEmail = prefs.getString(USER_EMAIL_KEY, "");
        
        Log.d(TAG, "Email recuperado das preferências: '" + userEmail + "'");
        
        if (userEmail.isEmpty()) {
            Log.e(TAG, "Email do usuário não encontrado nas preferências");
            callback.onLoadFailed("Email não encontrado");
            return;
        }
        
        Log.d(TAG, "Buscando usuário pelo email na API...");
        
        // Buscar usuário pelo email
        SupabaseApi supabaseApi = SupabaseClient.createService(SupabaseApi.class);
        if (supabaseApi == null) {
            Log.e(TAG, "Falha ao criar instância da API");
            callback.onLoadFailed("Erro na API");
            return;
        }
        
        supabaseApi.getUserByEmail(userEmail).enqueue(new ApiCallback<User>() {
            @Override
            public void onSuccess(User user, int statusCode) {
                Log.d(TAG, "Resposta da API recebida - Status: " + statusCode);
                if (user != null) {
                    Log.d(TAG, "Usuário encontrado: " + user.getName() + " (Company: " + user.getCompanyId() + ")");
                    setCurrentUser(user);
                    callback.onUserLoaded(user);
                } else {
                    Log.e(TAG, "Usuário é null na resposta da API para o email: " + userEmail);
                    callback.onLoadFailed("Usuário não encontrado");
                }
            }
            
            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Erro ao carregar usuário da API: " + e.getMessage(), e);
                callback.onLoadFailed("Erro ao carregar usuário: " + e.getMessage());
            }
        });
    }
    
    /**
     * Salva o email do usuário nas preferências (usado no login)
     */
    public void saveUserEmail(Context context, String email) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(USER_EMAIL_KEY, email).apply();
        Log.d(TAG, "Email do usuário salvo nas preferências: " + email);
    }
    
    /**
     * Limpa a sessão do usuário (usado no logout)
     */
    public void clearSession(Context context) {
        currentUser = null;
        isUserLoaded = false;
        
        // Limpar também das preferências
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
        
        Log.d(TAG, "Sessão do usuário limpa");
    }
    
    /**
     * Interface para callback de carregamento do usuário
     */
    public interface UserLoadCallback {
        void onUserLoaded(User user);
        void onLoadFailed(String error);
    }
}
