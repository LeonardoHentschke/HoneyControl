package com.honeycontrol.requests;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class StockLogCreateRequest {
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

    public StockLogCreateRequest(String stockId, Integer quantity, String type, String reason) {
        this.stockId = stockId;
        this.quantity = quantity;
        this.type = type;
        this.reason = reason;
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

    @Override
    public String toString() {
        return "StockLogCreateRequest{" +
                "stockId='" + stockId + '\'' +
                ", quantity=" + quantity +
                ", type='" + type + '\'' +
                ", reason='" + reason + '\'' +
                '}';
    }
}
