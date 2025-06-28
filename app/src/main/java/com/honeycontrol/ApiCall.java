package com.honeycontrol;

public interface ApiCall<T> {

    void enqueue(ApiCallback<T> callback);
}
