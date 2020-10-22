package com.wechatshop.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wechatshop.WechatshopApplication;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;

import static com.wechatshop.service.TelVerificitonServiceTest.INVALID_PARAMETER;
import static com.wechatshop.service.TelVerificitonServiceTest.VALID_PARAMETER;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_OK;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = WechatshopApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application.yml")
public class IntegrationTest {
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
        int responseCode = post(getUrl("/api/code"), objectMapper.writeValueAsString(VALID_PARAMETER));
        Assertions.assertEquals(HTTP_OK, responseCode);
    }

    @Test
    public void returnHTTPBadWhenParameterIsCorrect() throws IOException {
        int responseCode = post(getUrl("/api/code"), objectMapper.writeValueAsString(INVALID_PARAMETER));
        Assertions.assertEquals(HTTP_BAD_REQUEST, responseCode);
    }

    Integer post(String url, String json) throws IOException {
        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            return response.code();
        }
    }
}
