package com.wechatshop.entity;

public class TelAndCode {
    private String tel;
    private String code;

    public TelAndCode(String tel, String code) {
        this.tel = tel;
        this.code = code;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTel() {
        return tel;
    }

    public String getCode() {
        return code;
    }
}
