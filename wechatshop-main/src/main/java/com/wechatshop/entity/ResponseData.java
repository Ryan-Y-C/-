package com.wechatshop.entity;

public class ResponseData<T>  {
    private T data;

    private ResponseData(){}

    private ResponseData(T data) {
        this.data = data;
    }

    public static <T> ResponseData<T> of(T data) {
        return new ResponseData<>(data);
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
