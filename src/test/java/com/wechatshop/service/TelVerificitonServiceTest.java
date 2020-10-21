package com.wechatshop.service;

import com.wechatshop.entity.TelAndCode;
import org.junit.jupiter.api.Assertions;

import static org.junit.jupiter.api.Assertions.*;

class TelVerificitonServiceTest {
    private TelAndCode VALLID_PARAMETER = new TelAndCode("17689231288", null);

    public void returnTrueIfVaild() {
        Assertions.assertTrue(new TelVerificitonService().verifyTelParameter(VALLID_PARAMETER), "true");
    }
}
