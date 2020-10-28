package com.wechatshop.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wechatshop.entity.LoginResponse;
import com.wechatshop.entity.TelAndCode;
import okhttp3.*;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.ClassicConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;

import java.io.IOException;
import java.util.Objects;

public class HttpUtils {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private OkHttpClient client = new OkHttpClient();
    @Value("${spring.datasource.url}")
    private String databaseUrl;
    @Value("${spring.datasource.username}")
    private String databaseUsername;
    @Value("${spring.datasource.password}")
    private String databasePassword;

    @BeforeEach
    public void initDatabase() {
        //进行测试前进行flyway:clean flyway:migrate
        ClassicConfiguration configuration = new ClassicConfiguration();
        configuration.setDataSource(databaseUrl, databaseUsername, databasePassword);
        Flyway flyway = new Flyway(configuration);
        flyway.clean();
        flyway.migrate();
    }

    public String post(String url, TelAndCode telAndCode, boolean isReturnResponse) throws IOException {
        if (isReturnResponse) {
            try (Response response = client.newCall(post(url, telAndCode)).execute()) {
                String setCookie = response.headers("Set-Cookie")
                        .stream()
                        .filter((cookie) -> cookie.contains("JSESSIONID"))
                        .findFirst().get().split(";")[0];
                return setCookie;
            }
        } else {
            return null;
        }
    }

    public Request post(String url, TelAndCode telAndCode) throws JsonProcessingException {
        RequestBody body = RequestBody.create(getJson(telAndCode), JSON);
        Request request = new Request.Builder()
                .url(getUrl(url))
                .post(body)
                .build();
        return request;
    }

    private String getJson(TelAndCode object) throws JsonProcessingException {
        return objectMapper.writeValueAsString(object);
    }

    public void post(String url, TelAndCode telAndCode, ResponseAndAssertion assertion) throws IOException {
        Request request = post(url, telAndCode);
        try (Response response = client.newCall(request).execute()) {
            assertion.assertResult(response);
        }
    }

    public LoginResponse get(Request request) throws IOException {
        try (Response response = client.newCall(request).execute()) {
            String json = Objects.requireNonNull(response.body()).string();

            return !json.equals("") ? objectMapper.readValue(json, LoginResponse.class) : null;
        }
    }

    public LoginResponse get(String url) throws IOException {
        Request request = new Request.Builder()
                .url(getUrl(url))
                .build();
        return get(request);
    }

    public LoginResponse get(String setCookie, String url) throws IOException {
        Request request = new Request.Builder()
                .addHeader("Cookie", setCookie)
                .url(getUrl(url))
                .build();
        return get(request);
    }

    interface ResponseAndAssertion {
        void assertResult(Response response) throws IOException;
    }

    @Autowired
    private Environment environment;

    public String getUrl(String apiName) {
        // 获取集成测试的端口号
        return "http://localhost:" + environment.getProperty("local.server.port") + apiName;
    }
}
