package com.honeycontrol.models;

import com.google.gson.annotations.SerializedName;
import com.google.gson.annotations.Expose;
import java.time.LocalDateTime;
import java.util.List;

public class Stock {
    @SerializedName("id")
    private transient String id;
    
    @SerializedName("product_id")
    @Expose
    private String productId;
    
    @SerializedName("quantity")
    @Expose
    private Integer quantity;
    
    @SerializedName("created_at")
    private transient LocalDateTime createdAt;
    
    @SerializedName("updated_at")
    private transient LocalDateTime updatedAt;
    
    private Product product;
    private List<StockLog> stockLogs;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public List<StockLog> getStockLogs() {
        return stockLogs;
    }

    public void setStockLogs(List<StockLog> stockLogs) {
        this.stockLogs = stockLogs;
    }
}
