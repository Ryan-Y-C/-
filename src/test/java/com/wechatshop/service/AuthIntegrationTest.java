package com.wechatshop.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wechatshop.WechatshopApplication;
import com.wechatshop.entity.LoginResponse;
import okhttp3.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;

import static com.wechatshop.service.TelVerificitonServiceTest.*;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_OK;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = WechatshopApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application.yml")
public class AuthIntegrationTest {
    private final ObjectMapper objectMapper = new ObjectMapper();
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private OkHttpClient client = new OkHttpClient();

    @Autowired
    Environment environment;

    private String getUrl(String apiName) {
        // 获取集成测试的端口号
        return "http://localhost:" + environment.getProperty("local.server.port") + apiName;
    }

    @Test
    public void returnHTTPokWhenParameterIsCorrect() throws IOException {
        post("/api/code", objectMapper.writeValueAsString(VALID_PARAMETER), response -> {
            int responseCode = response.code();
            Assertions.assertEquals(HTTP_OK, responseCode);
        });
    }

    @Test
    public void returnHTTPBadWhenParameterIsCorrect() throws IOException {
        post("/api/code", objectMapper.writeValueAsString(INVALID_PARAMETER), response -> Assertions.assertEquals(HTTP_BAD_REQUEST, response.code()));
    }

    @Test
    public void loginStatus() throws IOException {
        //查看未登录状态
        Request notLogin = new Request.Builder()
                .url(getUrl("/api/status"))
                .build();
        LoginResponse notLoginResponse = get(notLogin);
        Assertions.assertFalse(notLoginResponse.isLogin());

        //注册
        post("/api/code", objectMapper.writeValueAsString(VALID_PARAMETER), response -> Assertions.assertEquals(HTTP_OK, response.code()));
        //登录并获取Cookie
        Request request = post("/api/login", objectMapper.writeValueAsString(VALID_PARAMETER_CODE));
        Response response = client.newCall(request).execute();
        String setCookie = response.headers("Set-Cookie")
                .stream()
                .filter((cookie) -> cookie.contains("JSESSIONID"))
                .findFirst().get().split(";")[0];
        //查看登录状态
        Request loginRequest = new Request.Builder()
                .addHeader("Cookie", setCookie)
                .url(getUrl("/api/status"))
                .build();
        LoginResponse loginResponse = get(loginRequest);
        Assertions.assertTrue(loginResponse.isLogin());
        Assertions.assertEquals(VALID_PARAMETER_CODE.getTel(), loginResponse.getUser().getTel());

        //携带cookie注销登录
        new Request.Builder().addHeader("Cookie", setCookie).url(getUrl("/api/logout")).build();
        //查看注销后登录状态
        Request logOutStatus = new Request.Builder().url(getUrl("/api/status")).build();
        LoginResponse logOutStatusResponse = get(logOutStatus);
        Assertions.assertFalse(logOutStatusResponse.isLogin());
    }

    void post(String url, String json, ResponseAndAssertion assertion) throws IOException {
        Request request = post(url, json);
        try (Response response = client.newCall(request).execute()) {
            assertion.assertResult(response);
        }
    }

    Request post(String url, String json) {
        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder()
                .url(getUrl(url))
                .post(body)
                .build();
        return request;
    }

    LoginResponse get(Request request) throws IOException {
        try (Response response = client.newCall(request).execute()) {
            //json转化为对象
            LoginResponse responseStatus = objectMapper.readValue(response.body().string(), LoginResponse.class);
            return responseStatus;
        }
    }

    interface ResponseAndAssertion {
        void assertResult(Response response) throws IOException;
    }
}
