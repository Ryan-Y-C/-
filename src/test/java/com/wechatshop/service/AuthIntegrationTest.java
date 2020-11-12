package com.wechatshop.service;

import com.wechatshop.WechatshopApplication;
import com.wechatshop.api.OrderService;
import com.wechatshop.entity.LoginResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;

import static com.wechatshop.service.TelVerificitonServiceTest.*;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_OK;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = WechatshopApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {"spring.config.location=classpath:test-application.yml"})
public class AuthIntegrationTest extends HttpUtils {
    private OrderService orderService;

    @Autowired
    public AuthIntegrationTest(OrderService orderService) {
        this.orderService = orderService;
    }

    @Test
    public void returnHTTPokWhenParameterIsCorrect() throws IOException {
        post("/api/v1/code", VALID_PARAMETER, response -> Assertions.assertEquals(HTTP_OK, response.code()));
    }
//

    @Test
    public void returnHTTPBadWhenParameterIsCorrect() throws IOException {
        post("/api/v1/code", INVALID_PARAMETER, response -> Assertions.assertEquals(HTTP_BAD_REQUEST, response.code()));
    }

    //接受url 和 json对象
    @Test
    public void loginStatus() throws IOException {
        //查看未登录状态
        LoginResponse notLoginResponse = get("/api/v1/status");
        Assertions.assertFalse(notLoginResponse.isLogin());

        //登录并获取Cookie
        String setCookie = loginAndGetCookie().getCookie();
        //查看登录状态
        LoginResponse loginResponse = get(setCookie, "/api/v1/status");
        Assertions.assertTrue(loginResponse.isLogin());
        Assertions.assertEquals(VALID_PARAMETER_CODE.getTel(), loginResponse.getUser().getTel());

        //携带cookie注销登录
        postLogout(setCookie, "/api/v1/logout");
        //查看注销后登录状态
        Assertions.assertFalse(get("/api/v1/status").isLogin());
    }
}
