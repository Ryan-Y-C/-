package com.wechatshop.service;

import com.api.DataStatus;
import com.api.data.GoodsInfo;
import com.api.data.OrderInfo;
import com.api.data.PageResponse;
import com.api.data.RpcOrderGoods;
import com.api.generator.Order;
import com.fasterxml.jackson.core.type.TypeReference;
import com.wechatshop.WechatshopApplication;
import com.wechatshop.entity.GoodsWithNumber;
import com.wechatshop.entity.OrderResponse;
import com.wechatshop.entity.ResponseData;
import com.wechatshop.generator.Goods;
import com.wechatshop.generator.Shop;
import com.wechatshop.mock.MockOrderRpcService;
import okhttp3.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = WechatshopApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {"spring.config.location=classpath:test-application.yml"})
public class OrderIntegrationTest extends HttpUtils {
    @Autowired
    MockOrderRpcService mockOrderRpcService;

    @BeforeEach
    void setUp() {
        //注入测试远程调用服务
        MockitoAnnotations.initMocks(mockOrderRpcService);
        //当调用创建订单的方法时，返回测试订单对象
        when(mockOrderRpcService.createOrder(any(), any())).thenAnswer(invocationOnMock -> {
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
        ResponseData<OrderResponse> responseData = getObjectMapper(orderResponse.body().string(), new TypeReference<ResponseData<OrderResponse>>() {
        });
        Assertions.assertEquals(1234L, responseData.getData().getId());
        Assertions.assertEquals(2L, responseData.getData().getShop().getId());
        Assertions.assertEquals("shop2", responseData.getData().getShop().getName());
        Assertions.assertEquals(DataStatus.PENDING.getName(), responseData.getData().getStatus());
        Assertions.assertEquals("火星", responseData.getData().getAddress());
        Assertions.assertEquals(Arrays.asList(4L, 5L),
                responseData.getData().getGoods().stream().map(Goods::getId).collect(toList())
        );
        Assertions.assertEquals(Arrays.asList(3, 5),
                responseData.getData().getGoods().stream().map(GoodsWithNumber::getNumber).collect(toList())
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

    @Test
    public void canDeleteOrder() throws IOException {
        UserLoginResponse loginResponse = loginAndGetCookie();
        //获取当前订单

//        Response orderResponse = getResponse(loginResponse.getCookie(), "/api/v1/order?pageSize=2&pageNum=3");
//
//        PageResponse<OrderResponse> orders = getObjectMapper(orderResponse.body().string(), new TypeReference<PageResponse<OrderResponse>>() {
//        });
        when(mockOrderRpcService.rpcOrderService.getOrder(anyLong(), anyInt(), anyInt(), any()))
                .thenReturn(mockResponse());
        PageResponse<OrderResponse> orders = getObjectMapper(loginResponse.getCookie(), "/api/v1/order?pageSize=2&pageNum=3", new TypeReference<PageResponse<OrderResponse>>() {
        });
        Assertions.assertEquals(3, orders.getPageNum());
        Assertions.assertEquals(2, orders.getPageSize());
        Assertions.assertEquals(Arrays.asList("shop2", "shop2"),
                orders.getData()
                        .stream()
                        .map(OrderResponse::getShop)
                        .map(Shop::getName)
                        .collect(Collectors.toList()));
        Assertions.assertEquals(Arrays.asList("goods3", "goods4"),
                orders.getData()
                        .stream()
                        .map(OrderResponse::getGoods)
                        .flatMap(List::stream)
                        .map(Goods::getName)
                        .collect(Collectors.toList()));
        Assertions.assertEquals(Arrays.asList(5, 3),
                orders.getData()
                        .stream()
                        .map(OrderResponse::getGoods)
                        .flatMap(List::stream)
                        .map(GoodsWithNumber::getNumber)
                        .collect(Collectors.toList()));


        //再次获取
    }

    @Test
    public void canDeleteByOrder() throws IOException {
        UserLoginResponse loginResponse = loginAndGetCookie();
        //删除某个订单
        RpcOrderGoods orderGoods = mockRpcOrderGoods(100, 1, 3, 2, 5, DataStatus.DELETED);

        when(mockOrderRpcService.rpcOrderService.deleteOrder(anyLong(), anyLong()))
                .thenReturn(orderGoods);
        final Response delete = delete("/api/v1/order/100", loginResponse.getCookie());
        System.out.println("delete"+delete.body().string());
        ResponseData<OrderResponse> deleteOrders = delete("/api/v1/order/100", loginResponse.getCookie(), new TypeReference<ResponseData<OrderResponse>>() {
        });
//        Response deleteOrderResponse = delete("/api/v1/order/100", loginResponse.getCookie());
//        System.out.println("deleteOrderResponse:" + deleteOrderResponse.body().string());
//        ResponseData<OrderResponse> deleteOrders = getObjectMapper(deleteOrderResponse.body().string(), new TypeReference<ResponseData<OrderResponse>>() {
//        });
        Assertions.assertEquals(DataStatus.DELETED.getName(), deleteOrders.getData().getStatus());
        Assertions.assertEquals(100, deleteOrders.getData().getId());
        Assertions.assertEquals(1, deleteOrders.getData().getGoods().size());
        Assertions.assertEquals(3, deleteOrders.getData().getGoods().get(0).getId());
        Assertions.assertEquals(5, deleteOrders.getData().getGoods().get(0).getNumber());
        Assertions.assertEquals(2, deleteOrders.getData().getShop().getId());
    }


    private PageResponse<RpcOrderGoods> mockResponse() {
        RpcOrderGoods order1 = mockRpcOrderGoods(100, 1, 3, 2, 5, DataStatus.DELETED);
        RpcOrderGoods order2 = mockRpcOrderGoods(101, 1, 4, 2, 3, DataStatus.RECEIVED);
        return PageResponse.pageData(3, 2, 10, Arrays.asList(order1, order2));
    }

    private RpcOrderGoods mockRpcOrderGoods(long orderId, long userId, long goodsId, long shopId, int number, DataStatus status) {
        GoodsInfo goodsInfo = new GoodsInfo();
        goodsInfo.setId(goodsId);
        goodsInfo.setNumber(number);

        Order order = new Order();
        order.setId(orderId);
        order.setUserId(userId);
        order.setShopId(shopId);
        order.setStatus(status.getName());

//        OrderResponse orderResponse=new OrderResponse(order);
//        orderResponse.setGoods(Arrays.asList(goodsInfo));

        RpcOrderGoods orderGoods = new RpcOrderGoods();
        orderGoods.setGoods(Arrays.asList(goodsInfo));
        orderGoods.setOrder(order);
        return orderGoods;
    }
}

