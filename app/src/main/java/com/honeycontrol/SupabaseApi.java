package com.honeycontrol;

import com.honeycontrol.models.Companies;
import com.honeycontrol.requests.CompanyCreateRequest;
import com.honeycontrol.models.User;
import com.honeycontrol.requests.UserCreateRequest;
import com.honeycontrol.models.Customer;
import com.honeycontrol.requests.CustomerCreateRequest;
import java.util.List;

public interface SupabaseApi {
    ApiCall<Companies> createCompanies(CompanyCreateRequest companyRequest);
    ApiCall<User> createUser(UserCreateRequest userRequest);
    ApiCall<User> getUserByEmail(String email);
    
    // Customer methods
    ApiCall<List<Customer>> getCustomersByCompany(long companyId);
    ApiCall<Customer> createCustomer(CustomerCreateRequest customerRequest);
    ApiCall<Customer> updateCustomer(long customerId, CustomerCreateRequest customerRequest);
    ApiCall<Void> deleteCustomer(long customerId);
}
