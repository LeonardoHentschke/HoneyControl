package com.honeycontrol;

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
    ApiCall<Product> getProductById(String productId);
    ApiCall<Product> createProduct(ProductCreateRequest productRequest);
    ApiCall<Product> updateProduct(String productId, ProductCreateRequest productRequest);
    ApiCall<Void> deleteProduct(String productId);
}
