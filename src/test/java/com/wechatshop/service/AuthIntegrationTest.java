package com.wechatshop.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wechatshop.WechatshopApplication;
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
import java.util.Map;
import java.util.Objects;

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
//        Request request = post(getUrl("/api/code"), objectMapper.writeValueAsString(VALID_PARAMETER));
//        try (Response response = client.newCall(request).execute()) {
//            int responseCode = response.code();
//            Assertions.assertEquals(HTTP_OK, responseCode);
//        }
        post(getUrl("/api/code"), objectMapper.writeValueAsString(VALID_PARAMETER), response -> {
            int responseCode = response.code();
            Assertions.assertEquals(HTTP_OK, responseCode);
        });
    }

    @Test
    public void returnHTTPBadWhenParameterIsCorrect() throws IOException {
//        Request request = post(getUrl("/api/code"), objectMapper.writeValueAsString(INVALID_PARAMETER));
//        try (Response response = client.newCall(request).execute()) {
//            Assertions.assertEquals(HTTP_BAD_REQUEST, response.code());
//        }
        post(getUrl("/api/code")
                , objectMapper.writeValueAsString(INVALID_PARAMETER)
                , response -> Assertions.assertEquals(HTTP_BAD_REQUEST, response.code()));
    }

    @Test
    public void loginStatus() throws IOException {

        get(getUrl("/api/status")
                , response -> {
                    Map responseStatus = objectMapper.readValue(response.body().string(), Map.class);
                    Assertions.assertFalse((Boolean) responseStatus.get("login"));
                });

        post(getUrl("/api/code")
                , objectMapper.writeValueAsString(VALID_PARAMETER)
                , response -> Assertions.assertEquals(HTTP_OK, response.code()));

        get(getUrl("/api/code")
                , response -> {
                    String setCookie = response.headers().get("Set-Cookie");
                    Assertions.assertNotNull(setCookie!=null);
                });
    }

    void post(String url, String json, ResponseAndAssertion assertion) throws IOException {
        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            assertion.assertResult(response);
        }
    }
    void get(String url,ResponseAndAssertion assertion) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();
        Response response = client.newCall(request).execute();
        Map responseStatus = objectMapper.readValue(response.body().string(), Map.class);
        Assertions.assertFalse((Boolean) responseStatus.get("login"));
//        assertion.assertResult(response);
    }

    interface ResponseAndAssertion {
        void assertResult(Response response) throws IOException;
    }
}
