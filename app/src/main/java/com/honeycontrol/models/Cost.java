package com.honeycontrol.models;

import com.google.gson.annotations.SerializedName;
import com.google.gson.annotations.Expose;
import java.time.LocalDateTime;

public class Cost {
    @SerializedName("id")
    @Expose
    private String id;
    
    @SerializedName("name")
    @Expose
    private String name;
    
    @SerializedName("description")
    @Expose
    private String description;
    
    @SerializedName("category")
    @Expose
    private String category;
    
    @SerializedName("amount")
    @Expose
    private Float amount;
    
    @SerializedName("company_id")
    @Expose
    private String company_id;
    
    @SerializedName("user_id")
    @Expose
    private String user_id;
    
    @SerializedName("created_at")
    @Expose
    private LocalDateTime created_at;
    
    @SerializedName("updated_at")
    @Expose
    private LocalDateTime updated_at;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Float getAmount() {
        return amount;
    }

    public void setAmount(Float amount) {
        this.amount = amount;
    }

    public String getCompany_id() {
        return company_id;
    }

    public void setCompany_id(String company_id) {
        this.company_id = company_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public LocalDateTime getCreated_at() {
        return created_at;
    }

    public void setCreated_at(LocalDateTime created_at) {
        this.created_at = created_at;
    }

    public LocalDateTime getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(LocalDateTime updated_at) {
        this.updated_at = updated_at;
    }
}
