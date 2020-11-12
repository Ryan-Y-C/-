package com.wechatshop.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wechatshop.entity.LoginResponse;
import com.wechatshop.generator.User;
import okhttp3.*;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.ClassicConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;

import java.io.IOException;
import java.util.Objects;

import static com.wechatshop.service.TelVerificitonServiceTest.VALID_PARAMETER;
import static com.wechatshop.service.TelVerificitonServiceTest.VALID_PARAMETER_CODE;
import static java.net.HttpURLConnection.HTTP_OK;

public class HttpUtils {
    @Autowired
    private Environment environment;
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

    UserLoginResponse loginAndGetCookie() throws IOException {
        //注册
        post("/api/v1/code", VALID_PARAMETER,
                response -> Assertions.assertEquals(HTTP_OK, response.code()));
        //登录并获取Cookie
        String cookie = post("/api/v1/login", VALID_PARAMETER_CODE, true);
        LoginResponse loginResponse = get(cookie, "/api/v1/status");
        User user = loginResponse.getUser();
        return new UserLoginResponse(cookie, user);
    }

    Response post(String url, Object object, String cookie) throws IOException {
        RequestBody body = RequestBody.create(getJson(object), JSON);
        Request request = new Request.Builder()
                .url(getUrl(url))
                .post(body)
                .addHeader("Cookie", cookie)
                .build();
        return client.newCall(request).execute();
    }

    public String post(String url, Object object, boolean isReturnResponse) throws IOException {
        if (isReturnResponse) {
            try (Response response = client.newCall(post(url, object)).execute()) {
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

    public static class UserLoginResponse {
        private String cookie;
        private User user;

        public UserLoginResponse(String cookie, User user) {
            this.cookie = cookie;
            this.user = user;
        }

        public String getCookie() {
            return cookie;
        }

        public User getUser() {
            return user;
        }
    }

    public Request post(String url, Object object) throws JsonProcessingException {
        RequestBody body = RequestBody.create(getJson(object), JSON);
        return new Request.Builder()
                .url(getUrl(url))
                .post(body)
                .build();
    }

    private String getJson(Object object) throws JsonProcessingException {
        return objectMapper.writeValueAsString(object);
    }

    public void post(String url, Object object, ResponseAndAssertion assertion) throws IOException {
        Request request = post(url, object);
        try (Response response = client.newCall(request).execute()) {
            assertion.assertResult(response);
        }
    }

    public Request post(String url, Object object, String cookie, ResponseAndAssertion assertion) throws IOException {
        RequestBody body = RequestBody.create(getJson(object), JSON);
        Request request = new Request.Builder()
                .url(getUrl(url))
                .post(body)
                .addHeader("Cookie", cookie)
                .build();
        try (Response response = client.newCall(request).execute()) {
            assertion.assertResult(response);
        }
        return request;
    }


    public LoginResponse get(Request request) throws IOException {
        try (Response response = client.newCall(request).execute()) {
            String json = Objects.requireNonNull(response.body()).string();

            return !json.equals("") ? getLoginResponse(json) : null;
        }
    }

    protected LoginResponse getLoginResponse(String json) throws JsonProcessingException {
        return objectMapper.readValue(json, LoginResponse.class);
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

    public void postLogout(String setCookie, String url) throws IOException {
        Request request = new Request.Builder()
                .addHeader("Cookie", setCookie)
                .url(getUrl(url))
                .build();
        try (Response response = client.newCall(request).execute()) {
            System.out.println(response.code());
        }
    }

    public <T> T getPageResponse(String setCookie, String url, TypeReference<T> typeReference) throws IOException {
        Request request = new Request.Builder()
                .addHeader("Cookie", setCookie)
                .url(getUrl(url))
                .build();
        Response response = client.newCall(request).execute();
        return objectMapper.readValue(response.body().string(), typeReference);
    }


    interface ResponseAndAssertion {
        void assertResult(Response response) throws IOException;
    }

    public String getUrl(String apiName) {
        // 获取集成测试的端口号
        return "http://localhost:" + environment.getProperty("local.server.port") + apiName;
    }

    Response delete(String url, String cookie) throws IOException {
        Request request = new Request.Builder()
                .addHeader("Cookie", cookie)
                .delete()
                .url(getUrl(url))
                .build();
        return client.newCall(request).execute();
    }
}
