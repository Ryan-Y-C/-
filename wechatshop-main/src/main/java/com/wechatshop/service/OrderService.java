package com.wechatshop.service;

import com.api.DataStatus;
import com.api.data.GoodsInfo;
import com.api.data.OrderInfo;
import com.api.generator.Order;
import com.api.rpc.RpcOrderService;
import com.wechatshop.dao.GoodsStockMapper;
import com.wechatshop.entity.GoodsWithNumber;
import com.wechatshop.entity.HttpException;
import com.wechatshop.entity.OrderResponse;
import com.wechatshop.generator.Goods;
import com.wechatshop.generator.GoodsMapper;
import com.wechatshop.generator.ShopMapper;
import com.wechatshop.generator.UserMapper;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderService {
    @DubboReference(version = "${wechatshop.orderservice.version}")
    private RpcOrderService rpcOrderService;

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderService.class);

    private UserMapper userMapper;

    private ShopMapper shopMapper;

    private GoodsService goodsService;

    private GoodsStockMapper goodsStockMapper;

    private SqlSessionFactory sqlSessionFactory;

    @Autowired
    public OrderService(UserMapper userMapper, GoodsMapper goodsMapper, ShopMapper shopMapper, GoodsService goodsService, GoodsStockMapper goodsStockMapper, SqlSessionFactory sqlSessionFactory) {
        this.userMapper = userMapper;
        this.shopMapper = shopMapper;
        this.goodsService = goodsService;
        this.goodsStockMapper = goodsStockMapper;
        this.sqlSessionFactory = sqlSessionFactory;
    }

    public OrderResponse createOrder(OrderInfo orderInfo, Long userId) {
        Map<Long, Goods> idToGoodsMap = getIdToGoodsMap(orderInfo);
        Order createOrder = createOrderViaRpc(userId, orderInfo, idToGoodsMap);
        return generateResponse(orderInfo, idToGoodsMap, createOrder);
    }

    @NotNull
    private OrderResponse generateResponse(OrderInfo orderInfo, Map<Long, Goods> idToGoodsMap, Order createOrder) {
        OrderResponse orderResponse = new OrderResponse(createOrder);

        Long shopId = new ArrayList<>(idToGoodsMap.values()).get(0).getShopId();
        orderResponse.setShop(shopMapper.selectByPrimaryKey(shopId));
        //将订单中每个商品转换成包含商品数量的商品
        orderResponse.setGoodsList(orderInfo.getGoods().stream().map(goodsInfo -> getGoodsWithNumber(goodsInfo, idToGoodsMap)).collect(Collectors.toList()));
        return orderResponse;
    }

    private Map<Long, Goods> getIdToGoodsMap(OrderInfo orderInfo) {
        List<Long> goodsId = orderInfo.getGoods().stream().map(GoodsInfo::getId).collect(Collectors.toList());
        return goodsService.getIdToGoodsMap(goodsId);
    }

    private Order createOrderViaRpc(long userId, OrderInfo orderInfo, Map<Long, Goods> idToGoodsMap) {
        Order order = new Order();
        order.setUserId(userId);
        order.setAddress(userMapper.selectByPrimaryKey(userId).getAddress());
        order.setTotalPrice(calculateTotalPrice(orderInfo, idToGoodsMap));
        order.setStatus(DataStatus.PENDING.getName());
        return rpcOrderService.createOrder(orderInfo, order);
    }
    //spring Transactional 遇到异常自动回滚
    //注： Transactional不能自己调用（当前类的其他方法不能调用deductStock，除非其他类也标注Transactional注解），调用OrderService服务的类可以调用
    @Transactional
    public void deductStock(OrderInfo orderInfo){
        for (GoodsInfo goodsInfo : orderInfo.getGoods()) {
            if (goodsStockMapper.deductStock(goodsInfo) <= 0) {
                LOGGER.error("扣减库存失败，商品id：" + goodsInfo.getId() + "，数量：" + goodsInfo.getNumber());
                throw HttpException.gone("扣减库存失败");
            }
        }
    }
//    public boolean deductStock(OrderInfo orderInfo) {
//        try (SqlSession session = sqlSessionFactory.openSession(false)) {
//            for (GoodsInfo goodsInfo : orderInfo.getGoods()) {
//                if (goodsStockMapper.deductStock(goodsInfo) <= 0) {
//                    LOGGER.error("扣减库存失败，商品id：" + goodsInfo.getId() + "，数量：" + goodsInfo.getNumber());
//                    session.rollback();
//                    return false;
//                }
//            }
//            session.commit();
//            return true;
//        }
//    }

    private GoodsWithNumber getGoodsWithNumber(GoodsInfo goodsInfo, Map<Long, Goods> idToGoodsMap) {
        GoodsWithNumber result = new GoodsWithNumber(idToGoodsMap.get(goodsInfo.getId()));
        result.setNumber(goodsInfo.getNumber());
        return result;
    }

    private long calculateTotalPrice(OrderInfo orderInfo, Map<Long, Goods> idToGoodsMap) {
        long result = 0;
        for (GoodsInfo goodsInfo : orderInfo.getGoods()) {
            Goods goods = idToGoodsMap.get(goodsInfo.getId());
            if (goods == null) {
                throw HttpException.badRequest("good id 非法" + goodsInfo.getId());
            }
            if (goodsInfo.getNumber() <= 0) {
                throw HttpException.badRequest("Number非法" + goodsInfo.getNumber());
            }
            result = result + goods.getPrice() * goodsInfo.getNumber();
        }
        return result;
    }
}
