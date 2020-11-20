package com.api.rpc;

import com.api.data.OrderInfo;
import com.api.generator.Order;

public interface RpcOrderService{
    Order createOrder(OrderInfo orderInfo,Order order);
}