package com.honeycontrol.models;

import com.google.gson.annotations.SerializedName;
import com.google.gson.annotations.Expose;
import java.time.LocalDateTime;

public class Customer {
    @SerializedName("id")
    @Expose(serialize = false)
    private String id;
    
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
    private String company_id;
    
    @SerializedName("created_at")
    @Expose(serialize = false)
    private LocalDateTime created_at;
    
    @SerializedName("updated_at")
    @Expose(serialize = false)
    private LocalDateTime updated_at;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDocument() {
        return document;
    }

    public void setDocument(String document) {
        this.document = document;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCompany_id() {
        return company_id;
    }

    public void setCompany_id(String company_id) {
        this.company_id = company_id;
    }

    public LocalDateTime getCreated_at() {
        return created_at;
    }

    public void setCreated_at(LocalDateTime created_at) {
        this.created_at = created_at;
    }

    public LocalDateTime getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(LocalDateTime updated_at) {
        this.updated_at = updated_at;
    }
}
