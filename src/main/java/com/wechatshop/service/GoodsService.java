package com.wechatshop.service;

import com.wechatshop.entity.DataStatus;
import com.wechatshop.entity.PageResponse;
import com.wechatshop.generator.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;


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
        if (shop==null||Objects.equals(UserContext.getCurrentUser().getId(), shop.getOwnerUserId())) {
            long goodsId = goodsMapper.insert(goods);
            goods.setId(goodsId);
            return goods;
        } else {
            throw new NotAuthorizedForShopException("无权访问");
        }
    }

    public Goods updateGoods(Goods goods) {
        Shop shop = shopMapper.selectByPrimaryKey(goods.getShopId());
        if (Objects.equals(UserContext.getCurrentUser().getId(), shop.getOwnerUserId())) {
            GoodsExample byId =new GoodsExample();
            byId.createCriteria().andIdEqualTo(goods.getId());
            int affectedRows = goodsMapper.updateByExample(goods, byId);
            if (affectedRows == 0) {
                throw new ResourceNotFoundException(" 未找到！");
            }
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
            deleteGoods.setStatus(DataStatus.DELETED.getName());
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
        int totalNumber = countGoods(shopId);
        int totalPage = totalNumber % pageSize == 0 ? totalNumber / pageSize : totalNumber / pageSize + 1;
        GoodsExample page = new GoodsExample();
        page.setLimit(pageSize);
        page.setOffset((pageNum - 1) * pageSize);

        List<Goods> pageGoods = goodsMapper.selectByExample(page);
        return PageResponse.pageData(pageNum, pageSize, totalPage, pageGoods);
    }

    public int countGoods(Integer shopId) {
        if (shopId == null) {
            GoodsExample goodsExample = new GoodsExample();
            goodsExample.createCriteria().andStatusEqualTo(DataStatus.OK.getName());
            return (int) goodsMapper.countByExample(goodsExample);
        } else {
            GoodsExample goodsExample = new GoodsExample();
            goodsExample.createCriteria().andStatusEqualTo(DataStatus.OK.getName()).andShopIdEqualTo(shopId.longValue());
            return (int) goodsMapper.countByExample(goodsExample);
        }
    }

    public static class NotAuthorizedForShopException extends RuntimeException {
        public NotAuthorizedForShopException(String message) {
            super(message);
        }
    }
}
