package com.order.service;

import com.api.DataStatus;
import com.api.data.GoodsInfo;
import com.api.data.PageResponse;
import com.api.exceptions.HttpException;
import com.api.data.OrderInfo;
import com.api.data.RpcOrderGoods;
import com.api.generator.*;
import com.api.rpc.RpcOrderService;
import com.order.mapper.MyOrderMapper;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.api.DataStatus.*;

@DubboService(version = "${wechatshop.orderservice.version}")
public class RpcOrderServiceImpl implements RpcOrderService {
    private OrderMapper orderMapper;

    private MyOrderMapper myOrderMapper;

    private OrderGoodsMapper orderGoodsMapper;

    @Autowired
    public RpcOrderServiceImpl(OrderMapper orderMapper, MyOrderMapper myOrderMapper, OrderGoodsMapper orderGoodsMapper) {
        this.orderMapper = orderMapper;
        this.myOrderMapper = myOrderMapper;
        this.orderGoodsMapper = orderGoodsMapper;
    }

    @Override
    public Order createOrder(OrderInfo orderInfo, Order order) {
        insertOrder(order);
        orderInfo.setOrderId(order.getId());
        myOrderMapper.insertOrders(orderInfo);
        return order;
    }

    @Override
    public RpcOrderGoods deleteOrder(long orderId, long userId) {
        Order order = orderMapper.selectByPrimaryKey(orderId);
        verifyOrder(userId, order, orderId);

        List<GoodsInfo> goodsInfos = myOrderMapper.getGoodsInfoOfOrder(orderId);
        order.setStatus(DELETED.getName());
        order.setUpdatedAt(new Date());
        orderMapper.updateByPrimaryKey(order);
        RpcOrderGoods result = new RpcOrderGoods();
        result.setGoods(goodsInfos);
        result.setOrder(order);
        return result;
    }

    @Override
    public PageResponse<RpcOrderGoods> getOrder(long userId, Integer pageNum, Integer pageSize, DataStatus status) {
        OrderExample countByStatus = new OrderExample();
        setStatus(status, countByStatus);
        long count = orderMapper.countByExample(countByStatus);
        OrderExample pageOrder = new OrderExample();
        pageOrder.setLimit((pageNum - 1) * pageSize);
        pageOrder.setOffset(pageNum);
        setStatus(status, pageOrder).andUserIdEqualTo(userId);

        //获取指定范围的订单信息
        List<Order> orders = orderMapper.selectByExample(pageOrder);
        List<Long> orderId = orders.stream().map(Order::getId).collect(Collectors.toList());

        //通过该范围订单ID获取商品信息
        OrderGoodsExample selectByOrderId = new OrderGoodsExample();
        selectByOrderId.createCriteria().andOrderIdIn(orderId);
        List<OrderGoods> orderGoods = orderGoodsMapper.selectByExample(selectByOrderId);
        //计算总页数
        Long totalPage = count % pageSize == 0 ? count / pageSize : count / pageSize + 1;
        //将所有商品按订单id分组
        Map<Long, List<OrderGoods>> orderIdToGoodsMap = orderGoods
                        .stream()
                        .collect(Collectors.groupingBy(OrderGoods::getOrderId, Collectors.toList()));
        List<RpcOrderGoods> rpcOrderGoods = orders
                        .stream()
                        .map(order -> toRpcOrderGoods(order, orderIdToGoodsMap))
                        .collect(Collectors.toList());
        return PageResponse.pageData(pageNum,pageSize,totalPage.intValue(),rpcOrderGoods);
    }

    @Override
    public Order getOrderById(long orderId) {
        return orderMapper.selectByPrimaryKey(orderId);
    }

    @Override
    public RpcOrderGoods updateOrder(Order order) {
        orderMapper.updateByPrimaryKey(order);
        List<GoodsInfo> goodsInfos = myOrderMapper.getGoodsInfoOfOrder(order.getId());
        RpcOrderGoods result = new RpcOrderGoods();
        result.setGoods(goodsInfos);
        result.setOrder(orderMapper.selectByPrimaryKey(order.getId()));
        return result;
    }

    public RpcOrderGoods toRpcOrderGoods(Order order, Map<Long, List<OrderGoods>> orderIdToGoodsMap) {
        RpcOrderGoods result = new RpcOrderGoods();
        result.setOrder(order);
        List<GoodsInfo> goodsInfos = orderIdToGoodsMap
                .getOrDefault(order.getId(), Collections.emptyList())
                .stream()
                .map(this::toGoodsInfo)
                .collect(Collectors.toList());
        result.setGoods(goodsInfos);
        return result;
    }

    private GoodsInfo toGoodsInfo(OrderGoods orderGoods) {
        GoodsInfo result = new GoodsInfo();
        result.setId(orderGoods.getGoodsId());
        result.setNumber(orderGoods.getNumber().intValue());
        return result;

    }

    private OrderExample.Criteria setStatus(DataStatus status, OrderExample countByStatus) {
        if (status == null) {
            return countByStatus.createCriteria().andStatusEqualTo(DELETED.getName());
        } else {
            return countByStatus.createCriteria().andStatusEqualTo(status.getName());
        }
    }

    private void verifyOrder(long UserId, Order order, long orderId) {
        if (order == null) {
            throw HttpException.notFound("订单不存在" + orderId);
        }
        if (order.getUserId() != UserId) {
            throw HttpException.forbidden("无权访问！");
        }
    }

    private void insertOrder(Order order) {
        order.setStatus(PENDING.getName());
        verify(order);
        order.setExpressCompany(null);
        order.setExpressId(null);
        order.setCreatedAt(new Date());
        order.setUpdatedAt(new Date());

        long id = orderMapper.insert(order);
        order.setId(id);
    }

    private void verify(Order order) {
        if (order.getUserId() == null) {
            throw new IllegalArgumentException("userId不为空");
        }
        if (order.getTotalPrice() == null || order.getTotalPrice().doubleValue() < 0) {
            throw new IllegalArgumentException("totalPrice非法");
        }
        if (order.getAddress() == null) {
            throw new IllegalArgumentException("address不能为空");
        }
    }
}
