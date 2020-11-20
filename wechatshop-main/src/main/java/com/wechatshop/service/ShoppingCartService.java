package com.wechatshop.service;

import com.api.DataStatus;
import com.wechatshop.controller.ShoppingCartController;
import com.wechatshop.dao.ShoppingCartQueryMapper;
import com.wechatshop.entity.GoodsWithNumber;
import com.wechatshop.entity.HttpException;
import com.wechatshop.entity.PageResponse;
import com.wechatshop.entity.ShoppingCartData;
import com.wechatshop.generator.Goods;
import com.wechatshop.generator.GoodsMapper;
import com.wechatshop.generator.ShoppingCart;
import com.wechatshop.generator.ShoppingCartMapper;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.*;

@Service
public class ShoppingCartService {
    private Logger logger = LoggerFactory.getLogger(ShoppingCartService.class);
    private ShoppingCartQueryMapper shoppingCartQueryMapper;
    private GoodsMapper goodsMapper;
    private SqlSessionFactory sqlSessionFactory;
    private GoodsService goodsService;

    @Autowired
    public ShoppingCartService(ShoppingCartQueryMapper shoppingCartQueryMapper, GoodsMapper goodsMapper, SqlSessionFactory sqlSessionFactory, GoodsService goodsService) {
        this.shoppingCartQueryMapper = shoppingCartQueryMapper;
        this.goodsMapper = goodsMapper;
        this.sqlSessionFactory = sqlSessionFactory;
        this.goodsService = goodsService;
    }

    public PageResponse<ShoppingCartData> getShoppingCartOfUser(Long userId, int pageNum, int pageSize) {

        int offset = (pageNum - 1) * pageSize;
        int totalNum = shoppingCartQueryMapper.countHowManyShopsInUserShoppingCart(userId);

        int totalPage = totalNum % pageSize == 0 ? totalNum / pageSize : totalNum / pageSize + 1;
        List<ShoppingCartData> pageData =
                shoppingCartQueryMapper.selectShoppingCartDataByUserId(userId, pageSize, offset)
                        .stream()
                        .collect(groupingBy(shoppingCartData -> shoppingCartData.getShop().getId()))
                        .values()
                        .stream()
                        .map(this::merge)
                        .collect(toList());
        return PageResponse.pageData(pageNum, pageSize, totalPage, pageData);
    }

    private ShoppingCartData merge(List<ShoppingCartData> goodsOfSameShop) {
        ShoppingCartData result = new ShoppingCartData();
        result.setShop(goodsOfSameShop.get(0).getShop());
        List<GoodsWithNumber> goods = goodsOfSameShop
                .stream()
                .map(ShoppingCartData::getGoods).flatMap(List::stream).collect(toList());
        result.setGoods(goods);
        return result;
    }

    public ShoppingCartData addToShoppingCart(ShoppingCartController.AddToShoppingCartRequest request) {
        //获取商品列表中每个商品id
        List<Long> goodsId = request.getGoods().stream()
                .map(ShoppingCartController.AddToShoppingCartItem::getId)
                .collect(toList());

        if (goodsId.isEmpty()) {
            throw HttpException.badRequest("商品id为空");
        }

        Map<Long, Goods> idToGoodsMap = goodsService.getIdToGoodsMap(goodsId);
        if (idToGoodsMap.values().stream().map(Goods::getShopId).collect(toSet()).size() != 1) {
            logger.debug("非法请求：{}{}", goodsId, idToGoodsMap.values());
            throw HttpException.badRequest("商品id非法");
        }
        //将请求的添加商品的id和数量转换成ShoppingCart
        List<ShoppingCart> shoppingCartRows = request.getGoods()
                .stream()
                .map(item -> toShoppingCartRow(item, idToGoodsMap))
                .collect(toList());
        try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
            ShoppingCartMapper mapper = sqlSession.getMapper(ShoppingCartMapper.class);
            shoppingCartRows.forEach(mapper::insert);
            sqlSession.commit();
        }
        //通过店铺id获取所有该用户当前店铺所有的商品
        return getLatestShoppingCartDataByUserIdShopId(new ArrayList<>(idToGoodsMap.values()).get(0).getShopId(), UserContext.getCurrentUser().getId());
    }


    private ShoppingCart toShoppingCartRow(ShoppingCartController.AddToShoppingCartItem item, Map<Long, Goods> idToGoodsMap) {
        Goods goods = idToGoodsMap.get(item.getId());
        if (goods == null) {
            return null;
        }
        ShoppingCart result = new ShoppingCart();
        result.setGoodsId(item.getId());
        result.setNumber(item.getNumber());
        result.setUserId(UserContext.getCurrentUser().getId());
        result.setShopId(goods.getShopId());
        result.setStatus(DataStatus.OK.getName().toLowerCase());
        result.setCreatedAt(new Date());
        result.setUpdatedAt(new Date());
        return result;
    }

    public Object deleteShoppingCartByGoodsId(long goodsId, long userId) {
        Goods goods = goodsMapper.selectByPrimaryKey(goodsId);
        if (goods == null) {
            return HttpException.notFound("未找到该商品:" + goodsId);
        }
        shoppingCartQueryMapper.deleteShoppingCart(goodsId, userId);
        //
        return getLatestShoppingCartDataByUserIdShopId(goods.getShopId(), userId);
    }

    private ShoppingCartData getLatestShoppingCartDataByUserIdShopId(long shopId, long userId) {
        return merge(shoppingCartQueryMapper.selectShoppingCartDataByUserIdShopId(userId, shopId));
    }
}
