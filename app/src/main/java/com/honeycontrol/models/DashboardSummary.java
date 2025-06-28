package com.honeycontrol.models;

import java.util.List;
import java.util.Map;

public class DashboardSummary {
    private Float totalSales;
    private Float totalCosts;
    private Float profit;
    private Integer totalProducts;
    private Integer totalCustomers;
    private Integer lowStockItems;
    private List<Map<String, Object>> salesByMonth;
    private List<Map<String, Object>> costsByCategory;
    private List<Map<String, Object>> topSellingProducts;
    private List<Map<String, Object>> topCustomers;

    public DashboardSummary() {
    }

    public Float getTotalSales() {
        return totalSales;
    }

    public void setTotalSales(Float totalSales) {
        this.totalSales = totalSales;
    }

    public Float getTotalCosts() {
        return totalCosts;
    }

    public void setTotalCosts(Float totalCosts) {
        this.totalCosts = totalCosts;
    }

    public Float getProfit() {
        return profit;
    }

    public void setProfit(Float profit) {
        this.profit = profit;
    }

    public Integer getTotalProducts() {
        return totalProducts;
    }

    public void setTotalProducts(Integer totalProducts) {
        this.totalProducts = totalProducts;
    }

    public Integer getTotalCustomers() {
        return totalCustomers;
    }

    public void setTotalCustomers(Integer totalCustomers) {
        this.totalCustomers = totalCustomers;
    }

    public Integer getLowStockItems() {
        return lowStockItems;
    }

    public void setLowStockItems(Integer lowStockItems) {
        this.lowStockItems = lowStockItems;
    }

    public List<Map<String, Object>> getSalesByMonth() {
        return salesByMonth;
    }

    public void setSalesByMonth(List<Map<String, Object>> salesByMonth) {
        this.salesByMonth = salesByMonth;
    }

    public List<Map<String, Object>> getCostsByCategory() {
        return costsByCategory;
    }

    public void setCostsByCategory(List<Map<String, Object>> costsByCategory) {
        this.costsByCategory = costsByCategory;
    }

    public List<Map<String, Object>> getTopSellingProducts() {
        return topSellingProducts;
    }

    public void setTopSellingProducts(List<Map<String, Object>> topSellingProducts) {
        this.topSellingProducts = topSellingProducts;
    }

    public List<Map<String, Object>> getTopCustomers() {
        return topCustomers;
    }

    public void setTopCustomers(List<Map<String, Object>> topCustomers) {
        this.topCustomers = topCustomers;
    }
}
