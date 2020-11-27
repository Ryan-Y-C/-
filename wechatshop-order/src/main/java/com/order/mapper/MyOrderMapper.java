package com.order.mapper;

import com.api.data.GoodsInfo;
import com.api.data.OrderInfo;
import com.api.generator.Order;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MyOrderMapper {
    void insertOrders(OrderInfo orderInfo);

    List<GoodsInfo> getGoodsInfoOfOrder(long orderId);

    void updateByOrderId(Order order);
}
