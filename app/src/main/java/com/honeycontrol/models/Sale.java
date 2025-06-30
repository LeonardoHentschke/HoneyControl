package com.honeycontrol.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.time.LocalDateTime;
import java.util.List;

public class Sale {
    @SerializedName("id")
    @Expose
    private String id;
    
    @SerializedName("company_id")
    @Expose
    private String companyId;
    
    @SerializedName("customer_id")
    @Expose
    private String customerId;
    
    @SerializedName("user_id")
    @Expose
    private String userId;
    
    @SerializedName("total")
    @Expose
    private Float total;
    
    @SerializedName("created_at")
    @Expose
    private LocalDateTime createdAt;
    
    @SerializedName("updated_at")
    @Expose
    private LocalDateTime updatedAt;
    
    @SerializedName("sale_items")
    @Expose
    private List<SaleItem> saleItems;
    
    @SerializedName("customers")
    @Expose
    private Customer customer;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Float getTotal() {
        return total;
    }

    public void setTotal(Float total) {
        this.total = total;
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

    public List<SaleItem> getSaleItems() {
        return saleItems;
    }

    public void setSaleItems(List<SaleItem> saleItems) {
        this.saleItems = saleItems;
    }
    
    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
}
