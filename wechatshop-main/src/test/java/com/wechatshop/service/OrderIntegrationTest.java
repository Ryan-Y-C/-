package com.wechatshop.service;

import com.api.DataStatus;
import com.api.data.GoodsInfo;
import com.api.data.OrderInfo;
import com.api.generator.Order;
import com.fasterxml.jackson.core.type.TypeReference;
import com.wechatshop.WechatshopApplication;
import com.wechatshop.entity.GoodsWithNumber;
import com.wechatshop.entity.OrderResponse;
import com.wechatshop.entity.ResponseData;
import com.wechatshop.generator.Goods;
import com.wechatshop.mock.MockOrderRpcService;
import okhttp3.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.util.Arrays;

import static java.util.stream.Collectors.toList;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = WechatshopApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {"spring.config.location=classpath:test-application.yml"})
public class OrderIntegrationTest extends HttpUtils {
    @Autowired
    MockOrderRpcService mockOrderRpcService;
    @BeforeEach
    void setUp(){
        //注入测试远程调用服务
        MockitoAnnotations.initMocks(mockOrderRpcService);
        //当调用创建订单的方法时，返回测试订单对象
        Mockito.when(mockOrderRpcService.createOrder(Mockito.any(), Mockito.any())).thenAnswer(invocationOnMock -> {
            Order order = invocationOnMock.getArgument(1, Order.class);
            order.setId(1234L);
            return order;
        });
    }
    @Test
    public void canCreateOrder() throws IOException {
        UserLoginResponse loginResponse = loginAndGetCookie();

        OrderInfo orderInfo = new OrderInfo();
        GoodsInfo goodsInfo1 = new GoodsInfo();
        goodsInfo1.setId(4);
        goodsInfo1.setNumber(3);
        GoodsInfo goodsInfo2 = new GoodsInfo();
        goodsInfo2.setId(5);
        goodsInfo2.setNumber(5);
        orderInfo.setGoods(Arrays.asList(goodsInfo1, goodsInfo2));

        Response orderResponse = post("/api/v1/order", orderInfo, loginResponse.getCookie());
        //通过响应body获取Goods对象
        ResponseData<OrderResponse> responseData=getObjectMapper(orderResponse.body().string(), new TypeReference<ResponseData<OrderResponse>>() {
        });
        Assertions.assertEquals(1234L, responseData.getData().getId());
        Assertions.assertEquals(2L, responseData.getData().getShop().getId());
        Assertions.assertEquals("shop2", responseData.getData().getShop().getName());
        Assertions.assertEquals(DataStatus.PENDING.getName(), responseData.getData().getStatus());
        Assertions.assertEquals("火星", responseData.getData().getAddress());
        Assertions.assertEquals(Arrays.asList(4L, 5L),
                responseData.getData().getGoodsList().stream().map(Goods::getId).collect(toList())
        );
        Assertions.assertEquals(Arrays.asList(3, 5),
                responseData.getData().getGoodsList().stream().map(GoodsWithNumber::getNumber).collect(toList())
        );
    }

    @Test
    public void canRollBackIfDeductStockFailed() throws IOException {
        UserLoginResponse loginResponse = loginAndGetCookie();

        OrderInfo orderInfo = new OrderInfo();
        GoodsInfo goodsInfo1 = new GoodsInfo();
        goodsInfo1.setId(4);
        goodsInfo1.setNumber(3);
        GoodsInfo goodsInfo2 = new GoodsInfo();
        goodsInfo2.setId(5);
        goodsInfo2.setNumber(6);
        orderInfo.setGoods(Arrays.asList(goodsInfo1, goodsInfo2));

        Response orderResponse = post("/api/v1/order", orderInfo, loginResponse.getCookie());

        Assertions.assertEquals(HttpStatus.GONE.value(), orderResponse.code());
        canCreateOrder();
    }
}
