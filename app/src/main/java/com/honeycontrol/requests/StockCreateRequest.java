package com.honeycontrol.requests;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class StockCreateRequest {
    @SerializedName("product_id")
    @Expose
    private String productId;
    
    @SerializedName("quantity")
    @Expose
    private Integer quantity;

    public StockCreateRequest(String productId, Integer quantity) {
        this.productId = productId;
        this.quantity = quantity;
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
}
