package com.wechatshop.entity;

public class MessageResponse {
    private String message;

    public static MessageResponse of(String message){
        return new MessageResponse(message);
    }

    private MessageResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
