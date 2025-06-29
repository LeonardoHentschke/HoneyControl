package com.honeycontrol.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.time.LocalDateTime;

public class SaleItem {
    @SerializedName("id")
    @Expose
    private String id;
    
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
    
    @SerializedName("created_at")
    @Expose
    private LocalDateTime createdAt;
    
    @SerializedName("updated_at")
    @Expose
    private LocalDateTime updatedAt;
    
    @SerializedName("product")
    @Expose
    private Product product;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }
}
