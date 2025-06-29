package com.honeycontrol.services;

public interface ApiCallback<T> {

    void onSuccess(T response, int code);

    void onFailure(Exception t);
}
