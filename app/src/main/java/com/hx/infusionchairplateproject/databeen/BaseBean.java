package com.hx.infusionchairplateproject.databeen;

import androidx.annotation.NonNull;

/**
 *{"status":200,"message":null,"data":"PUT_OFF"}
 */
public class BaseBean<T> {
    public T data;
    public int status;
    public String message;

    @Override
    public String toString() {
        return "BaseBean{" +
                "data=" + data +
                ", status=" + status +
                ", message='" + message + '\'' +
                '}';
    }
}