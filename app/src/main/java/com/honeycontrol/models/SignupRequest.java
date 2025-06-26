package com.honeycontrol.models;

import java.time.LocalDateTime;

public class SignupRequest {
    // Dados da empresa
    private String companyName;
    
    // Dados do usuário
    private String userName;
    private String userEmail;
    private String userPassword; // Será convertido para password_hash no backend
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Construtores
    public SignupRequest() {}
    
    public SignupRequest(String companyName, String userName, String userEmail, 
                        String userPassword, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.companyName = companyName;
        this.userName = userName;
        this.userEmail = userEmail;
        this.userPassword = userPassword;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    // Getters e Setters
    public String getCompanyName() {
        return companyName;
    }
    
    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    public String getUserEmail() {
        return userEmail;
    }
    
    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
    
    public String getUserPassword() {
        return userPassword;
    }
    
    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @Override
    public String toString() {
        return "SignupRequest{" +
                "companyName='" + companyName + '\'' +
                ", userName='" + userName + '\'' +
                ", userEmail='" + userEmail + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
