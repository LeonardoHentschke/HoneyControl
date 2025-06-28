package com.honeycontrol.utils;

import com.honeycontrol.UserSession;
import com.honeycontrol.models.User;

/**
 * Classe utilitária com métodos convenientes para acessar dados do usuário logado.
 * Esta classe serve como exemplo de como usar o UserSession em toda a aplicação.
 */
public class SessionUtils {
    
    /**
     * Verifica se há um usuário logado
     */
    public static boolean isUserLoggedIn() {
        return UserSession.getInstance().isUserLoggedIn();
    }
    
    /**
     * Obtém o usuário atual da sessão
     */
    public static User getCurrentUser() {
        return UserSession.getInstance().getCurrentUser();
    }
    
    /**
     * Obtém o company_id do usuário logado de forma segura
     */
    public static String getCurrentUserCompanyId() {
        return UserSession.getInstance().getCurrentUserCompanyId();
    }
    
    /**
     * Obtém o ID do usuário logado de forma segura
     */
    public static String getCurrentUserId() {
        return UserSession.getInstance().getCurrentUserId();
    }
    
    /**
     * Obtém o nome do usuário logado de forma segura
     */
    public static String getCurrentUserName() {
        return UserSession.getInstance().getCurrentUserName();
    }
    
    /**
     * Obtém o email do usuário logado de forma segura
     */
    public static String getCurrentUserEmail() {
        return UserSession.getInstance().getCurrentUserEmail();
    }
    
    /**
     * Converte o company_id do usuário para long de forma segura
     * @return company_id como long ou -1 se não for possível converter
     */
    public static long getCurrentUserCompanyIdAsLong() {
        String companyId = getCurrentUserCompanyId();
        if (companyId != null && !companyId.isEmpty()) {
            try {
                return Long.parseLong(companyId);
            } catch (NumberFormatException e) {
                return -1;
            }
        }
        return -1;
    }
    
    /**
     * Verifica se o usuário pertence a uma empresa específica
     */
    public static boolean isUserFromCompany(String companyId) {
        String userCompanyId = getCurrentUserCompanyId();
        return userCompanyId != null && userCompanyId.equals(companyId);
    }
    
    /**
     * Verifica se o usuário é o mesmo que o ID fornecido
     */
    public static boolean isCurrentUser(String userId) {
        String currentUserId = getCurrentUserId();
        return currentUserId != null && currentUserId.equals(userId);
    }
}
