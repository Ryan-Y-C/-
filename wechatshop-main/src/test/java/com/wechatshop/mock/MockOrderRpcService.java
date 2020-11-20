package com.wechatshop.mock;

import com.api.data.OrderInfo;
import com.api.generator.Order;
import com.api.rpc.RpcOrderService;
import org.apache.dubbo.config.annotation.DubboService;
import org.mockito.Mock;

@DubboService(version = "${wechatshop.orderservice.version}")
public class MockOrderRpcService implements RpcOrderService {
    @Mock
    RpcOrderService rpcOrderService;

    @Override
    public Order createOrder(OrderInfo orderInfo, Order order) {
        return rpcOrderService.createOrder(orderInfo, order);
    }
}
