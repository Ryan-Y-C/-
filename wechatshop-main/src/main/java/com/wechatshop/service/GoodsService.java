package com.wechatshop.service;

import com.api.DataStatus;
import com.wechatshop.entity.HttpException;
import com.wechatshop.entity.PageResponse;
import com.wechatshop.generator.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.stream.Collectors.toMap;


@Service()
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
        if (shop == null || Objects.equals(UserContext.getCurrentUser().getId(), shop.getOwnerUserId())) {
            goods.setStatus(DataStatus.OK.getName());
            long goodsId = goodsMapper.insert(goods);
            goods.setId(goodsId);
            return goods;
        } else {
            throw HttpException.forbidden("无权访问");
        }
    }

    public Goods updateGoods(long id, Goods goods) {
        Shop shop = shopMapper.selectByPrimaryKey(goods.getShopId());
        if (Objects.equals(UserContext.getCurrentUser().getId(), shop.getOwnerUserId())) {
            Goods goodsInDb = goodsMapper.selectByPrimaryKey(id);
            if (goodsInDb == null) {
                throw HttpException.notFound("未找到");
            }
            goodsInDb.setName(goods.getName());
            goodsInDb.setDetails(goods.getDetails());
            goodsInDb.setDescription(goods.getDescription());
            goodsInDb.setImgUrl(goods.getImgUrl());
            goodsInDb.setPrice(goods.getPrice());
            goodsInDb.setStock(goods.getStock());
            goodsInDb.setUpdatedAt(new Date());

            goodsMapper.updateByPrimaryKey(goodsInDb);

            return goodsInDb;
        } else {
            throw HttpException.forbidden("无权访问");
        }
    }

    public Goods deleteGoodsById(Long goodsId) {
        Shop shop = shopMapper.selectByPrimaryKey(goodsId);
        if (shop == null) {
            throw HttpException.notFound("商品未找到！");
        }
        if (Objects.equals(UserContext.getCurrentUser().getId(), shop.getOwnerUserId())) {
            Goods deleteGoods = goodsMapper.selectByPrimaryKey(goodsId);
            if (deleteGoods == null) {
                throw HttpException.notFound("商品未找到！");
            }
            deleteGoods.setStatus(DataStatus.DELETED.getName());
            goodsMapper.updateByPrimaryKey(deleteGoods);
            return deleteGoods;
        } else {
            throw HttpException.forbidden("无权访问");
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

    public Map<Long, Goods> getIdToGoodsMap(List<Long> goodsId) {
        //通过商品id获取对应的商品
        GoodsExample example = new GoodsExample();
        example.createCriteria().andIdIn(goodsId);
        List<Goods> goods = goodsMapper.selectByExample(example);
        Map<Long, Goods> idToGoodsMap = goods.stream().collect(toMap(Goods::getId, x -> x));
        return idToGoodsMap;
    }
}
