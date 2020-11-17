package com.order.service;

import com.api.rpc.RpcOrderService;
import org.apache.dubbo.config.annotation.DubboService;

@DubboService(version = "${wechatshop.orderservice.version}")
public class RpcOrderServiceImpl implements RpcOrderService {
    @Override
    public String sayHello(String name) {
        return "hello" + name;
    }
}
