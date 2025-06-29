package com.honeycontrol.requests;

import com.google.gson.annotations.SerializedName;
import com.google.gson.annotations.Expose;

public class SaleItemCreateRequest {
    @SerializedName("sale_id")
    @Expose
    private String saleId;
    
    @SerializedName("product_id")
    @Expose
    private String productId;
    
    @SerializedName("quantity")
    @Expose
    private Integer quantity;
    
    @SerializedName("unit_price")
    @Expose
    private Float unitPrice;
    
    @SerializedName("subtotal")
    @Expose
    private Float subtotal;
    
    @SerializedName("discount")
    @Expose
    private Float discount;

    public SaleItemCreateRequest(String saleId, String productId, Integer quantity, Float unitPrice, Float subtotal, Float discount) {
        this.saleId = saleId;
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subtotal = subtotal;
        this.discount = discount;
    }

    public String getSaleId() {
        return saleId;
    }

    public void setSaleId(String saleId) {
        this.saleId = saleId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Float getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(Float unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Float getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(Float subtotal) {
        this.subtotal = subtotal;
    }

    public Float getDiscount() {
        return discount;
    }

    public void setDiscount(Float discount) {
        this.discount = discount;
    }
}
