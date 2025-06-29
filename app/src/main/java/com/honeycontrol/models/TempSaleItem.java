package com.honeycontrol.models;

public class TempSaleItem {
    private Product product;
    private Integer quantity;
    private Float unitPrice;
    private Float discount;
    private Float subtotal;

    public TempSaleItem(Product product, Integer quantity, Float unitPrice, Float discount) {
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.discount = discount != null ? discount : 0.0f;
        this.subtotal = calculateSubtotal();
    }

    public TempSaleItem(Product product, Integer quantity, Float unitPrice) {
        this(product, quantity, unitPrice, 0.0f);
    }

    private Float calculateSubtotal() {
        Float total = (unitPrice * quantity) - discount;
        return Math.max(0.0f, total); // Não permitir subtotal negativo
    }

    // Getters and Setters
    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
        this.subtotal = calculateSubtotal();
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
        this.subtotal = calculateSubtotal();
    }

    public Float getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(Float unitPrice) {
        this.unitPrice = unitPrice;
        this.subtotal = calculateSubtotal();
    }

    public Float getDiscount() {
        return discount;
    }

    public void setDiscount(Float discount) {
        this.discount = discount != null ? discount : 0.0f;
        this.subtotal = calculateSubtotal();
    }

    public Float getSubtotal() {
        return subtotal;
    }

    // Método para verificar se tem desconto
    public boolean hasDiscount() {
        return discount != null && discount > 0;
    }
}
