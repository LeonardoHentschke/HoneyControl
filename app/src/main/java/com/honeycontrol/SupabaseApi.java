package com.honeycontrol;

import com.honeycontrol.models.Companies;
import com.honeycontrol.models.Cost;
import com.honeycontrol.models.Customer;
import com.honeycontrol.models.DashboardSummary;
import com.honeycontrol.models.LoginRequest;
import com.honeycontrol.models.Product;
import com.honeycontrol.models.ResetPasswordRequest;
import com.honeycontrol.models.Sale;
import com.honeycontrol.models.SaleItem;
import com.honeycontrol.models.SignupRequest;
import com.honeycontrol.models.Stock;
import com.honeycontrol.models.StockLog;
import com.honeycontrol.models.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface SupabaseApi {
    // Companies Endpoints
    @GET("companies")
    Call<List<Companies>> getCompanies();

    @GET("companies/{id}")
    Call<Companies> getCompanyById(@Path("id") String id);

    @POST("companies")
    Call<Companies> createCompanies(@Body Companies companies);

    @PUT("companies/{id}")
    Call<Companies> updateCompany(@Path("id") String id, @Body Companies companies);

    @DELETE("companies/{id}")
    Call<Void> deleteCompany(@Path("id") String id);

    // Users Endpoints
    @GET("users")
    Call<List<User>> getUsers();

    @GET("users/{id}")
    Call<User> getUserById(@Path("id") String id);

    @GET("users")
    Call<List<User>> getUsersByCompanyId(@Query("company_id") String companyId);

    @POST("users")
    Call<User> createUser(@Body User user);

    @PUT("users/{id}")
    Call<User> updateUser(@Path("id") String id, @Body User user);

    @DELETE("users/{id}")
    Call<Void> deleteUser(@Path("id") String id);

    // Products Endpoints
    @GET("products")
    Call<List<Product>> getProducts();

    @GET("products/{id}")
    Call<Product> getProductById(@Path("id") String id);

    @GET("products")
    Call<List<Product>> getProductsByCompanyId(@Query("company_id") String companyId);

    @POST("products")
    Call<Product> createProduct(@Body Product product);

    @PUT("products/{id}")
    Call<Product> updateProduct(@Path("id") String id, @Body Product product);

    @DELETE("products/{id}")
    Call<Void> deleteProduct(@Path("id") String id);

    // Customers Endpoints
    @GET("customers")
    Call<List<Customer>> getCustomers();

    @GET("customers/{id}")
    Call<Customer> getCustomerById(@Path("id") String id);

    @GET("customers")
    Call<List<Customer>> getCustomersByCompanyId(@Query("company_id") String companyId);

    @POST("customers")
    Call<Customer> createCustomer(@Body Customer customer);

    @PUT("customers/{id}")
    Call<Customer> updateCustomer(@Path("id") String id, @Body Customer customer);

    @DELETE("customers/{id}")
    Call<Void> deleteCustomer(@Path("id") String id);

    // Costs Endpoints
    @GET("costs")
    Call<List<Cost>> getCosts();

    @GET("costs/{id}")
    Call<Cost> getCostById(@Path("id") String id);

    @GET("costs")
    Call<List<Cost>> getCostsByCompanyId(@Query("company_id") String companyId);

    @GET("costs")
    Call<List<Cost>> getCostsByUserId(@Query("user_id") String userId);

    @POST("costs")
    Call<Cost> createCost(@Body Cost cost);

    @PUT("costs/{id}")
    Call<Cost> updateCost(@Path("id") String id, @Body Cost cost);

    @DELETE("costs/{id}")
    Call<Void> deleteCost(@Path("id") String id);

    // Sales Endpoints
    @GET("sales")
    Call<List<Sale>> getSales();

    @GET("sales/{id}")
    Call<Sale> getSaleById(@Path("id") String id);

    @GET("sales")
    Call<List<Sale>> getSalesByCompanyId(@Query("company_id") String companyId);

    @GET("sales")
    Call<List<Sale>> getSalesByCustomerId(@Query("customer_id") String customerId);

    @GET("sales")
    Call<List<Sale>> getSalesByUserId(@Query("user_id") String userId);

    @POST("sales")
    Call<Sale> createSale(@Body Sale sale);

    @PUT("sales/{id}")
    Call<Sale> updateSale(@Path("id") String id, @Body Sale sale);

    @DELETE("sales/{id}")
    Call<Void> deleteSale(@Path("id") String id);

    // SaleItems Endpoints
    @GET("sale-items")
    Call<List<SaleItem>> getSaleItems();

    @GET("sale-items/{id}")
    Call<SaleItem> getSaleItemById(@Path("id") String id);

    @GET("sale-items")
    Call<List<SaleItem>> getSaleItemsBySaleId(@Query("sale_id") String saleId);

    @GET("sale-items")
    Call<List<SaleItem>> getSaleItemsByProductId(@Query("product_id") String productId);

    @POST("sale-items")
    Call<SaleItem> createSaleItem(@Body SaleItem saleItem);

    @PUT("sale-items/{id}")
    Call<SaleItem> updateSaleItem(@Path("id") String id, @Body SaleItem saleItem);

    @DELETE("sale-items/{id}")
    Call<Void> deleteSaleItem(@Path("id") String id);

    // Stock Endpoints
    @GET("stocks")
    Call<List<Stock>> getStocks();

    @GET("stocks/{id}")
    Call<Stock> getStockById(@Path("id") String id);

    @GET("stocks")
    Call<List<Stock>> getStocksByProductId(@Query("product_id") String productId);

    @POST("stocks")
    Call<Stock> createStock(@Body Stock stock);

    @PUT("stocks/{id}")
    Call<Stock> updateStock(@Path("id") String id, @Body Stock stock);

    @DELETE("stocks/{id}")
    Call<Void> deleteStock(@Path("id") String id);

    // StockLog Endpoints
    @GET("stock-logs")
    Call<List<StockLog>> getStockLogs();

    @GET("stock-logs/{id}")
    Call<StockLog> getStockLogById(@Path("id") String id);

    @GET("stock-logs")
    Call<List<StockLog>> getStockLogsByStockId(@Query("stock_id") String stockId);

    @POST("stock-logs")
    Call<StockLog> createStockLog(@Body StockLog stockLog);

    @PUT("stock-logs/{id}")
    Call<StockLog> updateStockLog(@Path("id") String id, @Body StockLog stockLog);

    @DELETE("stock-logs/{id}")
    Call<Void> deleteStockLog(@Path("id") String id);
}
