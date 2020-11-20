package com.wechatshop.controller;


import com.api.data.OrderInfo;
import com.wechatshop.entity.HttpException;
import com.wechatshop.entity.MessageResponse;
import com.wechatshop.entity.OrderResponse;
import com.wechatshop.entity.ResponseData;
import com.wechatshop.service.OrderService;
import com.wechatshop.service.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

import static javax.servlet.http.HttpServletResponse.SC_CREATED;

@RestController
@RequestMapping("/api/v1")
public class OrderController {
    private OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/order")
    public Object createOrder(@RequestBody OrderInfo orderInfo, HttpServletResponse httpServletResponse) {
        try {
            orderService.deductStock(orderInfo);
            OrderResponse orderResponse=orderService.createOrder(orderInfo, UserContext.getCurrentUser().getId());
            httpServletResponse.setStatus(SC_CREATED);
            return ResponseData.of(orderResponse);
        } catch (HttpException e) {
            httpServletResponse.setStatus(e.getStatusCode());
            return MessageResponse.of(e.getMessage());
        }
    }
}
