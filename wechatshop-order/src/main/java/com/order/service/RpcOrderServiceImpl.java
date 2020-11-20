package com.order.service;

import com.api.DataStatus;
import com.api.data.OrderInfo;
import com.api.generator.Order;
import com.api.generator.OrderMapper;
import com.api.rpc.RpcOrderService;
import com.order.mapper.MyOrderMapper;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

@DubboService(version = "${wechatshop.orderservice.version}")
public class RpcOrderServiceImpl implements RpcOrderService {
    private OrderMapper orderMapper;

    private MyOrderMapper myOrderMapper;

    @Autowired
    public RpcOrderServiceImpl(OrderMapper orderMapper, MyOrderMapper myOrderMapper) {
        this.orderMapper = orderMapper;
        this.myOrderMapper = myOrderMapper;
    }

    @Override
    public Order createOrder(OrderInfo orderInfo, Order order) {
        insertOrder(order);
        myOrderMapper.insertOrders(orderInfo);
        return order;
    }

    private void insertOrder(Order order) {
        order.setStatus(DataStatus.PENDING.getName());
        verify(order);
        order.setExpressCompany(null);
        order.setExpressId(null);
        order.setCreatedAt(new Date());
        order.setUpdatedAt(new Date());

        long id=orderMapper.insert(order);
        order.setId(id);
    }
    private  void verify(Order order){
        if (order.getUserId() == null) {
            throw new IllegalArgumentException("userId不为空");
        }
        if (order.getTotalPrice()==null||order.getTotalPrice().doubleValue()<0){
            throw new IllegalArgumentException("totalPrice非法");
        }
        if(order.getAddress()==null){
            throw new IllegalArgumentException("address不能为空");
        }
    }
}
