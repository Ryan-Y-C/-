package com.wechatshop.mock;

import com.wechatshop.api.OrderService;
import org.apache.dubbo.config.annotation.DubboService;

@DubboService(version = "${wechatshop.orderservice.version}")
public class MockOrderService implements OrderService {
    @Override
    public void placeOrder(int goodsId, int number) {

    }
}
