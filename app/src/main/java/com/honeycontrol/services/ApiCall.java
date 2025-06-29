package com.honeycontrol.services;

public interface ApiCall<T> {

    void enqueue(ApiCallback<T> callback);
}
