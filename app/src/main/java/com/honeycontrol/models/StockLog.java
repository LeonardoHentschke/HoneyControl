package com.honeycontrol.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.time.LocalDateTime;

public class StockLog {
    @SerializedName("id")
    @Expose
    private String id;

    @SerializedName("stock_id")
    @Expose
    private String stockId;

    @SerializedName("quantity")
    @Expose
    private Integer quantity;

    @SerializedName("type")
    @Expose
    private String type;

    @SerializedName("reason")
    @Expose
    private String reason;

    @SerializedName("created_at")
    @Expose
    private LocalDateTime createdAt;

    @SerializedName("updatedAt")
    @Expose
    private LocalDateTime updatedAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStockId() {
        return stockId;
    }

    public void setStockId(String stockId) {
        this.stockId = stockId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
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
}
