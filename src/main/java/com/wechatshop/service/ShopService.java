package com.wechatshop.service;

import com.wechatshop.entity.DataStatus;
import com.wechatshop.entity.HttpException;
import com.wechatshop.entity.PageResponse;
import com.wechatshop.generator.Shop;
import com.wechatshop.generator.ShopExample;
import com.wechatshop.generator.ShopMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
public class ShopService {
    private ShopMapper shopMapper;

    @Autowired
    public ShopService(ShopMapper shopMapper) {
        this.shopMapper = shopMapper;
    }

    public Shop createdShop(Shop shop, long creatorId) {
        shop.setOwnerUserId(creatorId);
        Shop selectShop = shopMapper.selectByPrimaryKey(shop.getId());
        if (selectShop != null || shop.getOwnerUserId() == null) {
            throw new ShopIdExistException("商铺ID已存在");
        } else {
            shop.setStatus(DataStatus.OK.getName());
            long shopId = shopMapper.insert(shop);
            shop.setId(shopId);
            return shop;
        }
    }

    public PageResponse<Shop> getShopByUserId(Long userId, int pageNum, int pageSize) {
        ShopExample countByStatus = new ShopExample();
        countByStatus.createCriteria().andStatusEqualTo(DataStatus.DELETED.getName());
        int totalNumber = (int) shopMapper.countByExample(countByStatus);
        int totalPage = totalNumber % pageSize == 0 ? totalNumber / pageSize : totalNumber / pageSize + 1;

        ShopExample pageCondition = new ShopExample();
        pageCondition.createCriteria().andStatusEqualTo(DataStatus.OK.getName());
        pageCondition.setLimit(pageSize);
        pageCondition.setOffset((pageNum - 1) * pageSize);
        List<Shop> pagedShops = shopMapper.selectByExample(pageCondition);
        return PageResponse.pageData(pageNum, pageSize, totalPage, pagedShops);
    }

    public Shop updateShop(Shop shop, long userId) {
        Shop shopInDatabase = shopMapper.selectByPrimaryKey(shop.getId());
        if (shopInDatabase == null) {
            throw HttpException.notFound("店铺未找到");
        } else if (!Objects.equals(shopInDatabase.getOwnerUserId(), userId)) {
            throw HttpException.forbidden("无权访问");
        } else {
            shop.setUpdatedAt(new Date());
            shopMapper.updateByPrimaryKey(shop);
        }
        return shop;
    }

    public Object deleteShop(long shopId, Long userId) {
        Shop shopInDatabase = shopMapper.selectByPrimaryKey(shopId);
        if (shopInDatabase == null) {
            throw HttpException.notFound("店铺未找到");
        } else if (!Objects.equals(shopInDatabase.getOwnerUserId(), userId)) {
            throw HttpException.forbidden("无权访问");
        } else {
            shopInDatabase.setUpdatedAt(new Date());
            shopInDatabase.setStatus(DataStatus.DELETED.getName());
            shopMapper.updateByPrimaryKey(shopInDatabase);
        }
        return shopInDatabase;
    }

    private static class ShopIdExistException extends RuntimeException {
        ShopIdExistException(String message) {
            super(message);
        }
    }
}
