package com.honeycontrol.services;

import com.honeycontrol.models.Companies;
import com.honeycontrol.requests.CompanyCreateRequest;
import com.honeycontrol.models.User;
import com.honeycontrol.requests.UserCreateRequest;
import com.honeycontrol.models.Customer;
import com.honeycontrol.requests.CustomerCreateRequest;
import com.honeycontrol.models.Cost;
import com.honeycontrol.requests.CostCreateRequest;
import com.honeycontrol.models.Product;
import com.honeycontrol.requests.ProductCreateRequest;
import com.honeycontrol.models.Stock;
import com.honeycontrol.models.StockLog;
import com.honeycontrol.models.Sale;
import com.honeycontrol.models.SaleItem;
import com.honeycontrol.requests.StockCreateRequest;
import com.honeycontrol.requests.StockUpdateRequest;
import com.honeycontrol.requests.StockLogCreateRequest;
import com.honeycontrol.requests.SaleCreateRequest;
import com.honeycontrol.requests.SaleItemCreateRequest;

import java.util.List;

public interface SupabaseApi {
    ApiCall<Companies> createCompanies(CompanyCreateRequest companyRequest);
    ApiCall<User> createUser(UserCreateRequest userRequest);
    ApiCall<User> getUserByEmail(String email);
    ApiCall<List<Customer>> getCustomersByCompany(String companyId);
    ApiCall<Customer> getCustomerById(String customerId);
    ApiCall<Customer> createCustomer(CustomerCreateRequest customerRequest);
    ApiCall<Customer> updateCustomer(String customerId, CustomerCreateRequest customerRequest);
    ApiCall<Void> deleteCustomer(String customerId);
    ApiCall<List<Cost>> getCostsByCompany(String companyId);
    ApiCall<Cost> getCostById(String costId);
    ApiCall<Cost> createCost(CostCreateRequest costRequest);
    ApiCall<Cost> updateCost(String costId, CostCreateRequest costRequest);
    ApiCall<Void> deleteCost(String costId);
    ApiCall<List<Product>> getProductsByCompany(String companyId);
    ApiCall<List<Product>> getProductsWithStockByCompany(String companyId);
    ApiCall<Product> getProductById(String productId);
    ApiCall<Product> createProduct(ProductCreateRequest productRequest);
    ApiCall<Product> updateProduct(String productId, ProductCreateRequest productRequest);
    ApiCall<Void> deleteProduct(String productId);
    ApiCall<List<Stock>> getStockByProductId(String productId);
    ApiCall<List<StockLog>> getStockLogsByStockId(String stockId);
    ApiCall<Stock> createStock(StockCreateRequest stockRequest);
    ApiCall<Stock> updateStock(String stockId, StockUpdateRequest stockRequest);
    ApiCall<StockLog> createStockLog(StockLogCreateRequest stockLogRequest);
    ApiCall<List<Sale>> getSalesByCompany(String companyId);
    ApiCall<Sale> getSaleById(String saleId);
    ApiCall<Sale> createSale(SaleCreateRequest saleRequest);
    ApiCall<SaleItem> createSaleItem(SaleItemCreateRequest saleItemRequest);
}
