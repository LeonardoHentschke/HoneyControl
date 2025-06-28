package com.honeycontrol.requests;

import com.google.gson.annotations.SerializedName;
import com.google.gson.annotations.Expose;

public class CustomerCreateRequest {
    @SerializedName("name")
    @Expose
    private String name;
    
    @SerializedName("email")
    @Expose
    private String email;
    
    @SerializedName("phone")
    @Expose
    private String phone;

    @SerializedName("document")
    @Expose
    private String document;
    
    @SerializedName("address")
    @Expose
    private String address;
    
    @SerializedName("city")
    @Expose
    private String city;
    
    @SerializedName("company_id")
    @Expose
    private String companyId;

    public CustomerCreateRequest(String name, String email, String phone, String document, String address, String city, String companyId) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.document = document;
        this.address = address;
        this.city = city;
        this.companyId = companyId;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getDocument() {
        return document;
    }

    public void setDocument(String document) {
        this.document = document;
    }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getCompanyId() { return companyId; }
    public void setCompanyId(String companyId) { this.companyId = companyId; }
}
