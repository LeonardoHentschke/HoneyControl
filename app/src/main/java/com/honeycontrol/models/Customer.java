package com.honeycontrol.models;

import java.time.LocalDateTime;

public class Customer {
    private String id;
    private String name;
    private String email;
    private String phone;
    private String document;
    private String address;
    private String city;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getDocument() {
        return document;
    }

    public String getAddress() {
        return address;
    }

    public String getCity() {
        return city;
    }

    public LocalDateTime getCreated_at() {
        return created_at;
    }

    public LocalDateTime getUpdated_at() {
        return updated_at;
    }
}
