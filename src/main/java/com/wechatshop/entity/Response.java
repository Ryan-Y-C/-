package com.wechatshop.entity;

public class Response<T>  {
    private T data;

    private Response(T data) {
        this.data = data;
    }

    public static <T> Response<T> of(T data) {
        return new Response<>(data);
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
