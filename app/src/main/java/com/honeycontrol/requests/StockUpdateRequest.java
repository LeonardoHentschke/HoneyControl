package com.honeycontrol.requests;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class StockUpdateRequest {
    @SerializedName("quantity")
    @Expose
    private Integer quantity;

    public StockUpdateRequest(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
