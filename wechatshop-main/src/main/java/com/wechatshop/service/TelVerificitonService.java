package com.wechatshop.service;

import com.wechatshop.entity.TelAndCode;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class TelVerificitonService {
    private static final Pattern TEL_PATTERN = Pattern.compile("^1[3|4|5|8|7][0-9]\\d{8}$");

    /**
     * 验证输入的参数是否合法：
     * tel必须是合法的中国大陆手机号
     *
     * @param param
     * @return true 合法，否则false
     */
    public boolean verifyTelParameter(TelAndCode param) {
        if (param == null || param.getTel() == null || !TEL_PATTERN.matcher(param.getTel()).find()) {
            return false;
        } else {
            return true;
        }

    }
}

