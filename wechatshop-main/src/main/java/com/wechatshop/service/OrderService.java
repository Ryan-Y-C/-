package com.wechatshop.service;

import com.api.DataStatus;
import com.api.data.GoodsInfo;
import com.api.data.OrderInfo;
import com.api.data.PageResponse;
import com.api.data.RpcOrderGoods;
import com.api.exceptions.HttpException;
import com.api.generator.Order;
import com.api.rpc.RpcOrderService;
import com.wechatshop.dao.GoodsStockMapper;
import com.wechatshop.entity.GoodsWithNumber;
import com.wechatshop.entity.OrderResponse;
import com.wechatshop.generator.Goods;
import com.wechatshop.generator.Shop;
import com.wechatshop.generator.ShopMapper;
import com.wechatshop.generator.UserMapper;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service()
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class OrderService {
    @DubboReference(version = "${wechatshop.orderservice.version}")
    private RpcOrderService rpcOrderService;

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderService.class);

    private UserMapper userMapper;

    private ShopMapper shopMapper;

    private GoodsService goodsService;

    private GoodsStockMapper goodsStockMapper;


    @Autowired
    public OrderService(UserMapper userMapper, ShopMapper shopMapper, GoodsService goodsService, GoodsStockMapper goodsStockMapper) {
        this.userMapper = userMapper;
        this.shopMapper = shopMapper;
        this.goodsService = goodsService;
        this.goodsStockMapper = goodsStockMapper;

    }

    public OrderResponse createOrder(OrderInfo orderInfo, Long userId) {
        Map<Long, Goods> idToGoodsMap = getIdToGoodsMap(orderInfo.getGoods());
        Order createOrder = createOrderViaRpc(userId, orderInfo, idToGoodsMap);
        return generateResponse(orderInfo.getGoods(), idToGoodsMap, createOrder);
    }

    //将订单信息与商品信息拼装成订单响应所需的对象
    private OrderResponse generateResponse(List<GoodsInfo> goodsInfos, Map<Long, Goods> idToGoodsMap, Order createOrder) {
        OrderResponse orderResponse = new OrderResponse(createOrder);

        Long shopId = new ArrayList<>(idToGoodsMap.values()).get(0).getShopId();
        orderResponse.setShop(shopMapper.selectByPrimaryKey(shopId));
        //将订单中每个商品转换成包含商品数量的商品
        orderResponse.setGoods(goodsInfos.stream().map(goodsInfo -> getGoodsWithNumber(goodsInfo, idToGoodsMap)).collect(Collectors.toList()));
        return orderResponse;
    }

    private Map<Long, Goods> getIdToGoodsMap(List<GoodsInfo> goodsInfos) {
        List<Long> goodsId = goodsInfos.stream().map(GoodsInfo::getId).collect(Collectors.toList());
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
    public void deductStock(OrderInfo orderInfo) {
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

    public OrderResponse deleteOrder(long orderId, long userId) {
        return getOrderResponse(rpcOrderService.deleteOrder(orderId, userId));
    }

    private OrderResponse getOrderResponse(RpcOrderGoods rpcOrderGoods) {
        Map<Long, Goods> idToGoodsMap = getIdToGoodsMap(rpcOrderGoods.getGoods());
        return generateResponse(rpcOrderGoods.getGoods(), idToGoodsMap, rpcOrderGoods.getOrder());
    }

    public PageResponse<OrderResponse> getOrder(long userId, Integer pageNum, Integer pageSize, DataStatus status) {
        PageResponse<RpcOrderGoods> rpcOrderGoods = rpcOrderService.getOrder(userId, pageNum, pageSize, status);
        //获取该范围的所有订单的商品信息
        List<GoodsInfo> goodIds = rpcOrderGoods
                .getData()
                .stream()
                .map(RpcOrderGoods::getGoods)
                .flatMap(List::stream)
                .collect(Collectors.toList());
        Map<Long, Goods> idToGoodsMap = getIdToGoodsMap(goodIds);

        List<OrderResponse> orders = rpcOrderGoods
                .getData()
                .stream()
                .map(order -> generateResponse(order.getGoods(), idToGoodsMap, order.getOrder()))
                .collect(Collectors.toList());

        return PageResponse.pageData(rpcOrderGoods.getPageNum(), rpcOrderGoods.getPageSize(), rpcOrderGoods.getTotalPage(), orders);
    }

    public OrderResponse updateExpressInformation(Order order, long userId) {
        Order rpcOrder = rpcOrderService.getOrderById(order.getId());
        if (rpcOrder == null) {
            throw HttpException.notFound("订单未找到:" + order.getId());
        }
        Shop shop = shopMapper.selectByPrimaryKey(rpcOrder.getShopId());
        if (shop == null) {
            throw HttpException.notFound("店铺未找到：" + rpcOrder.getShopId());
        }
        if (!shop.getOwnerUserId().equals(userId)) {
            throw HttpException.badRequest("当前用户没有访问权限");
        }
        Order copy = new Order();
        copy.setId(order.getId());
        copy.setExpressId(order.getExpressId());
        copy.setExpressCompany(order.getExpressCompany());
        copy.setUpdatedAt(new Date());
        return getOrderResponse(rpcOrderService.updateOrder(copy));
    }

    public OrderResponse updateOrderStatus(Order order, long userId) {
        Order rpcOrder = rpcOrderService.getOrderById(order.getId());
        if (rpcOrder == null) {
            throw HttpException.notFound("订单未找到:" + order.getId());
        }
        if (rpcOrder.getUserId() != userId) {
            throw HttpException.badRequest("当前用户没有访问权限");
        }
        Order copy = new Order();
        copy.setStatus(order.getStatus());
        return getOrderResponse(rpcOrderService.updateOrder(copy));
    }
}
