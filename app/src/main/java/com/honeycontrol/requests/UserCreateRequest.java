package com.honeycontrol.requests;

import com.google.gson.annotations.SerializedName;
import com.google.gson.annotations.Expose;

public class UserCreateRequest {
    @SerializedName("name")
    @Expose
    private String name;
    
    @SerializedName("email")
    @Expose
    private String email;
    
    @SerializedName("password_hash")
    @Expose
    private String password_hash;
    
    @SerializedName("company_id")
    @Expose
    private String company_id;

    public UserCreateRequest(String name, String email, String password_hash, String company_id) {
        this.name = name;
        this.email = email;
        this.password_hash = password_hash;
        this.company_id = company_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword_hash() {
        return password_hash;
    }

    public void setPassword_hash(String password_hash) {
        this.password_hash = password_hash;
    }

    public String getCompany_id() {
        return company_id;
    }

    public void setCompany_id(String company_id) {
        this.company_id = company_id;
    }
}
