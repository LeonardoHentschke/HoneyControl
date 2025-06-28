package com.honeycontrol.models;

import com.google.gson.annotations.SerializedName;

import java.time.LocalDateTime;

public class Companies {
    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("created_at")
    private LocalDateTime created_at;

    @SerializedName("updated_at")
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
