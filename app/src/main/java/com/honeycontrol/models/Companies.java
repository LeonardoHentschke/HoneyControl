package com.honeycontrol.models;

import com.google.gson.annotations.SerializedName;
import com.google.gson.annotations.Expose;

import java.time.LocalDateTime;

public class Companies {
    @SerializedName("id")
    @Expose(serialize = false, deserialize = true)
    private String id;

    @SerializedName("name")
    @Expose
    private String name;

    @SerializedName("created_at")
    @Expose(serialize = false, deserialize = true)
    private LocalDateTime created_at;

    @SerializedName("updated_at")
    @Expose(serialize = false, deserialize = true)
    private LocalDateTime updated_at;

    public Companies(String id, String name, LocalDateTime created_at, LocalDateTime updated_at) {
        this.id = id;
        this.name = name;
        this.created_at = created_at;
        this.updated_at = updated_at;
    }

    public Companies() {}

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

    public LocalDateTime getCreatedAt() {
        return created_at;
    }

    public void setCreatedAt(LocalDateTime created_at) {
        this.created_at = created_at;
    }

    public LocalDateTime getUpdatedAt() {
        return updated_at;
    }

    public void setUpdatedAt(LocalDateTime updated_at) {
        this.updated_at = updated_at;
    }

    @Override
    public String toString() {
        return "Companies{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", created_at=" + created_at +
                ", updated_at=" + updated_at +
                '}';
    }
}
