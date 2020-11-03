package com.wechatshop.service;

import com.wechatshop.dao.GoodsDao;
import com.wechatshop.dao.ShopDao;
import com.wechatshop.entity.PageResponse;
import com.wechatshop.generator.Goods;
import com.wechatshop.generator.GoodsMapper;
import com.wechatshop.generator.Shop;
import com.wechatshop.generator.ShopMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static com.wechatshop.entity.DataStatus.DELETE_STATUS;

@Service
public class GoodsService {
    private GoodsMapper goodsMapper;
    private ShopMapper shopMapper;

    @Autowired
    public GoodsService(GoodsMapper goodsMapper, ShopMapper shopMapper) {
        this.goodsMapper = goodsMapper;
        this.shopMapper = shopMapper;
    }

    public Goods createdGoods(Goods goods) {

        Shop shop = shopMapper.selectByPrimaryKey(goods.getShopId());
        if (Objects.equals(UserContext.getCurrentUser().getId(), shop.getOwnerUserId())) {
            long goodsId = goodsMapper.insert(goods);
            goods.setId(goodsId);
            return goods;
        } else {
            throw new NotAuthorizedForShopException("无权访问");
        }
    }

    public Goods deleteGoodsById(Long goodsId) {
        Shop shop = shopMapper.selectByPrimaryKey(goodsId);
        if (Objects.equals(UserContext.getCurrentUser().getId(), shop.getOwnerUserId())) {
            Goods deleteGoods = goodsMapper.selectByPrimaryKey(goodsId);
            if (deleteGoods == null) {
                throw new ResourceNotFoundException("商品未找到！");
            }
            deleteGoods.setStatus(DELETE_STATUS);
            goodsMapper.updateByPrimaryKey(deleteGoods);
            return deleteGoods;
        } else {
            throw new NotAuthorizedForShopException("无权访问");
        }
    }
    public static class ResourceNotFoundException extends RuntimeException {
        public ResourceNotFoundException(String message) {
            super(message);
        }
    }


    public PageResponse<Goods> getGoods(Integer pageNum, Integer pageSize, Integer shopId) {
//        int totalNuber=shopDao.countAll();
        return null;
    }

    public static class NotAuthorizedForShopException extends RuntimeException {
        public NotAuthorizedForShopException(String message) {
            super(message);
        }
    }
}
