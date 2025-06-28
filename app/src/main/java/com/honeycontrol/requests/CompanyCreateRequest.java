package com.honeycontrol.requests;

import com.google.gson.annotations.SerializedName;
import com.google.gson.annotations.Expose;

public class CompanyCreateRequest {
    @SerializedName("name")
    @Expose
    private String name;

    public CompanyCreateRequest(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
