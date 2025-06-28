package com.honeycontrol;

public interface ApiCallback<T> {

    void onSuccess(T response, int code);

    void onFailure(Exception t);
}
