package com.wechatshop.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wechatshop.WechatshopApplication;
import com.wechatshop.generator.Goods;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;

import static javax.servlet.http.HttpServletResponse.SC_CREATED;

//junit5 spring扩展插件
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = WechatshopApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//创建集成测试数据库
@TestPropertySource(properties = {"spring.config.location=classpath:test-application.yml"})
public class GoodsIntegrationTest extends HttpUtils {
    @Test
    public void testCreateGoods() throws IOException {
        String setCookie = loginAndGetCookie();
        Goods goods = new Goods();
        goods.setName("肥皂");
        goods.setDescription("纯天然无污染肥皂");
        goods.setImgUrl("https://img.url");
        goods.setPrice(500L);
        goods.setStock(10);
        goods.setShopId(1L);
        //接受url goods 返回json字符串
        post("/api/v1/goods", goods, setCookie, new ResponseAndAssertion() {
            @Override
            public void assertResult(Response response) throws IOException {
                Assertions.assertEquals(SC_CREATED, response.code());
                //通过响应body获取Goods对象
                ObjectMapper objectMapper = new ObjectMapper();
                com.wechatshop.entity.Response<Goods> goodsResponse =
                        objectMapper.readValue(response.body().string(), new TypeReference<com.wechatshop.entity.Response<Goods>>() {
                        });
                Assertions.assertEquals("肥皂", goodsResponse.getData().getName());
            }
        });


    }

    @Test
    public void testDeleteGoods() {

    }


}
