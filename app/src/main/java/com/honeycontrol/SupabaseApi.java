package com.honeycontrol;

import com.honeycontrol.models.Companies;
import com.honeycontrol.models.CompanyCreateRequest;
import com.honeycontrol.models.User;
import com.honeycontrol.models.UserCreateRequest;

import java.util.List;

public interface SupabaseApi {
    ApiCall<Companies> createCompanies(CompanyCreateRequest companyRequest);
    ApiCall<User> createUser(UserCreateRequest userRequest);
}
