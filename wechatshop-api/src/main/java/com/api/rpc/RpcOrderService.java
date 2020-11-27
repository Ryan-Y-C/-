package com.api.rpc;

import com.api.DataStatus;
import com.api.data.OrderInfo;
import com.api.data.PageResponse;
import com.api.data.RpcOrderGoods;
import com.api.generator.Order;

public interface RpcOrderService {
    Order createOrder(OrderInfo orderInfo, Order order);

    RpcOrderGoods deleteOrder(long orderId, long userId);

    PageResponse<RpcOrderGoods> getOrder(long userId, Integer pageNum, Integer pageSize, DataStatus status);

    Order getOrderById(long id);

    RpcOrderGoods updateOrder(Order order);
}
