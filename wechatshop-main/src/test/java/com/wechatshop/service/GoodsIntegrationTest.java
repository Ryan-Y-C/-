package com.wechatshop.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wechatshop.WechatshopApplication;
import com.wechatshop.entity.ResponseData;
import com.wechatshop.generator.Goods;
import com.wechatshop.generator.Shop;
import okhttp3.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;

import static javax.servlet.http.HttpServletResponse.SC_CREATED;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.assertEquals;

//junit5 spring扩展插件
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = WechatshopApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//创建集成测试数据库
@TestPropertySource(properties = {"spring.config.location=classpath:test-application.yml"})
public class GoodsIntegrationTest extends HttpUtils {
    @Test
    public void testCreateGoods() throws IOException {
        UserLoginResponse loginResponse = loginAndGetCookie();
        String setCookie = loginResponse.getCookie();
        Shop shop = new Shop();
        shop.setName("微信店铺");
        shop.setDescription("微信小店铺");
        shop.setImgUrl("http://url");

        Response httpResponse = post("/api/v1/shop", shop, setCookie);
        assertEquals(SC_CREATED, httpResponse.code());
        //通过响应body获取Goods对象
        ObjectMapper shopObjectMapper = new ObjectMapper();
        ResponseData<Shop> shopResponse =
                shopObjectMapper.readValue(httpResponse.body().string(), new TypeReference<ResponseData<Shop>>() {
                });
        assertEquals("微信店铺", shopResponse.getData().getName());
        assertEquals("微信小店铺", shopResponse.getData().getDescription());
        assertEquals("http://url", shopResponse.getData().getImgUrl());
        assertEquals("ok", shopResponse.getData().getStatus());
        assertEquals(shopResponse.getData().getOwnerUserId(), loginResponse.getUser().getId());

        Goods goods = new Goods();
        goods.setName("肥皂");
        goods.setDescription("纯天然无污染肥皂");
        goods.setImgUrl("https://img.url");
        goods.setPrice(500L);
        goods.setStock(10);
        goods.setShopId(shopResponse.getData().getId());

        //接受url goods 返回json字符串
        post("/api/v1/goods", goods, setCookie, response -> {
            assertEquals(SC_CREATED, response.code());
            //通过响应body获取Goods对象
            ObjectMapper objectMapper = new ObjectMapper();
            ResponseData<Goods> goodsResponse =
                    objectMapper.readValue(response.body().string(), new TypeReference<ResponseData<Goods>>() {
                    });
            assertEquals(SC_CREATED, response.code());
            assertEquals("肥皂", goodsResponse.getData().getName());
            assertEquals(shopResponse.getData().getId(), goodsResponse.getData().getShopId());
            assertEquals("ok", goodsResponse.getData().getStatus());
        });
    }

    @Test
    public void returnNotFoundIfGoodsToDeleteNotExit() throws IOException {
        UserLoginResponse userLoginResponse = loginAndGetCookie();
        String cookie = userLoginResponse.getCookie();
        Response deleteResponse = delete("/api/v1/goods/123456", cookie);
        assertEquals(SC_NOT_FOUND, deleteResponse.code());
    }

}
