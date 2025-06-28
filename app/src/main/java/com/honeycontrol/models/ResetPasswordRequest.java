package com.honeycontrol.models;

import com.google.gson.annotations.SerializedName;
import com.google.gson.annotations.Expose;

public class ResetPasswordRequest {
    @SerializedName("email")
    @Expose
    private String email;

    public ResetPasswordRequest() {
    }

    public ResetPasswordRequest(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
