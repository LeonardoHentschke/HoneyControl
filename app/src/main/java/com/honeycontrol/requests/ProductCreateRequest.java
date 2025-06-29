package com.honeycontrol.requests;

import com.google.gson.annotations.SerializedName;
import com.google.gson.annotations.Expose;

public class ProductCreateRequest {
    @SerializedName("name")
    @Expose
    private String name;
    
    @SerializedName("description")
    @Expose
    private String description;
    
    @SerializedName("unit_price")
    @Expose
    private Float unitPrice;
    
    @SerializedName("unit")
    @Expose
    private String unit;
    
    @SerializedName("company_id")
    @Expose
    private String companyId;

    public ProductCreateRequest(String name, String description, Float unitPrice, String unit, String companyId) {
        this.name = name;
        this.description = description;
        this.unitPrice = unitPrice;
        this.unit = unit;
        this.companyId = companyId;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Float getUnitPrice() { return unitPrice; }
    public void setUnitPrice(Float unitPrice) { this.unitPrice = unitPrice; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getCompanyId() { return companyId; }
    public void setCompanyId(String companyId) { this.companyId = companyId; }
}
