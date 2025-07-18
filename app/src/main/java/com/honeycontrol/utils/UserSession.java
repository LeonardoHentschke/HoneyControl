package com.honeycontrol.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.honeycontrol.services.ApiCallback;
import com.honeycontrol.services.SupabaseApi;
import com.honeycontrol.services.SupabaseClient;
import com.honeycontrol.models.User;

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

    public static synchronized UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        this.isUserLoaded = true;
        Log.d(TAG, "Usuário definido na sessão: " + (user != null ? user.getName() : "null"));
    }

    public User getCurrentUser() {
        return currentUser;
    }

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

    public String getCurrentUserCompanyId() {
        return currentUser != null ? currentUser.getCompanyId() : null;
    }

    public String getCurrentUserName() {
        return currentUser != null ? currentUser.getName() : null;
    }

    public String getCurrentUserEmail() {
        return currentUser != null ? currentUser.getEmail() : null;
    }

    public String getCurrentUserId() {
        return currentUser != null ? currentUser.getId() : null;
    }

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

    public void saveUserEmail(Context context, String email) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(USER_EMAIL_KEY, email).apply();
        Log.d(TAG, "Email do usuário salvo nas preferências: " + email);
    }

    public void clearSession(Context context) {
        currentUser = null;
        isUserLoaded = false;
        
        // Limpar também das preferências
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
        
        Log.d(TAG, "Sessão do usuário limpa");
    }

    public interface UserLoadCallback {
        void onUserLoaded(User user);
        void onLoadFailed(String error);
    }
}
