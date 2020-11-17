package com.wechatshop.controller;


import com.api.rpc.RpcOrderService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class OrderController {
    @DubboReference(version = "${wechatshop.orderservice.version}")
    private RpcOrderService orderService;

    @GetMapping("/order")
    public void getOrder() {
        orderService.sayHello("老张");
    }
}
