package com.wechatshop.controller;


import com.api.DataStatus;
import com.api.data.OrderInfo;
import com.api.data.PageResponse;
import com.api.exceptions.HttpException;
import com.api.generator.Order;
import com.wechatshop.entity.OrderResponse;
import com.wechatshop.entity.ResponseData;
import com.wechatshop.service.OrderService;
import com.wechatshop.service.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

import static javax.servlet.http.HttpServletResponse.SC_CREATED;

@RestController
@RequestMapping("/api/v1")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @PostMapping("/order")
    public Object createOrder(@RequestBody OrderInfo orderInfo, HttpServletResponse httpServletResponse) {
        orderService.deductStock(orderInfo);
        OrderResponse orderResponse = orderService.createOrder(orderInfo, UserContext.getCurrentUser().getId());
        httpServletResponse.setStatus(SC_CREATED);
        return ResponseData.of(orderResponse);
    }

    @DeleteMapping("/order/{id}")
    public ResponseData<OrderResponse> deleteOrder(@PathVariable("id") long orderId) {
        OrderResponse orderResponse = orderService.deleteOrder(orderId, UserContext.getCurrentUser().getId());
        ResponseData<OrderResponse> ord = ResponseData.of(orderResponse);
        return ord;
    }

    @GetMapping("/order")
    public PageResponse<OrderResponse> getOrder(@RequestParam("pageNum") Integer pageNum, @RequestParam("pageSize") Integer pageSize,
                                                @RequestParam(value = "status", required = false) String status) {

        if (status != null && DataStatus.fromStatus(status) == null) {
            throw HttpException.badRequest("非法status：" + status);
        }
        return orderService.getOrder(UserContext.getCurrentUser().getId(), pageNum, pageSize, DataStatus.fromStatus(status));
    }

    @PatchMapping("/order/{id}")
    public ResponseData<OrderResponse> updateOrder(@PathVariable("id") Integer id, @RequestBody Order order) {
        if (order.getExpressCompany() != null) {
            return ResponseData.of(orderService.updateExpressInformation(order, UserContext.getCurrentUser().getId()));
        } else {
            return ResponseData.of(orderService.updateOrderStatus(order, UserContext.getCurrentUser().getId()));
        }
    }
}
