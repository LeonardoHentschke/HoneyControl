package com.honeycontrol.models;

import java.time.LocalDateTime;
import java.util.List;

public class Stock {
    private String id;
    private String product_id;
    private Integer quantity;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;
    private Product product;
    private List<StockLog> stockLogs;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProduct_id() {
        return product_id;
    }

    public void setProduct_id(String product_id) {
        this.product_id = product_id;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
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
