package com.wechatshop.service;

import com.wechatshop.entity.TelAndCode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class TelVerificitonServiceTest {
    public static TelAndCode VALID_PARAMETER = new TelAndCode("13689231288", null);
    //invalid
    public static TelAndCode INVALID_PARAMETER = new TelAndCode("123", null);

    public static TelAndCode VALID_PARAMETER_CODE = new TelAndCode("13689231288", "000000");

    @Test
    public void returnTrueIfValid() {
        Assertions.assertTrue(new TelVerificitonService().verifyTelParameter(VALID_PARAMETER));
    }

    @Test
    public void returnFalseIfValid() {
        Assertions.assertFalse(new TelVerificitonService().verifyTelParameter(INVALID_PARAMETER));
    }
}
