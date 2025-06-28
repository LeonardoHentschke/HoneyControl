package com.honeycontrol.requests;

import com.google.gson.annotations.SerializedName;
import com.google.gson.annotations.Expose;

public class CostCreateRequest {
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
    private String companyId;
    
    @SerializedName("user_id")
    @Expose
    private String userId;

    public CostCreateRequest(String name, String description, String category, Float amount, String companyId, String userId) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.amount = amount;
        this.companyId = companyId;
        this.userId = userId;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Float getAmount() { return amount; }
    public void setAmount(Float amount) { this.amount = amount; }

    public String getCompanyId() { return companyId; }
    public void setCompanyId(String companyId) { this.companyId = companyId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}
