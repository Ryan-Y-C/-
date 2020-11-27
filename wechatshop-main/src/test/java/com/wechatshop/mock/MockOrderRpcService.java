package com.wechatshop.mock;

import com.api.DataStatus;
import com.api.data.OrderInfo;
import com.api.data.PageResponse;
import com.api.data.RpcOrderGoods;
import com.api.generator.Order;
import com.api.rpc.RpcOrderService;
import org.apache.dubbo.config.annotation.DubboService;
import org.mockito.Mock;

@DubboService(version = "${wechatshop.orderservice.version}")
public class MockOrderRpcService implements RpcOrderService {
    @Mock
    public RpcOrderService rpcOrderService;

    @Override
    public Order createOrder(OrderInfo orderInfo, Order order) {
        return rpcOrderService.createOrder(orderInfo, order);
    }

    @Override
    public RpcOrderGoods deleteOrder(long orderId, long userId) {
        return rpcOrderService.deleteOrder(orderId, userId);
    }

    @Override
    public PageResponse<RpcOrderGoods> getOrder(long userId, Integer pageNum, Integer pageSize, DataStatus status) {
        return rpcOrderService.getOrder(userId, pageNum, pageSize, status);
    }

    @Override
    public Order getOrderById(long id) {
        return rpcOrderService.getOrderById(id);
    }

    @Override
    public RpcOrderGoods updateOrder(Order order) {
        return rpcOrderService.updateOrder(order);
    }
}
