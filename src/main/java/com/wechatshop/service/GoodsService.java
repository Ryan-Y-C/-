package com.wechatshop.service;

import com.wechatshop.dao.GoodsDao;
import com.wechatshop.dao.ShopDao;
import com.wechatshop.generator.Goods;
import com.wechatshop.generator.Shop;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class GoodsService {
    private GoodsDao goodsDao;
    private ShopDao shopDao;

    @Autowired
    public GoodsService(GoodsDao goodsDao, ShopDao shopDao) {
        this.goodsDao = goodsDao;
        this.shopDao = shopDao;
    }

    public Goods createdGoods(Goods goods) {

//        Shop shop = shopDao.findShopById(goods.getShopId());
            return goodsDao.insertGoods(goods);
//        if (Objects.equals(UserContext.getCurrentUser().getId(), shop.getOwnerUserId())) {
//        } else {
//            throw new NotAuthorizedForShopException("无权访问");
//        }
    }

    public Goods deleteGoodsById(Long goodsId) {
        Shop shop = shopDao.findShopById(goodsId);
        if (Objects.equals(UserContext.getCurrentUser().getId(), shop.getOwnerUserId())) {
            return goodsDao.deleteGoodsById(goodsId);
        } else {
            throw new NotAuthorizedForShopException("无权访问");
        }
    }

    public static class NotAuthorizedForShopException extends RuntimeException {
        public NotAuthorizedForShopException(String message) {
            super(message);
        }
    }
}
