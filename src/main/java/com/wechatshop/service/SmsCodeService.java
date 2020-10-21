package com.wechatshop.service;

public interface SmsCodeService {

    /**
     * 向指定手机号发验证码，返回正确答案
     *
     * @param tel
     * @return code
     */
    String sendSmsCode(String tel);
}
