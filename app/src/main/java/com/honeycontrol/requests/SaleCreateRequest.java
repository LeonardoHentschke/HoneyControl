package com.honeycontrol.requests;

import com.google.gson.annotations.SerializedName;
import com.google.gson.annotations.Expose;

public class SaleCreateRequest {
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

    public SaleCreateRequest(String companyId, String customerId, String userId, Float total) {
        this.companyId = companyId;
        this.customerId = customerId;
        this.userId = userId;
        this.total = total;
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
}
