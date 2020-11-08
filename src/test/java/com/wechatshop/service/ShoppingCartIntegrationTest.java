package com.wechatshop.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wechatshop.WechatshopApplication;
import com.wechatshop.controller.ShoppingCartController;
import com.wechatshop.entity.PageResponse;
import com.wechatshop.entity.ShoppingCartData;
import com.wechatshop.entity.ShoppingCartGoods;
import com.wechatshop.generator.Goods;
import okhttp3.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = WechatshopApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {"spring.config.location=classpath:test-application.yml"})
public class ShoppingCartIntegrationTest extends HttpUtils {
    private UserLoginResponse userLoginResponse;
    private ObjectMapper objectMapper = new ObjectMapper();


    @Test
    public void getShopCartTest() throws IOException {
        userLoginResponse = loginAndGetCookie();
        String setCookie = userLoginResponse.getCookie();
        PageResponse<ShoppingCartData> pageResponse = getPageResponse(setCookie, "/api/v1/shoppingCart?pageNum=2&pageSize=1", new TypeReference<PageResponse<ShoppingCartData>>() {
        });
        Assertions.assertEquals(2, pageResponse.getPageNum());
        Assertions.assertEquals(1, pageResponse.getPageSize());
        Assertions.assertEquals(2, pageResponse.getTotalPage());
        Assertions.assertEquals(1, pageResponse.getData().size());
        Assertions.assertEquals(2, pageResponse.getData().get(0).getShop().getId());
        Assertions.assertEquals(Arrays.asList(4L, 5L),
                pageResponse.getData().get(0).getGoods().stream()
                        .map(Goods::getId).collect(toList()));
        Assertions.assertEquals(Arrays.asList(100L, 200L),
                pageResponse.getData().get(0).getGoods().stream()
                        .map(ShoppingCartGoods::getPrice).collect(toList()));
        Assertions.assertEquals(Arrays.asList(200, 300),
                pageResponse.getData().get(0).getGoods().stream()
                        .map(ShoppingCartGoods::getNumber).collect(toList()));
    }

    @Test
    public void canAddShoppingCartData() throws IOException {


        userLoginResponse = loginAndGetCookie();
        String setCookie = userLoginResponse.getCookie();
        ShoppingCartController.AddToShoppingCartRequest request = new ShoppingCartController.AddToShoppingCartRequest();
        ShoppingCartController.AddToShoppingCartItem item = new ShoppingCartController.AddToShoppingCartItem();
        item.setId(2L);
        item.setNumber(2);

        request.setGoods(Collections.singletonList(item));
        Response addShoppingCartResponse = post("/api/v1/shoppingCart", request, setCookie);
        com.wechatshop.entity.Response<ShoppingCartData> shoppingCartData = objectMapper.readValue(addShoppingCartResponse.body().string(), new TypeReference<com.wechatshop.entity.Response<ShoppingCartData>>() {
        });
        Assertions.assertEquals(1L, shoppingCartData.getData().getShop().getId());
        Assertions.assertEquals(Arrays.asList(1L, 2L), shoppingCartData.getData().getGoods().stream().map(Goods::getId).collect(Collectors.toList()));
        Assertions.assertEquals(Arrays.asList(100, 2), shoppingCartData.getData().getGoods().stream().map(ShoppingCartGoods::getNumber).collect(Collectors.toList()));
        Assertions.assertTrue(shoppingCartData.getData().getGoods().stream().allMatch(shoppingCartGoods -> shoppingCartGoods.getShopId() == 1L));
    }
}
